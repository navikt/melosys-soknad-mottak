package no.nav.melosys.soknadmottak.soknad.dokgen.modell

data class LoennOgGodtgjoerelse(
    val norskArbgUtbetalerLoenn: Boolean,
    val erArbeidstakerAnsattHelePerioden: Boolean?,
    val utlArbgUtbetalerLoenn: Boolean,
    val utlArbTilhoererSammeKonsern: Boolean?,
    val bruttoLoennPerMnd: String?,
    val bruttoLoennUtlandPerMnd: String?,
    val mottarNaturalytelser: Boolean?,
    val samletVerdiNaturalytelser: String?,
    val erArbeidsgiveravgiftHelePerioden: Boolean?,
    val erTrukketTrygdeavgift: Boolean?
)
