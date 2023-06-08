package voidpointer.daemon.sshnotify.data.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import voidpointer.daemon.sshnotify.data.DatabaseCredentials;
import voidpointer.daemon.sshnotify.data.ExceptionOr;
import voidpointer.daemon.sshnotify.data.User;
import voidpointer.daemon.sshnotify.data.UserRepository;

import java.util.concurrent.CompletableFuture;

public final class RedisUserRepository implements UserRepository {

    private record UserResponse(String ip, boolean ignore) implements User {}

    private JedisPool redisPool;

    public void connect(final DatabaseCredentials credentials) {
        redisPool = new JedisPool(credentials.url(), credentials.port(), credentials.username(), credentials.password());
    }

    @Override public CompletableFuture<ExceptionOr<User>> userByIp(final String ip) {
        return CompletableFuture.supplyAsync(() -> {
            if (redisPool == null)
                return ExceptionOr.exception(new IllegalStateException("Not connected to redis"));
            try (final Jedis redis = redisPool.getResource()) {
                return ExceptionOr.result(new UserResponse(ip, Boolean.parseBoolean(redis.get(ip))));
            } catch (final Exception exception) {
                return ExceptionOr.exception(exception);
            }
        });
    }
}
