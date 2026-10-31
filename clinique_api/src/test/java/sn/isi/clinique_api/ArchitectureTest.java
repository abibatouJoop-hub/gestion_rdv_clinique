package sn.isi.clinique_api;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ArchitectureTest {

    private final ApplicationModules modules =
            ApplicationModules.of(CliniqueApiApplication.class);

    @Test
    void verifieLesReglesDeModularite() {
        modules.verify();
    }

    @Test
    void afficheLesModules() {
        modules.forEach(System.out::println);
    }
}