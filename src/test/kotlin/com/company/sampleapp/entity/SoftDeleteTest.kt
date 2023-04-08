@file:OptIn(ExperimentalStdlibApi::class)

package com.company.sampleapp.entity

import com.company.sampleapp.entity.User
import com.company.sampleapp.test_support.AuthenticatedAsAdmin
import io.jmix.core.DataManager
import io.jmix.core.Id
import io.jmix.core.SaveContext
import io.jmix.core.security.UserRepository
import io.jmix.data.PersistenceHints
import liquibase.integration.spring.SpringLiquibase
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.*
import kotlin.jvm.optionals.getOrNull

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin::class)
class SoftDeleteTest {

    @Autowired
    lateinit var dataManager: DataManager

    @Autowired
    lateinit var springLiquibase: SpringLiquibase

    private fun resetLiquibase () {
        springLiquibase.isDropFirst = true
        springLiquibase.afterPropertiesSet()
    }

    private fun createA () {
        val saveContext = SaveContext()
        val parent = dataManager.create(ParentEntity::class.java).apply { id= UUID.fromString("00000000-0000-0000-0000-000000000000") }
        val a1 = dataManager.create(EntitySoftDeleteA::class.java).apply { id= UUID.fromString("00000000-0000-0000-0000-000000000001") }
        val a2 = dataManager.create(EntitySoftDeleteA::class.java).apply {
            id= UUID.fromString("00000000-0000-0000-0000-000000000002")
            parentEntity = parent
        }
        val a3 = dataManager.create(EntitySoftDeleteA::class.java).apply {
            id = UUID.fromString("00000000-0000-0000-0000-000000000003")
            parentEntity = parent
        }
        val a4 = dataManager.create(EntitySoftDeleteA::class.java).apply {
            id = UUID.fromString("00000000-0000-0000-0000-000000000004")
            parentEntity = parent
        }
        parent.one2oneA = a1
        parent.collectionA.add(a2)
        parent.collectionA.add(a3)
        parent.collectionA.add(a4)

        saveContext.saving(parent, a1, a2, a3, a4)
        dataManager.save(saveContext)
    }

    @Test
    fun test_A() {
        fun reloadParent() = dataManager.load(ParentEntity::class.java).id(UUID.fromString("00000000-0000-0000-0000-000000000000")).one()

        resetLiquibase ()
        createA()

        var parent = reloadParent()
        val c1 = dataManager.load(EntitySoftDeleteA::class.java).id(UUID.fromString("00000000-0000-0000-0000-000000000001")).one()
        val c2 = dataManager.load(EntitySoftDeleteA::class.java).id(UUID.fromString("00000000-0000-0000-0000-000000000002")).one()
        val c3 = dataManager.load(EntitySoftDeleteA::class.java).id(UUID.fromString("00000000-0000-0000-0000-000000000003")).one()
        val c4 = dataManager.load(EntitySoftDeleteA::class.java).id(UUID.fromString("00000000-0000-0000-0000-000000000004")).one()

        // check initial creation
        Assertions.assertThat(parent.one2oneA).isEqualTo(c1)
        Assertions.assertThat(parent.collectionA.size).isEqualTo(3)
        Assertions.assertThat(parent.collectionA).contains(c2)
        Assertions.assertThat(parent.collectionA).contains(c3)
        Assertions.assertThat(parent.collectionA).contains(c4)

        //
        // check remove/soft delete behavior

        // remove one2one relation => soft delete must keep the relation (not filtered)
        dataManager.remove(c1)
        Assertions.assertThat(parent.one2oneA).isEqualTo(c1)
        parent = reloadParent()
        Assertions.assertThat(parent.one2oneA).isNull() // WHY ?

        // remove one2many relation => soft delete must (filtered)
        dataManager.remove(c2)
        Assertions.assertThat(parent.collectionA).doesNotContain(c2) // Why c2 is already removed here
        parent = reloadParent()
        Assertions.assertThat(parent.collectionA).doesNotContain(c2)
        Assertions.assertThat(parent.collectionA.size).isEqualTo(2)

        dataManager.remove(c3)
        Assertions.assertThat(parent.collectionA).contains(c3) // Why c3 is not already removed here ?
        parent = reloadParent()
        Assertions.assertThat(parent.collectionA).doesNotContain(c3)
        Assertions.assertThat(parent.collectionA.size).isEqualTo(1)

        dataManager.remove(c4)
        Assertions.assertThat(parent.collectionA).contains(c4)
        parent = reloadParent()
        Assertions.assertThat(parent.collectionA).doesNotContain(c4)
        Assertions.assertThat(parent.collectionA.size).isEqualTo(0)

        // All entities look like removed
        Assertions.assertThat(dataManager.load(EntitySoftDeleteA::class.java).all().list().size).isEqualTo(0)
        Assertions.assertThat(dataManager.load(Id.of(c1)).optional().getOrNull()).isNull()
        Assertions.assertThat(dataManager.load(Id.of(c2)).optional().getOrNull()).isNull()
        Assertions.assertThat(dataManager.load(Id.of(c3)).optional().getOrNull()).isNull()
        Assertions.assertThat(dataManager.load(Id.of(c4)).optional().getOrNull()).isNull()

        // But are in the database, only deactivated
        Assertions.assertThat(dataManager.load(Id.of(c1)).hint(PersistenceHints.SOFT_DELETION, false).one()).isNotNull
        Assertions.assertThat(dataManager.load(Id.of(c2)).hint(PersistenceHints.SOFT_DELETION, false).one()).isNotNull
        Assertions.assertThat(dataManager.load(Id.of(c3)).hint(PersistenceHints.SOFT_DELETION, false).one()).isNotNull
        Assertions.assertThat(dataManager.load(Id.of(c4)).hint(PersistenceHints.SOFT_DELETION, false).one()).isNotNull
    }


    private fun createB () {
        val saveContext = SaveContext()
        val parent = dataManager.create(ParentEntity::class.java).apply { id= UUID.fromString("00000000-0000-0000-0000-000000000000") }
        val b1 = dataManager.create(EntitySoftDeleteB::class.java).apply { id= UUID.fromString("00000000-0000-0000-0000-000000000001") }
        val b2 = dataManager.create(EntitySoftDeleteB::class.java).apply {
            id= UUID.fromString("00000000-0000-0000-0000-000000000002")
            parentEntity = parent
        }
        val b3 = dataManager.create(EntitySoftDeleteB::class.java).apply {
            id = UUID.fromString("00000000-0000-0000-0000-000000000003")
            parentEntity = parent
        }
        val b4 = dataManager.create(EntitySoftDeleteB::class.java).apply {
            id = UUID.fromString("00000000-0000-0000-0000-000000000004")
            parentEntity = parent
        }
        parent.one2oneB = b1
        parent.collectionB.add(b2)
        parent.collectionB.add(b3)
        parent.collectionB.add(b4)

        saveContext.saving(parent, b1, b2, b3, b4)
        dataManager.save(saveContext)
    }

    @Test
    fun test_B() {
        fun reloadParent() = dataManager.load(ParentEntity::class.java).id(UUID.fromString("00000000-0000-0000-0000-000000000000")).one()

        resetLiquibase ()
        createB()

        var parent = reloadParent()
        val c1 = dataManager.load(EntitySoftDeleteB::class.java).id(UUID.fromString("00000000-0000-0000-0000-000000000001")).one()
        val c2 = dataManager.load(EntitySoftDeleteB::class.java).id(UUID.fromString("00000000-0000-0000-0000-000000000002")).one()
        val c3 = dataManager.load(EntitySoftDeleteB::class.java).id(UUID.fromString("00000000-0000-0000-0000-000000000003")).one()
        val c4 = dataManager.load(EntitySoftDeleteB::class.java).id(UUID.fromString("00000000-0000-0000-0000-000000000004")).one()

        // check initial creation
        Assertions.assertThat(parent.one2oneB).isEqualTo(c1)
        Assertions.assertThat(parent.collectionB.size).isEqualTo(3)
        Assertions.assertThat(parent.collectionB).contains(c2)
        Assertions.assertThat(parent.collectionB).contains(c3)
        Assertions.assertThat(parent.collectionB).contains(c4)

        //
        // check remove/soft delete behavior

        // remove one2one relation => soft delete must keep the relation
        dataManager.remove(c1)
        Assertions.assertThat(parent.one2oneB).isEqualTo(c1)
        parent = reloadParent()
        Assertions.assertThat(parent.one2oneB).isNull() // WHY ?

        // remove one2many relation => soft delete must (filtered)
        dataManager.remove(c2)
        Assertions.assertThat(parent.collectionB).doesNotContain(c2) // Why c2 is already removed here
        parent = reloadParent()
        Assertions.assertThat(parent.collectionB).doesNotContain(c2)
        Assertions.assertThat(parent.collectionB.size).isEqualTo(2)

        dataManager.remove(c3)
        Assertions.assertThat(parent.collectionB).contains(c3) // Why c3 is not already removed here ?
        parent = reloadParent()
        Assertions.assertThat(parent.collectionB).doesNotContain(c3)
        Assertions.assertThat(parent.collectionB.size).isEqualTo(1)

        dataManager.remove(c4)
        Assertions.assertThat(parent.collectionB).contains(c4)
        parent = reloadParent()
        Assertions.assertThat(parent.collectionB).doesNotContain(c4)
        Assertions.assertThat(parent.collectionB.size).isEqualTo(0)

        // All entities look like removed
        Assertions.assertThat(dataManager.load(EntitySoftDeleteB::class.java).all().list().size).isEqualTo(0)
        Assertions.assertThat(dataManager.load(Id.of(c1)).optional().getOrNull()).isNull()
        Assertions.assertThat(dataManager.load(Id.of(c2)).optional().getOrNull()).isNull()
        Assertions.assertThat(dataManager.load(Id.of(c3)).optional().getOrNull()).isNull()
        Assertions.assertThat(dataManager.load(Id.of(c4)).optional().getOrNull()).isNull()

        // But are in the database, only deactivated
        Assertions.assertThat(dataManager.load(Id.of(c1)).hint(PersistenceHints.SOFT_DELETION, false).one()).isNotNull
        Assertions.assertThat(dataManager.load(Id.of(c2)).hint(PersistenceHints.SOFT_DELETION, false).one()).isNotNull
        Assertions.assertThat(dataManager.load(Id.of(c3)).hint(PersistenceHints.SOFT_DELETION, false).one()).isNotNull
        Assertions.assertThat(dataManager.load(Id.of(c4)).hint(PersistenceHints.SOFT_DELETION, false).one()).isNotNull
    }
}
