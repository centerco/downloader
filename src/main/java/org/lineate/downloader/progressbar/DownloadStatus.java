package org.lineate.downloader.progressbar;

/**
 * This class represent a set of statuses for download task.
 *
 * @since 1.0
 * @version 1.1
 * @author Andrey Chuchalov
 */
public enum DownloadStatus {
    /**
     * value for pending downloads.
     */
    NOT_STARTED,

    /**
     * value for in-progress downloads.
     */
    DOWNLOADING,

    /**
     * value for completed downloads.
     */
    FINISHED,

    /**
     * value for errors.
     */
    FAILED
}
