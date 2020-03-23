package no.nav.melosys.soknadmottak

import java.util.*
import javax.persistence.*

@Entity
@Table(name = "SOKNAD_MOTTAK")
class Soknad(
    @Column(name = "archive_ref", nullable = false)
    var archiveReference: String,

    @Column(nullable = false)
    var delivered: Boolean,

    @Column(nullable = false)
    var content: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "soknad_id", nullable = false, updatable = false)
    var soknadID: String = UUID.randomUUID().toString()
)