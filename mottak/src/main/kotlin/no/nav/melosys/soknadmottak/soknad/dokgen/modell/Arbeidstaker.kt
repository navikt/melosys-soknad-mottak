package no.nav.melosys.soknadmottak.soknad.dokgen.modell

data class Arbeidstaker(
    val barnMed: List<BarnMed>,
    val erMedBarnUnder18: Boolean,
    val fulltNavn: String,
    val fnr: String,
    val foedeland: String,
    val foedested: String,
    val utenlandskIDnummer: String?
)
