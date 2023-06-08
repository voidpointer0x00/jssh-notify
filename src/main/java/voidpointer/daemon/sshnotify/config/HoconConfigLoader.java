package voidpointer.daemon.sshnotify.config;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import java.nio.file.Path;
import java.util.function.Supplier;

@Slf4j
final class HoconConfigLoader {
    static <T> @NotNull T loadAndSave(final Path src, final Class<T> type, final Supplier<T> defaultSupplier) {
        var loader = HoconConfigurationLoader.builder().path(src).build();
        CommentedConfigurationNode root;
        try {
            root = loader.load();
            log.trace("Loaded {}", src);
        } catch (final ConfigurateException ex) {
            log.warn("Could not parse {}: {}", type.getName(), ex.getMessage());
            return defaultSupplier.get();
        }
        T config;
        try {
            config = root.get(type);
        } catch (final SerializationException ex) {
            log.warn("Could not deserialize {}: {}", type.getName(), ex.getMessage());
            return defaultSupplier.get();
        }
        if (config == null)
            config = defaultSupplier.get();
        try {
            root.set(config);
        } catch (final SerializationException ex) {
            log.warn("Could not serialize {}: {}", type.getName(), ex.getMessage());
            return config;
        }
        try {
            loader.save(root);
            log.trace("Saved {}", src);
        } catch (final ConfigurateException ex) {
            log.warn("Could not save {}: {}", type.getName(), ex.getMessage());
        }
        return config;
    }
}
