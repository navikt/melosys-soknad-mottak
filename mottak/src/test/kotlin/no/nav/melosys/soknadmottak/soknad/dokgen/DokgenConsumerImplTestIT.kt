package no.nav.melosys.soknadmottak.soknad.dokgen

import org.apache.pdfbox.pdmodel.PDDocument
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
internal class DokgenConsumerImplTestIT @Autowired constructor(
    val dokgenConsumer: DokgenConsumer
) {
    @Test
    fun `hent søknad-PDF fra dokgen`() {
        val doc = PDDocument.load(dokgenConsumer.lagPDF("soeknad", SoknadFelterBuilder().build()))
        assertThat(doc.numberOfPages).isGreaterThan(0)
    }
}