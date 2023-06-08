package voidpointer.daemon.sshnotify.telegram.callback;

import org.slf4j.Logger;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

interface CallbackHandler {
    Logger LOG = org.slf4j.LoggerFactory.getLogger(CallbackHandler.class);

    String name();

    default void onQuery(final CallbackQuery query, final String payload) {
        LOG.trace("{}->{}", name(), payload);
    }
}
