package com.company.sampleapp.entity

import io.jmix.core.entity.annotation.JmixGeneratedValue
import io.jmix.core.metamodel.annotation.JmixEntity
import java.util.*
import javax.persistence.*

@JmixEntity
@Table(name = "ENTITY_SOFT_DELETE_A", indexes = [
    Index(name = "IDX_ENTITY_SOFT_DELETE_A_PARENT_ENTITY", columnList = "PARENT_ENTITY_ID")
])
@Entity
open class EntitySoftDeleteA : SuperSoftDelete() {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    var id: UUID? = null

    @JoinColumn(name = "PARENT_ENTITY_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    var parentEntity: ParentEntity? = null
}
