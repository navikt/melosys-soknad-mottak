package no.nav.melosys.soknadmottak.common

import io.micrometer.core.instrument.Counter

object Metrikker {
    internal lateinit var søknadMottatt: Counter
    internal lateinit var kvitteringSendt: Counter
    internal lateinit var meldingSendt: Counter
}
