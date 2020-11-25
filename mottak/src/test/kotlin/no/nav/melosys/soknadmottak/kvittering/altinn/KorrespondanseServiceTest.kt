package no.nav.melosys.soknadmottak.kvittering.altinn

import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import no.altinn.services.serviceengine.correspondence._2009._10.ICorrespondenceAgencyExternalBasic
import no.altinn.services.serviceengine.correspondence._2009._10.InsertCorrespondenceBasicV2
import no.nav.melosys.soknadmottak.config.AltinnConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class KorrespondanseServiceTest {
    @RelaxedMockK
    lateinit var altinnConfig: AltinnConfig

    @RelaxedMockK
    lateinit var iCorrespondenceAgencyExternalBasic: ICorrespondenceAgencyExternalBasic

    private lateinit var korrespondanseService: KorrespondanseService
    private lateinit var insertCorrespondenceBasicV2: InsertCorrespondenceBasicV2

    @BeforeAll
    fun setUp() {
        korrespondanseService = KorrespondanseService(altinnConfig, iCorrespondenceAgencyExternalBasic)
        insertCorrespondenceBasicV2 = korrespondanseService.lagMelding(
            "mottakerID",
            "arkivRef",
            "MELDING_SENDER",
            Melding(
                "EMNE",
                "TEKST",
                Vedlegg("FIL_NAVN", ByteArray(7))
            ),
            22
        )
    }

    @Test
    fun sendMelding() {
        korrespondanseService.sendMelding(insertCorrespondenceBasicV2)

        verify { iCorrespondenceAgencyExternalBasic.insertCorrespondenceBasicV2(
            eq(insertCorrespondenceBasicV2.systemUserName),
            eq(insertCorrespondenceBasicV2.systemPassword),
            eq(insertCorrespondenceBasicV2.systemUserCode),
            eq(insertCorrespondenceBasicV2.externalShipmentReference),
            eq(insertCorrespondenceBasicV2.correspondence)
        ) }
    }

    @Test
    fun `test melding fra lagMelding`() {
        val melding = insertCorrespondenceBasicV2.correspondence

        assertThat(melding.reportee).isEqualTo("mottakerID")
        assertThat(melding.archiveReference).isEqualTo("arkivRef")
        assertThat(melding.messageSender).isEqualTo("MELDING_SENDER")
        assertThat(melding.content.messageTitle).isEqualTo("EMNE")
        assertThat(melding.content.messageBody).isEqualTo("TEKST")
        val binærVedlegg = melding.content.attachments.binaryAttachments.binaryAttachmentV2[0]
        assertThat(binærVedlegg.fileName).isEqualTo("FIL_NAVN.pdf")
        assertThat(binærVedlegg.name).isEqualTo("FIL_NAVN")
        assertThat(binærVedlegg.data.size).isEqualTo(7)
        assertThat(melding.allowSystemDeleteDateTime.year).isEqualTo(LocalDateTime.now().year + 22)
    }
}