package no.nav.melosys.soknadmottak.soknad

import com.fasterxml.jackson.databind.AnnotationIntrospector
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationIntrospector
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.melosys.altinn.soknad.*
import no.nav.melosys.soknadmottak.soknad.dokgen.modell.*
import no.nav.melosys.soknadmottak.soknad.dokgen.modell.ArbeidPaaLand
import no.nav.melosys.soknadmottak.soknad.dokgen.modell.Arbeidsgiver
import no.nav.melosys.soknadmottak.soknad.dokgen.modell.Arbeidssted
import no.nav.melosys.soknadmottak.soknad.dokgen.modell.Arbeidstaker
import no.nav.melosys.soknadmottak.soknad.dokgen.modell.FysiskArbeidssted
import no.nav.melosys.soknadmottak.soknad.dokgen.modell.LoennOgGodtgjoerelse
import no.nav.melosys.soknadmottak.soknad.dokgen.modell.Luftfart
import no.nav.melosys.soknadmottak.soknad.dokgen.modell.LuftfartBase
import no.nav.melosys.soknadmottak.soknad.dokgen.modell.OffshoreEnhet
import no.nav.melosys.soknadmottak.soknad.dokgen.modell.OffshoreEnheter
import no.nav.melosys.soknadmottak.soknad.dokgen.modell.Skip
import no.nav.melosys.soknadmottak.soknad.dokgen.modell.SkipListe
import org.apache.commons.lang3.StringUtils
import javax.xml.datatype.XMLGregorianCalendar

private val kotlinXmlMapper =

    XmlMapper.builder().configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
        .addModule(JakartaXmlBindAnnotationModule())
        .addModule(JacksonXmlModule().apply { setDefaultUseWrapper(false) })
        .annotationIntrospector(JakartaXmlBindAnnotationIntrospector(TypeFactory.defaultInstance()))
        .build()
        .registerKotlinModule()
        .setAnnotationIntrospector(
            AnnotationIntrospector.pair(
                JacksonAnnotationIntrospector(),
                JakartaXmlBindAnnotationIntrospector(TypeFactory.defaultInstance())
            )
        )


object SoknadSkjemaOversetter {
    fun tilSøknadsdata(søknad: Soknad): Soknadsdata {
        val innhold = kotlinXmlMapper.readValue(søknad.innhold, MedlemskapArbeidEOSM::class.java).innhold
        val søknadsdataBuilder = SoknadsdataBuilder().apply {
            tidspunktMottatt = søknad.innsendtTidspunkt.toString()
            arbeidsgiver = oversettArbeidsgiver(innhold)
            arbeidstaker = oversettArbeidstaker(innhold)
            kontakperson = oversettKontaktperson(innhold)
            utenlandsoppdrag = oversettUtenlandsoppdrag(innhold)
            arbeidssted = oversettArbeidssted(innhold)
            loennOgGodtgjoerelse = oversettLoennOgGodtgjoerelse(innhold)
            utenlandskVirksomhet = oversettUtenlandskVirksomhet(innhold)
            virksomhetNorge = oversettVirksomhetNorge(innhold)
            arbeidssituasjon = oversettArbeidssituasjon(innhold)
        }

        return søknadsdataBuilder.build()
    }

    private fun oversettUtenlandskVirksomhet(innhold: Innhold): UtenlandskVirksomhet? {
        return if (innhold.midlertidigUtsendt.virksomhetIUtlandet != null &&
            StringUtils.isNotBlank(innhold.midlertidigUtsendt.virksomhetIUtlandet.navn)
        ) {
            UtenlandskVirksomhet(
                innhold.midlertidigUtsendt.virksomhetIUtlandet.navn,
                innhold.midlertidigUtsendt.virksomhetIUtlandet.registreringsnummer,
                oversettUtenlandskAdresse(innhold.midlertidigUtsendt.virksomhetIUtlandet.adresse)
            )
        } else {
            null
        }
    }

    private fun oversettUtenlandskAdresse(postadresseUtland: PostadresseUtland): UtenlandskAdresse =
        UtenlandskAdresse(
            gate = postadresseUtland.gatenavn,
            postkode = postadresseUtland.postkode,
            by = postadresseUtland.by,
            region = postadresseUtland.region,
            land = postadresseUtland.land
        )

    private fun oversettArbeidsgiver(innhold: Innhold) =
        Arbeidsgiver(
            innhold.arbeidsgiver.virksomhetsnummer,
            innhold.arbeidsgiver.isOffentligVirksomhet,
            innhold.arbeidsgiver.virksomhetsnavn,
            oversettAdresse(innhold.arbeidsgiver.adresse)
        )

    private fun oversettAdresse(adresse: ArbeidsgiverAdresse?): Adresse? {
        return adresse?.let {
            Adresse(
                it.gate,
                it.postkode,
                it.poststed,
                it.land
            )
        }
    }

    private fun oversettArbeidssted(innhold: Innhold): Arbeidssted {
        return innhold.midlertidigUtsendt.arbeidssted.let {
            Arbeidssted(
                it.typeArbeidssted,
                oversettArbeidPaaLand(it.arbeidPaaLand),
                oversettOffshoreEnheter(it.offshoreEnheter),
                oversettSkipListe(it.skipListe),
                oversettLuftfart(it.luftfart)
            )
        }
    }

    private fun oversettArbeidPaaLand(arbeidPaaLand: no.nav.melosys.altinn.soknad.ArbeidPaaLand?): ArbeidPaaLand? {
        return arbeidPaaLand?.let {
            ArbeidPaaLand(
                arbeidPaaLand.isFastArbeidssted,
                arbeidPaaLand.isHjemmekontor,
                arbeidPaaLand.fysiskeArbeidssteder?.let {
                    it.fysiskArbeidssted.map { fysiskArbeidssted ->
                        FysiskArbeidssted(
                            firmanavn = fysiskArbeidssted.firmanavn,
                            gatenavn = fysiskArbeidssted.gatenavn,
                            by = fysiskArbeidssted.by,
                            postkode = fysiskArbeidssted.postkode,
                            region = fysiskArbeidssted.region,
                            land = fysiskArbeidssted.land
                        )
                    }
                }
            )
        }
    }

    private fun oversettOffshoreEnheter(offshoreEnheter: no.nav.melosys.altinn.soknad.OffshoreEnheter?): OffshoreEnheter? {
        return offshoreEnheter?.let {
            OffshoreEnheter(
                offshoreEnheter.offshoreEnhet.map { enhet ->
                    OffshoreEnhet(
                        enhet.enhetsNavn,
                        enhet.sokkelLand,
                        enhet.enhetsType.value()
                    )
                }
            )
        }
    }

    private fun oversettLuftfart(luftfart: no.nav.melosys.altinn.soknad.Luftfart?): Luftfart? {
        return luftfart?.let {
            Luftfart(
                luftfart.luftfartBaser?.luftfartbase?.map { base ->
                    LuftfartBase(
                        base.hjemmebaseNavn,
                        base.hjemmebaseLand,
                        base.typeFlyvninger.value()
                    )
                }
            )
        }
    }

    private fun oversettSkipListe(skipListe: no.nav.melosys.altinn.soknad.SkipListe?): SkipListe? {
        return skipListe?.let {
            SkipListe(
                skipListe.skip.map { skip ->
                    Skip(
                        skip.fartsomraade.value(),
                        skip.skipNavn,
                        skip.flaggland,
                        skip.territorialEllerHavnLand
                    )
                }
            )
        }
    }

    private fun oversettArbeidstaker(innhold: Innhold) =
        Arbeidstaker(
            barnMed = oversettMedfølgendeBarn(innhold),
            erMedBarnUnder18 = innhold.arbeidstaker.isReiserMedBarnTilUtlandet ?: false,
            fulltNavn = hentArbeidstakerNavn(innhold),
            fnr = innhold.arbeidstaker.foedselsnummer,
            foedeland = innhold.arbeidstaker.foedeland,
            foedested = innhold.arbeidstaker.foedested,
            utenlandskIDnummer = innhold.arbeidstaker.utenlandskIDnummer
        )

    private fun hentArbeidstakerNavn(innhold: Innhold): String =
        if (innhold.arbeidstaker.fulltNavn != null) innhold.arbeidstaker.fulltNavn else
            innhold.arbeidstaker.etternavn

    private fun oversettMedfølgendeBarn(innhold: Innhold): List<BarnMed> {
        return innhold.arbeidstaker.barn?.barnet
            ?.map { barnet -> BarnMed(barnet.foedselsnummer, barnet.navn) }
            ?: emptyList()
    }

    private fun oversettKontaktperson(innhold: Innhold): Kontakperson? {
        return innhold.arbeidsgiver.kontaktperson?.let {
            Kontakperson(
                it.kontaktpersonNavn,
                it.kontaktpersonTelefon,
                oversettAnsattHos(innhold),
                arbeidstakerHarGittFullmakt(innhold),
                hentKontaktVirksomhetsnummer(innhold),
                hentKontaktVirksomhetsnavn(innhold)
            )
        }
    }

    private fun oversettAnsattHos(innhold: Innhold): String {
        return if (erRådgivningsfirmaFullmektig(innhold)) {
            "I et rådgivningsfirma"
        } else {
            "Hos arbeidsgiveren"
        }
    }

    private fun oversettUtenlandsoppdrag(innhold: Innhold): Utenlandsoppdrag {
        val midlertidigUtsendt = innhold.midlertidigUtsendt
        val utenlandsoppdraget = midlertidigUtsendt.utenlandsoppdraget
        return Utenlandsoppdrag(
            innhold.midlertidigUtsendt.arbeidsland,
            oversettTidsrom(utenlandsoppdraget.periodeUtland)!!,
            utenlandsoppdraget.isErstatterTidligereUtsendte,
            oversettTidsrom(utenlandsoppdraget.samletUtsendingsperiode),
            utenlandsoppdraget.isSendesUtOppdragIUtlandet,
            utenlandsoppdraget.isDrattPaaEgetInitiativ,
            utenlandsoppdraget.isAnsattForOppdragIUtlandet,
            utenlandsoppdraget.isAnsattEtterOppdraget
        )
    }

    private fun oversettTidsrom(tidsrom: Tidsrom?): Periode? {
        return tidsrom?.let {
            val fom = oversettDato(it.periodeFra)
            val tom = oversettDato(it.periodeTil)
            Periode(fom, tom)
        }
    }

    private fun oversettDato(calendar: XMLGregorianCalendar?): String {
        return calendar?.let {
            "${it.day}-${it.month}-${it.year}"
        } ?: ""
    }

    private fun oversettLoennOgGodtgjoerelse(innhold: Innhold): LoennOgGodtgjoerelse {
        val erArbeidstakerAnsattHelePerioden =
            innhold.midlertidigUtsendt.utenlandsoppdraget.isErArbeidstakerAnsattHelePerioden
        return innhold.midlertidigUtsendt.loennOgGodtgjoerelse.let {
            LoennOgGodtgjoerelse(
                norskArbgUtbetalerLoenn = it.isNorskArbgUtbetalerLoenn,
                erArbeidstakerAnsattHelePerioden = erArbeidstakerAnsattHelePerioden,
                utlArbgUtbetalerLoenn = it.isUtlArbgUtbetalerLoenn,
                utlArbTilhoererSammeKonsern = it.isUtlArbTilhorerSammeKonsern,
                bruttoLoennPerMnd = it.loennNorskArbg?.toPlainString(),
                bruttoLoennUtlandPerMnd = it.loennUtlArbg?.toPlainString(),
                mottarNaturalytelser = it.isMottarNaturalytelser,
                samletVerdiNaturalytelser = it.samletVerdiNaturalytelser?.toPlainString(),
                erArbeidsgiveravgiftHelePerioden = it.isBetalerArbeidsgiveravgift,
                erTrukketTrygdeavgift = it.isTrukketTrygdeavgift
            )
        }
    }

    private fun oversettVirksomhetNorge(innhold: Innhold): VirksomhetNorge? {
        val samletVirksomhetINorge = innhold.arbeidsgiver.samletVirksomhetINorge
        return if (samletVirksomhetINorge == null || samletVirksomhetINorge.isEmpty()) null
        else VirksomhetNorge(
            samletVirksomhetINorge.antallAdministrativeAnsatteINorge.toInt(),
            samletVirksomhetINorge.andelOppdragINorge.toInt(),
            samletVirksomhetINorge.andelKontrakterInngaasINorge.toInt(),
            samletVirksomhetINorge.andelOmsetningINorge.toInt(),
            samletVirksomhetINorge.andelRekrutteresINorge.toInt(),
            samletVirksomhetINorge.antallAnsatte.toInt(),
            samletVirksomhetINorge.antallUtsendte.toInt()
        )
    }

    private fun oversettArbeidssituasjon(innhold: Innhold) =
        Arbeidssituasjon(
            innhold.midlertidigUtsendt.isAndreArbeidsgivereIUtsendingsperioden,
            innhold.midlertidigUtsendt.beskrivArbeidSisteMnd,
            innhold.midlertidigUtsendt.beskrivelseAnnetArbeid,
            innhold.midlertidigUtsendt.isSkattepliktig,
            innhold.midlertidigUtsendt.isLoennetArbeidMinstEnMnd,
            innhold.midlertidigUtsendt.isMottaYtelserNorge,
            innhold.midlertidigUtsendt.isMottaYtelserUtlandet
        )

    private fun arbeidstakerHarGittFullmakt(innhold: Innhold): Boolean {
        return java.lang.Boolean.TRUE == innhold.fullmakt.isFullmaktFraArbeidstaker
    }

    private fun erRådgivningsfirmaFullmektig(innhold: Innhold): Boolean {
        return StringUtils.isNotBlank(innhold.fullmakt.fullmektigVirksomhetsnummer)
    }

    private fun hentFullmektigVirksomhetsnummer(innhold: Innhold): String {
        return if (erRådgivningsfirmaFullmektig(innhold)) innhold.fullmakt.fullmektigVirksomhetsnummer
        else innhold.arbeidsgiver.virksomhetsnummer
    }

    private fun hentKontaktVirksomhetsnummer(innhold: Innhold): String {
        return if (erRådgivningsfirmaFullmektig(innhold)) innhold.fullmakt.fullmektigVirksomhetsnummer
        else innhold.arbeidsgiver.virksomhetsnummer
    }

    private fun hentKontaktVirksomhetsnavn(innhold: Innhold): String {
        return if (erRådgivningsfirmaFullmektig(innhold)) innhold.fullmakt.fullmektigVirksomhetsnavn
        else innhold.arbeidsgiver.virksomhetsnavn
    }

    fun avklarKvitteringMottaker(søknad: Soknad): String {
        val innhold = kotlinXmlMapper.readValue(søknad.innhold, MedlemskapArbeidEOSM::class.java).innhold
        return if (arbeidstakerHarGittFullmakt(innhold)) {
            hentFullmektigVirksomhetsnummer(innhold)
        } else {
            innhold.arbeidstaker.foedselsnummer
        }
    }
}

private fun SamletVirksomhetINorge.isEmpty(): Boolean {
    return antallAdministrativeAnsatteINorge == null
            && andelOppdragINorge == null
            && andelKontrakterInngaasINorge == null
            && andelOmsetningINorge == null
            && andelRekrutteresINorge == null
            && antallAnsatte == null
            && antallUtsendte == null
}