package no.nav.melosys.soknadmottak.dokument

import java.time.Instant
import java.util.*

data class DokumentDto(
    val soknadID: String,
    val dokumentID: String,
    val tittel: String,
    val dokumentType: String,
    val innhold: String,
    val innsendtTidspunkt: Instant
) {
    constructor(dokument: Dokument) : this(
        dokument.soknad.soknadID.toString(),
        dokument.dokumentID!!,
        dokument.filnavn,
        dokument.type,
        Base64.getEncoder().encodeToString(dokument.innhold),
        dokument.soknad.innsendtTidspunkt
    )
}
