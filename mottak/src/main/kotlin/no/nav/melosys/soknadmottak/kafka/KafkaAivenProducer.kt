package no.nav.melosys.soknadmottak.kafka

import no.nav.melosys.soknadmottak.common.PubliserSoknadException
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class KafkaAivenProducer(
    private val aivenKafkaTemplate: KafkaTemplate<String, SoknadMottatt>,
    private val callbackService: CallbackService,
    @Value("\${melosys.kafka.producer.topic-name-aiven}") private val topicName: String
) {
    fun publiserMelding(
        soknadMottatt: SoknadMottatt,
        vedFeil: (throwable: Throwable) -> Unit = {
            throw PubliserSoknadException(
                "Kunne ikke publisere melding på Aiven",
                it
            )
        }
    ) {
        aivenKafkaTemplate.send(topicName, soknadMottatt).toCompletableFuture()
            .thenAccept(callbackService::kvitter)
            .exceptionally { throwable ->
                vedFeil(throwable)
                null
            }
    }
}
