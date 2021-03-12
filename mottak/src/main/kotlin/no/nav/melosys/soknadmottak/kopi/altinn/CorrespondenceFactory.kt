package no.nav.melosys.soknadmottak.kopi.altinn

import no.altinn.schemas.services.serviceengine.correspondence._2010._10.AttachmentsV2
import no.altinn.schemas.services.serviceengine.correspondence._2010._10.ExternalContentV2
import no.altinn.schemas.services.serviceengine.correspondence._2010._10.InsertCorrespondenceV2
import no.altinn.services.serviceengine.reporteeelementlist._2010._10.BinaryAttachmentExternalBEV2List
import no.altinn.services.serviceengine.reporteeelementlist._2010._10.BinaryAttachmentV2
import java.time.LocalDateTime
import javax.xml.datatype.DatatypeFactory
import javax.xml.datatype.XMLGregorianCalendar

object CorrespondenceFactory {
    private const val SPRÅK_KODE = "1044" // Bokmål
    private const val FIL_EXT = ".pdf"

    fun insertCorrespondence(
        tjenesteKode: String,
        tjenesteutgaveKode: String,
        mottakerID: String,
        arkivRef: String,
        avsender: String,
        melding: Melding,
        varighetÅr: Long
    ): InsertCorrespondenceV2 {
        return InsertCorrespondenceV2()
            .withServiceCode(tjenesteKode)
            .withServiceEdition(tjenesteutgaveKode)
            .withReportee(mottakerID)
            .withVisibleDateTime(tilXMLCalendar(LocalDateTime.now()))
            .withAllowSystemDeleteDateTime(tilXMLCalendar(LocalDateTime.now().plusYears(varighetÅr)))
            .withArchiveReference(arkivRef)
            .withAllowForwarding(true)
            .withMessageSender(avsender)
            .withIsReservable(true)
            .withContent(
                ExternalContentV2()
                    .withLanguageCode(SPRÅK_KODE)
                    .withMessageTitle(melding.tittel)
                    .withMessageBody(melding.tekst)
                    .withAttachments(
                        AttachmentsV2()
                            .withBinaryAttachments(
                                BinaryAttachmentExternalBEV2List()
                                    .withBinaryAttachmentV2(
                                        BinaryAttachmentV2()
                                            .withData(melding.vedlegg.fil)
                                            .withFileName(
                                                StringBuilder()
                                                    .append(melding.vedlegg.tittel)
                                                    .append(FIL_EXT)
                                                    .toString()
                                            )
                                            .withName(melding.vedlegg.tittel)
                                    )
                            )
                    )
            )
    }

    private fun tilXMLCalendar(localDateTime: LocalDateTime): XMLGregorianCalendar {
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(localDateTime.toString())
    }
}
