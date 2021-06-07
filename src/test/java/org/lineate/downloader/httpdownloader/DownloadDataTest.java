package org.lineate.downloader.httpdownloader;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DownloadDataTest {
    @Test
    public void testDownloadData() {
        String source = "Source";
        String destination = "Destination";

        DownloadData downloadData = new DownloadData(source, destination);
        DownloadData downloadData1 = new DownloadData(source, destination);

        assertEquals(downloadData, downloadData1);
        assertEquals(downloadData.getSourceUri(), source);
        assertEquals(downloadData.getLocalFile(), destination);
        assertEquals(downloadData.hashCode(), downloadData1.hashCode());
    }
}
