package com.company.sampleapp.entity

import io.jmix.core.annotation.DeletedBy
import io.jmix.core.annotation.DeletedDate
import io.jmix.core.entity.annotation.JmixGeneratedValue
import io.jmix.core.metamodel.annotation.JmixEntity
import java.util.*
import javax.persistence.*

@JmixEntity
@Table(name = "ENTITY_SOFT_DELETE_B", indexes = [
    Index(name = "IDX_ENTITY_SOFT_DELETE_B_PARENT_ENTITY", columnList = "PARENT_ENTITY_ID")
])
@Entity
open class EntitySoftDeleteB {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    var id: UUID? = null

    @DeletedBy
    @Column(name = "DELETED_BY")
    var deletedBy: String? = null

    @DeletedDate
    @Column(name = "DELETED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    var deletedDate: Date? = null

    @JoinColumn(name = "PARENT_ENTITY_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    var parentEntity: ParentEntity? = null
}
