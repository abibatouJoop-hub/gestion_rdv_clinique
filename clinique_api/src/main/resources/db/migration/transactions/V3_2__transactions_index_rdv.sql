CREATE INDEX idx_rdv_patient_statut ON rendez_vous (patient_id, statut);

CREATE UNIQUE INDEX uq_rdv_creneau_confirme
    ON rendez_vous (creneau_id)
    WHERE statut = 'CONFIRME';