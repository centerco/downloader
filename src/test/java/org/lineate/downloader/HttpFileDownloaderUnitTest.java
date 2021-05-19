package org.lineate.downloader;

import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

public class HttpFileDownloaderUnitTest {
    @Test
    public void classTest() {
        Downloader downloader = new HttpFileDownloader();
        UUID uuid = downloader.create("Some source", "Some local");
        Assert.assertEquals("Some source", downloader.getSource(uuid));
        Assert.assertEquals("Some local", downloader.getDestination(uuid));
        Assert.assertEquals(0, downloader.getProgress(uuid), 0.0);
    }
}
