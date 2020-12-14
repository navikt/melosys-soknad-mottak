package no.nav.melosys.soknadmottak.config

import no.nav.security.token.support.core.api.Unprotected
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@Unprotected
@RestController
class FaviconController {
    @GetMapping("favicon.ico")
    fun returnNoFavicon() {
        // Unngår varsel "No mapping for GET /favicon.ico"
        return
    }
}
