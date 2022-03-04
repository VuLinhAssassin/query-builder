package com.vulinh.querybuilder.support;

/**
 * Common runtime exception that can be thrown by this library.
 *
 * @author Nguyen Vu Linh
 */
public class BuilderException extends RuntimeException {

    private static final long serialVersionUID = 5203060814865885797L;

    public BuilderException(String message, Exception ex) {
        super(message, ex);
    }

    public BuilderException(String message) {
        super(message);
    }
}