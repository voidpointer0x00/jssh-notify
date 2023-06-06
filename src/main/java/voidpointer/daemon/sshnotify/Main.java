package voidpointer.daemon.sshnotify;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import voidpointer.daemon.sshnotify.config.TelegramConfig;
import voidpointer.daemon.sshnotify.server.DaemonServer;
import voidpointer.daemon.sshnotify.telegram.TelegramService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;

@Slf4j
public class Main {
    private static final Path CONFIG_FOLDER_PATH = System.getProperty("user.name").equals("root")
            ? Path.of("/etc/ssh_notify.d/") : Path.of(System.getProperty("user.home"), ".config/ssh_notify.d/");

    public static void main(final String[] args) {
        final Path telegramConfigPath = CONFIG_FOLDER_PATH.resolve("telegram_bot.conf");
        var telegramConfig = TelegramConfig.loadAndSave(telegramConfigPath);
        updateConfigDirPermissions();
        TelegramService telegramWorker;
        try {
            telegramWorker = new TelegramService(telegramConfig);
        } catch (final TelegramApiException telegramApiException) {
            log.error("Could not start telegram bot: {}", telegramApiException.getMessage());
            return;
        }
        var daemonServer = new DaemonServer(telegramWorker::notifyConnected);
        Runtime.getRuntime().addShutdownHook(new Thread(daemonServer::shutdown));
        try {
            daemonServer.bind();
        } catch (final IOException ioException) {
            log.atError().setCause(ioException).log("Could not create daemon server: {}", ioException.getMessage());
            return;
        }
        daemonServer.startAccepting();
    }

    private static void updateConfigDirPermissions() {
        try {
            Files.setPosixFilePermissions(CONFIG_FOLDER_PATH, PosixFilePermissions.fromString("rwx------"));
        } catch (final IOException ioException) {
            log.error("Could not update {} permissions: {}", CONFIG_FOLDER_PATH, ioException.getMessage());
        }
    }
}
