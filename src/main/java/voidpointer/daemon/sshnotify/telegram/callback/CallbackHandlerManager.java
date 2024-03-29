package voidpointer.daemon.sshnotify.telegram.callback;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import voidpointer.daemon.sshnotify.config.BanConfig;
import voidpointer.daemon.sshnotify.data.UserRepository;
import voidpointer.daemon.sshnotify.telegram.SshNotifyVoidBot;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public final class CallbackHandlerManager {
    private final Map<String, CallbackHandler> handlers;

    public CallbackHandlerManager(final UserRepository userRepository, final SshNotifyVoidBot bot, final BanConfig banConfig) {
        handlers = new ConcurrentHashMap<>(2);
        registerCallbackHandler(new IgnoreCallbackHandler(bot, userRepository));
        registerCallbackHandler(new BanCallbackHandler(bot, banConfig));
    }

    public void registerCallbackHandler(final CallbackHandler callbackHandler) {
        handlers.put(callbackHandler.name(), callbackHandler);
    }

    public void execute(final CallbackQuery query) {
        String[] data = query.getData().split(":");
        if (data.length < 2) {
            log.debug("Could not parse callback query data (\"{}\".length < 2) of {}", query.getData(), query);
            return;
        }
        CallbackHandler callbackHandler = handlers.get(data[0]);
        if (callbackHandler == null) {
            log.warn("Missing query handler for «{}»", data[0]);
            return;
        }
        callbackHandler.onQuery(query, data[1]);
    }
}
