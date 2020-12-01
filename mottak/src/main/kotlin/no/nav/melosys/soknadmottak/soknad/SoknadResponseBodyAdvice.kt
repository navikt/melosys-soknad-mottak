package no.nav.melosys.soknadmottak.soknad

import mu.KotlinLogging
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice

private val logger = KotlinLogging.logger { }

// FIXME: Denne må fjernes før vi går i prod, siden søknaden inneholder sensitive opplysninger
@ControllerAdvice
class SoknadResponseBodyAdvice : ResponseBodyAdvice<Any?> {
    override fun supports(returnType: MethodParameter, converterType: Class<out HttpMessageConverter<*>>): Boolean {
        return (returnType.method?.name == "hentSøknad")
    }

    override fun beforeBodyWrite(
        body: Any?,
        returnType: MethodParameter,
        selectedContentType: MediaType,
        selectedConverterType: Class<out HttpMessageConverter<*>>,
        request: ServerHttpRequest,
        response: ServerHttpResponse
    ): Any? {
        logger.info { "Søknad $body" }
        return body
    }
}