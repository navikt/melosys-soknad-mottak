package no.nav.melosys.soknadmottak.soknad

import mu.KotlinLogging
import no.nav.melosys.soknadmottak.common.IkkeFunnetException
import no.nav.melosys.soknadmottak.soknad.dokgen.DokgenService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

private val logger = KotlinLogging.logger { }

@Service
class SoknadService @Autowired constructor(
    private val soknadRepository: SoknadRepository,
    private val dokgenService: DokgenService
) {
    fun erSøknadArkivIkkeLagret(arkivRef: String): Boolean {
        return soknadRepository.findByArkivReferanse(arkivRef).count() == 0
    }

    fun hentSøknad(soknadID: String): Soknad {
        logger.debug { "Henter søknad med ID $soknadID" }
        return soknadRepository.findBySoknadID(UUID.fromString(soknadID))
            ?: throw IkkeFunnetException("Finner ikke søknad med ID $soknadID")
    }

    fun lagPdf(søknad: Soknad): ByteArray {
        return dokgenService.lagSøknadPDF(SoknadSkjemaOversetter.tilSøknadsdata(søknad))
    }

    fun lagre(soknad: Soknad): Soknad {
        return soknadRepository.save(soknad)
    }

    fun oppdaterLeveringsstatus(soknadID: String) {
        soknadRepository.save(
            hentSøknad(soknadID).apply {
                levert = true
            }
        )
    }
}
