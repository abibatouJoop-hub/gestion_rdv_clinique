package sn.isi.clinique_api.transactions.internal;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ListeAttenteRepository extends JpaRepository<ListeAttente, Long> {

    List<ListeAttente> findByCreneauIdOrderByInscritLeAscIdAsc(Long creneauId);

    boolean existsByPatientIdAndCreneauId(Long patientId, Long creneauId);
}