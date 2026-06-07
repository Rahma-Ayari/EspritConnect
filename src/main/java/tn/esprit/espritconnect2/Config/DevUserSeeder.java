package tn.esprit.espritconnect2.Config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import tn.esprit.espritconnect2.Entitie.Role;
import tn.esprit.espritconnect2.Entitie.User;
import tn.esprit.espritconnect2.Entitie.Etudiant;
import tn.esprit.espritconnect2.Repository.UserRepository;
import tn.esprit.espritconnect2.Repository.EtudiantRepository;

import java.util.Date;
import java.util.Optional;

/**
 * Ensures dev accounts used by the Angular app (environment.devAuth) exist at startup.
 */
@Component
@RequiredArgsConstructor
public class DevUserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EtudiantRepository etudiantRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        System.out.println("========== DEV USER SEEDER STARTING ==========");
        long initialCount = userRepository.count();
        System.out.println("Current user count in DB: " + initialCount);
        
        ensureUser("admin@esprit.tn", "admin123", "Admin User", Role.ADMIN);
        ensureUser("student@esprit.tn", "student123", "Student User", Role.ETUDIANT);
        
        long finalCount = userRepository.count();
        System.out.println("User count after seeding: " + finalCount);
        System.out.println("All users in DB:");
        userRepository.findAll().forEach(u -> 
            System.out.println(" - " + u.getEmail() + " (" + u.getRole() + "), Enabled: " + u.isEnabled())
        );
        System.out.println("========== DEV USER SEEDER COMPLETE ==========");
    }

    private void ensureUser(String email, String rawPassword, String nom, Role role) {
        String encodedPassword = passwordEncoder.encode(rawPassword);
        Optional<User> existingUserOpt = userRepository.findByEmail(email);
        
        if (existingUserOpt.isPresent()) {
            User user = existingUserOpt.get();
            user.setPassword(encodedPassword);
            user.setRole(role);
            user.setEnabled(true);
            userRepository.save(user);
            System.out.println("Updated password and role for existing user: " + email);
        } else {
            User user = User.builder()
                    .nom(nom)
                    .email(email)
                    .password(encodedPassword)
                    .role(role)
                    .enabled(true)
                    .build();
            userRepository.save(user);
            System.out.println("Created user: " + email);
        }

        if (role == Role.ETUDIANT) {
            Optional<Etudiant> existingEtudiantOpt = etudiantRepository.findByEmail(email);
            if (existingEtudiantOpt.isPresent()) {
                Etudiant etudiant = existingEtudiantOpt.get();
                etudiant.setPassword(encodedPassword);
                etudiant.setNom(nom);
                etudiantRepository.save(etudiant);
                System.out.println("Updated existing Etudiant: " + email);
            } else {
                Etudiant etudiant = new Etudiant();
                etudiant.setNom(nom);
                etudiant.setEmail(email);
                etudiant.setPassword(encodedPassword);
                etudiant.setScoreReadiness(0);
                etudiant.setDateInscription(new Date());
                etudiantRepository.save(etudiant);
                System.out.println("Created Etudiant: " + email);
            }
        }
    }
}
