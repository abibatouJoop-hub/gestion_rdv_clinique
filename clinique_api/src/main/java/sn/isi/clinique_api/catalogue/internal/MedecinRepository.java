package sn.isi.clinique_api.catalogue.internal;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MedecinRepository extends JpaRepository<Medecin, Long> {

    @Query("""
            select m from Medecin m
            where (lower(m.nom) like lower(concat('%', :nom, '%'))
                   or lower(m.prenom) like lower(concat('%', :nom, '%')))
              and (:specialiteId is null or m.specialite.id = :specialiteId)
              and (:filtrerCreneaux = false or exists (
                    select 1 from Creneau c
                    where c.medecin = m
                      and c.debut >= :debutMin and c.debut < :debutMax
                      and (:seulementLibres = false or c.libre = true)))
            """)
    Page<Medecin> rechercher(@Param("nom") String nom,
                             @Param("specialiteId") Long specialiteId,
                             @Param("filtrerCreneaux") boolean filtrerCreneaux,
                             @Param("seulementLibres") boolean seulementLibres,
                             @Param("debutMin") LocalDateTime debutMin,
                             @Param("debutMax") LocalDateTime debutMax,
                             Pageable pageable);
}