package org.lineate.downloader;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;

public class HttpFileDownloaderIntegrationTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(HttpFileDownloaderIntegrationTest.class);

    @Test
    public void httpFileDownloaderTest() {
        try(Downloader downloader = new HttpFileDownloader()) {
            List<UUID> threads = new ArrayList<>();
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

    @Test
    public void httpFileDownloaderTestObject() throws Exception {
        try(Downloader downloader = new HttpFileDownloader()) {
            ExecutorService pool = Executors.newFixedThreadPool(2);

            UUID id = downloader.create("https://mirror.linux-ia64.org/apache/knox/1.5.0/knox-1.5.0-src.zip", "target/knox.zip");
            UUID id2 = downloader.create("https://mirror.linux-ia64.org/apache/knox/1.5.0/knox-1.5.0-src.zip", "target/knox1.zip");
            Future<File> result = downloader.download(id);
            pool.execute(new Task(downloader, id.toString()));
            Future<File> result2 = downloader.download(id2);
            pool.execute(new Task(downloader, id2.toString()));

            File file1 = result.get();
            File file2 = result2.get();

            assertNotNull(file1);
            LOGGER.info("File1 done. Size: {} bytes", file1.length());
            assertNotNull(file2);
            LOGGER.info("File2 done. Size: {} bytes", file2.length());
        }
    }

    private static final class Task implements Runnable {

        private final Downloader downloader;
        private final String destination;
        private final UUID id;

        public Task(Downloader downloader, String id) {
            this.downloader = downloader;
            this.id = UUID.fromString(id);
            this.destination = downloader.getDestination(this.id);
        }

        @Override
        public void run() {
            int size = 0;
            while (!downloader.downloaded(id)) {
                int newSize = Math.round(downloader.getProgress(id));
                if (newSize > size) {
                    size = newSize;
                    if(size % 10 == 0) {
                        LOGGER.info("Downloaded ~{}% of {}", size, destination);
                    }
                }
            }

        }
    }

}
