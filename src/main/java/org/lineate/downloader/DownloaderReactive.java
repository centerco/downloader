package org.lineate.downloader;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Future;

public interface DownloaderReactive extends AutoCloseable {
    UUID create(String sourceUri, String destinationFilePath);
    Mono<Future<File>> download(UUID id);
    Flux<Future<File>> downloadAll();
    byte getProgress(UUID id);
    Set<UUID> getDownloads();
    String getDestination(UUID id);
}
