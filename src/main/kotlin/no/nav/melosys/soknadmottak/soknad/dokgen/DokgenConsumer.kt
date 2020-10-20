package no.nav.melosys.soknadmottak.soknad.dokgen

import no.nav.melosys.soknadmottak.soknad.dokgen.modell.SoknadFelter

interface DokgenConsumer {
    fun lagPDF(malNavn: String, soknadFelter: SoknadFelter): ByteArray
}