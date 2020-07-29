package no.nav.melosys.soknadmottak.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiKey
import springfox.documentation.service.SecurityReference
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.service.contexts.SecurityContext
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

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
        clazz.packageName.startsWith(API_PAKKE)
                && clazz.isAnnotationPresent(RestController::class.java)
}

@Configuration
@EnableSwagger2
@ConditionalOnProperty(name = ["NAIS_CLUSTER_NAME"], havingValue = "dev-fss", matchIfMissing = true)
class SwaggerConfig {
    @Bean
    fun api(): Docket =
        Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.basePackage(API_PAKKE))
            .paths(PathSelectors.any())
            .build()
            .securityContexts(
                listOf(
                    SecurityContext.builder()
                        .securityReferences(
                            listOf(
                                SecurityReference.builder()
                                    .reference("JWT")
                                    .scopes(emptyArray())
                                    .build()
                            )
                        )
                        .build()
                )
            )
            .securitySchemes(listOf(ApiKey("JWT", "Authorization", "header")))
}
