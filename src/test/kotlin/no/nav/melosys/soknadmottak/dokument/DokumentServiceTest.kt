package no.nav.melosys.soknadmottak.dokument

import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class DokumentServiceTest {
    @RelaxedMockK()
    lateinit var dokumentRepository: DokumentRepository

    lateinit var dokumentService: DokumentService

    @BeforeAll
    fun setUp() {
        dokumentService = DokumentService(dokumentRepository)
    }

    @Test
    fun hentDokument() {
        val dokumentID = "dokID"
        dokumentService.hentDokument(dokumentID)
        verify { dokumentRepository.findByDokumentID(dokumentID) }
    }

    @Test
    fun hentVedlegg() {
        val soknadID = "soknadID"
        dokumentService.hentVedlegg(soknadID)
        verify { dokumentRepository.findBySoknadSoknadID(soknadID) }
    }

    @Test
    fun lagreDokument() {
        val dokument = DokumentFactory.lagDokument()
        every { dokumentRepository.save<Dokument>(any()) } returns mockk()

        dokumentService.lagreDokument(dokument)
        verify { dokumentRepository.save(dokument) }
    }
}