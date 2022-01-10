package no.nav.melosys.soknadmottak

import no.nav.common.KafkaEnvironment
import no.nav.melosys.soknadmottak.kafka.SoknadMottatt
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.annotation.Order
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer

@TestConfiguration
class SoknadMottakTestConfiguration {

    @Bean
    @Order(1)
    fun embeddedKafka(
        @Value("\${melosys.kafka.producer.topic-name}") topic: String,
        @Value("\${melosys.kafka.producer.topic-name-aiven}") aivenTopic: String
    ) = KafkaEnvironment(
        topicNames = listOf(topic, aivenTopic)
    )

    @Bean
    fun producerFactory(embeddedKafka: KafkaEnvironment) = DefaultKafkaProducerFactory<String, SoknadMottatt>(
        mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to embeddedKafka.brokersURL,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java
        )
    )
}
