package voidpointer.daemon.sshnotify.telegram;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import voidpointer.daemon.sshnotify.config.TelegramConfig;

final class SshNotifyVoidBot extends TelegramLongPollingBot {
    private final TelegramConfig config;
    private final ChatUpdateListener chatUpdateListener = new ChatUpdateListener();

    SshNotifyVoidBot(final TelegramConfig config) {
        super(config.token());
        this.config = config;
    }

    @Override public void onUpdateReceived(final Update update) {
        chatUpdateListener.onUpdate(update);
    }

    @Override public String getBotUsername() {
        return config.botUsername();
    }
}
