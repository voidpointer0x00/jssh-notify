package voidpointer.daemon.sshnotify.telegram;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;
import voidpointer.daemon.sshnotify.telegram.callback.CallbackHandlerManager;

@Slf4j
final class ChatUpdateListener {
    private final CallbackHandlerManager handlerManager = new CallbackHandlerManager();

    public void onUpdate(final Update update) {
        if (update.hasCallbackQuery()) {
            handlerManager.execute(update.getCallbackQuery());
        }
        if (update.hasMessage()) {
            var msg = update.getMessage();
            log.debug("@{}({}): {}", msg.getFrom().getUserName(), msg.getChatId(), msg.getText());
            update.hasCallbackQuery();
        }
    }
}
