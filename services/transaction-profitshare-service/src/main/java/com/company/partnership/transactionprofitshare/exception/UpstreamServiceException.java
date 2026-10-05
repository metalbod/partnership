package com.company.partnership.transactionprofitshare.exception;

/** A service this one depends on for commercial terms was unreachable or misbehaved. */
public class UpstreamServiceException extends RuntimeException {
    public UpstreamServiceException(String message) {
        super(message);
    }
}
