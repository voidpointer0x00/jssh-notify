package voidpointer.daemon.sshnotify.config;

import lombok.NoArgsConstructor;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.nio.file.Path;

@NoArgsConstructor
@ConfigSerializable
public final class TelegramConfig {
    public static TelegramConfig loadAndSave(final Path src) {
        return voidpointer.daemon.sshnotify.config.HoconConfigLoader.loadAndSave(src, TelegramConfig.class, TelegramConfig::new);
    }

    private String token = "";
    private String botUsername = "";

    public String token() {
        return token;
    }

    public String botUsername() {
        return botUsername;
    }
}
