package voidpointer.daemon.sshnotify.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.nio.file.Path;

@ConfigSerializable
public final class TelegramConfig {
    public static TelegramConfig loadAndSave(final Path src) {
        return HoconConfigLoader.loadAndSave(src, TelegramConfig.class, TelegramConfig::new);
    }

    @Comment("Telegram bot token from @BotFather")
    private String token = "1000000000:ABCDEFGHIJKLMNOPqrstuvwxyz-12345678";
    @Comment("The exact @username_bot of your bot")
    private String botUsername = "SshNotifyVoidBot";
    @Setting("ban")
    private BanConfig banConfig = new BanConfig();

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

    public BanConfig banConfig() {
        return banConfig;
    }
}
