package org.lineate.downloader;

import org.lineate.downloader.progressbar.DownloadStatus;

import java.util.Set;
import java.util.UUID;

/**
 * {@link org.lineate.downloader.Downloader} - file download interface.
 *
 * @version 2.0
 * @author Andrey Chuchalov
 *
 * @param <M> container for {@link Downloader#download(UUID)} method
 * @param <F> container for {@link Downloader#downloadAll()} method
 */

public interface Downloader<M, F> extends AutoCloseable {

    /**
     * Method creates task for download.
     *
     * @param sourceUri source http(s) download url
     * @param destinationFilePath destination local file name
     * @return generated UUID for the download
     */
    UUID create(String sourceUri, String destinationFilePath);

    /**
     * Removes download task.
     *
     * @param id download UUID
     */
    void remove(UUID id);

    /**
     * Returns current download status.
     *
     * @param id download UUID
     * @return {@link DownloadStatus} status
     */
    DownloadStatus getStatus(UUID id);

    /**
     * Returns source link for specified download.
     *
     * @param id download UUID
     * @return source url
     */
    String getSource(UUID id);

    /**
     * Returns destination file name for specified download.
     *
     * @param id download UUID
     * @return destination file name
     */
    String getDestination(UUID id);

    /**
     * Download file for specified task.
     *
     * @param id download UUID
     * @return M type for single file container
     */
    M download(UUID id);

    /**
     * Download files for all tasks.
     *
     * @return F type for all files container
     * @throws InterruptedException throws when async errors occurred on
     * download process at in-thread operations
     */
    F downloadAll() throws InterruptedException;

    /**
     * Returns if download is complete.
     *
     * @param id download UUID
     * @return true if download is complete, otherwise returns false
     */
    boolean downloaded(UUID id);

    /**
     * Returns if download is in progress.
     *
     * @param id download UUID
     * @return true if download is in progress, otherwise returns false
     */
    boolean downloading(UUID id);

    /**
     * Returns if FAILED flag is set for current download.
     *
     * @param id download UUID
     * @return true is flag FAILED is set, otherwise returns false
     */
    boolean failed(UUID id);

    /**
     * Returns current download progress in percent.
     *
     * @param id download UUID
     * @return current 0 to 100 percent
     */
    byte getProgress(UUID id);

    /**
     * Returns current download progress in bytes.
     *
     * @param id download UUID
     * @return current downloaded size in bytes
     */
    long getProgressBytes(UUID id);

    /**
     * Returns a set of task UUID to download.
     *
     * @return set of UUID
     */
    Set<UUID> getDownloads();
}
