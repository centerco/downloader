package org.lineate.downloader;

import org.lineate.downloader.progressbar.DownloadStatus;

import java.util.Set;
import java.util.UUID;

public interface Downloader<M, F> extends AutoCloseable {
    UUID create(String sourceUri, String destinationFilePath);
    void remove(UUID id);
    DownloadStatus getStatus(UUID id);
    String getSource(UUID id);
    String getDestination(UUID id);
    M download(UUID id);
    F downloadAll() throws InterruptedException;
    boolean downloaded(UUID id);
    boolean downloading(UUID id);
    boolean failed(UUID id);
    byte getProgress(UUID id);
    long getProgressBytes(UUID id);
    Set<UUID> getDownloads();
}
