package org.lineate.downloader.exceptions;

/**
 * Runtime exception for malformed url format.
 *
 * @version 1.0
 * @author Andrey Chuchalov
 */

public class BadUrlException extends RuntimeException {

    /**
     * Throws runtime exception for bad url.
     *
     * @param msg custom message
     */
    public BadUrlException(final String msg) {
        super(msg);
    }
}
