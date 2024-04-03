package no.nav.melosys.soknadmottak.dokument

import jakarta.persistence.*
import no.nav.melosys.soknadmottak.soknad.Soknad
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Parameter
import java.time.Instant

@Entity
@Table(name = "DOKUMENT")
class Dokument(
    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_soknad")
    var soknad: Soknad,

    @Column(name = "filnavn", nullable = false)
    var filnavn: String,

    @Column(name = "dok_type", nullable = false)
    var type: String,

    @Column(name = "innhold")
    var innhold: ByteArray? = null,

    @Column(name = "lagret_tidspunkt")
    @CreationTimestamp
    var lagretTidspunkt: Instant? = null,

    @Column(name = "dokument_id", nullable = false, updatable = false)
    var dokumentID: String? = null,

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dokument_seq")
    @GenericGenerator(
        name = "dokument_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = [Parameter(name = "sequence_name", value = "dokument_id_seq")]
    )
    var id: Long? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Dokument

        if (dokumentID != other.dokumentID) return false

        return true
    }

    override fun hashCode(): Int {
        return dokumentID?.hashCode() ?: 0
    }
}
