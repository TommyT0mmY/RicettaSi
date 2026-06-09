package it.unibo.psm.ricettasi.data.mapper

import it.unibo.psm.ricettasi.data.local.entity.PantryItemEntity
import it.unibo.psm.ricettasi.data.remote.dto.PantryItemDto
import it.unibo.psm.ricettasi.domain.model.PantryItem
import java.time.Instant

fun PantryItemDto.toDomain(): PantryItem = PantryItem(
    id = id,
    ingredientId = ingredientId,
    quantity = quantity,
    expiryDate = expiryDate?.toLocalDate(),
    addedDate = addedDate.toInstant(),
    consumed = consumed,
    consumedDate = consumedDate?.toInstant(),
)

/** Payload for Supabase: [userId] comes from the session (auth.uid()). */
fun PantryItem.toDto(userId: String): PantryItemDto = PantryItemDto(
    id = id,
    userId = userId,
    ingredientId = ingredientId,
    quantity = quantity,
    expiryDate = expiryDate?.toString(),
    addedDate = addedDate.toString(),
    consumed = consumed,
    consumedDate = consumedDate?.toString(),
)

fun PantryItemEntity.toDomain(): PantryItem = PantryItem(
    id = id,
    ingredientId = ingredientId,
    quantity = quantity,
    expiryDate = expiryDate?.toLocalDate(),
    addedDate = Instant.ofEpochMilli(addedDate),
    consumed = consumed,
    consumedDate = consumedDate?.let(Instant::ofEpochMilli),
)

fun PantryItem.toEntity(): PantryItemEntity = PantryItemEntity(
    id = id,
    ingredientId = ingredientId,
    quantity = quantity,
    expiryDate = expiryDate?.toString(),
    addedDate = addedDate.toEpochMilli(),
    consumed = consumed,
    consumedDate = consumedDate?.toEpochMilli(),
)
