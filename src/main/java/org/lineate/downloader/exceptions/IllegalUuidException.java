package org.lineate.downloader.exceptions;

/**
 * Runtime exception for illegal download id.
 *
 * @version 1.0
 * @author Andrey Chuchalov
 */

public class IllegalUuidException extends RuntimeException {

    /**
     * Throws runtime exception for bad (absent) download id.
     *
     * @param msg custom message
     */
    public IllegalUuidException(final String msg) {
        super(msg);
    }
}
