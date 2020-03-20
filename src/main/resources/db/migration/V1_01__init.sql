CREATE TABLE SOKNAD_MOTTAK(
    id SERIAL PRIMARY KEY,
    archive_ref VARCHAR(500) NOT NULL,
    delivered BOOLEAN NOT NULL,
    content TEXT NOT NULL,
    soknad_id VARCHAR(36) NOT NULL
);

CREATE INDEX idx_soknad_mottak_soknad_id ON soknad_mottak (soknad_id);
