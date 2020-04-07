package no.nav.melosys.soknadmottak.soknad

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Parameter
import java.time.Instant
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "SOKNAD_MOTTAK")
class Soknad(
    @Column(name = "arkiv_ref", nullable = false)
    var arkivReferanse: String,

    @Column(name = "levert", nullable = false)
    var levert: Boolean,

    @Column(name = "innhold", nullable = false)
    var innhold: String,

    @Column(name = "lagret_tidspunkt", nullable = false)
    @CreationTimestamp
    var lagretTidspunkt: Instant? = null,

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "soknad_mottak_seq")
    @GenericGenerator(
        name = "soknad_mottak_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = [Parameter(name = "sequence_name", value = "soknad_mottak_id_seq")]
    )
    var id: Long? = null,

    @Column(name = "soknad_id", nullable = false, updatable = false)
    var soknadID: UUID = UUID.randomUUID()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Soknad

        if (soknadID != other.soknadID) return false

        return true
    }

    override fun hashCode(): Int {
        return soknadID.hashCode()
    }
}