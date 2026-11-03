package sn.isi.clinique_api.transactions.internal;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import sn.isi.clinique_api.transactions.api.StatutRendezVous;

public interface RendezVousRepository extends JpaRepository<RendezVous, Long> {

    long countByPatientIdAndStatutAndDebutAfter(Long patientId, StatutRendezVous statut,
                                                LocalDateTime apres);

    @Query("""
            select count(r) > 0 from RendezVous r
            where r.patientId = :patientId and r.statut = :statut
              and r.debut < :fin and r.fin > :debut
            """)
    boolean existeChevauchement(@Param("patientId") Long patientId,
                                @Param("statut") StatutRendezVous statut,
                                @Param("debut") LocalDateTime debut,
                                @Param("fin") LocalDateTime fin);

    @Query("""
            select r from RendezVous r
            where (:patientId is null or r.patientId = :patientId)
              and (:filtrerStatut = false or r.statut = :statut)
            """)
    Page<RendezVous> rechercher(@Param("patientId") Long patientId,
                                @Param("filtrerStatut") boolean filtrerStatut,
                                @Param("statut") StatutRendezVous statut,
                                Pageable pageable);
}