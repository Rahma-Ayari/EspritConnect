package tn.esprit.espritconnect2.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.espritconnect2.Entitie.User;
import tn.esprit.espritconnect2.Repository.EtudiantRepository;
import tn.esprit.espritconnect2.Repository.UserRepository;

/**
 * Valide email/mot de passe comme DaoAuthenticationProvider, mais :
 * - accepte un ancien mot de passe stocké en clair une fois, puis le ré-encode en BCrypt ;
 * - la création de {@link User} à partir de {@link tn.esprit.espritconnect2.Entitie.Etudiant}
 * est faite dans {@link UserDetailsServiceImpl}.
 */
@Component
@RequiredArgsConstructor
public class LegacyCompatibleAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final EtudiantRepository etudiantRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        UsernamePasswordAuthenticationToken authReq = (UsernamePasswordAuthenticationToken) authentication;
        String email = authReq.getName();
        String rawPassword = String.valueOf(authReq.getCredentials());

        UserDetails loaded;
        try {
            loaded = userDetailsService.loadUserByUsername(email);
        } catch (UsernameNotFoundException e) {
            throw new BadCredentialsException("Email ou mot de passe incorrect.");
        }

        if (!(loaded instanceof User user)) {
            throw new BadCredentialsException("Email ou mot de passe incorrect.");
        }

        String stored = user.getPassword();
        boolean bcryptOk = stored != null && passwordEncoder.matches(rawPassword, stored);
        boolean legacyPlain = stored != null && stored.equals(rawPassword);

        if (!bcryptOk && !legacyPlain) {
            throw new BadCredentialsException("Email ou mot de passe incorrect.");
        }

        if (!bcryptOk && legacyPlain) {
            String encoded = passwordEncoder.encode(rawPassword);
            user.setPassword(encoded);
            userRepository.save(user);
            etudiantRepository.findByEmail(email).ifPresent(e -> {
                e.setPassword(encoded);
                etudiantRepository.save(e);
            });
        }

        return UsernamePasswordAuthenticationToken.authenticated(
                user, null, user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
