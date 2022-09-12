package no.nav.melosys.soknadmottak.config

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.binder.MeterBinder
import no.nav.melosys.soknadmottak.soknad.SoknadCache
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MetrikkConfig {

    companion object {
        private const val NAMESPACE = "melosys.soknad.mottak_"
        private const val SOKNAD_LEVERT = NAMESPACE + "soknad.levert"
        private const val SOKNAD_IKKE_LEVERT = NAMESPACE + "soknad.ikke.levert"
    }

    @Bean
    fun metricsCommonTags(): MeterRegistryCustomizer<MeterRegistry>? {
        return MeterRegistryCustomizer { registry: MeterRegistry ->
            registry.config().commonTags("app", "melosys-soknad-mottak", "team", "teammelosys")
        }
    }

    @Bean
    fun SoknadMetrikker(meterRegistry: MeterRegistry, soknadCache: SoknadCache): MeterBinder {
        return MeterBinder {
            run {
                registrerAntallSoknaderSomErLevert(meterRegistry, soknadCache)
                registerAntallSoknaderSomIkkeErLevert(meterRegistry, soknadCache)
            }
        }
    }

    fun registrerAntallSoknaderSomErLevert(meterRegistry: MeterRegistry, soknadCache: SoknadCache) {
        Gauge.builder(
            SOKNAD_LEVERT,
            soknadCache
        ) { soknadCache.hentSoknaderMedLevert(true) }.register(meterRegistry)
    }

    fun registerAntallSoknaderSomIkkeErLevert(meterRegistry: MeterRegistry, soknadCache: SoknadCache) {
        Gauge.builder(
            SOKNAD_IKKE_LEVERT,
            soknadCache
        ) { soknadCache.hentSoknaderMedLevert(false) }.register(meterRegistry)
    }
}
