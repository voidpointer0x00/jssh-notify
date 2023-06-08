package voidpointer.daemon.sshnotify.data;

import java.util.concurrent.CompletableFuture;

public interface UserRepository {
    CompletableFuture<ExceptionOr<User>> userByIp(final String ip);

    CompletableFuture<ExceptionOr<User>> ignore(final String ip);
}
