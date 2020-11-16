package no.nav.melosys.soknadmottak.soknad.dokgen.modell

data class Arbeidssted(
    val type: String,
    val arbeidPaaLand: ArbeidPaaLand?,
    val offshoreEnheter: OffshoreEnheter?,
    val skipListe: SkipListe?,
    val luftfart: Luftfart?
)

data class ArbeidPaaLand(
    val fastArbeidssted: Boolean,
    val hjemmekontor: Boolean,
    val fysiskeArbeidssteder: List<FysiskArbeidssted>
)

data class FysiskArbeidssted(
    val firmanavn: String,
    val gatenavn: String,
    val by: String,
    val postkode: String,
    val region: String,
    val land: String
)

data class OffshoreEnheter(
    val offshoreEnheter: List<OffshoreEnhet>
)

data class OffshoreEnhet(
    val enhetsNavn: String,
    val sokkelLand: String,
    val enhetsType: String
)

data class SkipListe(
    val skipListe: List<Skip>
)

data class Skip(
    val fartsomraade: String,
    val skipNavn: String,
    val flaggland: String,
    val territorialEllerHavnLand: String
)

data class Luftfart(
    val luftfartBaser: List<LuftfartBase>
)

data class LuftfartBase(
    val hjemmebaseNavn: String,
    val hjemmebaseLand: String,
    val typeFlyvninger: String
)
