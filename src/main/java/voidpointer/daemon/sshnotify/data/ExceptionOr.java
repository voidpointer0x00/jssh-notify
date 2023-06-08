package voidpointer.daemon.sshnotify.data;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
public class ExceptionOr<ResultT> {

    public static <T> ExceptionOr<T> exception(final Exception exception) {
        return new ExceptionOr<>(exception, null);
    }

    public static <T> ExceptionOr<T> result(final T result) {
        return new ExceptionOr<>(null, result);
    }

    private final Exception exception;
    private final ResultT result;

    public void ifExceptionOrElse(final Consumer<Exception> onException, final Consumer<ResultT> orElse) {
        if (exception != null)
            onException.accept(exception);
        else
            orElse.accept(result);
    }
}
