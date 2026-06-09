package it.unibo.psm.ricettasi.data.mapper

import it.unibo.psm.ricettasi.data.local.entity.IngredientCacheEntity
import it.unibo.psm.ricettasi.data.remote.dto.IngredientDto
import it.unibo.psm.ricettasi.domain.model.Ingredient

fun IngredientDto.toDomain(): Ingredient = Ingredient(
    id = id,
    name = name,
    parentIngredientId = parentIngredientId,
    createdByUser = createdByUser,
)

fun IngredientDto.toEntity(): IngredientCacheEntity = IngredientCacheEntity(
    id = id,
    name = name,
    parentIngredientId = parentIngredientId,
    isGlobal = !createdByUser,
)

fun IngredientCacheEntity.toDomain(): Ingredient = Ingredient(
    id = id,
    name = name,
    parentIngredientId = parentIngredientId,
    createdByUser = !isGlobal,
)

fun Ingredient.toEntity(): IngredientCacheEntity = IngredientCacheEntity(
    id = id,
    name = name,
    parentIngredientId = parentIngredientId,
    isGlobal = !createdByUser,
)

/** Payload for inserting a personal ingredient coined by the user. */
fun Ingredient.toDto(userId: String?): IngredientDto = IngredientDto(
    id = id,
    name = name,
    parentIngredientId = parentIngredientId,
    createdByUser = createdByUser,
    userId = userId,
)
