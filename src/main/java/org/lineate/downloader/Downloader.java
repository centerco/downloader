package org.lineate.downloader;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.Future;

public interface Downloader extends AutoCloseable {
    UUID create(String sourceUri, String destinationFilePath);
    String getSource(UUID id);
    String getDestination(UUID id);
    Future<File> download(UUID id);
    boolean downloaded(UUID id);
    float getProgress(UUID id);
}
