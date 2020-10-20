package no.nav.melosys.soknadmottak.soknad

import mu.KotlinLogging
import no.nav.melosys.soknadmottak.common.IkkeFunnetException
import no.nav.melosys.soknadmottak.soknad.dokgen.DokgenConsumer
import no.nav.melosys.soknadmottak.soknad.dokgen.DokgenService
import no.nav.melosys.soknadmottak.soknad.dokgen.SoknadFelterBuilder
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

    fun hentPdf(søknad: Soknad): ByteArray {
        // TODO mapping fra søknad.innhold til soknadFelter
        val soknadFelter = SoknadFelterBuilder().build()
        return dokgenService.lagSøknadPDF(soknadFelter)
    }

    fun hentSøknad(soknadID: String): Soknad {
        logger.debug{ "Henter søknad med ID $soknadID" }
        return soknadRepository.findBySoknadID(UUID.fromString(soknadID))
            ?: throw IkkeFunnetException("Finner ikke søknad med ID $soknadID")
    }

    fun lagre(soknad: Soknad): Soknad {
        return soknadRepository.save(soknad)
    }

    fun oppdaterLeveringsstatus(soknadID: String) {
        soknadRepository.save(hentSøknad(soknadID).apply {
            levert = true
        })
    }
}