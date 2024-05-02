package no.nav.melosys.soknadmottak.ws

import org.apache.cxf.Bus
import org.apache.cxf.BusFactory
import org.apache.cxf.binding.soap.Soap12
import org.apache.cxf.binding.soap.SoapMessage
import org.apache.cxf.endpoint.Client
import org.apache.cxf.ext.logging.LoggingFeature
import org.apache.cxf.frontend.ClientProxy
import org.apache.cxf.ws.addressing.WSAddressingFeature
import org.apache.cxf.ws.policy.PolicyBuilder
import org.apache.cxf.ws.policy.PolicyEngine
import org.apache.cxf.ws.policy.attachment.reference.ReferenceResolver
import org.apache.cxf.ws.policy.attachment.reference.RemoteReferenceResolver
import org.apache.cxf.ws.security.SecurityConstants
import org.apache.cxf.ws.security.trust.STSClient
import org.apache.neethi.Policy

const val STS_CLIENT_AUTHENTICATION_POLICY = "classpath:sts/policies/untPolicy.xml"
const val STS_SAML_POLICY = "classpath:sts/policies/requestSamlPolicy.xml"
const val STS_SAML_POLICY_NO_TRANSPORT_BINDING = "classpath:sts/policies/requestSamlPolicyNoTransportBinding.xml"

fun stsClient(stsProps: StsProperties): STSClient {
    return stsClient(
        stsUrl = stsProps.url,
        credentials = stsProps.username to stsProps.password
    )
}

fun stsClient(stsUrl: String, credentials: Pair<String, String>): STSClient {
    val bus = BusFactory.getDefaultBus()
    return STSClient(bus).apply {
        isEnableAppliesTo = false
        isAllowRenewing = false
        location = stsUrl
        // Uten WSAddressingFeature -> Operation is not supported at this time og manglende wsa:Action
        features = listOf(WSAddressingFeature(), LoggingFeature())
        properties = mapOf(
            SecurityConstants.USERNAME to credentials.first,
            SecurityConstants.PASSWORD to credentials.second
        )
        setPolicy(bus.resolvePolicy(STS_CLIENT_AUTHENTICATION_POLICY))
    }
}

fun STSClient.configureFor(servicePort: Any, policyUri: String) {
    val client = ClientProxy.getClient(servicePort)
    client.configureSTS(this, policyUri)
}

fun Client.configureSTS(
    stsClient: STSClient,
    policyUri: String = STS_SAML_POLICY,
    cacheTokenInEndpoint: Boolean = false
) {
    requestContext[SecurityConstants.STS_CLIENT] = stsClient
    requestContext[SecurityConstants.CACHE_ISSUED_TOKEN_IN_ENDPOINT] = cacheTokenInEndpoint
    setClientEndpointPolicy(stsClient.client, resolvePolicyReference(stsClient.client, policyUri))
}

private fun Bus.resolvePolicy(policyUri: String): Policy {
    val registry = getExtension(PolicyEngine::class.java).registry
    val resolved = registry.lookup(policyUri)

    val policyBuilder = getExtension(PolicyBuilder::class.java)
    val referenceResolver = RemoteReferenceResolver("", policyBuilder)

    return resolved ?: referenceResolver.resolveReference(policyUri)
}

private fun setClientEndpointPolicy(client: Client, policy: Policy) {
    val endpoint = client.endpoint
    val endpointInfo = endpoint.endpointInfo

    val policyEngine = client.bus.getExtension(
        PolicyEngine::class.java
    )
    val message = SoapMessage(Soap12.getInstance())
    val endpointPolicy = policyEngine.getClientEndpointPolicy(endpointInfo, null, message)
    policyEngine.setClientEndpointPolicy(endpointInfo, endpointPolicy.updatePolicy(policy, message))
}

private fun resolvePolicyReference(client: Client, uri: String): Policy {
    val policyBuilder = client.bus.getExtension(
        PolicyBuilder::class.java
    )
    val resolver: ReferenceResolver = RemoteReferenceResolver("", policyBuilder)
    return resolver.resolveReference(uri)
}
