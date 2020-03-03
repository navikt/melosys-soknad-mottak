package no.nav.melosys.soknadmottak.metrics

import io.prometheus.client.Counter
import io.prometheus.client.Summary

private const val NAMESPACE = "melosys_soknad_mottak"

object Metrics {
    val altinnSkjemaReceivedCounter: Counter = Counter.build()
        .namespace(NAMESPACE)
        .name("altinnskjema_received")
        .help("Number of received Altinnskjemas")
        .register()
    val networkCallSummary: Summary = Summary.build()
        .namespace(NAMESPACE)
        .name("network_call_summary")
        .help("Summary for networked call times")
        .labelNames("call_name")
        .register()
    val networkCallFailuresCounter: Counter = Counter.build()
        .namespace(NAMESPACE)
        .name("network_call_failures")
        .help("Number of network call failures")
        .labelNames("call_name")
        .register()
}
