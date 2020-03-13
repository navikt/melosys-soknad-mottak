package no.nav.melosys.soknadmottak.kafka

import mu.KotlinLogging
import no.nav.melosys.soknadmottak.Soknad
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger { }

@Service
class CallbackService {
    fun kvitter(result: SendResult<String, Soknad>?) =
        logger.info { "Melding ble sendt på topic: ${result?.producerRecord?.value()}" }
}
