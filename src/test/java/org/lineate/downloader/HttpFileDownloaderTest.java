package org.lineate.downloader;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HttpFileDownloaderTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(HttpFileDownloaderTest.class);
    private final static List<UUID> threads = new ArrayList<>();

    @Test
    public void httpFileDownloaderTest() {
        try(Downloader downloader = new HttpFileDownloader()) {
            for(int i=0; i<1200; i++) {
                threads.add(downloader.create("https://mirror.linux-ia64.org/apache/knox/1.5.0/knox-1.5.0-src.zip", "target/knox"+i+".html"));
            }

            ExecutorService pool = Executors.newFixedThreadPool(10);

            threads.forEach((id) -> {
                downloader.download(id);
                pool.execute(new Task(downloader, id.toString()));
            });

            pool.shutdown();
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final class Task implements Runnable {

        private final Downloader downloader;
        private final String id;

        public Task(Downloader downloader, String id) {
            this.downloader = downloader;
            this.id = id;
        }

        @Override
        public void run() {
            int size = 0;
            UUID id = UUID.fromString(this.id);
            while (!downloader.downloaded(id)) {
                int newSize = Math.round(downloader.getProgress(id));
                if (newSize > size) {
                    size = newSize;
                    LOGGER.info("Downloaded ~{}MB of {}", size, downloader.getDestination(id));
                }
            }

        }
    }

}
