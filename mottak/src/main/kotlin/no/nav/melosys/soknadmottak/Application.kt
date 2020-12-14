package no.nav.melosys.soknadmottak

import no.nav.melosys.soknadmottak.common.Metrikker
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
class Application

fun main(args: Array<String>) {
    registrerMetrikker()
    runApplication<Application>(*args).registerShutdownHook()
}

// Et Kotlin-object blir initialisert første gang det blir aksessert:
// https://kotlinlang.org/docs/reference/object-declarations.html
private fun registrerMetrikker() { Metrikker }
