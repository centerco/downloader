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

public final class HttpFileDownloaderReactive
        implements Downloader<Mono<Future<File>>, Flux<Future<File>>> {

    /**
     * Thread pool capacity by default.
     */
    private static final int THREADS_BY_DEFAULT = 10;

    /**
     * Default class logger.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(HttpFileDownloaderReactive.class);

    /**
     * Container for download data: source url and destination file name.
     */
    private final Map<UUID, DownloadData> files = new ConcurrentHashMap<>();

    /**
     * Container for progress status.
     */
    private final Map<UUID, Progressbar> progresses = new ConcurrentHashMap<>();

    /**
     * Download pool for file downloading tasks.
     */
    private final ExecutorService downloadPool;

    /**
     * Switch verbose mode.
     */
    private final boolean verbose;

    /**
     * Message constant for wrong uuid exception.
     */
    private static final String WRONG_UUID_MESSAGE = "Unable to find uuid: ";

    /**
     * Public constructor for the Downloader.
     *
     * @param threads number of threads for async mode
     * @param verboseValue switch verbose mode
     */
    public HttpFileDownloaderReactive(final int threads,
                                      final boolean verboseValue) {
        downloadPool = Executors.newFixedThreadPool(threads < 1
                ? THREADS_BY_DEFAULT : threads);
        this.verbose = verboseValue;
    }

    @Override
    public UUID create(final String sourceUri,
                       final String destinationFilePath) {
        UUID uuid = UUID.randomUUID();
        files.put(uuid, new DownloadData(sourceUri, destinationFilePath));
        progresses.put(uuid, new Progressbar(0, 0,
                DownloadStatus.NOT_STARTED));
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
                .fromIterable(files.keySet())
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
        return progresses.get(id).getPercentage();
    }

    @Override
    public long getProgressBytes(final UUID id) {
        return 0;
    }

    @Override
    public Set<UUID> getDownloads() {
        return progresses.keySet();
    }

    @Override
    public String getDestination(final UUID uuid) {
        if (files.containsKey(uuid)) {
            DownloadData names = files.get(uuid);
            if (names != null) {
                return files.get(uuid).getLocalFile();
            }
            throw new IllegalUuidException("No data found for uuid: " + uuid);
        }
        throw new IllegalUuidException(WRONG_UUID_MESSAGE + uuid);
    }

    @Override
    public void close() {
        files.clear();
        progresses.clear();

        downloadPool.shutdown();
        try {
            if (!downloadPool.awaitTermination(Long.MAX_VALUE,
                    TimeUnit.MILLISECONDS)) {
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
            LOGGER.info("Downloading file '{}'", files.get(id).getLocalFile());
        }
        try {
            return downloadPool.submit(
                    new DownloadTask(id,
                            new URL(files.get(id).getSourceUri()),
                            new File(files.get(id).getLocalFile())));
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load file: " + id);
        }
    }

    private final class DownloadTask implements Callable<File> {

        /**
         * Buffer size for output data writing.
         */
        private static final int BUFFER_SIZE = 1024 * 1024;

        /**
         * Block size to store read data.
         */
        private static final int BLOCK_SIZE = 10240;

        /**
         * Field for storing source {@link URL} object.
         */
        private final URL targetUrl;

        /**
         * Field for storing destination {@link File} object.
         */
        private final File destination;

        /**
         * Field for storing downloading task id.
         */
        private final UUID uuid;

        DownloadTask(final UUID uuidValue,
                     final URL targetUrlValue,
                     final File destinationValue) {
            this.uuid = uuidValue;
            this.targetUrl = targetUrlValue;
            this.destination = destinationValue;
        }

        public File call() throws IOException {

            final URLConnection request = this.targetUrl.openConnection();

            try (InputStream inputStream = request.getInputStream();
                 FileOutputStream fileStream =
                         new FileOutputStream(this.destination);
                 BufferedOutputStream outputStream =
                         new BufferedOutputStream(fileStream, BUFFER_SIZE)) {

                final byte[] data = new byte[BLOCK_SIZE];
                int bytesRead;
                int progress = 0;
                long targetSize = request.getContentLengthLong();

                if (verbose) {
                    LOGGER.info("Fetching from uri: '{}' to file '{}'",
                            this.targetUrl.getPath(),
                            this.destination.getPath());
                }

                while ((bytesRead = inputStream.read(data)) != -1) {
                    progress += bytesRead;
                    progresses.replace(uuid,
                            new Progressbar(targetSize,
                                    progress,
                                    DownloadStatus.DOWNLOADING));
                    outputStream.write(data, 0, bytesRead);
                }

                files.remove(uuid);
                progresses.replace(uuid,
                        new Progressbar(targetSize,
                                progress,
                                DownloadStatus.FINISHED));

                if (verbose) {
                    LOGGER.info("Fetching for {} completed",
                            this.destination.getPath());
                }

            } catch (Exception exception) {
                progresses.replace(uuid,
                        new Progressbar(0, 0, DownloadStatus.FAILED));
                exception.printStackTrace();
            }

            return this.destination;

        }
    }

}
