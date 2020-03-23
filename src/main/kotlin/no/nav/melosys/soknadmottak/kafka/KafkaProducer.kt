package no.nav.melosys.soknadmottak.kafka

import mu.KotlinLogging
import no.nav.melosys.soknadmottak.common.PubliserSoknadException
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Service
import org.springframework.util.concurrent.ListenableFutureCallback

private val logger = KotlinLogging.logger { }

@Service
class KafkaProducer(
    private val kafkaTemplate: KafkaTemplate<String, SoknadMottatt>,
    private val callbackService: CallbackService,
    @Value("\${melosys.kafka.producer.topic-name}") private val topicName: String
) {
    fun publiserMelding(
        soknadMottatt: SoknadMottatt,
        vedFeil: (throwable: Throwable) -> Unit = { throw PubliserSoknadException("Kunne ikke publisere melding", it) }
    ) {
        val future = kafkaTemplate.send(topicName, soknadMottatt)

        future.addCallback(object : ListenableFutureCallback<SendResult<String, SoknadMottatt>?> {
            override fun onSuccess(result: SendResult<String, SoknadMottatt>?) {
                callbackService.kvitter(result)
            }

            override fun onFailure(throwable: Throwable) = vedFeil(throwable)
        })
    }
}


