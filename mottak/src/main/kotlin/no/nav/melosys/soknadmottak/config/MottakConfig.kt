package no.nav.melosys.soknadmottak.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "mottak")
@ConstructorBinding
data class MottakConfig(
    val fjernFraDq: Boolean = true
)
