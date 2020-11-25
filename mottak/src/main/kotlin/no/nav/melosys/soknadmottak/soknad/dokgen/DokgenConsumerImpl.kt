package no.nav.melosys.soknadmottak.soknad.dokgen

import no.nav.melosys.soknadmottak.common.IntegrasjonException
import no.nav.melosys.soknadmottak.soknad.dokgen.modell.Soknadsdata
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient

class DokgenConsumerImpl(private val webClient: WebClient) : DokgenConsumer {

    override fun lagPDF(malNavn: String, soknadsdata: Soknadsdata): ByteArray {
        return webClient.post().uri("/api/v1/mal/$malNavn/lag-pdf")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(soknadsdata)
            .retrieve()
            .bodyToMono(ByteArray::class.java)
            .block() ?: throw IntegrasjonException("Henting av PDF $malNavn feilet.")
    }
}
