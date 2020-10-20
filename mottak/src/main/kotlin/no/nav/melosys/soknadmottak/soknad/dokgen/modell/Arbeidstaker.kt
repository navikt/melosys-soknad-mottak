package no.nav.melosys.soknadmottak.soknad.dokgen.modell

data class Arbeidstaker(
    val barnMed: List<BarnMed>,
    val erMedBarnUnder18: Boolean,
    val etternavn: String,
    val fnr: String,
    val foedeland: String,
    val foedsted: String,
    val fornavn: String
)