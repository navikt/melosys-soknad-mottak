package no.nav.melosys.soknadmottak.kvittering.altinn

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class CorrespondenceFactoryTest {
    @Test
    fun lagKvittering() {
        val melding = Melding("tittel", "tekst", Vedlegg("vedlegg", ByteArray(8)))
        val insertCorrespondence = CorrespondenceFactory.insertCorrespondence(
            "serviceCode",
            "serviceEdition",
            "mottakerID",
            "arkivRef",
            "avsender",
            melding,
            42
        )

        assertThat(insertCorrespondence.serviceCode).isEqualTo("serviceCode")
        assertThat(insertCorrespondence.serviceEdition).isEqualTo("serviceEdition")
        assertThat(insertCorrespondence.reportee).isEqualTo("mottakerID")
        assertThat(insertCorrespondence.archiveReference).isEqualTo("arkivRef")
        assertThat(insertCorrespondence.messageSender).isEqualTo("avsender")
        assertThat(insertCorrespondence.content.messageTitle).isEqualTo("tittel")
        assertThat(insertCorrespondence.content.messageBody).isEqualTo("tekst")
        val binærVedlegg = insertCorrespondence.content.attachments.binaryAttachments.binaryAttachmentV2[0]
        assertThat(binærVedlegg.fileName).isEqualTo("vedlegg.pdf")
        assertThat(binærVedlegg.name).isEqualTo("vedlegg")
        assertThat(binærVedlegg.data.size).isEqualTo(8)
        assertThat(insertCorrespondence.allowSystemDeleteDateTime.year).isEqualTo(LocalDateTime.now().year + 42)
    }
}