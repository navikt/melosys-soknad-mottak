package no.nav.melosys.soknadmottak.kafka

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Service
import org.springframework.util.concurrent.ListenableFutureCallback

private val logger = KotlinLogging.logger { }

@Service
class KafkaProducer(
    private val kafkaTemplate: KafkaTemplate<String, Soknad>,
    private val callbackService: CallbackService,
    @Value("\${melosys.kafka.producer.topic-name}") private val topicName: String
) {
    fun publiserMelding(
        soknad: Soknad,
        vedFeil: (throwable: Throwable) -> Unit = { throw PubliserSoknadException("Kunne ikke publisere melding", it) }
    ) {
        val future = kafkaTemplate.send(topicName, soknad)

        future.addCallback(object : ListenableFutureCallback<SendResult<String, Soknad>?> {
            override fun onSuccess(result: SendResult<String, Soknad>?) {
                callbackService.kvitter(result)
            }

            override fun onFailure(throwable: Throwable) = vedFeil(throwable)
        })
    }
}

// todo: Denne tjenesten byttes ut med den som skal kvittere mot altinn
@Service
class CallbackService {
    fun kvitter(result: SendResult<String, Soknad>?) =
        logger.info { "Melding ble sendt på topic: ${result?.producerRecord?.value()}" }
}

