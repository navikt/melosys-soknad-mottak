CREATE TABLE DOKUMENT
(
    id          INTEGER PRIMARY KEY,
    dokument_id VARCHAR(26) NOT NULL,
    fk_soknad   INTEGER     NOT NULL,
    filnavn     VARCHAR     NOT NULL,
    dok_type    VARCHAR     NOT NULL,
    innhold     BYTEA       NOT NULL,
    FOREIGN KEY (fk_soknad) REFERENCES SOKNAD_MOTTAK (id)
);

CREATE SEQUENCE dokument_id_seq;
