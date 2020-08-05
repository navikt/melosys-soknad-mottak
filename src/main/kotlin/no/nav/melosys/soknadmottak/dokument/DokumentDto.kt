package no.nav.melosys.soknadmottak.dokument

import java.util.*

data class DokumentDto(
    val soknadID: String,
    val dokumentID: String,
    val tittel: String,
    val dokumentType: DokumentType,
    val innhold: String
) {
    constructor(dokument: Dokument) : this(
        dokument.soknad.soknadID.toString(),
        dokument.dokumentID!!,
        dokument.filnavn,
        dokument.type,
        Base64.getEncoder().encodeToString(dokument.innhold)
    )
}
