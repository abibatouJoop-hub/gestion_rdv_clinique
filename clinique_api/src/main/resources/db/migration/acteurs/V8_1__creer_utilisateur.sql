CREATE TABLE utilisateur (
                             id           BIGSERIAL PRIMARY KEY,
                             email        VARCHAR(150) NOT NULL UNIQUE,
                             mot_de_passe VARCHAR(100) NOT NULL,
                             role         VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN', 'SECRETAIRE', 'MEDECIN')),
                             medecin_id   BIGINT UNIQUE,
                             actif        BOOLEAN      NOT NULL DEFAULT TRUE,
                             cree_le      TIMESTAMP    NOT NULL DEFAULT now(),
                             CONSTRAINT ck_medecin_id_selon_role CHECK ((role = 'MEDECIN') = (medecin_id IS NOT NULL))
);