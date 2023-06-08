package voidpointer.daemon.sshnotify.telegram.callback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import voidpointer.daemon.sshnotify.data.UserRepository;
import voidpointer.daemon.sshnotify.telegram.SshNotifyVoidBot;

import java.text.MessageFormat;

@Slf4j
@RequiredArgsConstructor
final class IgnoreCallbackHandler implements CallbackHandler {
    private final SshNotifyVoidBot bot;
    private final UserRepository userRepository;

    @Override public String name() {
        return "ignore";
    }

    @Override public void onQuery(CallbackQuery query, String payloadIp) {
        userRepository.ignore(payloadIp).thenAccept(userOrException -> {
            userOrException.ifExceptionOrElse(ex -> {
                log.warn("Could not ignore ip {}: {}", payloadIp, ex.getMessage());
                log.atDebug().setCause(ex).log("Exception on repository#ignore({}): {}", payloadIp, ex.getMessage());
                notifyFailed(query.getMessage().getChat(), payloadIp, ex.getMessage());
            }, user -> {
                log.trace("({}) user#ignore() -> {}", user.ip(), user.ignore());
                notifyIgnoring(query.getMessage().getChat(), payloadIp);
            });
        });
    }

    private void notifyIgnoring(final Chat chat, final String ip) {
        bot.sendMessage(SendMessage.builder()
                .chatId(chat.getId())
                .text(MessageFormat.format("""
                    Now ignoring `{0}`.""", ip))
                .parseMode("markdown")
                .build());
    }

    private void notifyFailed(final Chat chat, final String ip, final String failMessage) {
        bot.sendMessage(SendMessage.builder()
                .text(MessageFormat.format("""
                    Не удалось игнорировать `{0}`:
                    `{1}`""", ip, failMessage))
                .chatId(chat.getId())
                .parseMode("markdown")
                .build());
    }
}
