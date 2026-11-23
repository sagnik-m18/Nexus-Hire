package com.nexushire.exception;

/** Thrown when someone registers with an email that already exists. */
public class DuplicateEmailException extends DatabaseException {
    public DuplicateEmailException(String email) {
        super("An account with email '" + email + "' already exists");
    }
}
