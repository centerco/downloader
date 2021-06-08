package org.lineate.downloader.progressbar;

/**
 * This class is a simple container for progress bar data.
 *
 * @since 1.0
 * @version 1.0
 * @author Andrey Chuchalov
 */

public final class Progressbar {

    /**
     * Full load value in percent.
     */
    private static final int FULL_LOAD = 100;

    /**
     * Value for current downloaded size (in bytes).
     */
    private final long downloaded;

    /**
     * Value for current downloaded state (in percent).
     */
    private final long percentage;

    /**
     * Value for current download state (see {@link DownloadStatus}).
     */
    private final DownloadStatus status;

    /**
     * Progressbar is a container for storing current download status.
     *
     * @param size size of the downloading file
     * @param downloadedValue downloaded size in bytes
     * @param downloadStatus current download status
     */
    public Progressbar(final long size, final long downloadedValue,
                       final DownloadStatus downloadStatus) {

        this.downloaded = downloadedValue;
        percentage = size == 0 ? 0 : FULL_LOAD * this.downloaded / size;

        status = downloadStatus;
    }

    /**
     * Returns current download size in percent.
     *
     * @return size in percent
     */
    public byte getPercentage() {
        return (byte) percentage;
    }

    /**
     * Returns current download size in bytes.
     *
     * @return size in bytes
     */
    public long getDownloaded() {
        return downloaded;
    }

    /**
     * Returns current download status.
     *
     * @return status
     */
    public DownloadStatus getStatus() {
        return status;
    }
}
