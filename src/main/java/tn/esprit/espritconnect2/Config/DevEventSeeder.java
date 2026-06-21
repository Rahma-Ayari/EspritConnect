package tn.esprit.espritconnect2.Config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tn.esprit.espritconnect2.Entitie.Evenement;
import tn.esprit.espritconnect2.Entitie.Role;
import tn.esprit.espritconnect2.Entitie.TypeEvenement;
import tn.esprit.espritconnect2.Entitie.User;
import tn.esprit.espritconnect2.Repository.EvenementRepository;
import tn.esprit.espritconnect2.Repository.TypeEvenementRepository;
import tn.esprit.espritconnect2.Repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

@Component
@Order(101)
@RequiredArgsConstructor
public class DevEventSeeder implements ApplicationRunner {

    private final TypeEvenementRepository typeEvenementRepository;
    private final EvenementRepository evenementRepository;
    private final UserRepository userRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (evenementRepository.count() > 0) {
            System.out.println("DevEventSeeder: events already exist, skipping.");
            return;
        }

        User admin = userRepository.findByEmail("admin@esprit.tn")
                .orElseThrow(() -> new IllegalStateException("Admin user not found"));

        TypeEvenement workshop = ensureType("Workshop", "Hands-on technical workshop");
        TypeEvenement conference = ensureType("Conference", "Industry conference and talks");
        TypeEvenement hackathon = ensureType("Hackathon", "Team-based coding competition");

        Evenement evt1 = new Evenement();
        evt1.setTitre("Spring Boot Workshop");
        evt1.setLieu(null);
        evt1.setOnline(true);
        evt1.setDateDebut(LocalDate.now());
        evt1.setDateFin(LocalDate.now().plusDays(1));
        evt1.setHeureDebut(LocalTime.of(9, 0));
        evt1.setHeureFin(LocalTime.of(17, 0));
        evt1.setTypeEvenement(workshop);
        evt1.setType(workshop.getNom());
        evt1.setUnlimitedParticipants(true);
        evt1.setNombreParticipants(0);
        evt1.setOwner(admin);
        evt1.setDateEvenement(new Date());
        evenementRepository.save(evt1);

        Evenement evt2 = new Evenement();
        evt2.setTitre("Career Fair 2026");
        evt2.setLieu("Esprit Campus, Ariana");
        evt2.setOnline(false);
        evt2.setDateDebut(LocalDate.now().plusWeeks(1));
        evt2.setDateFin(LocalDate.now().plusWeeks(1));
        evt2.setHeureDebut(LocalTime.of(10, 0));
        evt2.setHeureFin(LocalTime.of(16, 0));
        evt2.setTypeEvenement(conference);
        evt2.setType(conference.getNom());
        evt2.setUnlimitedParticipants(false);
        evt2.setCapacite(200);
        evt2.setNombreParticipants(0);
        evt2.setOwner(admin);
        evt2.setDateEvenement(new Date());
        evenementRepository.save(evt2);

        Evenement evt3 = new Evenement();
        evt3.setTitre("AI Hackathon");
        evt3.setLieu(null);
        evt3.setOnline(true);
        evt3.setDateDebut(LocalDate.now().plusWeeks(2));
        evt3.setDateFin(LocalDate.now().plusWeeks(3));
        evt3.setHeureDebut(LocalTime.of(8, 30));
        evt3.setHeureFin(LocalTime.of(18, 30));
        evt3.setTypeEvenement(hackathon);
        evt3.setType(hackathon.getNom());
        evt3.setUnlimitedParticipants(true);
        evt3.setNombreParticipants(0);
        evt3.setOwner(admin);
        evt3.setDateEvenement(new Date());
        evenementRepository.save(evt3);

        System.out.println("DevEventSeeder: created " + evenementRepository.count() + " sample events.");
    }

    private TypeEvenement ensureType(String nom, String description) {
        return typeEvenementRepository.findByNomIgnoreCase(nom)
                .orElseGet(() -> {
                    TypeEvenement t = new TypeEvenement();
                    t.setNom(nom);
                    t.setDescription(description);
                    t.setActif(true);
                    return typeEvenementRepository.save(t);
                });
    }
}
