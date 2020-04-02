CREATE TABLE SOKNAD_MOTTAK
(
    id          INTEGER PRIMARY KEY,
    archive_ref VARCHAR(500) NOT NULL,
    delivered   BOOLEAN      NOT NULL,
    content     TEXT         NOT NULL,
    soknad_id   UUID         NOT NULL
);

CREATE SEQUENCE soknad_mottak_id_seq;
CREATE INDEX idx_soknad_mottak_soknad_id ON soknad_mottak (soknad_id);
