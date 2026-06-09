package it.unibo.psm.ricettasi.data.mapper

import it.unibo.psm.ricettasi.data.local.entity.CategoryCacheEntity
import it.unibo.psm.ricettasi.data.remote.dto.CategoryDto

fun CategoryDto.toEntity(): CategoryCacheEntity = CategoryCacheEntity(id = id, name = name)
