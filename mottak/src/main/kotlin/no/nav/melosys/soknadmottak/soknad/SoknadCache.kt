package no.nav.melosys.soknadmottak.soknad

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class SoknadCache @Autowired constructor(
    private val soknadRepository: SoknadRepository,
    @Value("\${melosys.cache.levetid:300000}")
    private val cacheLevetid: Long
) {
    private var soknadLevert: MutableMap<Boolean, Long> = mutableMapOf()
    private var sistLestTidspunkt: Long = 0L

    fun hentSoknaderMedLevert(levert: Boolean): Double {
        oppfriskCacheHvisUtløpt()

        return soknadLevert[levert]!!.toDouble()
    }

    private fun oppfriskCacheHvisUtløpt() {
        val nå = System.currentTimeMillis()
        val alderMs = nå - sistLestTidspunkt
        if (alderMs >= cacheLevetid) {
            oppfriskCache()
            sistLestTidspunkt = System.currentTimeMillis()
        }
    }

    private fun oppfriskCache() {
        soknadLevert[true] = soknadRepository.hentAntallSoknaderLevert()
        soknadLevert[false] = soknadRepository.hentAntallSoknaderIkkeLevert()
    }
}