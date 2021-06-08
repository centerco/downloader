package org.lineate.downloader.httpdownloader;

import org.junit.Test;
import org.lineate.downloader.Downloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HttpFileDownloaderReactiveTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpFileDownloaderReactiveTest.class);
    private static final String ERROR_MESSAGE = "Error occurred while downloading: {}";

    @Test
    public void testDownloadSingleFile() throws Exception {
        try(Downloader<Mono<Future<File>>, Flux<Future<File>>> downloader = new HttpFileDownloaderReactive(1, false)) {
            UUID id = downloader.create("https://apache-mirror.rbc.ru/pub/apache/kafka/2.8.0/kafka-2.8.0-src.tgz ", "target/kafka20.zip");

            Mono<Future<File>> result = downloader.download(id);
            assertNotNull(result);

            result
                    .doOnSuccess(this::info)
                    .doOnError((f) -> LOGGER.error(ERROR_MESSAGE, f.getLocalizedMessage()))
                    .subscribe();
        }
    }

    @Test
    public void testDownloadAllFiles() {
        try(Downloader<Mono<Future<File>>, Flux<Future<File>>> downloader = new HttpFileDownloaderReactive(3, false)) {

            downloader.create("https://apache-mirror.rbc.ru/pub/apache/kafka/2.8.0/kafka-2.8.0-src.tgz ", "target/kafka.zip");
            downloader.create("https://apache-mirror.rbc.ru/pub/apache/kafka/2.8.0/kafka-2.8.0-src.tgz ", "target/kafka1.zip");
            downloader.create("https://apache-mirror.rbc.ru/pub/apache/kafka/2.8.0/kafka-2.8.0-src.tgz ", "target/kafka2.zip");
            downloader.create("https://apache-mirror.rbc.ru/pub/apache/kafka/2.8.0/kafka-2.8.0-src.tgz ", "target/kafka3.zip");
            downloader.create("https://apache-mirror.rbc.ru/pub/apache/kafka/2.8.0/kafka-2.8.0-src.tgz ", "target/kafka4.zip");
            downloader.create("https://apache-mirror.rbc.ru/pub/apache/kafka/2.8.0/kafka-2.8.0-src.tgz ", "target/kafka5.zip");

            ParallelFlux<Future<File>> result = downloader.downloadAll().parallel().runOn(Schedulers.boundedElastic());
            assertNotNull(result);

            ExecutorService downloadMonitor = Executors.newSingleThreadExecutor();
            downloadMonitor.execute(new DownloadMonitor(downloader));

            AtomicInteger counter = new AtomicInteger(0);
            int fetchSize = 2;

            result
                    .sequential()
                    .takeLast(fetchSize)
                    .doOnNext((r) -> {
                        info(r);
                        counter.getAndIncrement();
                    })
                    .doOnError((f) -> LOGGER.error(ERROR_MESSAGE, f.getLocalizedMessage()))
                    .doOnComplete(() -> assertEquals(fetchSize, counter.get()))
                    .subscribe();

            downloadMonitor.shutdown();
            if(!downloadMonitor.awaitTermination(2, TimeUnit.MINUTES)) {
                LOGGER.error("Error while shutdown download monitor.");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void info(Future<File> f) {
        try {
            File result = f.get();
            LOGGER.info("Downloaded '{}', size: {} bytes", result.getPath(), result.length());
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static final class DownloadMonitor implements Runnable {
        private final Set<UUID> downloads;
        private final Map<UUID, Byte> progresses = new HashMap<>();
        private final Map<UUID, String> files = new HashMap<>();
        private final Downloader<Mono<Future<File>>, Flux<Future<File>>> downloaderReactive;

        public DownloadMonitor(Downloader<Mono<Future<File>>, Flux<Future<File>>> downloader) {
            this.downloads = downloader.getDownloads();
            this.downloaderReactive = downloader;
            downloads.forEach((v) -> {
                progresses.put(v, (byte) 0);
                files.put(v, downloaderReactive.getDestination(v));
            });

        }

        @Override
        public void run() {
            while(!downloaded()) {
                for(UUID download : downloads) {

                    if(!progresses.containsKey(download)) {
                        progresses.put(download, (byte) 0);
                        continue;
                    }

                    byte progress = progresses.get(download);
                    if(progress==100) {
                        continue;
                    }

                    byte newProgress = downloaderReactive.getProgress(download);
                    if(newProgress > progress) {
                        progresses.replace(download, newProgress);
                        if(newProgress % 10 == 0) {
                            LOGGER.info("File {} downloaded status: {}%", files.get(download), newProgress);
                        }
                    }

                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        throw new RuntimeException("Interrupted.");
                    }
                }
            }
        }

        private boolean downloaded() {
            return progresses.values().stream().noneMatch((progress) -> progress < 100);
        }
    }
}
