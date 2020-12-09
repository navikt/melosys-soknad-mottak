CREATE TABLE DOKUMENT
(
    id               INTEGER PRIMARY KEY,
    dokument_id      VARCHAR(26) NOT NULL,
    fk_soknad        INTEGER     NOT NULL,
    filnavn          VARCHAR     NOT NULL,
    dok_type         VARCHAR     NOT NULL,
    innhold          BYTEA       NULL,
    lagret_tidspunkt TIMESTAMP   NOT NULL,
    FOREIGN KEY (fk_soknad) REFERENCES SOKNAD_MOTTAK (id)
);

CREATE SEQUENCE dokument_id_seq;
CREATE INDEX idx_dokument_dokument_id ON DOKUMENT (dokument_id);
CREATE INDEX idx_dokument_fk_soknad ON DOKUMENT (fk_soknad);
CREATE INDEX idx_dokument_type ON DOKUMENT (dok_type);
