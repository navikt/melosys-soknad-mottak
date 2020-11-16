package no.nav.melosys.soknadmottak.soknad

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class SoknadSkjemaOversetterTest {
    private val søknad = SoknadFactory.lagSoknadFraXmlFil()

    @Test
    fun `mapping arbeidsgiver`() {
        val felter = SoknadSkjemaOversetter.tilSøknadFelter(søknad)

        assertThat(felter.arbeidsgiver.navn).isEqualTo("virksomhetsnavn")
        assertThat(felter.arbeidsgiver.adresse).isEqualTo("gate, 1234 poststed land")
        assertThat(felter.arbeidsgiver.orgnr).isEqualTo("virksomhetsnummer")

        assertThat(felter.virksomhetNorge.ansatte).isEqualTo(99)
        assertThat(felter.virksomhetNorge.administrativtAnsatte).isEqualTo(98)
        assertThat(felter.virksomhetNorge.utsendteArbeidstakere).isEqualTo(97)
        assertThat(felter.virksomhetNorge.andelOpptjent).isEqualTo(96)
        assertThat(felter.virksomhetNorge.andelOppdragskontrakter).isEqualTo(95)
        assertThat(felter.virksomhetNorge.andelOppdrag).isEqualTo(94)
        assertThat(felter.virksomhetNorge.andelRekruttert).isEqualTo(93)
    }

    @Test
    fun `mapping arbeidstaker`() {
        val felter = SoknadSkjemaOversetter.tilSøknadFelter(søknad)

        assertThat(felter.arbeidstaker.barnMed).isNotEmpty()
        assertThat(felter.arbeidstaker.barnMed[0].fnr).isEqualTo("barnFnr")
        assertThat(felter.arbeidstaker.barnMed[0].navn).isEqualTo("barnNavn")
        assertThat(felter.arbeidstaker.erMedBarnUnder18).isTrue()
        assertThat(felter.arbeidstaker.fulltNavn).isEqualTo("Fullt Navn")
        assertThat(felter.arbeidstaker.fnr).isEqualTo("foedselsnummer")
        assertThat(felter.arbeidstaker.foedeland).isEqualTo("foedeland")
        assertThat(felter.arbeidstaker.foedested).isEqualTo("foedested")
        assertThat(felter.arbeidstaker.utenlandskIDnummer).isEqualTo("utenlandskIDnummer")
    }

    @Test
    fun `mapping kontakperson`() {
        val felter = SoknadSkjemaOversetter.tilSøknadFelter(søknad)

        assertThat(felter.kontakperson!!.navn).isEqualTo("kontaktpersonNavn")
        assertThat(felter.kontakperson!!.telefon).isEqualTo("kontaktpersonTelefon")
        assertThat(felter.kontakperson!!.ansattHos).isEqualTo("I et rådgivningsfirma")
        assertThat(felter.kontakperson!!.fullmektigVirksomhetsnummer).isEqualTo("fullmektigVirksomhetsnummer")
        assertThat(felter.kontakperson!!.fullmektigVirksomhetsnavn).isEqualTo("fullmektigVirksomhetsnavn")
        assertThat(felter.kontakperson!!.harFullmakt).isTrue()
    }

    @Test
    fun `mapping arbeidssted arbeidPaaLand`() {
        val soknadFraXmlFil = SoknadFactory.lagSoknadFraXmlFil("søknad_arbeidPaaLand.xml")
        val felter = SoknadSkjemaOversetter.tilSøknadFelter(soknadFraXmlFil)

        assertThat(felter.arbeidssted.type).isEqualTo("arbeidPaaLand")
        val arbeidPaaLand = felter.arbeidssted.arbeidPaaLand
        assertThat(arbeidPaaLand).isNotNull()
        assertThat(arbeidPaaLand!!.fastArbeidssted).isFalse()
        assertThat(arbeidPaaLand.hjemmekontor).isTrue()
        assertThat(arbeidPaaLand.fysiskeArbeidssteder).isNotEmpty()
        assertThat(arbeidPaaLand.fysiskeArbeidssteder[0].firmanavn).isEqualTo("firmanavn")
        assertThat(arbeidPaaLand.fysiskeArbeidssteder[0].gatenavn).isEqualTo("gatenavn")
        assertThat(arbeidPaaLand.fysiskeArbeidssteder[0].by).isEqualTo("by")
        assertThat(arbeidPaaLand.fysiskeArbeidssteder[0].postkode).isEqualTo("postkode")
        assertThat(arbeidPaaLand.fysiskeArbeidssteder[0].region).isEqualTo("region")
        assertThat(arbeidPaaLand.fysiskeArbeidssteder[0].land).isEqualTo("land")
    }

    @Test
    fun `mapping arbeidssted offshoreEnheter`() {
        val felter = SoknadSkjemaOversetter.tilSøknadFelter(søknad)

        assertThat(felter.arbeidssted.type).isEqualTo("offshoreEnheter")
        val offshoreEnheter = felter.arbeidssted.offshoreEnheter!!.offshoreEnheter
        assertThat(offshoreEnheter).isNotEmpty()
        assertThat(offshoreEnheter[0].enhetsNavn).isEqualTo("Askepott")
        assertThat(offshoreEnheter[0].sokkelLand).isEqualTo("Polen")
        assertThat(offshoreEnheter[0].enhetsType).isEqualTo("annenStasjonaerEnhet")
    }

    @Test
    fun `mapping arbeidssted luftfart`() {
        val soknadFraXmlFil = SoknadFactory.lagSoknadFraXmlFil("søknad_luftfart.xml")
        val felter = SoknadSkjemaOversetter.tilSøknadFelter(soknadFraXmlFil)

        assertThat(felter.arbeidssted.type).isEqualTo("luftfart")
        val luftfartBaser = felter.arbeidssted.luftfart!!.luftfartBaser
        assertThat(luftfartBaser).isNotEmpty()
        assertThat(luftfartBaser[0].hjemmebaseNavn).isEqualTo("hjemmebaseNavn")
        assertThat(luftfartBaser[0].hjemmebaseLand).isEqualTo("hjemmebaseLand")
        assertThat(luftfartBaser[0].typeFlyvninger).isEqualTo("nasjonal")
    }

    @Test
    fun `mapping arbeidssted skipsliste`() {
        val soknadFraXmlFil = SoknadFactory.lagSoknadFraXmlFil("søknad_skipListe.xml")
        val felter = SoknadSkjemaOversetter.tilSøknadFelter(soknadFraXmlFil)

        assertThat(felter.arbeidssted.type).isEqualTo("skipListe")
        val skipListe = felter.arbeidssted.skipListe!!.skipListe
        assertThat(skipListe).isNotEmpty()
        assertThat(skipListe[0].skipNavn).isEqualTo("skipNavn")
        assertThat(skipListe[0].fartsomraade).isEqualTo("utenriks")
        assertThat(skipListe[0].flaggland).isEqualTo("flaggland")
        assertThat(skipListe[0].territorialEllerHavnLand).isEqualTo("havnLand")
    }

    @Test
    fun `mapping utenlandsk oppdrag`() {
        val felter = SoknadSkjemaOversetter.tilSøknadFelter(søknad)

        assertThat(felter.utenlandsoppdrag.arbeidsland).isEqualTo("arbeidsland")
        assertThat(felter.utenlandsoppdrag.periode).isEqualTo("f.o.m. 2008-11-15 t.o.m. 2017-5-15")
        assertThat(felter.utenlandsoppdrag.erErstatning).isTrue()
        assertThat(felter.utenlandsoppdrag.samletUtsendingPeriode).isEqualTo("f.o.m. 2013-12-21 t.o.m. 2016-1-1")
        assertThat(felter.utenlandsoppdrag.erSendingForOppdrag).isFalse()
        assertThat(felter.utenlandsoppdrag.erDrattPaaEgetInitiativ).isFalse()
        assertThat(felter.utenlandsoppdrag.erAnsettelseForOpphold).isTrue()
        assertThat(felter.utenlandsoppdrag.erFortsattAnsattEtterOppdrag).isFalse()
    }

    @Test
    fun `mapping lønn og godtgjørelser`() {
        val felter = SoknadSkjemaOversetter.tilSøknadFelter(søknad)

        assertThat(felter.loennOgGodtgjoerelse.norskArbgUtbetalerLoenn).isTrue()
        assertThat(felter.loennOgGodtgjoerelse.utlArbgUtbetalerLoenn).isTrue()
        assertThat(felter.loennOgGodtgjoerelse.bruttoLoennPerMnd).isEqualTo("1000.00")
        assertThat(felter.loennOgGodtgjoerelse.bruttoLoennUtlandPerMnd).isEqualTo("1000.00")
        assertThat(felter.loennOgGodtgjoerelse.mottarNaturalytelser).isTrue()
        assertThat(felter.loennOgGodtgjoerelse.samletVerdiNaturalytelser).isEqualTo("9876.55")
        assertThat(felter.loennOgGodtgjoerelse.erArbeidsgiveravgiftHelePerioden).isTrue()
        assertThat(felter.loennOgGodtgjoerelse.erTrukketTrygdeavgift).isFalse()
    }

    @Test
    fun `mapping arbeidssituasjon`() {
        val felter = SoknadSkjemaOversetter.tilSøknadFelter(søknad)

        assertThat(felter.arbeidssituasjon.loennetArbeidMinstEnMnd).isFalse()
        assertThat(felter.arbeidssituasjon.beskrivArbeidSisteMnd).isEqualTo("Universell konsulent")
        assertThat(felter.arbeidssituasjon.andreArbeidsgivereIUtsendingsperioden).isTrue()
        assertThat(felter.arbeidssituasjon.beskrivelseAnnetArbeid).isEqualTo("Tankeleser")
        assertThat(felter.arbeidssituasjon.erSkattepliktig).isFalse()
        assertThat(felter.arbeidssituasjon.mottaYtelserNorge).isTrue()
        assertThat(felter.arbeidssituasjon.mottaYtelserUtlandet).isTrue()
    }
}