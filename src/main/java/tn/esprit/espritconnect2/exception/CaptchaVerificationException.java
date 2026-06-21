package tn.esprit.espritconnect2.exception;

public class CaptchaVerificationException extends RuntimeException {

    private final String code;

    public CaptchaVerificationException(String message) {
        this(message, "CAPTCHA_INVALID");
    }

    public CaptchaVerificationException(String message, String code) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
