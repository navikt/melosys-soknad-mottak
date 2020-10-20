package no.nav.melosys.soknadmottak.soknad.dokgen.modell

data class Utenlandsoppdrag(
    val arbeidsland: String,
    val erAnsattHelePeriode: Boolean,
    val erAnsettelseForOpphold: Boolean,
    val erErstatning: Boolean,
    val erSendingForOppdrag: Boolean,
    val periode: String
)