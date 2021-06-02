package org.lineate.downloader.progressbar;

public final class Progressbar {
    private final long downloaded;
    private final long percentage;
    private final DownloadStatus status;

    public Progressbar(long size, long downloaded, DownloadStatus downloadStatus) {

        this.downloaded = downloaded;
        percentage = size == 0 ? 0 : 100 * this.downloaded / size;

        status = downloadStatus;
    }

    public byte getPercentage() {
        return (byte)percentage;
    }

    public long getDownloaded() {
        return downloaded;
    }

    public DownloadStatus getStatus() {
        return status;
    }
}
