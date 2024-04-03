package no.nav.melosys.soknadmottak.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "altinn")
data class AltinnConfig(
    val username: String,
    val password: String,
    val behandleMelding: BehandleMelding,
    val informasjon: Informasjon,
    val downloadQueue: DownloadQueue,
    val correspondence: Correspondence
) {
    data class BehandleMelding(
        var url: String
    )

    data class Informasjon(
        var url: String
    )

    data class DownloadQueue(
        var code: String,
        var editionCode: String
    )

    data class Correspondence(
        var code: String,
        var editionCode: String
    )
}
