package org.lineate.downloader.httpdownloader;

import org.lineate.downloader.Downloader;
import org.lineate.downloader.exceptions.BadUrlException;
import org.lineate.downloader.exceptions.IllegalUuidException;
import org.lineate.downloader.progressbar.DownloadStatus;
import org.lineate.downloader.progressbar.Progressbar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public final class HttpFileDownloader implements Downloader<Future<File>, List<Future<File>>> {

    private static final int THREADS_BY_DEFAULT = 10;

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpFileDownloader.class);

    private final Map<UUID, DownloadData> FILES = new ConcurrentHashMap<>();
    private final Map<UUID, Progressbar> PROGRESSES = new ConcurrentHashMap<>();

    private static final String WRONG_UUID_MESSAGE = "Unable to find uuid: ";

    private final ExecutorService executor;

    public HttpFileDownloader(final Properties properties) {
        int nThreads = THREADS_BY_DEFAULT;
        if (properties != null) {
            nThreads = (Integer) properties.getOrDefault("threads", THREADS_BY_DEFAULT);
        }

        executor = Executors.newFixedThreadPool(nThreads);
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
        FILES.remove(id);
        PROGRESSES.remove(id);
    }

    @Override
    public DownloadStatus getStatus(final UUID id) {
        Progressbar progress = PROGRESSES.get(id);
        if (progress == null) {
            throw new IllegalUuidException(WRONG_UUID_MESSAGE + id);
        }
        return progress.getStatus();
    }

    @Override
    public Future<File> download(final UUID id) {

        DownloadData names = FILES.get(id);
        if (names == null) {
            throw new BadUrlException("Unknown process id '" + id + "'");
        } else {
            LOGGER.info("Downloading '{}' into '{}'", names.getSourceUri(), names.getLocalFile());
        }

        try {
            return executor.submit(new DownloadTask(id, new URL(names.getSourceUri()), new File(names.getLocalFile())));
        } catch (MalformedURLException ex) {
            throw new BadUrlException(ex.getLocalizedMessage());
        }

    }

    @Override
    public List<Future<File>> downloadAll() throws InterruptedException {
        List<DownloadTask> tasks = new ArrayList<>();
        FILES.forEach((id, names) -> {
            try {
                LOGGER.info("Starting job for id '{}' of total {} jobs, storing '{}' to '{}'", id, FILES.size(), names.getSourceUri(), names.getLocalFile());
                tasks.add(new DownloadTask(id, new URL(names.getSourceUri()), new File(names.getLocalFile())));
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
                throw new BadUrlException(ex.getLocalizedMessage());
            }
        });
        return executor.invokeAll(tasks);
    }

    @Override
    public boolean downloaded(final UUID uuid) {
        return PROGRESSES.get(uuid).getStatus() == DownloadStatus.FINISHED;
    }

    @Override
    public boolean downloading(final UUID uuid) {
        return PROGRESSES.get(uuid).getStatus() == DownloadStatus.DOWNLOADING;
    }

    @Override
    public boolean failed(final UUID uuid) {
        return PROGRESSES.get(uuid).getStatus() == DownloadStatus.FAILED;
    }

    @Override
    public byte getProgress(final UUID uuid) {
        Progressbar progressbar = PROGRESSES.getOrDefault(uuid, null);
        if (progressbar == null) {
            throw new IllegalUuidException(WRONG_UUID_MESSAGE + uuid);
        }
        return progressbar.getPercentage();
    }

    @Override
    public long getProgressBytes(final UUID uuid) {
        Progressbar progressbar = PROGRESSES.getOrDefault(uuid, null);
        if (progressbar == null) {
            throw new IllegalUuidException(WRONG_UUID_MESSAGE + uuid);
        }
        return  progressbar.getDownloaded();
    }

    @Override
    public Set<UUID> getDownloads() {
        return PROGRESSES.keySet();
    }

    @Override
    public String getSource(final UUID uuid) {
        if (FILES.containsKey(uuid)) {
            DownloadData names = FILES.get(uuid);
            if (names != null) {
                return FILES.get(uuid).getSourceUri();
            }
            throw new IllegalUuidException("No data found for uuid: " + uuid);
        }
        throw new IllegalUuidException(WRONG_UUID_MESSAGE + uuid);
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
    public void close() throws InterruptedException {
        FILES.clear();
        PROGRESSES.clear();

        executor.shutdown();
        if (!executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)) {
            throw new RuntimeException("Thread pool termination error.");
        } else {
            LOGGER.info("Downloader closed.");
        }
    }

    private final class DownloadTask implements Callable<File> {

        private static final int BUFFER_SIZE = 1024 * 1024;
        private static final int BLOCK_SIZE = 10240;

        private final URL targetUrl;
        private final File destination;
        private final UUID uuid;

        DownloadTask(final UUID uuid,
                     final URL targetUrl,
                     final File destination) {
            this.uuid = uuid;
            this.targetUrl = targetUrl;
            this.destination = destination;
        }

        @Override
        public File call() throws IOException {

            final URLConnection request = this.targetUrl.openConnection();

            try (InputStream inputStream = request.getInputStream();
                 FileOutputStream fileStream = new FileOutputStream(this.destination);
                 BufferedOutputStream outputStream = new BufferedOutputStream(fileStream, BUFFER_SIZE)) {

                final byte[] data = new byte[BLOCK_SIZE];
                int bytesRead;
                int progress = 0;
                long targetSize = request.getContentLengthLong();

                LOGGER.info("Fetching from uri: '{}' to file '{}'", this.targetUrl.getPath(), this.destination.getPath());

                while ((bytesRead = inputStream.read(data)) != -1) {
                    progress += bytesRead;
                    PROGRESSES.replace(uuid, new Progressbar(targetSize, progress, DownloadStatus.DOWNLOADING));
                    outputStream.write(data, 0, bytesRead);
                }

                FILES.remove(uuid);
                PROGRESSES.replace(uuid, new Progressbar(targetSize, progress, DownloadStatus.FINISHED));

            } catch (Exception exception) {
                PROGRESSES.replace(uuid, new Progressbar(0, 0, DownloadStatus.FAILED));
                exception.printStackTrace();
            }

            return this.destination;

        }
    }

}
