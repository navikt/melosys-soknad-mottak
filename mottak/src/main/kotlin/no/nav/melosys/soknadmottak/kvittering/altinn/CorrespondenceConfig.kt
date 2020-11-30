package no.nav.melosys.soknadmottak.kvittering.altinn

import no.altinn.services.serviceengine.correspondence._2009._10.ICorrespondenceAgencyExternalBasic
import no.nav.melosys.soknadmottak.config.AltinnConfig
import no.nav.melosys.soknadmottak.ws.StsProperties
import no.nav.melosys.soknadmottak.ws.createServicePort
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CorrespondenceConfig(
    private val altinnConfig: AltinnConfig,
    private val stsProps: StsProperties
) {
    @Bean
    fun iCorrespondenceExternalBasic(): ICorrespondenceAgencyExternalBasic =
        createServicePort(
            serviceUrl = altinnConfig.behandleMelding.url,
            serviceClazz = ICorrespondenceAgencyExternalBasic::class.java,
            stsProps
        )
}
