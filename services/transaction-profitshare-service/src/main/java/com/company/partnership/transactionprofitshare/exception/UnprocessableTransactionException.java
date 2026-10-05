package com.company.partnership.transactionprofitshare.exception;

/** The sale can't be recorded as asked: terms that can't be applied (shares exceed the cost) or an unusable subscription. */
public class UnprocessableTransactionException extends RuntimeException {
    public UnprocessableTransactionException(String message) {
        super(message);
    }
}
