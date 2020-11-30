package no.nav.melosys.soknadmottak.ws

import org.apache.cxf.ext.logging.LoggingFeature
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean
import org.apache.cxf.ws.addressing.WSAddressingFeature

fun <PORT_TYPE : Any> createServicePort(
    serviceUrl: String,
    serviceClazz: Class<PORT_TYPE>,
    stsProperties: StsProperties
): PORT_TYPE = JaxWsProxyFactoryBean()
    .apply {
        address = serviceUrl
        serviceClass = serviceClazz
        features = listOf(WSAddressingFeature(), LoggingFeature())
    }
    .create(serviceClazz)
    .apply {
        stsClient(stsProperties).configureFor(this, STS_SAML_POLICY)
    }
