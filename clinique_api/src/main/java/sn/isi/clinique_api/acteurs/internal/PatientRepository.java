package sn.isi.clinique_api.acteurs.internal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    @Query("""
            select p from Patient p
            where lower(p.nom) like lower(concat('%', :critere, '%'))
               or lower(p.prenom) like lower(concat('%', :critere, '%'))
               or lower(p.email) like lower(concat('%', :critere, '%'))
            """)
    Page<Patient> rechercher(@Param("critere") String critere, Pageable pageable);
}