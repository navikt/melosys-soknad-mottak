package no.nav.melosys.soknadmottak.mottak

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.melosys.soknadmottak.config.AltinnConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

private val logger = KotlinLogging.logger { }

@RestController
@RequestMapping("/admin")
class MottakAdminController @Autowired constructor(
    private val mottakService: MottakService,
    private val altinnConfig: AltinnConfig
) {
    @GetMapping("/downloadqueue")
    fun sjekkDownloadQueueStatus(
        @RequestParam(required = false) serviceCode: String?
    ): ResponseEntity<String> {
        val code = serviceCode ?: altinnConfig.downloadQueue.code
        return try {
            mottakService.getDownloadQueueItems(code)
            ResponseEntity.ok("OK")
        } catch (t: Throwable) {
            logger.warn(t) { "Klarte ikke å hente download queue items fra Altinn" }
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("ERROR")
        }
    }
}
