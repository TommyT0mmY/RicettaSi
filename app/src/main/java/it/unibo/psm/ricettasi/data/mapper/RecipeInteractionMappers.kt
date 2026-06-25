package it.unibo.psm.ricettasi.data.mapper

import it.unibo.psm.ricettasi.data.local.entity.CookedRecipeEntity
import it.unibo.psm.ricettasi.data.local.entity.FavoriteEntity
import it.unibo.psm.ricettasi.data.remote.dto.CookedRecipeDto
import it.unibo.psm.ricettasi.data.remote.dto.FavoriteDto
import java.time.Instant

// ---------- Favorite: DTO <-> Entity (nessun domain model intermedio) ----------
fun FavoriteDto.toEntity(): FavoriteEntity =
    FavoriteEntity(recipeId = recipeId, savedDate = savedDate.toInstant().toEpochMilli())

fun FavoriteEntity.toDto(userId: String): FavoriteDto =
    FavoriteDto(userId = userId, recipeId = recipeId, savedDate = Instant.ofEpochMilli(savedDate).toString())

// ---------- Cooked recipe: DTO <-> Entity ----------
fun CookedRecipeDto.toEntity(): CookedRecipeEntity =
    CookedRecipeEntity(id = id, recipeId = recipeId, cookedDate = cookedDate.toInstant().toEpochMilli())

fun CookedRecipeEntity.toDto(userId: String): CookedRecipeDto =
    CookedRecipeDto(id = id, userId = userId, recipeId = recipeId, cookedDate = Instant.ofEpochMilli(cookedDate).toString())
