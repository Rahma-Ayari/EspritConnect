package tn.esprit.espritconnect2.exception;

import org.springframework.security.core.AuthenticationException;

public class EmailNotVerifiedException extends AuthenticationException {

    public EmailNotVerifiedException() {
        super("Veuillez vérifier votre email.");
    }
}
