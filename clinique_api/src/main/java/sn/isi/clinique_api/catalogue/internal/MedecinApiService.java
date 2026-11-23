package sn.isi.clinique_api.catalogue.internal;

import org.springframework.stereotype.Service;
import sn.isi.clinique_api.catalogue.api.MedecinApi;

@Service
class MedecinApiService implements MedecinApi {

    private final MedecinRepository repository;

    MedecinApiService(MedecinRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existe(Long medecinId) {
        return repository.existsById(medecinId);
    }
}