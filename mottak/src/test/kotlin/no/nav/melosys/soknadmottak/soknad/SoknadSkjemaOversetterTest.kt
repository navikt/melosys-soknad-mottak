package no.nav.melosys.soknadmottak.soknad

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class SoknadSkjemaOversetterTest {
    private var søknad = SoknadFactory.lagSoknadFraXmlFil()
    private var søknadsdata = SoknadSkjemaOversetter.tilSøknadsdata(søknad)

    @Test
    fun `mapping tidspunktMottatt `() {
        assertThat(søknadsdata.tidspunktMottatt).isEqualTo(søknad.innsendtTidspunkt.toString())
    }

    @Test
    fun `mapping arbeidsgiver`() {
        assertThat(søknadsdata.arbeidsgiver.navn).isEqualTo("virksomhetsnavn")
        assertThat(søknadsdata.arbeidsgiver.adresse!!.gate).isEqualTo("gate")
        assertThat(søknadsdata.arbeidsgiver.adresse!!.postkode).isEqualTo("1234")
        assertThat(søknadsdata.arbeidsgiver.adresse!!.poststed).isEqualTo("poststed")
        assertThat(søknadsdata.arbeidsgiver.adresse!!.land).isEqualTo("land")
        assertThat(søknadsdata.arbeidsgiver.orgnr).isEqualTo("virksomhetsnummer")
        assertThat(søknadsdata.arbeidsgiver.erOffenlig).isFalse()
    }

    @Test
    fun `mapping samlet virksomhet i Norge`() {
        assertThat(søknadsdata.virksomhetNorge!!.ansatte).isEqualTo(99)
        assertThat(søknadsdata.virksomhetNorge!!.administrativtAnsatte).isEqualTo(98)
        assertThat(søknadsdata.virksomhetNorge!!.utsendteArbeidstakere).isEqualTo(97)
        assertThat(søknadsdata.virksomhetNorge!!.andelOpptjent).isEqualTo(96)
        assertThat(søknadsdata.virksomhetNorge!!.andelOppdragskontrakter).isEqualTo(95)
        assertThat(søknadsdata.virksomhetNorge!!.andelOppdrag).isEqualTo(94)
        assertThat(søknadsdata.virksomhetNorge!!.andelRekruttert).isEqualTo(93)
    }

    @Test
    fun `mapping arbeidstaker`() {
        assertThat(søknadsdata.arbeidstaker.barnMed).isNotEmpty()
        assertThat(søknadsdata.arbeidstaker.barnMed[0].fnr).isEqualTo("barnFnr")
        assertThat(søknadsdata.arbeidstaker.barnMed[0].navn).isEqualTo("barnNavn")
        assertThat(søknadsdata.arbeidstaker.erMedBarnUnder18).isTrue()
        assertThat(søknadsdata.arbeidstaker.fulltNavn).isEqualTo("Fullt Navn")
        assertThat(søknadsdata.arbeidstaker.fnr).isEqualTo("foedselsnummer")
        assertThat(søknadsdata.arbeidstaker.foedeland).isEqualTo("foedeland")
        assertThat(søknadsdata.arbeidstaker.foedested).isEqualTo("foedested")
        assertThat(søknadsdata.arbeidstaker.utenlandskIDnummer).isEqualTo("utenlandskIDnummer")
    }

    @Test
    fun `mapping kontakperson`() {
        assertThat(søknadsdata.kontakperson!!.navn).isEqualTo("kontaktpersonNavn")
        assertThat(søknadsdata.kontakperson!!.telefon).isEqualTo("kontaktpersonTelefon")
        assertThat(søknadsdata.kontakperson!!.ansattHos).isEqualTo("I et rådgivningsfirma")
        assertThat(søknadsdata.kontakperson!!.fullmektigVirksomhetsnummer).isEqualTo("fullmektigVirksomhetsnummer")
        assertThat(søknadsdata.kontakperson!!.fullmektigVirksomhetsnavn).isEqualTo("fullmektigVirksomhetsnavn")
        assertThat(søknadsdata.kontakperson!!.harFullmakt).isTrue()
    }

    @Test
    fun `mapping arbeidssted arbeidPaaLand`() {
        val soknadFraXmlFil = SoknadFactory.lagSoknadFraXmlFil("søknad_arbeidPaaLand.xml")
        val søknadsdata = SoknadSkjemaOversetter.tilSøknadsdata(soknadFraXmlFil)

        assertThat(søknadsdata.arbeidssted.type).isEqualTo("arbeidPaaLand")
        val arbeidPaaLand = søknadsdata.arbeidssted.arbeidPaaLand
        assertThat(arbeidPaaLand).isNotNull()
        assertThat(arbeidPaaLand!!.fastArbeidssted).isFalse()
        assertThat(arbeidPaaLand.hjemmekontor).isTrue()
        assertThat(arbeidPaaLand.fysiskeArbeidssteder).isNotEmpty()
        assertThat(arbeidPaaLand.fysiskeArbeidssteder!![0].firmanavn).isEqualTo("firmanavn")
        assertThat(arbeidPaaLand.fysiskeArbeidssteder!![0].gatenavn).isEqualTo("gatenavn")
        assertThat(arbeidPaaLand.fysiskeArbeidssteder!![0].by).isEqualTo("by")
        assertThat(arbeidPaaLand.fysiskeArbeidssteder!![0].postkode).isEqualTo("postkode")
        assertThat(arbeidPaaLand.fysiskeArbeidssteder!![0].region).isEqualTo("region")
        assertThat(arbeidPaaLand.fysiskeArbeidssteder!![0].land).isEqualTo("land")
    }

    @Test
    fun `mapping arbeidssted offshoreEnheter`() {
        assertThat(søknadsdata.arbeidssted.type).isEqualTo("offshoreEnheter")
        val offshoreEnheter = søknadsdata.arbeidssted.offshoreEnheter!!.offshoreEnheter
        assertThat(offshoreEnheter).isNotEmpty()
        assertThat(offshoreEnheter[0].enhetsNavn).isEqualTo("Askepott")
        assertThat(offshoreEnheter[0].sokkelLand).isEqualTo("Polen")
        assertThat(offshoreEnheter[0].enhetsType).isEqualTo("annenStasjonaerEnhet")
    }

    @Test
    fun `mapping arbeidssted luftfart`() {
        val soknadFraXmlFil = SoknadFactory.lagSoknadFraXmlFil("søknad_luftfart.xml")
        val søknadsdata = SoknadSkjemaOversetter.tilSøknadsdata(soknadFraXmlFil)

        assertThat(søknadsdata.arbeidssted.type).isEqualTo("luftfart")
        val luftfartBaser = søknadsdata.arbeidssted.luftfart!!.luftfartBaser
        assertThat(luftfartBaser!!).isNotEmpty()
        assertThat(luftfartBaser[0].hjemmebaseNavn).isEqualTo("hjemmebaseNavn")
        assertThat(luftfartBaser[0].hjemmebaseLand).isEqualTo("hjemmebaseLand")
        assertThat(luftfartBaser[0].typeFlyvninger).isEqualTo("nasjonal")
    }

    @Test
    fun `mapping arbeidssted skipsliste`() {
        val soknadFraXmlFil = SoknadFactory.lagSoknadFraXmlFil("søknad_skipListe.xml")
        val søknadsdata = SoknadSkjemaOversetter.tilSøknadsdata(soknadFraXmlFil)

        assertThat(søknadsdata.arbeidssted.type).isEqualTo("skipListe")
        val skipListe = søknadsdata.arbeidssted.skipListe!!.skipListe
        assertThat(skipListe).isNotEmpty()
        assertThat(skipListe[0].skipNavn).isEqualTo("skipNavn")
        assertThat(skipListe[0].fartsomraade).isEqualTo("utenriks")
        assertThat(skipListe[0].flaggland).isEqualTo("flaggland")
        assertThat(skipListe[0].territorialEllerHavnLand).isEqualTo("havnLand")
    }

    @Test
    fun `mapping utenlandsk oppdrag`() {
        assertThat(søknadsdata.utenlandsoppdrag.arbeidsland).isEqualTo("arbeidsland")
        assertThat(søknadsdata.utenlandsoppdrag.periode.fom).isEqualTo("15-11-2008")
        assertThat(søknadsdata.utenlandsoppdrag.periode.tom).isEqualTo("15-5-2017")
        assertThat(søknadsdata.utenlandsoppdrag.erErstatning).isTrue()
        assertThat(søknadsdata.utenlandsoppdrag.samletUtsendingPeriode!!.fom).isEqualTo("21-12-2013")
        assertThat(søknadsdata.utenlandsoppdrag.samletUtsendingPeriode!!.tom).isEqualTo("1-1-2016")
        assertThat(søknadsdata.utenlandsoppdrag.erSendingForOppdrag).isFalse()
        assertThat(søknadsdata.utenlandsoppdrag.erDrattPaaEgetInitiativ).isFalse()
        assertThat(søknadsdata.utenlandsoppdrag.erAnsettelseForOpphold).isTrue()
        assertThat(søknadsdata.utenlandsoppdrag.erFortsattAnsattEtterOppdrag).isFalse()
    }

    @Test
    fun `mapping lønn og godtgjørelser`() {
        assertThat(søknadsdata.loennOgGodtgjoerelse.norskArbgUtbetalerLoenn).isFalse()
        assertThat(søknadsdata.loennOgGodtgjoerelse.erArbeidstakerAnsattHelePerioden).isTrue()
        assertThat(søknadsdata.loennOgGodtgjoerelse.utlArbgUtbetalerLoenn).isTrue()
        assertThat(søknadsdata.loennOgGodtgjoerelse.utlArbTilhoererSammeKonsern).isFalse()
        assertThat(søknadsdata.loennOgGodtgjoerelse.bruttoLoennPerMnd).isEqualTo("1000.00")
        assertThat(søknadsdata.loennOgGodtgjoerelse.bruttoLoennUtlandPerMnd).isEqualTo("1000.00")
        assertThat(søknadsdata.loennOgGodtgjoerelse.mottarNaturalytelser).isFalse()
        assertThat(søknadsdata.loennOgGodtgjoerelse.samletVerdiNaturalytelser).isEqualTo("9876.55")
        assertThat(søknadsdata.loennOgGodtgjoerelse.erArbeidsgiveravgiftHelePerioden).isTrue()
        assertThat(søknadsdata.loennOgGodtgjoerelse.erTrukketTrygdeavgift).isFalse()
    }

    @Test
    fun `mapping utenlandsk virksomhet`() {
        assertThat(søknadsdata.utenlandskVirksomhet!!.navn).isEqualTo("Virskomheten i utlandet")
        assertThat(søknadsdata.utenlandskVirksomhet!!.registreringsnummer).isEqualTo("XYZ123456789")
        assertThat(søknadsdata.utenlandskVirksomhet!!.adresse.gate).isEqualTo("gatenavn med mer")
        assertThat(søknadsdata.utenlandskVirksomhet!!.adresse.by).isEqualTo("testbyen")
        assertThat(søknadsdata.utenlandskVirksomhet!!.adresse.postkode).isEqualTo("UTLAND-1234")
        assertThat(søknadsdata.utenlandskVirksomhet!!.adresse.region).isEqualTo("testregion")
        assertThat(søknadsdata.utenlandskVirksomhet!!.adresse.land).isEqualTo("BELGIA")
    }

    @Test
    fun `mapping arbeidssituasjon`() {
        assertThat(søknadsdata.arbeidssituasjon.loennetArbeidMinstEnMnd).isFalse()
        assertThat(søknadsdata.arbeidssituasjon.beskrivArbeidSisteMnd).isEqualTo("Universell konsulent")
        assertThat(søknadsdata.arbeidssituasjon.andreArbeidsgivereIUtsendingsperioden).isTrue()
        assertThat(søknadsdata.arbeidssituasjon.beskrivelseAnnetArbeid).isEqualTo("Tankeleser")
        assertThat(søknadsdata.arbeidssituasjon.erSkattepliktig).isFalse()
        assertThat(søknadsdata.arbeidssituasjon.mottaYtelserNorge).isTrue()
        assertThat(søknadsdata.arbeidssituasjon.mottaYtelserUtlandet).isTrue()
    }

    @Test
    fun `offentlig virksomhet har ikke virksomhet i Norge fyllt ut`() {
        val soknadFraXmlFil = SoknadFactory.lagSoknadFraXmlFil("søknad_offentlig_virksomhet.xml")
        val søknadsdata = SoknadSkjemaOversetter.tilSøknadsdata(soknadFraXmlFil)

        assertThat(søknadsdata.virksomhetNorge).isNull()
    }

    @Test
    fun `avklar hvem skal motta kvittering`() {
        val mottaker = SoknadSkjemaOversetter.avklarKvitteringMottaker(søknad)
        assertThat(mottaker).isEqualTo("fullmektigVirksomhetsnummer")
    }
}
