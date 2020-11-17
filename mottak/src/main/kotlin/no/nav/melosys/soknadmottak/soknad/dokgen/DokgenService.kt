package no.nav.melosys.soknadmottak.soknad.dokgen

import no.nav.melosys.soknadmottak.soknad.dokgen.modell.SoknadFlettedata
import org.springframework.stereotype.Service

@Service
class DokgenService(private val dokgenConsumer: DokgenConsumer) {
    fun lagSøknadPDF(soknadFlettedata: SoknadFlettedata): ByteArray {
        return dokgenConsumer.lagPDF("soeknad", soknadFlettedata)
    }
}