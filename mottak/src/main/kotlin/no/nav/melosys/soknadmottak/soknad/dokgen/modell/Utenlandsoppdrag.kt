package no.nav.melosys.soknadmottak.soknad.dokgen.modell

data class Utenlandsoppdrag(
    val arbeidsland: String,
    val periode: String,
    val erErstatning: Boolean,
    val samletUtsendingPeriode: String,
    val erSendingForOppdrag: Boolean,
    val erDrattPaaEgetInitiativ: Boolean?,
    val erAnsettelseForOpphold: Boolean,
    val erFortsattAnsattEtterOppdrag: Boolean?
)
