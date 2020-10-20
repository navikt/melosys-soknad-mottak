package no.nav.melosys.soknadmottak.soknad.dokgen

import no.nav.melosys.soknadmottak.soknad.dokgen.modell.SoknadFelter
import org.springframework.stereotype.Service

@Service
class DokgenService(private val dokgenConsumer: DokgenConsumer) {
    fun lagSøknadPDF(soknadFelter: SoknadFelter): ByteArray {
        return dokgenConsumer.lagPDF("soeknad", soknadFelter)
    }
}