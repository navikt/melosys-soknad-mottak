package no.nav.melosys.soknadmottak

import java.util.*
import javax.persistence.*

@Entity
@Table(name = "SOKNAD_MOTTAK")
class SoknadMottak(
    @Column(name = "archive_ref", nullable = false)
    var arkivReferanse: String,

    @Column(name = "delivered", nullable = false)
    var levert: Boolean,

    @Column(name = "content", nullable = false)
    var innhold: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "soknad_id", nullable = false, updatable = false)
    var soknadID: String = UUID.randomUUID().toString()
)