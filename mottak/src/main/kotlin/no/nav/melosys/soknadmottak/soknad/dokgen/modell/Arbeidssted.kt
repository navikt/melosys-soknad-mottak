package no.nav.melosys.soknadmottak.soknad.dokgen.modell

data class Arbeidssted(
    val type: String,
    val innretningNavn: String,
    val innretningType: String,
    val sokkelLand: String
)