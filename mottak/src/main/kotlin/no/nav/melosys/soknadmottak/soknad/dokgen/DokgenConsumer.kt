package no.nav.melosys.soknadmottak.soknad.dokgen

import no.nav.melosys.soknadmottak.soknad.dokgen.modell.SoknadFlettedata

interface DokgenConsumer {
    fun lagPDF(malNavn: String, soknadFlettedata: SoknadFlettedata): ByteArray
}