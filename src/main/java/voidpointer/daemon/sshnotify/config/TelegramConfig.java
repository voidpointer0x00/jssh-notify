package voidpointer.daemon.sshnotify.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.nio.file.Path;

@ConfigSerializable
public final class TelegramConfig {
    public static TelegramConfig loadAndSave(final Path src) {
        return HoconConfigLoader.loadAndSave(src, TelegramConfig.class, TelegramConfig::new);
    }

    private String token;
    private String botUsername;

    public TelegramConfig() {
        this.token = "";
        this.botUsername = "";
    }

    public String token() {
        return token;
    }

    public String botUsername() {
        return botUsername;
    }
}
