package voidpointer.daemon.sshnotify.data.redis;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisPooled;
import voidpointer.daemon.sshnotify.data.DatabaseCredentials;
import voidpointer.daemon.sshnotify.data.ExceptionOr;
import voidpointer.daemon.sshnotify.data.User;
import voidpointer.daemon.sshnotify.data.UserRepository;

import java.util.concurrent.CompletableFuture;

@Slf4j
public final class RedisUserRepository implements UserRepository {

    private record UserResponse(String ip, boolean ignore) implements User {}

    private JedisPooled redisPool;

    public void connect(final DatabaseCredentials credentials) {
        try {
            if (credentials.password() == null || credentials.username() == null)
                redisPool = new JedisPooled(credentials.url(), credentials.port());
            else
                redisPool = new JedisPooled(credentials.url(), credentials.port(), credentials.username(), credentials.password());
        } catch (final Exception exception) {
            log.warn("Could not connect to Redis: {}", exception.getMessage());
            log.atDebug().setCause(exception).log("Exception on creating JedisPooled: {}", exception.getMessage());
        }
    }

    @Override public CompletableFuture<ExceptionOr<User>> userByIp(final String ip) {
        return CompletableFuture.supplyAsync(() -> {
            if (redisPool == null)
                return ExceptionOr.exception(new IllegalStateException("Not connected to redis"));
            try {
                return ExceptionOr.get(new UserResponse(ip, Boolean.parseBoolean(redisPool.get(ip))));
            } catch (final Exception exception) {
                return ExceptionOr.exception(exception);
            }
        });
    }

    @Override public CompletableFuture<ExceptionOr<User>> ignore(String ip) {
        return CompletableFuture.supplyAsync(() -> {
            if (redisPool == null)
                return ExceptionOr.exception(new IllegalStateException("Not connected to redis"));
            try {
                redisPool.set(ip, Boolean.TRUE.toString());
                return ExceptionOr.get(new UserResponse(ip, true));
            } catch (final Exception exception) {
                return ExceptionOr.exception(exception);
            }
        });
    }
}
