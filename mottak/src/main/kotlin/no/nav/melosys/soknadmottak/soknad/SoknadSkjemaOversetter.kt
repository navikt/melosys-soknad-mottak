package no.nav.melosys.soknadmottak.soknad

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.melosys.altinn.soknad.*
import no.nav.melosys.soknadmottak.soknad.dokgen.SoknadFelterBuilder
import no.nav.melosys.soknadmottak.soknad.dokgen.modell.*
import no.nav.melosys.soknadmottak.soknad.dokgen.modell.Arbeidsgiver
import no.nav.melosys.soknadmottak.soknad.dokgen.modell.Arbeidssted
import no.nav.melosys.soknadmottak.soknad.dokgen.modell.Arbeidstaker
import no.nav.melosys.soknadmottak.soknad.dokgen.modell.LoennOgGodtgjoerelse
import org.apache.commons.lang3.StringUtils
import javax.xml.datatype.XMLGregorianCalendar

private val kotlinXmlMapper = XmlMapper(JacksonXmlModule().apply {
    setDefaultUseWrapper(false)
}).registerKotlinModule()
    .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)

class SoknadSkjemaOversetter {
    fun tilSøknadFelter(søknad: Soknad): SoknadFelter {
        val innhold = kotlinXmlMapper.readValue(søknad.innhold, MedlemskapArbeidEOSM::class.java).innhold
        val soknadFelterBuilder = SoknadFelterBuilder().apply {
            arbeidsgiver = oversettArbeidsgiver(innhold)
            arbeidstaker = oversettArbeidstaker(innhold)
            kontakperson = oversettKontaktperson(innhold)
            utenlandsoppdrag = oversettUtenlandsoppdrag(innhold)
            arbeidssted = oversettArbeidssted(innhold)
            loennOgGodtgjoerelse = oversettLoennOgGodtgjoerelse(innhold)
            virksomhetNorge = oversettVirksomhetNorge(innhold)
        }

        return soknadFelterBuilder.build()
    }

    private fun oversettLoennOgGodtgjoerelse(innhold: Innhold): LoennOgGodtgjoerelse {
        val lønn = innhold.midlertidigUtsendt.loennOgGodtgjoerelse
        return LoennOgGodtgjoerelse(
            norskArbgUtbetalerLoenn = lønn.isNorskArbgUtbetalerLoenn,
            utlArbgUtbetalerLoenn = lønn.isNorskArbgUtbetalerLoenn,
            bruttoLoennPerMnd = lønn.loennNorskArbg.toPlainString(),
            bruttoLoennUtlandPerMnd = lønn.loennUtlArbg.toPlainString(),
            mottarNaturalytelser = lønn.isUtlArbTilhorerSammeKonsern,
            samletVerdiNaturalytelser = lønn.samletVerdiNaturalytelser.toPlainString(),
            erArbeidsgiveravgiftHelePerioden = lønn.isBetalerArbeidsgiveravgift,
            erTrukketTrygdeavgift = lønn.isTrukketTrygdeavgift
        )
    }

    private fun oversettArbeidsgiver(innhold: Innhold) =
        Arbeidsgiver(
            innhold.arbeidsgiver.virksomhetsnummer,
            innhold.arbeidsgiver.virksomhetsnavn,
            oversettAdresse(innhold.arbeidsgiver.adresse)
        )

    private fun oversettAdresse(adresse: ArbeidsgiverAdresse?): String {
        if (adresse == null) return ""
        return "${adresse.gate}, ${adresse.postkode} ${adresse.poststed} ${adresse.land}"
    }

    private fun oversettArbeidssted(innhold: Innhold): Arbeidssted {
        val arbeidssted = innhold.midlertidigUtsendt.arbeidssted
        //FIXME
        return Arbeidssted(
            arbeidssted.typeArbeidssted,
            "FIXME",
            "FIXME",
            "LAND"
        )
    }

    private fun oversettArbeidstaker(innhold: Innhold): Arbeidstaker {
        val arbeidstaker = innhold.arbeidstaker
        return Arbeidstaker(
            oversettMedfølgendeBarn(innhold),
            arbeidstaker.isReiserMedBarnTilUtlandet,
            arbeidstaker.fulltNavn,
            arbeidstaker.foedselsnummer,
            arbeidstaker.foedeland,
            arbeidstaker.foedested,
            arbeidstaker.utenlandskIDnummer
        )
    }

    private fun oversettMedfølgendeBarn(innhold: Innhold): List<BarnMed> {
        val barn = innhold.arbeidstaker.barn
        if (barn == null || barn.barnet == null) return emptyList()
        return barn.barnet.map { b -> BarnMed(b.foedselsnummer, b.navn) }
    }

    private fun oversettKontaktperson(innhold: Innhold): Kontakperson? {
        val kontaktperson = innhold.arbeidsgiver.kontaktperson ?: return null
        return Kontakperson(
            kontaktperson.kontaktpersonNavn,
            kontaktperson.kontaktpersonTelefon,
            oversettAnsattHos(innhold),
            arbeidstakerHarGittFullmakt(innhold),
            hentKontaktVirksomhetsnummer(innhold),
            hentKontaktVirksomhetsnavn(innhold)
        )
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
            oversettTidsrom(utenlandsoppdraget.periodeUtland),
            utenlandsoppdraget.isErstatterTidligereUtsendte,
            oversettTidsrom(utenlandsoppdraget.samletUtsendingsperiode),
            utenlandsoppdraget.isSendesUtOppdragIUtlandet,
            utenlandsoppdraget.isDrattPaaEgetInitiativ,
            utenlandsoppdraget.isAnsattForOppdragIUtlandet,
            utenlandsoppdraget.isAnsattEtterOppdraget
        )
    }

    private fun oversettTidsrom(tidsrom: Tidsrom?): String {
        if (tidsrom == null) return ""
        val fom = oversettDato(tidsrom.periodeFra)
        val tom = oversettDato(tidsrom.periodeTil)
        return "f.o.m. $fom t.o.m. $tom"
    }

    private fun oversettDato(calendar: XMLGregorianCalendar?): String {
        if (calendar == null) return ""
        return "${calendar.year}-${calendar.month}-${calendar.day}"
    }

    private fun oversettVirksomhetNorge(innhold: Innhold): VirksomhetNorge {
        val samletVirksomhetINorge = innhold.arbeidsgiver.samletVirksomhetINorge
        return VirksomhetNorge(
            innhold.arbeidsgiver.isOffentligVirksomhet,
            samletVirksomhetINorge.antallAdministrativeAnsatteINorge.toInt(),
            samletVirksomhetINorge.andelOppdragINorge.toInt(),
            samletVirksomhetINorge.andelKontrakterInngaasINorge.toInt(),
            samletVirksomhetINorge.andelOmsetningINorge.toInt(),
            samletVirksomhetINorge.andelRekrutteresINorge.toInt(),
            samletVirksomhetINorge.antallAnsatte.toInt(),
            samletVirksomhetINorge.antallUtsendte.toInt()
        )
    }

    private fun arbeidstakerHarGittFullmakt(innhold: Innhold): Boolean {
        return java.lang.Boolean.TRUE == innhold.fullmakt.isFullmaktFraArbeidstaker
    }

    private fun erRådgivningsfirmaFullmektig(innhold: Innhold): Boolean {
        return StringUtils.isNotBlank(innhold.fullmakt.fullmektigVirksomhetsnummer)
    }

    private fun hentKontaktVirksomhetsnummer(innhold: Innhold): String {
        return if (erRådgivningsfirmaFullmektig(innhold)) innhold.fullmakt.fullmektigVirksomhetsnummer
        else innhold.arbeidsgiver.virksomhetsnummer
    }

    private fun hentKontaktVirksomhetsnavn(innhold: Innhold): String {
        return if (erRådgivningsfirmaFullmektig(innhold)) innhold.fullmakt.fullmektigVirksomhetsnavn
        else innhold.arbeidsgiver.virksomhetsnavn
    }
}