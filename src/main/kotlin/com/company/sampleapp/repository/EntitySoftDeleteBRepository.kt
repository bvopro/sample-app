package com.company.sampleapp.repository

import com.company.sampleapp.entity.EntitySoftDeleteB
import io.jmix.core.repository.JmixDataRepository
import java.util.UUID

interface EntitySoftDeleteBRepository: JmixDataRepository<EntitySoftDeleteB, UUID>
