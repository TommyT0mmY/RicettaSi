package it.unibo.psm.ricettasi.data.mapper

import it.unibo.psm.ricettasi.data.local.entity.CategoryEntity
import it.unibo.psm.ricettasi.data.remote.dto.CategoryDto

fun CategoryDto.toEntity(): CategoryEntity = CategoryEntity(id = id, name = name)
