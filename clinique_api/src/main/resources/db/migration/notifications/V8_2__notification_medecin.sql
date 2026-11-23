ALTER TABLE notification ALTER COLUMN patient_id DROP NOT NULL;
ALTER TABLE notification ADD COLUMN medecin_id BIGINT;

-- exactement un destinataire : un patient OU un médecin
ALTER TABLE notification
    ADD CONSTRAINT ck_notification_un_destinataire
        CHECK ((patient_id IS NULL) <> (medecin_id IS NULL));

CREATE INDEX idx_notification_medecin ON notification (medecin_id) WHERE medecin_id IS NOT NULL;