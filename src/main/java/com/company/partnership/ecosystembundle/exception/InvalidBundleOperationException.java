package com.company.partnership.ecosystembundle.exception;

/** Thrown when an operation would mutate a PUBLISHED bundle's locked offering composition
 *  (BRD FR-BUN-03) instead of going through the new-version + re-consent flow (FR-BUN-04). */
public class InvalidBundleOperationException extends RuntimeException {
    public InvalidBundleOperationException(String message) {
        super(message);
    }
}
