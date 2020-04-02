package no.nav.melosys.soknadmottak.soknad

import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Parameter
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "SOKNAD_MOTTAK")
class Soknad(
    @Column(name = "archive_ref", nullable = false)
    var arkivReferanse: String,

    @Column(name = "delivered", nullable = false)
    var levert: Boolean,

    @Column(name = "content", nullable = false)
    var innhold: String,

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "soknad_mottak_seq")
    @GenericGenerator(name = "soknad_mottak_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = arrayOf(Parameter(name = "sequence_name", value = "soknad_mottak_id_seq"))
    )
    var id: Long? = null,

    @Column(name = "soknad_id", nullable = false, updatable = false)
    var soknadID: UUID = UUID.randomUUID()
)