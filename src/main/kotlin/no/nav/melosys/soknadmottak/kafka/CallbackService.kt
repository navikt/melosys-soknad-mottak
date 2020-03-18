package no.nav.melosys.soknadmottak.kafka

import mu.KotlinLogging
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger { }

@Service
class CallbackService {
    fun kvitter(result: SendResult<String, MottattSoknadMelding>?) =
        logger.info { "Melding ble sendt på topic: ${result?.producerRecord?.value()}" }
}
