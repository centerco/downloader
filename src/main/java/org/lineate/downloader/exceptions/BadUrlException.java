package org.lineate.downloader.exceptions;

public class BadUrlException extends RuntimeException {
    public BadUrlException(String msg) {
        super(msg);
    }
}
