package no.nav.melosys.soknadmottak.soknad.dokgen

import no.nav.melosys.soknadmottak.soknad.dokgen.modell.Soknadsdata
import org.springframework.stereotype.Service

@Service
class DokgenService(private val dokgenConsumer: DokgenConsumer) {
    fun lagSøknadPDF(soknadsdata: Soknadsdata): ByteArray {
        return dokgenConsumer.lagPDF("soeknad", soknadsdata)
    }
}