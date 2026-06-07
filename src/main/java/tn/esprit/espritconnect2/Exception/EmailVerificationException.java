package tn.esprit.espritconnect2.exception;

public class EmailVerificationException extends RuntimeException {

    private final String errorCode;

    public EmailVerificationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
