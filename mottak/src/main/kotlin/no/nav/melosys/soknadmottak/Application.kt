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

private fun registrerMetrikker() { Metrikker }
