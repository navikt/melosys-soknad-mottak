package no.nav.melosys.soknadmottak.kafka

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import no.nav.melosys.soknadmottak.kvittering.KvitteringService
import no.nav.melosys.soknadmottak.soknad.SoknadFactory
import no.nav.melosys.soknadmottak.soknad.SoknadService
import org.apache.kafka.clients.producer.ProducerRecord
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.kafka.support.SendResult

@ExtendWith(MockKExtension::class)
internal class CallbackServiceTest {
    @MockK(relaxUnitFun = true)
    lateinit var soknadService: SoknadService
    @RelaxedMockK
    lateinit var kvitteringService: KvitteringService

    private lateinit var callbackService: CallbackService

    @BeforeEach
    internal fun beforeEach() {
        callbackService = CallbackService(soknadService, kvitteringService)
        every { soknadService.hentSøknad(any()) }.returns(SoknadFactory.lagSoknadFraXmlFil())
        every { soknadService.lagPdf(any()) }.returns(ByteArray(0))
    }

    @Test
    fun kvitter() {
        val uuid = "UUID"
        val soknadMottatt = SoknadMottatt(uuid)
        val producerRecord = ProducerRecord<String, SoknadMottatt>("topic", soknadMottatt)

        callbackService.kvitter(SendResult(producerRecord, mockk()))

        verify { soknadService.oppdaterLeveringsstatus(uuid) }
    }
}
