package voidpointer.daemon.sshnotify.server;

import lombok.extern.slf4j.Slf4j;
import voidpointer.daemon.sshnotify.connection.ConnectionDescription;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.function.Consumer;

import static java.lang.System.getProperty;

@Slf4j
public final class DaemonServer {
    private final UnixDomainSocketAddress address;
    private final Consumer<ConnectionDescription> onClientFinished;

    private ServerSocketChannel serverChannel;
    private DaemonWorker worker;

    public DaemonServer(final Consumer<ConnectionDescription> onClientFinished) {
        final String basePath = getProperty("user.name").equals("root") ? "/run" : getProperty("user.home");
        this.address = UnixDomainSocketAddress.of(Path.of(basePath).resolve("ssh-notify.sock"));
        this.onClientFinished = onClientFinished;
    }

    public void bind() throws IOException {
        deleteUnixSocket();
        serverChannel = ServerSocketChannel.open(StandardProtocolFamily.UNIX);
        serverChannel.bind(address);
        Files.setPosixFilePermissions(this.address.getPath(), PosixFilePermissions.fromString("rwxrwxrwx"));
        log.info("Bind {}", serverChannel.getLocalAddress());
    }

    public void startAccepting() {
        try {
            worker = new DaemonWorker(onClientFinished);
        } catch (final IOException ioException) {
            log.atError().setCause(ioException).log("Could not create worker thread: {}", ioException.getMessage());
            return;
        }
        new Thread(worker).start();
        while (true) {
            try {
                worker.offer(serverChannel.accept());
            } catch (final Exception ex) {
                log.atError().setCause(ex).log("ServerSocketChannel#accept() failed: {}", ex.getMessage());
                worker.stop();
                break;
            }
        }
    }

    public void shutdown() {
        worker.stop();
        deleteUnixSocket();
    }

    private void deleteUnixSocket() {
        try {
            Files.deleteIfExists(address.getPath());
        } catch (final IOException ioException) {
            log.atError().setCause(ioException).log("Could not delete unix domain socket file");
        }
    }
}
