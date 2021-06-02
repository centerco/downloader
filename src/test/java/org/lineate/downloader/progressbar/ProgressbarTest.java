package org.lineate.downloader.progressbar;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ProgressbarTest {

    @Test
    public void getPercentage() {
        Progressbar progressbar = new Progressbar(0, 0, DownloadStatus.NOT_STARTED);
        assertNotNull(progressbar);
        assertEquals(0, progressbar.getPercentage());

        progressbar = new Progressbar(1000, 333, DownloadStatus.DOWNLOADING);
        assertEquals(33, progressbar.getPercentage());

    }

    @Test
    public void getDownloaded() {
        Progressbar progressbar = new Progressbar(100, 100, DownloadStatus.FINISHED);
        assertNotNull(progressbar);
        assertEquals(100, progressbar.getDownloaded());
    }
}