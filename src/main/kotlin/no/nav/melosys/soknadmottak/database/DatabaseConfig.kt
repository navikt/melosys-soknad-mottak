package no.nav.melosys.soknadmottak.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.vault.jdbc.hikaricp.HikariCPVaultUtil
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.Database
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import javax.persistence.EntityManagerFactory
import javax.sql.DataSource

@Profile("!test")
@Configuration
@EnableJpaRepositories(basePackages = ["no.nav.melosys.soknadmottak"])
class DatabaseConfig(private val environment: Environment) {

    private val isProduction: Boolean
        get() {
            val cluster = environment.getProperty("NAIS_CLUSTER_NAME")
            return cluster != null && cluster.equals("prod-fss", ignoreCase = true)
        }

    @Bean
    fun adminDataSource(): DataSource {
        return dataSource("admin")
    }

    @Primary
    @Bean
    fun userDataSource(): DataSource {
        return dataSource("user")
    }

    @Bean
    fun flywayConfig(@Qualifier("adminDataSource") adminDataSource: DataSource): FlywayConfigurationCustomizer =
        FlywayConfigurationCustomizer {
            it.initSql(String.format("SET ROLE \"%s-admin\"", environment.getRequiredProperty("DATABASE_NAME")))
            .dataSource(adminDataSource);
        }


    @Bean
    fun entityManagerFactory(): LocalContainerEntityManagerFactoryBean {
        val entityManagerFactoryBean = LocalContainerEntityManagerFactoryBean()
        entityManagerFactoryBean.setDataSource(userDataSource())
        entityManagerFactoryBean.setPackagesToScan("no.nav.melosys.soknadmottak")

        val vendorAdapter = HibernateJpaVendorAdapter()
        vendorAdapter.setDatabase(Database.POSTGRESQL)
        entityManagerFactoryBean.setJpaVendorAdapter(vendorAdapter)

        return entityManagerFactoryBean
    }

    @Bean(name = ["transactionManager"])
    fun jpaTransactionManager(entityManagerFactory: EntityManagerFactory): PlatformTransactionManager {
        val transactionManager = JpaTransactionManager()
        transactionManager.setEntityManagerFactory(entityManagerFactory)
        return transactionManager
    }

    private fun dataSource(user: String): HikariDataSource {
        val config = HikariConfig()
        config.setJdbcUrl(environment.getProperty("spring.datasource.url"))
        config.setMaximumPoolSize(3)
        config.setMinimumIdle(1)
        val mountPath = if (isProduction) PROD_MOUNT_PATH else PREPROD_MOUNT_PATH
        return HikariCPVaultUtil.createHikariDataSourceWithVaultIntegration(config, mountPath, dbRole(user))
    }

    private fun dbRole(role: String): String {
        val namespace = environment.getProperty("NAIS_NAMESPACE")
        return if (isProduction) {
            arrayOf(DATABASE_NAME, role).joinToString("-")
        } else arrayOf(DATABASE_NAME, namespace, role).joinToString("-")
    }

    companion object {
        private val DATABASE_NAME = "melosys-soknad"
        private val PROD_MOUNT_PATH = "postgresql/prod-fss"
        private val PREPROD_MOUNT_PATH = "postgresql/preprod-fss"
    }
}
