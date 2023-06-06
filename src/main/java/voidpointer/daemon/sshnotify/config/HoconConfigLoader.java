package voidpointer.daemon.sshnotify.config;

import lombok.extern.slf4j.Slf4j;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import java.nio.file.Path;
import java.util.function.Supplier;

@Slf4j
final class HoconConfigLoader {
    static <T> T loadAndSave(final Path src, final Class<T> type, final Supplier<T> defaultSupplier) {
        var loader = HoconConfigurationLoader.builder().path(src).build();
        CommentedConfigurationNode root;
        try {
            root = loader.load();
        } catch (final ConfigurateException ex) {
            log.atWarn().setCause(ex).log("Could not parse {}: {}", type, ex.getMessage());
            return defaultSupplier.get();
        }
        T config;
        try {
            config = root.get(type);
        } catch (final SerializationException ex) {
            log.atError().setCause(ex).log("Could not deserialize {}: {}", type, ex.getMessage());
            return defaultSupplier.get();
        }
        if (config == null)
            config = defaultSupplier.get();
        try {
            root.set(config);
        } catch (final SerializationException ex) {
            log.atError().setCause(ex).log("Could not serialize {}: {}", type, ex.getMessage());
            return config;
        }
        try {
            loader.save(root);
        } catch (final ConfigurateException ex) {
            log.atWarn().setCause(ex).log("Could not save {}: {}", type, ex.getMessage());
        }
        return config;
    }
}
