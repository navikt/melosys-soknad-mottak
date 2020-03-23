package no.nav.melosys.soknadmottak.kafka

import mu.KotlinLogging
import no.nav.melosys.soknadmottak.soknad.AltinnSoknadService
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger { }

@Service
class CallbackService(private val altinnSoknadService: AltinnSoknadService) {
    fun kvitter(result: SendResult<String, SoknadMottatt>?) {
        val soknadMottatt = result?.producerRecord?.value()
        logger.info { "Melding ble sendt på topic: $soknadMottatt" }
        altinnSoknadService.updateDeliveryStatus(soknadMottatt!!.soknadID)
    }
}
