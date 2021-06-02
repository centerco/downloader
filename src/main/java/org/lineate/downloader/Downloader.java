package org.lineate.downloader;

import org.lineate.downloader.progressbar.DownloadStatus;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

public interface Downloader extends AutoCloseable {
    UUID create(String sourceUri, String destinationFilePath);
    void remove(UUID id);
    DownloadStatus getStatus(UUID id);
    String getSource(UUID id);
    String getDestination(UUID id);
    Future<File> download(UUID id);
    List<Future<File>> downloadAll() throws InterruptedException;
    boolean downloaded(UUID id);
    boolean downloading(UUID id);
    boolean failed(UUID id);
    byte getProgress(UUID id);
    long getProgressBytes(UUID id);
}
