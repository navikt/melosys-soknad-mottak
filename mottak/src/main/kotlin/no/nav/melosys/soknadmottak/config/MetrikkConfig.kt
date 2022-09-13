package no.nav.melosys.soknadmottak.config

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Metrics
import io.micrometer.core.instrument.binder.MeterBinder
import no.nav.melosys.soknadmottak.soknad.SoknadStatsCache
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MetrikkConfig {

    companion object {
        private const val NAMESPACE = "melosys.soknad.mottak_"
        private const val SOKNAD_LEVERT = NAMESPACE + "soknad.levert"
        private const val SOKNAD_IKKE_LEVERT = NAMESPACE + "soknad.ikke.levert"
        private const val SOKNAD_MOTTATT = NAMESPACE + "soknad.mottatt"
    }

    object Metrikker {
        internal val søknadMottatt = Counter.builder(SOKNAD_MOTTATT)
            .description("Antall søknader mottatt fra Altinn")
            .register(Metrics.globalRegistry)
    }

    @Bean
    fun metricsCommonTags(): MeterRegistryCustomizer<MeterRegistry>? {
        return MeterRegistryCustomizer { registry: MeterRegistry ->
            registry.config().commonTags("app", "melosys-soknad-mottak", "team", "teammelosys")
        }
    }

    @Bean
    fun SoknadMetrikker(meterRegistry: MeterRegistry, soknadStatsCache: SoknadStatsCache): MeterBinder {
        return MeterBinder {
            run {
                registrerAntallSoknaderSomErLevert(meterRegistry, soknadStatsCache)
                registerAntallSoknaderSomIkkeErLevert(meterRegistry, soknadStatsCache)
            }
        }
    }

    fun registrerAntallSoknaderSomErLevert(meterRegistry: MeterRegistry, soknadStatsCache: SoknadStatsCache) {
        Gauge.builder(
            SOKNAD_LEVERT,
            soknadStatsCache
        ) { soknadStatsCache.hentSoknaderMedLevert(true) }.register(meterRegistry)
    }

    fun registerAntallSoknaderSomIkkeErLevert(meterRegistry: MeterRegistry, soknadStatsCache: SoknadStatsCache) {
        Gauge.builder(
            SOKNAD_IKKE_LEVERT,
            soknadStatsCache
        ) { soknadStatsCache.hentSoknaderMedLevert(false) }.register(meterRegistry)
    }
}
