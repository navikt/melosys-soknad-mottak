package no.nav.melosys.soknadmottak.config

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Metrics
import no.nav.melosys.soknadmottak.common.Metrikker.kvitteringSendt
import no.nav.melosys.soknadmottak.common.Metrikker.meldingSendt
import no.nav.melosys.soknadmottak.common.Metrikker.søknadMottatt
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

private const val NAMESPACE = "melosys_soknad_mottak"

@Configuration
class MetrikkConfig {

    @Bean
    fun metricsCommonTags(): MeterRegistryCustomizer<MeterRegistry>? {
        return MeterRegistryCustomizer { registry: MeterRegistry ->
            registry.config().commonTags("app", "melosys-soknad-mottak", "team", "teammelosys")
        }
    }

    @Bean
    fun initialiserMetrikker() {
        søknadMottatt = Counter.builder("soknad-mottatt")
            .baseUnit(NAMESPACE)
            .description("Antall søknader mottatt fra Altinn")
            .register(Metrics.globalRegistry)
        kvitteringSendt = Counter.builder("kvittering-sendt")
            .baseUnit(NAMESPACE)
            .description("Antall kvitteringer sendt til søker")
            .register(Metrics.globalRegistry)
        meldingSendt = Counter.builder("melding-sendt")
            .baseUnit(NAMESPACE)
            .description("Antall søknader sendt til Melosys")
            .register(Metrics.globalRegistry)
    }
}
