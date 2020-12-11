package no.nav.melosys.soknadmottak.common

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics

private const val NAMESPACE = "melosys_soknad_mottak"

object Metrikker {
    internal val søknadMottatt: Counter = Counter.builder("soknad-mottatt")
        .baseUnit(NAMESPACE)
        .description("Antall søknader mottatt fra Altinn")
        .register(Metrics.globalRegistry).apply { increment() }
    internal val kvitteringSendt: Counter = Counter.builder("kvittering-sendt")
        .baseUnit(NAMESPACE)
        .description("Antall kvitteringer sendt til søker")
        .register(Metrics.globalRegistry).apply { increment() }
    internal val meldingSendt: Counter = Counter.builder("melding-sendt")
        .baseUnit(NAMESPACE)
        .description("Antall søknader sendt til Melosys")
        .register(Metrics.globalRegistry).apply { increment() }
}
