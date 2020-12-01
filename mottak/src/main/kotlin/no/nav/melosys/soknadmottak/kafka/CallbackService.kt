package no.nav.melosys.soknadmottak.kafka

import mu.KotlinLogging
import no.nav.melosys.soknadmottak.kvittering.KvitteringService
import no.nav.melosys.soknadmottak.soknad.SoknadService
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger { }

@Service
class CallbackService(
    private val soknadService: SoknadService,
    private val kvitteringService: KvitteringService
) {
    fun kvitter(result: SendResult<String, SoknadMottatt>?) {
        val soknadMottatt = result?.producerRecord?.value()
        logger.info { "Melding ble sendt på topic: $soknadMottatt" }
        soknadService.oppdaterLeveringsstatus(soknadMottatt!!.soknadID)

        // FIXME Legg til mottakerID, arkivReferanse og søknadPDF i SoknadMottatt
        val søknad = soknadService.hentSøknad(soknadMottatt.soknadID)
        val søknadPDF = soknadService.lagPdf(søknad)
        kvitteringService.sendKvittering(søknad.hentKvitteringMottakerID(), søknad.arkivReferanse, søknadPDF)
    }
}
