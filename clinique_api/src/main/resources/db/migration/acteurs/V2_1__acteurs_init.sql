CREATE TABLE patient (
                         id        BIGSERIAL PRIMARY KEY,
                         nom       VARCHAR(100) NOT NULL,
                         prenom    VARCHAR(100) NOT NULL,
                         email     VARCHAR(150) NOT NULL UNIQUE,
                         telephone VARCHAR(30)
);