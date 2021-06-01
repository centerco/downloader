package org.lineate.downloader;

import org.junit.Assert;
import org.junit.Test;
import org.lineate.downloader.exceptions.IllegalUuidException;
import org.lineate.downloader.httpdownloader.HttpFileDownloader;

import java.util.UUID;

public class HttpFileDownloaderUnitTest {
    @Test
    public void classTest() {
        Downloader downloader = new HttpFileDownloader(null);
        UUID uuid = downloader.create("Some source", "Some local");
        Assert.assertNotNull(uuid);
        Assert.assertEquals("Some source", downloader.getSource(uuid));
        Assert.assertEquals("Some local", downloader.getDestination(uuid));
        Assert.assertEquals(0, downloader.getProgress(uuid));
        Assert.assertEquals(0, downloader.getProgressBytes(uuid));
        Assert.assertFalse(downloader.downloaded(uuid));
    }

    @Test(expected = IllegalUuidException.class)
    public void testGetProgressBadUuid() {
        Downloader downloader = new HttpFileDownloader(null);
        UUID uuid = downloader.create("Some source", "Some local");
        Assert.assertNotNull(uuid);
        downloader.getProgress(UUID.randomUUID());
    }

    @Test(expected = IllegalUuidException.class)
    public void testGetProgressBytesBadUuid() {
        Downloader downloader = new HttpFileDownloader(null);
        UUID uuid = downloader.create("Some source", "Some local");
        Assert.assertNotNull(uuid);
        downloader.getProgressBytes(UUID.randomUUID());
    }

    @Test(expected = IllegalUuidException.class)
    public void testGetSourceBadUuid() {
        Downloader downloader = new HttpFileDownloader(null);
        UUID uuid = downloader.create("Some source", "Some local");
        Assert.assertNotNull(uuid);
        downloader.getSource(UUID.randomUUID());
    }

    @Test(expected = IllegalUuidException.class)
    public void testGetDestinationBadUuid() {
        Downloader downloader = new HttpFileDownloader(null);
        UUID uuid = downloader.create("Some source", "Some local");
        Assert.assertNotNull(uuid);
        downloader.getDestination(UUID.randomUUID());
    }
}
