package org.lineate.downloader;

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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class HttpFileDownloader implements Downloader {

    private final static Logger LOGGER = LoggerFactory.getLogger(HttpFileDownloader.class);

    private final static Map<UUID, DownloadData> files = new ConcurrentHashMap<>();
    private final static Map<UUID, Float> progresses = new ConcurrentHashMap<>();

    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    @Override
    public UUID create(String sourceUri, String destinationFilePath) {
        UUID uuid = UUID.randomUUID();
        files.put(uuid, new DownloadData(sourceUri, destinationFilePath));
        progresses.put(uuid, 0f);
        return uuid;
    }

    @Override
    public Future<File> download(UUID id) {

        DownloadData names = files.get(id);
        if(names == null) {
            throw new RuntimeException("Unknown process id '"+id+"'");
        } else {
            LOGGER.info("Downloading '{}' into '{}'", names.getSourceUri(), names.getLocalFile());
        }

        try {
            return executor.submit(new DownloadCallable(id, new URL(names.getSourceUri()), new File(names.getLocalFile())));
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }

        return null;

    }

    @Override
    public boolean downloaded(UUID uuid) {
        return !files.containsKey(uuid);
    }

    @Override
    public float getProgress(UUID uuid) {
        return progresses.getOrDefault(uuid, 0f);
    }

    @Override
    public String getSource(UUID uuid) {
        if(files.containsKey(uuid)) {
            return files.get(uuid).getSourceUri();
        }
        return null;
    }

    @Override
    public String getDestination(UUID uuid) {
        if(files.containsKey(uuid)) {
            return files.get(uuid).getLocalFile();
        }
        return null;
    }

    @Override
    public void close() throws InterruptedException {
        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        LOGGER.info("Downloader closed.");
    }

    private static final class DownloadCallable implements Callable<File> {

        private static final int BUFFER_SIZE = 1024 * 1024;

        private final URL targetUrl;
        private final File destination;
        private final UUID uuid;

        public DownloadCallable(UUID uuid, final URL targetUrl, final File destination) {
            this.uuid = uuid;
            this.targetUrl = targetUrl;
            this.destination = destination;
        }

        @Override
        public File call() throws IOException {
            final URLConnection request = this.targetUrl.openConnection();
            try (final InputStream inputStream = request.getInputStream();
                 final FileOutputStream fileStream = new FileOutputStream(this.destination);
                 final BufferedOutputStream outputStream = new BufferedOutputStream(fileStream, BUFFER_SIZE)) {

                final byte[] data = new byte[10240];
                int bytesRead;
                int progress = 0;

                while ((bytesRead = inputStream.read(data)) != -1) {
                    progress += bytesRead;
                    progresses.replace(uuid, progress / 1024 / 1024f);
                    outputStream.write(data, 0, bytesRead);
                }
                files.remove(uuid);
                progresses.remove(uuid);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
            return this.destination;
        }
    }

}
