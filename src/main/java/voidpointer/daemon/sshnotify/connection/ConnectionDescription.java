package voidpointer.daemon.sshnotify.connection;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Builder(access=AccessLevel.PACKAGE)
@AllArgsConstructor(access=AccessLevel.PACKAGE)
@EqualsAndHashCode
@ToString
public final class ConnectionDescription {
    private static final Pattern DESCRIPTION_PATTERN = Pattern.compile("""
            ^username:\\s(?<username>.*)$
            ^userip:\\s(?<userip>.*)$
            ^hostname:\\s(?<hostname>.*)$
            ^hostip:\\s(?<hostip>.*)$
            ?\\n?(?<message>(.|\\n)+)?""", Pattern.MULTILINE);

    @Contract(pure=true)
    public static @NotNull Optional<ConnectionDescription> parse(final String description) {
        Matcher matcher = DESCRIPTION_PATTERN.matcher(description);
        if (!matcher.find()) {
            log.warn("Non matching connection description:\n{}", description);
            log.debug("Pattern:\n{}", DESCRIPTION_PATTERN.pattern());
            return Optional.empty();
        }
        final String customMessage = matcher.group("message");
        return Optional.of(ConnectionDescription.builder()
                        .username(matcher.group("username"))
                        .userIp(matcher.group("userip"))
                        .hostname(matcher.group("hostname"))
                        .hostIp(matcher.group("hostip"))
                        .customMessage(customMessage != null ? customMessage.strip() : null)
                        .build());
    }

    private static final String MESSAGE_FORMAT = """
            *SSH Connection*
            User `{0}`@`{1}` connected to `{2}`@`{3}`{4}""";
    private static final String BRIEF_FORMAT = "{0}@{1} -> {2}@{3}";

    private final @NotNull String username;
    private final @NotNull String userIp;
    private final @NotNull String hostname;
    private final @NotNull String hostIp;
    private final @Nullable String customMessage;

    public String userIp() {
        return userIp;
    }

    public String toMessageText() {
        return MessageFormat.format(MESSAGE_FORMAT, username, userIp, hostname, hostIp,
                customMessage != null ? "\n" + customMessage : "");
    }

    public String brief() {
        return MessageFormat.format(BRIEF_FORMAT, username, userIp, hostname, hostIp);
    }
}
