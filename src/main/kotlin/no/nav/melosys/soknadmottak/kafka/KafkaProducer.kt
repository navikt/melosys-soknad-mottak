package no.nav.melosys.soknadmottak.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.SendResult
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.stereotype.Service
import org.springframework.util.concurrent.ListenableFutureCallback
import javax.annotation.PostConstruct

@Configuration
class KafkaProducerConfig(
        private val producerFactory: ProducerFactory<Any, Any>,
        private val objectMapper: ObjectMapper
) {
    @PostConstruct
    fun producerFactory() {
        val jsonSerializer = JsonSerializer<Any>(objectMapper)
        (producerFactory as DefaultKafkaProducerFactory).setValueSerializer(jsonSerializer)
    }
}

@Service
class KafkaProducer(
        @Autowired
        private val kafkaTemplate: KafkaTemplate<String, Soknad>,
        @Autowired
        private val callbackService: CallbackService,
        @Value("\${melosys.kafka.producer.topic-name}")
        private val topicName: String
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
    fun kvitter(result: SendResult<String, Soknad>?) = result?.producerRecord?.value()?.felt
}

