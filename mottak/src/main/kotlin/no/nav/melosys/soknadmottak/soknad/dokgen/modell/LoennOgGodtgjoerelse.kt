package no.nav.melosys.soknadmottak.soknad.dokgen.modell

data class LoennOgGodtgjoerelse(
    val norskArbgUtbetalerLoenn: Boolean,
    val utlArbgUtbetalerLoenn: Boolean,
    val bruttoLoennPerMnd: String?,
    val bruttoLoennUtlandPerMnd: String?,
    val mottarNaturalytelser: Boolean?,
    val samletVerdiNaturalytelser: String?,
    val erArbeidsgiveravgiftHelePerioden: Boolean,
    val erTrukketTrygdeavgift: Boolean
)
