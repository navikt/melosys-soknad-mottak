package no.nav.melosys.soknadmottak.soknad.dokgen

import no.nav.melosys.soknadmottak.soknad.dokgen.modell.Soknadsdata

interface DokgenConsumer {
    fun lagPDF(malNavn: String, soknadsdata: Soknadsdata): ByteArray
}