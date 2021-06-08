package org.lineate.downloader.httpdownloader;

import java.util.Objects;

public final class DownloadData {
    private final String sourceUri;
    private final String localFile;

    public DownloadData(final String sourceUri, final String destinationFilePath) {
        this.sourceUri = sourceUri;
        this.localFile = destinationFilePath;
    }

    public String getSourceUri() {
        return sourceUri;
    }

    public String getLocalFile() {
        return localFile;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DownloadData downloadData = (DownloadData) o;
        return Objects.equals(sourceUri, downloadData.sourceUri) && Objects.equals(localFile, downloadData.localFile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceUri, localFile);
    }

}
