package com.company.sampleapp.repository

import com.company.sampleapp.entity.EntitySoftDeleteA
import io.jmix.core.repository.JmixDataRepository
import java.util.UUID

interface EntitySoftDeleteARepository: JmixDataRepository<EntitySoftDeleteA, UUID>
