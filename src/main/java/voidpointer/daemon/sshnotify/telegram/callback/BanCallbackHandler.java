package voidpointer.daemon.sshnotify.telegram.callback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import voidpointer.daemon.sshnotify.config.BanConfig;
import voidpointer.daemon.sshnotify.telegram.SshNotifyVoidBot;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static java.text.MessageFormat.format;

@Slf4j
@RequiredArgsConstructor
final class BanCallbackHandler implements CallbackHandler {
    private final SshNotifyVoidBot bot;
    private final BanConfig banConfig;

    @Override public String name() {
        return "ban";
    }

    @Override public void onQuery(final CallbackQuery query, final String payloadIp) {
        log.trace("> ban query from @{}({}): {}", query.getFrom().getUserName(), query.getFrom().getId(), payloadIp);
        CompletableFuture.runAsync(() -> runBanProcessFor(payloadIp).ifPresentOrElse(
            process -> handleBanProcess(query.getFrom().getId(), process, payloadIp),
            () -> bot.sendMessage(SendMessage.builder()
                    .text(format("Could not start process `{0}`", String.join(" ", banConfig.banCommand(payloadIp))))
                    .build())
        )).exceptionally(th -> {
            log.error("Error while handling process: {}", th.getMessage());
            log.atDebug().setCause(th).log("Exception occurred when was handling a Process: {}", th.getMessage());
            return null;
        });
    }

    private Optional<Process> runBanProcessFor(final String ip) {
        try {
            return Optional.of(new ProcessBuilder(banConfig.banCommand(ip)).start());
        } catch (final IOException ioException) {
            log.error("Could not ban {}: {}", ip, ioException.getMessage());
            log.atDebug().setCause(ioException).log("Exception on ban process creation: {}", ioException.getMessage());
            return Optional.empty();
        }
    }

    private void handleBanProcess(final long chatId, final Process banProcess, final String ip) {
        final String command = String.join(" ", banConfig.banCommand(ip));
        int returnCode;
        try {
            returnCode = banProcess.waitFor();
        } catch (final InterruptedException interruptedException) {
            log.error("Interrupted on wait for PID {}, cli: {}", banProcess.pid(), command);
            log.atDebug().setCause(interruptedException).log("Interrupted on wait for PID {}, cli: {}", banProcess.pid(),
                command);
            return;
        }
        log.trace("PID {} <{}> returned {}", banProcess.pid(), command, returnCode);
        reportProcessFinished(chatId, command, banProcess, returnCode);
    }

    private void reportProcessFinished(final long chatId, final String cmd, final Process process, final int returnCode) {
        bot.sendMessage(SendMessage.builder()
            .chatId(chatId)
            .text(format("""
                    `{0}` *({1,number,#} {2,number,#})*
                    {3}
                    """,
                cmd,
                process.pid(),
                returnCode,
                readErrorsAndStdout(process)))
            .parseMode("markdown")
            .build());
    }

    private String readErrorsAndStdout(final Process process) {
        StringBuilder responseBuilder = new StringBuilder(4096);
        String stdout = readAllAndClose(process.inputReader());
        String errors = readAllAndClose(process.errorReader());
        if (!errors.isBlank())
            responseBuilder.append("Err:\n```").append(errors).append("```");
        if (!stdout.isBlank())
            responseBuilder.append("STDOUT:\n```").append(stdout).append("```");
        return responseBuilder.toString();
    }

    private @NotNull String readAllAndClose(final BufferedReader reader) {
        try (reader) {
            return readAll(reader);
        } catch (final IOException ioException) {
            log.warn("Could not close reader: {}", ioException.getMessage());
            log.atDebug().setCause(ioException).log("Exception on BufferedReader#close(): {}", ioException.getMessage());
            return "";
        }
    }

    private @NotNull String readAll(final BufferedReader reader) {
        StringBuilder lines = new StringBuilder(1024);
        String line;
        try {
            while ((line = reader.readLine()) != null)
                lines.append(line);
        } catch (final IOException ioException) {
            log.error("Error on process IO read: {}", ioException.getMessage());
            log.atDebug().setCause(ioException).log("Exception on process BufferedReader#readLine(): {}", ioException.getMessage());
            return lines.toString();
        }
        return lines.toString();
    }
}
