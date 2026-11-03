package sn.isi.clinique_api.notifications.internal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("""
            select n from Notification n
            where (:patientId is null or n.patientId = :patientId)
              and (:seulementNonLues = false or n.lue = false)
            """)
    Page<Notification> rechercher(@Param("patientId") Long patientId,
                                  @Param("seulementNonLues") boolean seulementNonLues,
                                  Pageable pageable);
}