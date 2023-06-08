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
import voidpointer.daemon.sshnotify.data.ExceptionOr;
import voidpointer.daemon.sshnotify.data.User;
import voidpointer.daemon.sshnotify.data.UserRepository;

import java.util.List;

@Slf4j
public final class TelegramNotificationService {
    private final UserRepository userRepository;
    private final TelegramBotsApi api;
    private final SshNotifyVoidBot bot;

    public TelegramNotificationService(final TelegramConfig config, final UserRepository userRepository)
            throws TelegramApiException {
        this.userRepository = userRepository;
        bot = new SshNotifyVoidBot(config, userRepository);
        api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(bot);
    }

    public void notifyConnected(final ConnectionDescription connDescription) {
        log.trace("SSH connection: {}", connDescription.brief());
        ExceptionOr<User> userOrException = userRepository.userByIp(connDescription.userIp()).join();
        if (userOrException.hasException()) {
            final Exception exception = userOrException.exception();
            log.warn("Could check whether to ignore {}: {}", connDescription.userIp(), exception.getMessage());
            log.atDebug().setCause(exception).log("Exception on user fetch: {}", exception.getMessage());
        } else if (userOrException.get().ignore()) {
            log.trace("Ignoring connection from {}", connDescription.userIp());
            return;
        }

        /* FIXME protection: got to create a queue for actions, so that
         *  a malicious user could not send their own message with custom data
         * TODO protection: try and exploit the described above vulnerability */
        var ignoreButton = new InlineKeyboardButton("Ignore");
        ignoreButton.setCallbackData("ignore:" + connDescription.userIp());
        var killButton = new InlineKeyboardButton("Ban IP");
        killButton.setCallbackData("ban:" + connDescription.userIp());
        bot.sendMessage(SendMessage.builder()
                .chatId(355420409L) /* TODO fetch from db/config */
                .text(connDescription.toMessageText())
                .parseMode("Markdown")
                .replyMarkup(InlineKeyboardMarkup.builder()
                        .keyboard(List.of(List.of(ignoreButton, killButton)))
                        .build())
                .build());
    }
}
