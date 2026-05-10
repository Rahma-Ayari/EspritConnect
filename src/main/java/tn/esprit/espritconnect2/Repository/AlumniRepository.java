package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.espritconnect2.Entitie.Alumni;

public interface AlumniRepository extends JpaRepository<Alumni, Long> {
    boolean existsByEmail(String email);
}