package no.nav.melosys.soknadmottak.kafka

import no.nav.common.KafkaEnvironment
import no.nav.melosys.soknadmottak.TestConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@Import(TestConfig::class)
@ActiveProfiles(profiles = ["test"])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KafkaProducerTest {

    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<String, Soknad>

    @Mock
    private lateinit var callbackService: CallbackService

    @Value("\${melosys.kafka.producer.topic-name}")
    private lateinit var topicName: String

    private lateinit var kafkaProducer: KafkaProducer

    @Autowired
    private lateinit var embeddedKafka: KafkaEnvironment

    @Captor
    private lateinit var resultCaptor: ArgumentCaptor<SendResult<String, Soknad>>

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
        `when`(callbackService.kvitter(ArgumentMatchers.any())).thenCallRealMethod()

        kafkaProducer.publiserMelding(Soknad("innhold"))

        verify(callbackService, timeout(5_000).times(1)).kvitter(resultCaptor.capture())
        assertThat(resultCaptor.value.producerRecord.value().felt).isEqualTo("innhold")
    }
}
