package no.nav.melosys.soknadmottak.mottak

import no.altinn.schemas.services.archive.downloadqueue._2012._08.DownloadQueueItemBE
import no.nav.melosys.soknadmottak.config.AltinnConfig
import no.nav.security.token.support.core.api.Protected
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@Protected
@RestController
@RequestMapping("/admin")
class MottakAdminController @Autowired constructor(
    private val mottakService: MottakService,
    private val altinnConfig: AltinnConfig
) {
    @GetMapping("/downloadqueue")
    fun hentDownloadQueueItems(@RequestParam(required = false) serviceCode: String?): List<DownloadQueueItemDto> {
        val code = serviceCode ?: altinnConfig.downloadQueue.code
        return mottakService.getDownloadQueueItems(code).downloadQueueItemBE.map { DownloadQueueItemDto(it) }
    }
}

data class DownloadQueueItemDto(
    val archiveReference: String?,
    val archivedDate: Instant?,
    val reporteeId: String?,
    val reporteeType: String?,
    val serviceCode: String?,
    val serviceEditionCode: Int?
) {
    constructor(item: DownloadQueueItemBE) : this(
        archiveReference = item.archiveReference,
        archivedDate = item.archivedDate?.toGregorianCalendar()?.toInstant(),
        reporteeId = item.reporteeID,
        reporteeType = item.reporteeType?.value(),
        serviceCode = item.serviceCode,
        serviceEditionCode = item.serviceEditionCode
    )
}
