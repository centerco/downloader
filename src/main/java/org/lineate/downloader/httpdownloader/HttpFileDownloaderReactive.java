package org.lineate.downloader.httpdownloader;

import org.lineate.downloader.Downloader;
import org.lineate.downloader.exceptions.IllegalUuidException;
import org.lineate.downloader.progressbar.DownloadStatus;
import org.lineate.downloader.progressbar.Progressbar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public final class HttpFileDownloaderReactive implements Downloader<Mono<Future<File>>, Flux<Future<File>>> {

    private static final int THREADS_BY_DEFAULT = 10;

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpFileDownloaderReactive.class);

    private final Map<UUID, DownloadData> FILES = new ConcurrentHashMap<>();
    private final Map<UUID, Progressbar> PROGRESSES = new ConcurrentHashMap<>();

    private final ExecutorService downloadPool;
    private final boolean verbose;

    private static final String WRONG_UUID_MESSAGE = "Unable to find uuid: ";

    public HttpFileDownloaderReactive(final int threads, final boolean verbose) {
        downloadPool = Executors.newFixedThreadPool(threads < 1 ? THREADS_BY_DEFAULT : threads);
        this.verbose = verbose;
    }

    @Override
    public UUID create(final String sourceUri, final String destinationFilePath) {
        UUID uuid = UUID.randomUUID();
        FILES.put(uuid, new DownloadData(sourceUri, destinationFilePath));
        PROGRESSES.put(uuid, new Progressbar(0, 0, DownloadStatus.NOT_STARTED));
        return uuid;
    }

    @Override
    public void remove(final UUID id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DownloadStatus getStatus(final UUID id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getSource(final UUID id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<Future<File>> download(final UUID id) {
        return Mono.just(get(id));
    }

    @Override
    public Flux<Future<File>> downloadAll() {
        return Flux
                .fromIterable(FILES.keySet())
                .flatMap((id) -> Mono.defer(() -> Mono.just(get(id))));
    }

    @Override
    public boolean downloaded(final UUID id) {
        return false;
    }

    @Override
    public boolean downloading(final UUID id) {
        return false;
    }

    @Override
    public boolean failed(final UUID id) {
        return false;
    }

    @Override
    public byte getProgress(final UUID id) {
        return PROGRESSES.get(id).getPercentage();
    }

    @Override
    public long getProgressBytes(final UUID id) {
        return 0;
    }

    @Override
    public Set<UUID> getDownloads() {
        return PROGRESSES.keySet();
    }

    @Override
    public String getDestination(final UUID uuid) {
        if (FILES.containsKey(uuid)) {
            DownloadData names = FILES.get(uuid);
            if (names != null) {
                return FILES.get(uuid).getLocalFile();
            }
            throw new IllegalUuidException("No data found for uuid: " + uuid);
        }
        throw new IllegalUuidException(WRONG_UUID_MESSAGE + uuid);
    }

    @Override
    public void close() {
        FILES.clear();
        PROGRESSES.clear();

        downloadPool.shutdown();
        try {
            if (!downloadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Thread pool termination error.");
            } else {
                LOGGER.info("Downloader closed.");
            }
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex.getLocalizedMessage());
        }
    }

    private Future<File> get(final UUID id) {
        if (verbose) {
            LOGGER.info("Downloading file '{}'", FILES.get(id).getLocalFile());
        }
        try {
            return downloadPool.submit(new DownloadTask(id, new URL(FILES.get(id).getSourceUri()), new File(FILES.get(id).getLocalFile()), verbose));
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load file: " + id);
        }
    }

    private final class DownloadTask implements Callable<File> {

        private static final int BUFFER_SIZE = 1024 * 1024;
        private static final int BLOCK_SIZE = 10240;

        private final URL targetUrl;
        private final File destination;
        private final UUID uuid;
        private final boolean verbose;

        DownloadTask(final UUID uuid,
                     final URL targetUrl,
                     final File destination,
                     final boolean verbose) {
            this.uuid = uuid;
            this.targetUrl = targetUrl;
            this.destination = destination;
            this.verbose = verbose;
        }

        public File call() throws IOException {

            final URLConnection request = this.targetUrl.openConnection();

            try (InputStream inputStream = request.getInputStream();
                 FileOutputStream fileStream = new FileOutputStream(this.destination);
                 BufferedOutputStream outputStream = new BufferedOutputStream(fileStream, BUFFER_SIZE)) {

                final byte[] data = new byte[BLOCK_SIZE];
                int bytesRead;
                int progress = 0;
                long targetSize = request.getContentLengthLong();

                if (verbose) {
                    LOGGER.info("Fetching from uri: '{}' to file '{}'", this.targetUrl.getPath(), this.destination.getPath());
                }

                while ((bytesRead = inputStream.read(data)) != -1) {
                    progress += bytesRead;
                    PROGRESSES.replace(uuid, new Progressbar(targetSize, progress, DownloadStatus.DOWNLOADING));
                    outputStream.write(data, 0, bytesRead);
                }

                FILES.remove(uuid);
                PROGRESSES.replace(uuid, new Progressbar(targetSize, progress, DownloadStatus.FINISHED));

                if (verbose) {
                    LOGGER.info("Fetching for {} completed", this.destination.getPath());
                }

            } catch (Exception exception) {
                PROGRESSES.replace(uuid, new Progressbar(0, 0, DownloadStatus.FAILED));
                exception.printStackTrace();
            }

            return this.destination;

        }
    }

}
