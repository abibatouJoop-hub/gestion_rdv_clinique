package sn.isi.clinique_api.catalogue.api;

public interface CreneauApi {

    /** Retourne le créneau, ou lève une erreur 404 s'il n'existe pas. */
    CreneauDto trouver(Long id);

    /** Marque le créneau comme pris, ou lève une erreur 409 s'il ne l'est plus. */
    void reserver(Long id);

    /** Rend le créneau de nouveau disponible. */
    void liberer(Long id);
}