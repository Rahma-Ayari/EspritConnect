package tn.esprit.espritconnect2.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.espritconnect2.Entitie.Etudiant;
import tn.esprit.espritconnect2.Entitie.Role;
import tn.esprit.espritconnect2.Entitie.User;
import tn.esprit.espritconnect2.Repository.EtudiantRepository;
import tn.esprit.espritconnect2.Repository.UserRepository;
import tn.esprit.espritconnect2.Service.ApprovalSettingsService;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final EtudiantRepository etudiantRepository;
    private final ApprovalSettingsService approvalSettingsService;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Utilisateur introuvable avec l'email: " + email));
    }
}
