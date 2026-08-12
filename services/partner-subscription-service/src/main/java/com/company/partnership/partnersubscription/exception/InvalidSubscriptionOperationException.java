package com.company.partnership.partnersubscription.exception;

/** E.g. attempting a partial/offering-level subscription, or acting on a
 *  subscription that is not in PENDING_RECONSENT when re-consent is submitted. */
public class InvalidSubscriptionOperationException extends RuntimeException {
    public InvalidSubscriptionOperationException(String message) { super(message); }
}
