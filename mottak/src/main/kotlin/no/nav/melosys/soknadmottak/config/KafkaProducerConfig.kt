package no.nav.melosys.soknadmottak.config

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.melosys.soknadmottak.kafka.SoknadMottatt
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer
import java.util.*

@Configuration
class KafkaProducerConfig(
    private val env: Environment,
    @Value("\${melosys.kafka.aiven.brokers}")
    private val brokersUrl: String,
    @Value("\${melosys.kafka.aiven.keystorePath}")
    private val keystorePath: String,
    @Value("\${melosys.kafka.aiven.truststorePath}")
    private val truststorePath: String,
    @Value("\${melosys.kafka.aiven.credstorePassword}")
    private val credstorePassword: String
) {

    @Bean
    fun aivenKafkaTemplate(objectMapper: ObjectMapper?): KafkaTemplate<String?, SoknadMottatt?>? {
        val producerFactory: ProducerFactory<String, SoknadMottatt> =
            DefaultKafkaProducerFactory(commonProps(), StringSerializer(), JsonSerializer(objectMapper))
        return KafkaTemplate(producerFactory)
    }

    private fun commonProps(): Map<String, Any> {
        val props: MutableMap<String, Any> = HashMap()
        props[CommonClientConfigs.CLIENT_ID_CONFIG] = "aiven-producer"
        props[ProducerConfig.ACKS_CONFIG] = "all"
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = brokersUrl
        if (!isLocal()) {
            props[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = "SSL"
            props[SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG] = truststorePath
            props[SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG] = credstorePassword
            props[SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG] = "JKS"
            props[SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG] = keystorePath
            props[SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG] = credstorePassword
            props[SslConfigs.SSL_KEY_PASSWORD_CONFIG] = credstorePassword
            props[SslConfigs.SSL_KEYSTORE_TYPE_CONFIG] = "PKCS12"
        }
        return props
    }

    private fun isLocal(): Boolean {
        return Arrays.stream(env.activeProfiles).anyMatch { profile: String ->
            profile.equals("local", ignoreCase = true) || profile.equals("test", ignoreCase = true)
        }
    }
}
