package com.nexushire.exception;

/** Checked exception that wraps any SQL problem so callers never deal with raw SQLException. */
public class DatabaseException extends Exception {
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
    public DatabaseException(String message) {
        super(message);
    }
}
