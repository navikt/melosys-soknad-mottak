package no.nav.melosys.soknadmottak.polling.altinn

import no.altinn.services.archive.downloadqueue._2012._08.IDownloadQueueExternalBasic
import no.nav.melosys.soknadmottak.ws.*
import org.apache.cxf.ext.logging.LoggingFeature
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean
import org.apache.cxf.ws.addressing.WSAddressingFeature
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

@Configuration
class AltinnClientsConfig(
    private val altinnProps: AltinnProperties,
    stsProps: StsProperties
) {
    private val stsClient = stsClient(
        stsUrl = stsProps.url,
        credentials = stsProps.username to stsProps.password
    )

    @Bean
    fun iDownloadQueueExternalBasic(): IDownloadQueueExternalBasic =
        createServicePort(
            serviceUrl = altinnProps.informasjon.url,
            serviceClazz = IDownloadQueueExternalBasic::class.java
        )

    private fun <PORT_TYPE : Any> createServicePort(
        serviceUrl: String,
        serviceClazz: Class<PORT_TYPE>
    ): PORT_TYPE = JaxWsProxyFactoryBean()
        .apply {
            address = serviceUrl
            serviceClass = serviceClazz
            features = listOf(WSAddressingFeature(), LoggingFeature())
        }
        .create(serviceClazz)
        .apply {
            stsClient.configureFor(this, STS_SAML_POLICY)
        }
}
