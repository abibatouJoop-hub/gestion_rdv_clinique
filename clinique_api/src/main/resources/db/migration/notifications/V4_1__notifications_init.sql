CREATE TABLE notification (
                              id         BIGSERIAL PRIMARY KEY,
                              patient_id BIGINT       NOT NULL,
                              type       VARCHAR(40)  NOT NULL,
                              message    TEXT         NOT NULL,
                              lue        BOOLEAN      NOT NULL DEFAULT FALSE,
                              cree_le    TIMESTAMP    NOT NULL DEFAULT NOW()
);