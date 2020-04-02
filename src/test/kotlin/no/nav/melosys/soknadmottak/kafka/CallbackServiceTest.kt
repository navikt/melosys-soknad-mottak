package no.nav.melosys.soknadmottak.kafka

import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
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

    lateinit var callbackService: CallbackService

    @BeforeEach
    internal fun beforeEach() {
        callbackService = CallbackService(soknadService)
    }

    @Test
    fun kvitter() {
        val uuid = "UUID"
        val soknadMottatt = SoknadMottatt(uuid)
        val producerRecord = ProducerRecord<String, SoknadMottatt>("topic", soknadMottatt)

        callbackService.kvitter(SendResult(producerRecord, mockk()))

        verify { soknadService.updateDeliveryStatus(uuid) }
    }
}