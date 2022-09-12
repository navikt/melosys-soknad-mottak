package no.nav.melosys.soknadmottak.soknad

import io.kotest.matchers.doubles.shouldBeExactly
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class SoknadCacheTest {
    @MockK
    lateinit var soknadRepository: SoknadRepository

    private lateinit var cache : SoknadCache

    @BeforeEach
    internal fun setUp() {
        cache = SoknadCache(soknadRepository, 100)
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
}