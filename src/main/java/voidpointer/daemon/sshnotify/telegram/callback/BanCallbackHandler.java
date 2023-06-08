package voidpointer.daemon.sshnotify.telegram.callback;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class BanCallbackHandler implements CallbackHandler {
    @Override public String name() {
        return "ban";
    }
}
