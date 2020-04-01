package no.nav.melosys.soknadmottak.dokument

import no.nav.melosys.soknadmottak.soknad.Soknad
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Parameter
import javax.persistence.*

@Entity
@Table(name = "DOKUMENT")
class Dokument(
    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_soknad")
    var soknad: Soknad,

    @Column(name = "filnavn", nullable = false)
    var filnavn: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "dok_type", nullable = false)
    var type: DokumentType,

    @Lob
    @Column(name = "innhold", nullable = false)
    var innhold: ByteArray,

    @Column(name = "dokument_id", nullable = false, updatable = false)
    var dokumentID: String? = null,

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dokument_seq")
    @GenericGenerator(
        name = "dokument_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = arrayOf(Parameter(name = "sequence_name", value = "dokument_id_seq"))
    )
    var id: Long? = null
)