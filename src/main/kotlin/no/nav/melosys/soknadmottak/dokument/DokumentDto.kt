package no.nav.melosys.soknadmottak.dokument

data class DokumentDto(
    val soknadID: String,
    val dokumentID: String,
    val tittel: String,
    val dokumentType: DokumentType
) {
    constructor(dokument: Dokument) : this(dokument.soknad.soknadID.toString(),
        dokument.dokumentID!!, dokument.filnavn, dokument.type)
}