package com.company.sampleapp.entity

import io.jmix.core.entity.annotation.JmixGeneratedValue
import io.jmix.core.metamodel.annotation.JmixEntity
import io.jmix.data.impl.lazyloading.NotInstantiatedList
import java.util.*
import javax.persistence.*

@JmixEntity
@Table(name = "PARENT_ENTITY", indexes = [
    Index(name = "IDX_PARENT_ENTITY_ONE2ONE_A", columnList = "ONE2ONE_A_ID"),
    Index(name = "IDX_PARENT_ENTITY_ONE2ONE_B", columnList = "ONE2ONE_B_ID")
])
@Entity
open class ParentEntity {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    var id: UUID? = null

    @JoinColumn(name = "ONE2ONE_A_ID")
    @OneToOne(fetch = FetchType.LAZY)
    var one2oneA: EntitySoftDeleteA? = null

    @JoinColumn(name = "ONE2ONE_B_ID")
    @OneToOne(fetch = FetchType.LAZY)
    var one2oneB: EntitySoftDeleteB? = null

    @OneToMany(mappedBy = "parentEntity")
    var collectionB: MutableCollection<EntitySoftDeleteB> = NotInstantiatedList()

    @OneToMany(mappedBy = "parentEntity")
    var collectionA: MutableCollection<EntitySoftDeleteA> = NotInstantiatedList()
}
