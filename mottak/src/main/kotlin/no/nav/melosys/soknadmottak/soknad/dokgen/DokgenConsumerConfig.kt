package no.nav.melosys.soknadmottak.soknad.dokgen

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class DokgenConsumerConfig(@Value("\${dokgen.url}") private val url: String) {
    @Bean
    fun dokgenConsumer(): DokgenConsumer {
        return DokgenConsumerImpl(WebClient.create(url))
    }
}