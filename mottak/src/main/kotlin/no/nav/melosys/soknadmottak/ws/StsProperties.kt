package no.nav.melosys.soknadmottak.ws

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "sts")
data class StsProperties(
    val url: String,
    val username: String,
    val password: String
)
