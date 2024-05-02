package no.nav.melosys.soknadmottak.mottak.altinn

import no.altinn.services.archive.downloadqueue._2012._08.IDownloadQueueExternalBasic
import no.nav.melosys.soknadmottak.config.AltinnConfig
import no.nav.melosys.soknadmottak.ws.StsProperties
import no.nav.melosys.soknadmottak.ws.createServicePort
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(StsProperties::class, AltinnConfig::class)
class DownloadQueueConfig(
    private val altinnConfig: AltinnConfig,
    private val stsProperties: StsProperties
) {
    @Bean
    fun iDownloadQueueExternalBasic(): IDownloadQueueExternalBasic =
        createServicePort(
            serviceUrl = altinnConfig.informasjon.url,
            serviceClazz = IDownloadQueueExternalBasic::class.java,
            stsProperties
        )
}
