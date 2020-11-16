package no.nav.melosys.soknadmottak.soknad.dokgen

import no.nav.melosys.soknadmottak.soknad.SoknadFactory
import no.nav.melosys.soknadmottak.soknad.SoknadSkjemaOversetter
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
        val søknad = SoknadFactory.lagSoknadFraXmlFil()
        val felter = SoknadSkjemaOversetter.tilSøknadFelter(søknad)

        val doc = PDDocument.load(dokgenConsumer.lagPDF("soeknad", felter))
        assertThat(doc.numberOfPages).isGreaterThan(0)
    }
}