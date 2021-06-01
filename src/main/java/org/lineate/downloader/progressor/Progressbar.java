package org.lineate.downloader.progressor;

public final class Progressbar {
    private final long size;
    private final long downloaded;
    private final long percentage;

    public Progressbar(long size, long downloaded) {
        this.size = size;
        this.downloaded = downloaded;
        percentage = size == 0 ? 0 : 100 * this.downloaded / this.size;
    }

    public byte getPercentage() {
        return (byte)percentage;
    }

    public long getDownloaded() {
        return downloaded;
    }
}
