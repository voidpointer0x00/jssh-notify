package voidpointer.daemon.sshnotify.telegram;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import voidpointer.daemon.sshnotify.config.TelegramConfig;
import voidpointer.daemon.sshnotify.connection.ConnectionDescription;

@Slf4j
public final class TelegramService {
    private final TelegramBotsApi api;
    private final SshNotifyVoidBot bot;

    public TelegramService(final TelegramConfig config) throws TelegramApiException {
        bot = new SshNotifyVoidBot(config);
        api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(bot);
    }

    public void notifyConnected(final ConnectionDescription connectionDescription) {
        log.debug("SSH connection: {}", connectionDescription.brief());
        var sendMessage = SendMessage.builder()
                .chatId(355420409L)
                .text(connectionDescription.toMessageText())
                .parseMode("Markdown")
                .build();
        try {
            bot.execute(sendMessage);
        } catch (final TelegramApiException telegramApiException) {
            log.error("Could not execute SendMessage: {}", telegramApiException.getMessage());
            log.atDebug().setCause(telegramApiException).log("Exception on SendMessage {}", sendMessage);
        }
    }
}
