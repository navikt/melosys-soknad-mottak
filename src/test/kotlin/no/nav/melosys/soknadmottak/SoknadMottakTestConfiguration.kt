package no.nav.melosys.soknadmottak

import no.nav.common.KafkaEnvironment
import no.nav.melosys.soknadmottak.polling.altinn.AltinnClientsConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.annotation.Order
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource

@TestConfiguration
class SoknadMottakTestConfiguration {

    @Bean
    @Order(1)
    fun embeddedKafka(@Value("\${melosys.kafka.producer.topic-name}") topic: String) = KafkaEnvironment(
        topicNames = listOf(topic)
    )

    @Bean
    fun producerFactory(embeddedKafka: KafkaEnvironment) = DefaultKafkaProducerFactory<Any, Any>(
        mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to embeddedKafka.brokersURL,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java
        )
    )
}