package org.lineate.downloader.httpdownloader;

import java.util.Objects;

/**
 * Class is a simple store download information.
 * Information includes a source url and destination file name.
 *
 * @author Andrey Chuchalov
 * @version 1.0
 */

public final class DownloadData {

    /**
     * Stores source url.
     */
    private final String sourceUri;

    /**
     * Stores destination file name.
     */
    private final String localFile;

    /**
     * Constructor accepts both source url and destination file name.
     *
     * @param sourceUriValue url to download from
     * @param destinationFilePathValue file name to save to
     */
    public DownloadData(final String sourceUriValue,
                        final String destinationFilePathValue) {
        this.sourceUri = sourceUriValue;
        this.localFile = destinationFilePathValue;
    }

    /**
     * Returns download source url.
     *
     * @return string value for source url
     */
    public String getSourceUri() {
        return sourceUri;
    }

    /**
     * Returns download destination file name.
     *
     * @return string value for destination file name
     */
    public String getLocalFile() {
        return localFile;
    }

    /**
     * Overridden method for comparing a couple of objects.
     *
     * @param o element to compared with current object
     * @return true if fields are equivalent
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DownloadData downloadData = (DownloadData) o;
        return Objects.equals(sourceUri, downloadData.sourceUri)
                && Objects.equals(localFile, downloadData.localFile);
    }

    /**
     * Calculates hash for the object.
     *
     * @return int value for hash
     */
    @Override
    public int hashCode() {
        return Objects.hash(sourceUri, localFile);
    }

}
