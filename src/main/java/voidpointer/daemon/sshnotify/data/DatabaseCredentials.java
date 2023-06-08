package voidpointer.daemon.sshnotify.data;

public interface DatabaseCredentials {
    String url();

    int port();

    String username();

    String password();
}
