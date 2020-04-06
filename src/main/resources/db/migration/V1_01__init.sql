CREATE TABLE SOKNAD_MOTTAK
(
    id               INTEGER PRIMARY KEY,
    soknad_id        UUID         NOT NULL,
    arkiv_ref        VARCHAR(100) NOT NULL,
    levert           BOOLEAN      NOT NULL,
    innhold          TEXT         NOT NULL,
    lagret_tidspunkt TIMESTAMP    NOT NULL
);

CREATE SEQUENCE soknad_mottak_id_seq;
CREATE INDEX idx_soknad_mottak_arkiv_ref ON SOKNAD_MOTTAK (arkiv_ref);
CREATE INDEX idx_soknad_mottak_soknad_id ON SOKNAD_MOTTAK (soknad_id);
