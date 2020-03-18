package no.nav.melosys.soknadmottak.kafka

import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import no.nav.common.KafkaEnvironment
import no.nav.melosys.soknadmottak.SoknadMottakTestConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ExtendWith(MockKExtension::class)
@Import(SoknadMottakTestConfiguration::class)
@ActiveProfiles(profiles = ["test"])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KafkaProducerIT @Autowired constructor(
    private val kafkaTemplate: KafkaTemplate<String, MottattSoknadMelding>,
    private val embeddedKafka: KafkaEnvironment,
    @Value("\${melosys.kafka.producer.topic-name}")
    private val topicName: String
) {
    private val callbackService = spyk<CallbackService>()
    private lateinit var kafkaProducer: KafkaProducer

    @BeforeEach
    internal fun beforeEach() {
        kafkaProducer = KafkaProducer(kafkaTemplate, callbackService, topicName)
    }

    @BeforeAll
    internal fun beforeAll() {
        embeddedKafka.start()
    }

    @AfterAll
    internal fun afterAll() {
        embeddedKafka.tearDown()
    }

    @Test
    internal fun publiserMelding() {
        kafkaProducer.publiserMelding(MottattSoknadMelding(123))

        val slot = slot<SendResult<String, MottattSoknadMelding>>()
        verify(timeout = 5_000) { callbackService.kvitter(capture(slot)) }

        assertThat(slot.captured.producerRecord.value().soknadID).isEqualTo(123)
    }
}
