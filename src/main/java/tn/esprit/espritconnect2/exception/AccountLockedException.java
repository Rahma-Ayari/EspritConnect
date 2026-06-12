package tn.esprit.espritconnect2.exception;

import org.springframework.security.core.AuthenticationException;
import lombok.Getter;

@Getter
public class AccountLockedException extends AuthenticationException {
    private final int remainingAttempts;
    private final long lockoutSeconds;

    public AccountLockedException(String msg, int remainingAttempts, long lockoutSeconds) {
        super(msg);
        this.remainingAttempts = remainingAttempts;
        this.lockoutSeconds = lockoutSeconds;
    }
}
