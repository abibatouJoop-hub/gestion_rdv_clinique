CREATE TABLE specialite (
                            id  BIGSERIAL PRIMARY KEY,
                            nom VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE medecin (
                         id            BIGSERIAL PRIMARY KEY,
                         nom           VARCHAR(100) NOT NULL,
                         prenom        VARCHAR(100) NOT NULL,
                         specialite_id BIGINT NOT NULL REFERENCES specialite (id)
);

CREATE TABLE creneau (
                         id         BIGSERIAL PRIMARY KEY,
                         medecin_id BIGINT    NOT NULL REFERENCES medecin (id),
                         debut      TIMESTAMP NOT NULL,
                         fin        TIMESTAMP NOT NULL,
                         libre      BOOLEAN   NOT NULL DEFAULT TRUE,
                         version    BIGINT    NOT NULL DEFAULT 0,
                         CONSTRAINT ck_creneau_horaire CHECK (fin > debut)
);