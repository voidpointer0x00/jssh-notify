package voidpointer.daemon.sshnotify.telegram;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
final class ChatUpdateListener {
    public void onUpdate(final Update update) {
        if (update.hasMessage()) {
            var msg = update.getMessage();
            log.debug("@{}({}): {}", msg.getFrom().getUserName(), msg.getChatId(), msg.getText());
        }
    }
}
