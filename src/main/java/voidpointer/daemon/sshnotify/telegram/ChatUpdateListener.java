package voidpointer.daemon.sshnotify.telegram;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;
import voidpointer.daemon.sshnotify.config.BanConfig;
import voidpointer.daemon.sshnotify.telegram.callback.CallbackHandlerManager;

@Slf4j
final class ChatUpdateListener {
    private final CallbackHandlerManager callbackHandlerManager;

    public ChatUpdateListener(final SshNotifyVoidBot bot, final BanConfig banConfig) {
        callbackHandlerManager = new CallbackHandlerManager(bot, banConfig);
    }

    public void onUpdate(final Update update) {
        if (update.hasCallbackQuery()) {
            callbackHandlerManager.execute(update.getCallbackQuery());
        }
        if (update.hasMessage()) {
            var msg = update.getMessage();
            log.debug("@{}({}): {}", msg.getFrom().getUserName(), msg.getChatId(), msg.getText());
            update.hasCallbackQuery();
        }
    }
}
