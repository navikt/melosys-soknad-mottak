package no.nav.melosys.soknadmottak.soknad.dokgen.modell

data class UtenlandskVirksomhet(
    val navn: String,
    val registreringsnummer: String,
    val adresse: UtenlandskAdresse
)

data class UtenlandskAdresse(
    val gate: String,
    val postkode: String,
    val by: String,
    val region: String?,
    val land: String
)
