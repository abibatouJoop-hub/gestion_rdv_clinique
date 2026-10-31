CREATE TABLE rendez_vous (
                             id         BIGSERIAL PRIMARY KEY,
                             patient_id BIGINT      NOT NULL,
                             creneau_id BIGINT      NOT NULL,
                             medecin_id BIGINT      NOT NULL,
                             debut      TIMESTAMP   NOT NULL,
                             statut     VARCHAR(20) NOT NULL,
                             cree_le    TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE TABLE consultation (
                              id               BIGSERIAL PRIMARY KEY,
                              rendez_vous_id   BIGINT    NOT NULL REFERENCES rendez_vous (id),
                              compte_rendu     TEXT,
                              date_consultation TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE liste_attente (
                               id         BIGSERIAL PRIMARY KEY,
                               patient_id BIGINT    NOT NULL,
                               creneau_id BIGINT    NOT NULL,
                               inscrit_le TIMESTAMP NOT NULL DEFAULT NOW(),
                               CONSTRAINT uq_attente UNIQUE (patient_id, creneau_id)
);