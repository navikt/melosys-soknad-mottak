package no.nav.melosys.soknadmottak.config

import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

private const val API_PAKKE = "no.nav.melosys.soknadmottak"

@Configuration
class ApiConfig : WebMvcConfigurer {
    companion object {
        private const val API_PREFIX = "/api"
    }

    override fun configurePathMatch(configurer: PathMatchConfigurer) {
        configurer.addPathPrefix(API_PREFIX) { erApiTjeneste(it) }
    }

    override fun configureContentNegotiation(configurer: ContentNegotiationConfigurer) {
        super.configureContentNegotiation(configurer)
        configurer.defaultContentType(MediaType.APPLICATION_JSON)
    }

    private fun erApiTjeneste(clazz: Class<*>): Boolean =
        clazz.packageName.startsWith(API_PAKKE) &&
            clazz.isAnnotationPresent(RestController::class.java)
}
