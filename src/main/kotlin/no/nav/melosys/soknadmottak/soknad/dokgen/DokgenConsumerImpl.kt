package no.nav.melosys.soknadmottak.soknad.dokgen

import no.nav.melosys.soknadmottak.common.IntegrasjonException
import no.nav.melosys.soknadmottak.soknad.dokgen.modell.SoknadFelter
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient

class DokgenConsumerImpl(private val webClient: WebClient) : DokgenConsumer {

    override fun lagPDF(malNavn: String, soknadFelter: SoknadFelter): ByteArray {
        return webClient.post().uri("/api/v1/mal/$malNavn/lag-pdf")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(soknadFelter)
            .retrieve()
            .bodyToMono(ByteArray::class.java)
            .block() ?: throw IntegrasjonException("Henting av PDF $malNavn feilet.")
    }
}
