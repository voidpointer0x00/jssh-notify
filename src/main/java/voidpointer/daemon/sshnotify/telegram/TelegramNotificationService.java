package voidpointer.daemon.sshnotify.telegram;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import voidpointer.daemon.sshnotify.config.TelegramConfig;
import voidpointer.daemon.sshnotify.connection.ConnectionDescription;

import java.util.List;

@Slf4j
public final class TelegramNotificationService {
    private final TelegramBotsApi api;
    private final SshNotifyVoidBot bot;

    public TelegramNotificationService(final TelegramConfig config) throws TelegramApiException {
        bot = new SshNotifyVoidBot(config);
        api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(bot);
    }

    public void notifyConnected(final ConnectionDescription connectionDescription) {
        log.debug("SSH connection: {}", connectionDescription.brief());
        /* FIXME protection: got to create a queue for actions, so that
         *  a malicious user could not send their own message with custom data
         * TODO protection: try and exploit the described above vulnerability */
        var ignoreButton = new InlineKeyboardButton("Ignore");
        ignoreButton.setCallbackData("ignore:" + connectionDescription.userIp());
        var killButton = new InlineKeyboardButton("Ban IP");
        killButton.setCallbackData("ban:" + connectionDescription.userIp());
        var sendMessage = SendMessage.builder()
                .chatId(355420409L) /* TODO fetch from db/config */
                .text(connectionDescription.toMessageText())
                .parseMode("Markdown")
                .replyMarkup(InlineKeyboardMarkup.builder()
                        .keyboard(List.of(List.of(ignoreButton, killButton)))
                        .build())
                .build();
        try {
            bot.execute(sendMessage);
        } catch (final TelegramApiException telegramApiException) {
            log.error("Could not execute SendMessage: {}", telegramApiException.getMessage());
            log.atDebug().setCause(telegramApiException).log("Exception on SendMessage {}", sendMessage);
        }
    }
}
