package no.nav.melosys.soknadmottak.mottak.altinn

import no.altinn.services.archive.downloadqueue._2012._08.IDownloadQueueExternalBasic
import no.nav.melosys.soknadmottak.ws.StsProperties
import no.nav.melosys.soknadmottak.ws.createServicePort
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DownloadQueueConfig(
    private val altinnProps: AltinnProperties,
    private val stsProperties: StsProperties
) {
    @Bean
    fun iDownloadQueueExternalBasic(): IDownloadQueueExternalBasic =
        createServicePort(
            serviceUrl = altinnProps.informasjon.url,
            serviceClazz = IDownloadQueueExternalBasic::class.java,
            stsProperties
        )
}
