package org.lineate.downloader.httpdownloader;

import org.junit.Assert;
import org.junit.Test;
import org.lineate.downloader.Downloader;
import org.lineate.downloader.exceptions.BadUrlException;
import org.lineate.downloader.exceptions.IllegalUuidException;
import org.lineate.downloader.progressbar.DownloadStatus;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

public class HttpFileDownloaderUnitTest {
    @Test
    public void classTest() throws Exception {
        try (Downloader<Future<File>, List<Future<File>>> downloader = new HttpFileDownloader(null)) {
            UUID uuid = downloader.create("Some source", "Some local");
            Assert.assertNotNull(uuid);
            Assert.assertEquals("Some source", downloader.getSource(uuid));
            Assert.assertEquals("Some local", downloader.getDestination(uuid));
            Assert.assertEquals(0, downloader.getProgress(uuid));
            Assert.assertEquals(0, downloader.getProgressBytes(uuid));
            Assert.assertEquals(DownloadStatus.NOT_STARTED, downloader.getStatus(uuid));
            Assert.assertFalse(downloader.downloaded(uuid));
            Assert.assertFalse(downloader.downloading(uuid));
            Assert.assertFalse(downloader.failed(uuid));
        }
    }

    @Test(expected = IllegalUuidException.class)
    public void testGetProgressBadUuid() throws Exception {
        try(Downloader<Future<File>, List<Future<File>>> downloader = new HttpFileDownloader(null)) {
            UUID uuid = downloader.create("Some source", "Some local");
            Assert.assertNotNull(uuid);
            downloader.getProgress(UUID.randomUUID());
        }
    }

    @Test(expected = IllegalUuidException.class)
    public void testGetProgressBytesBadUuid() throws Exception {
        try(Downloader<Future<File>, List<Future<File>>> downloader = new HttpFileDownloader(null)) {
            UUID uuid = downloader.create("Some source", "Some local");
            Assert.assertNotNull(uuid);
            downloader.getProgressBytes(UUID.randomUUID());
        }
    }

    @Test(expected = IllegalUuidException.class)
    public void testGetSourceBadUuid() throws Exception {
        try(Downloader<Future<File>, List<Future<File>>> downloader = new HttpFileDownloader(null)) {
            UUID uuid = downloader.create("Some source", "Some local");
            Assert.assertNotNull(uuid);
            downloader.getSource(UUID.randomUUID());
        }
    }

    @Test(expected = IllegalUuidException.class)
    public void testGetDestinationBadUuid() throws Exception {
        try(Downloader<Future<File>, List<Future<File>>> downloader = new HttpFileDownloader(null)) {
            UUID uuid = downloader.create("Some source", "Some local");
            Assert.assertNotNull(uuid);
            downloader.getDestination(UUID.randomUUID());
        }
    }

    @Test(expected = IllegalUuidException.class)
    public void testGetStatusBadUuid() throws Exception {
        try(Downloader<Future<File>, List<Future<File>>> downloader = new HttpFileDownloader(null)) {
            UUID uuid = downloader.create("Some source", "Some local");
            Assert.assertNotNull(uuid);
            downloader.getStatus(UUID.randomUUID());
        }
    }

    @Test(expected = BadUrlException.class)
    public void testRemove() throws Exception {
        try(Downloader<Future<File>, List<Future<File>>> downloader = new HttpFileDownloader(null)) {
            UUID id = downloader.create("Some source", "Some local");
            downloader.remove(id);
            downloader.download(id);
        }
    }
}
