package org.lineate.downloader;

import java.util.UUID;

public interface Downloader extends AutoCloseable {
    UUID create(String sourceUri, String destinationFilePath);
    String getSource(UUID id);
    String getDestination(UUID id);
    void download(UUID id);
    boolean downloaded(UUID id);
    float getProgress(UUID id);
}
