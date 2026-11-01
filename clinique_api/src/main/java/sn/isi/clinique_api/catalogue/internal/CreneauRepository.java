package sn.isi.clinique_api.catalogue.internal;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CreneauRepository extends JpaRepository<Creneau, Long> {

    @Query("""
            select c from Creneau c
            where (:medecinId is null or c.medecin.id = :medecinId)
              and c.debut >= :debutMin and c.debut < :debutMax
              and (:filtrerLibre = false or c.libre = :valeurLibre)
            """)
    Page<Creneau> rechercher(@Param("medecinId") Long medecinId,
                             @Param("debutMin") LocalDateTime debutMin,
                             @Param("debutMax") LocalDateTime debutMax,
                             @Param("filtrerLibre") boolean filtrerLibre,
                             @Param("valeurLibre") boolean valeurLibre,
                             Pageable pageable);

    @Query("""
            select count(c) > 0 from Creneau c
            where c.medecin.id = :medecinId
              and c.debut < :fin and c.fin > :debut
              and c.id <> :exclureId
            """)
    boolean existeChevauchement(@Param("medecinId") Long medecinId,
                                @Param("debut") LocalDateTime debut,
                                @Param("fin") LocalDateTime fin,
                                @Param("exclureId") Long exclureId);
}