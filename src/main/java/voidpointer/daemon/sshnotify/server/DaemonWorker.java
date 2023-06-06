package voidpointer.daemon.sshnotify.server;

import lombok.extern.slf4j.Slf4j;
import voidpointer.daemon.sshnotify.connection.ConnectionDescription;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Slf4j
public final class DaemonWorker implements Runnable {
    private final AtomicBoolean isRunning = new AtomicBoolean(true);
    private final ByteBuffer buffer = ByteBuffer.allocate(1024 * 16 /* 16KiB */);
    private final BlockingQueue<SocketChannel> socketChannels = new ArrayBlockingQueue<>(16);
    private final Consumer<ConnectionDescription> submitFinishedTask;

    public DaemonWorker(final Consumer<ConnectionDescription> submitFinishedTask) throws IOException {
        this.submitFinishedTask = submitFinishedTask;
    }

    public void stop() {
        isRunning.set(false);
    }

    @Override public void run() {
        while (isRunning.get()) {
            try {
                process(socketChannels.take());
            } catch (final InterruptedException interruptedException) {
                log.info("Interrupted: {}", interruptedException.getMessage());
                break;
            }
        }
    }

    public void offer(final SocketChannel socketChannel) {
        if (!this.socketChannels.offer(socketChannel))
            log.warn("Could not offer SocketChannel as the queue is full");
    }

    private void process(final SocketChannel socketChannel) {
        final StringBuilder builder = new StringBuilder(1024 * 4 /* 4 KiB */);
        try (socketChannel) {
            int read;
            while ((read = socketChannel.read(buffer)) != -1) {
                buffer.flip();
                builder.append(new String(buffer.array()), 0, read);
                buffer.clear();
            }
            final String result = builder.toString().trim();
            if (!result.isBlank())
                ConnectionDescription.parse(result).ifPresent(submitFinishedTask);
        } catch (final IOException ioException) {
            log.atWarn().setCause(ioException).log("Exception on SocketChannel#read(): {}", ioException.getMessage());
        } finally {
            buffer.clear();
        }
    }
}
