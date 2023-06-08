package voidpointer.daemon.sshnotify.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ConfigSerializable
public final class BanConfig {
    @Comment("""
             A command for your operating system to ban an IP of a connected user.
            Available placeholders: {ip} — the IP that will be banned.""")
    private String banCommand = "ufw deny from {ip} to any";

    public List<String> banCommand(final String ip) {
        return Stream.of(banCommand.split("\\s"))
                .map(cmd -> cmd.replace("{ip}", ip))
                .collect(Collectors.toList());
    }
}
