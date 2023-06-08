package voidpointer.daemon.sshnotify.telegram;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import voidpointer.daemon.sshnotify.config.TelegramConfig;

@Slf4j
public final class SshNotifyVoidBot extends TelegramLongPollingBot {
    private final TelegramConfig config;
    private final ChatUpdateListener chatUpdateListener;

    SshNotifyVoidBot(final TelegramConfig config) {
        super(config.token());
        this.config = config;
        chatUpdateListener = new ChatUpdateListener(this, config.banConfig());
    }

    @Override public void onUpdateReceived(final Update update) {
        chatUpdateListener.onUpdate(update);
    }

    @Override public String getBotUsername() {
        return config.botUsername();
    }

    public void sendMessage(final SendMessage message) {
        try {
            execute(message);
        } catch (final TelegramApiException telegramApiException) {
            log.error("Could not execute SendMessage: {}", telegramApiException.getMessage());
            log.atDebug().setCause(telegramApiException).log("Exception on SendMessage {}", message);
        }
    }
}
