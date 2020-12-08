package no.nav.melosys.soknadmottak.mottak

import no.nav.melosys.soknadmottak.common.SikkerhetsbegrensningException
import no.nav.melosys.soknadmottak.soknad.SoknadService
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

private const val API_KEY_HEADER = "X-MELOSYS-ADMIN-APIKEY"

@Unprotected
@RestController
@RequestMapping("/mottak/admin")
class MottakAdminController @Autowired constructor(
    @Value("\${API_KEY_VALUE:dummy}")
    private val apiKey: String,
    private val mottakService: MottakService,
    private val soknadService: SoknadService
) {

    @GetMapping("/feilede")
    fun hentIkkeLeverteSøknader(@RequestHeader(API_KEY_HEADER) apiKey: String): ResponseEntity<List<UUID>> {
        validerApikey(apiKey)
        return ResponseEntity.ok(soknadService.hentIkkeLeverteSøknader().map { it.soknadID })
    }

    @PostMapping("/restart")
    fun sendIkkeLeverteSøknader(
        @RequestHeader(API_KEY_HEADER) apiKey: String,
        @RequestBody soknadIder: List<UUID>
    ): ResponseEntity<Void> {
        validerApikey(apiKey)
        mottakService.sendIkkeLeverteSøknader(soknadIder)
        return ResponseEntity.ok().build()
    }

    private fun validerApikey(value: String) {
        if (apiKey != value) {
            throw SikkerhetsbegrensningException("Trenger gyldig apikey")
        }
    }
}
