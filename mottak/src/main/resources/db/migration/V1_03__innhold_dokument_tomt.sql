ALTER TABLE dokument ALTER COLUMN innhold DROP NOT NULL;

CREATE UNIQUE INDEX idx_soknad_mottak_arkiv_ref_unik ON soknad_mottak(arkiv_ref);
