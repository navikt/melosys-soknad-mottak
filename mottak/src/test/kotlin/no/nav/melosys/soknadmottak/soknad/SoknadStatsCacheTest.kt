package no.nav.melosys.soknadmottak.soknad

import io.kotest.matchers.doubles.shouldBeExactly
import io.micrometer.core.instrument.Metrics
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import no.nav.melosys.soknadmottak.config.MetrikkConfig
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

// NB!
// Litt ustabile tester på raskere PC-er. Kan være at det er for lite tid mellom oppdatering av cache og henting av data.
// Kjører stabilt i Github Actions.
@ExtendWith(MockKExtension::class)
internal class SoknadStatsCacheTest {
    @MockK
    lateinit var soknadRepository: SoknadRepository

    private lateinit var cache: SoknadStatsCache

    @BeforeEach
    internal fun setUp() {
        cache = SoknadStatsCache(soknadRepository, 100)
    }

    @Test
    internal fun antallSoknaderICache() {
        every { soknadRepository.hentAntallSoknaderIkkeLevert() } returns 3
        every { soknadRepository.hentAntallSoknaderLevert() } returns 6

        cache.hentSoknaderMedLevert(false)
            .shouldBeExactly(3.0)
        cache.hentSoknaderMedLevert(true)
            .shouldBeExactly(6.0)
    }

    @Test
    internal fun soknadMottattIncrement() {
        Metrics.addRegistry(SimpleMeterRegistry())
        MetrikkConfig.Metrikker.søknadMottatt.count().shouldBeExactly(0.0)
        MetrikkConfig.Metrikker.søknadMottatt.increment()
        MetrikkConfig.Metrikker.søknadMottatt.increment()
        MetrikkConfig.Metrikker.søknadMottatt.count().shouldBeExactly(2.0)
    }
}