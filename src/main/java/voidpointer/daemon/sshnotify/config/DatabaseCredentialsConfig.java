package voidpointer.daemon.sshnotify.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import voidpointer.daemon.sshnotify.data.DatabaseCredentials;

import java.nio.file.Path;

@ConfigSerializable
public final class DatabaseCredentialsConfig {
    public static DatabaseCredentialsConfig loadAndSave(final Path path) {
        return HoconConfigLoader.loadAndSave(path, DatabaseCredentialsConfig.class, DatabaseCredentialsConfig::new);
    }

    @ConfigSerializable
    private record CredentialsConfig(String url, int port, String username, String password)
            implements DatabaseCredentials {}

    @Setting
    private CredentialsConfig credentials = new CredentialsConfig("localhost", 6379, null, null);

    public DatabaseCredentials credentials() {
        return credentials;
    }
}
