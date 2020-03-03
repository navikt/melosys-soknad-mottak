package no.nav.melosys.soknadmottak.polling.altinn.client

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "altinn")
data class AltinnProperties(
    val informasjon: Informasjon,
    val username: String,
    val password: String,
    val service: Service
) {
    data class Informasjon(
        var url: String
    )
    data class Service(
        var code: String
    )
}