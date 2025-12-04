package no.nav.melosys.soknadmottak

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class FlywayMigrationTest @Autowired constructor(
    private val flyway: Flyway
) {

    @Test
    fun `verify flyway migrations have been applied successfully`() {
        val info = flyway.info()
        val appliedMigrations = info.applied()

        // Verify migrations were applied
        assertThat(appliedMigrations).isNotEmpty
        assertThat(appliedMigrations).hasSize(3)

        // Verify all migrations succeeded
        appliedMigrations.forEach { migration ->
            assertThat(migration.state.isApplied).isTrue()
        }

        // Verify specific versions
        val versions = appliedMigrations.map { it.version.version }
        assertThat(versions).containsExactly("1.01", "1.02", "1.03")
    }

    @Test
    fun `verify no pending migrations`() {
        val info = flyway.info()
        val pending = info.pending()

        assertThat(pending).isEmpty()
    }

    @Test
    fun `verify flyway validation passes`() {
        // This will throw an exception if validation fails
        assertThatCode { flyway.validate() }.doesNotThrowAnyException()
    }
}
