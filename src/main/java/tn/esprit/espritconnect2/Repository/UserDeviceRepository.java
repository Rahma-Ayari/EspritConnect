package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.espritconnect2.Entitie.User;
import tn.esprit.espritconnect2.Entitie.UserDevice;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {
    Optional<UserDevice> findByDeviceTokenAndUser(String deviceToken, User user);
    void deleteByExpiresAtBefore(LocalDateTime now);
    void deleteByUser(User user);
}
