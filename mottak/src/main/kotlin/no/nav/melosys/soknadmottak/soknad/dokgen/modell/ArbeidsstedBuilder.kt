package no.nav.melosys.soknadmottak.soknad.dokgen.modell

data class ArbeidsstedBuilder(
    val type: String = "",
    val arbeidPaaLand: ArbeidPaaLand? = null,
    val offshoreEnheter: OffshoreEnheter? = null,
    val skipListe: SkipListe? = null,
    val luftfart: Luftfart? = null
) {
    fun build(): Arbeidssted {
        return Arbeidssted(
            type = type,
            arbeidPaaLand = arbeidPaaLand,
            offshoreEnheter = offshoreEnheter,
            skipListe = skipListe,
            luftfart = luftfart
        )
    }
}

data class ArbeidPaaLandBuilder(
    val fastArbeidssted: Boolean = false,
    val hjemmekontor: Boolean = false,
    val fysiskeArbeidssteder: List<FysiskArbeidssted> = listOf()
) {
    fun build(): ArbeidPaaLand {
        return ArbeidPaaLand(
            fastArbeidssted = fastArbeidssted,
            hjemmekontor = hjemmekontor,
            fysiskeArbeidssteder = fysiskeArbeidssteder
        )
    }
}

data class OffshoreEnheterBuilder(
    val offshoreEnheter: List<OffshoreEnhet> = listOf()
) {
    fun build(): OffshoreEnheter {
        return OffshoreEnheter(
            offshoreEnheter = offshoreEnheter
        )
    }
}

data class SkipListeBuilder(
    val skipListe: List<Skip> = listOf()
) {
    fun build(): SkipListe {
        return SkipListe(
            skipListe = skipListe
        )
    }
}

data class LuftfartBuilder(
    val luftfartBaser: List<LuftfartBase> = listOf()
) {
    fun build(): Luftfart {
        return Luftfart(
            luftfartBaser = luftfartBaser
        )
    }
}
