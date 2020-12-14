package no.nav.melosys.soknadmottak.kafka

import mu.KotlinLogging
import no.nav.melosys.soknadmottak.common.Metrikker
import no.nav.melosys.soknadmottak.soknad.SoknadService
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger { }

@Service
class CallbackService(
    private val soknadService: SoknadService,
) {
    fun kvitter(result: SendResult<String, SoknadMottatt>?) {
        val soknadMottatt = result?.producerRecord?.value()
        Metrikker.meldingSendt.increment()
        logger.info { "Melding ble sendt på topic: $soknadMottatt" }
        soknadService.oppdaterLeveringsstatus(soknadMottatt!!.soknadID)
    }
}
