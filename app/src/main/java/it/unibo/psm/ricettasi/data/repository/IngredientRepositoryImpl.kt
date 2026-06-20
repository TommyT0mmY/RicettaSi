package it.unibo.psm.ricettasi.data.repository

import it.unibo.psm.ricettasi.data.local.dao.IngredientDao
import it.unibo.psm.ricettasi.data.mapper.toDomain
import it.unibo.psm.ricettasi.data.mapper.toDto
import it.unibo.psm.ricettasi.data.mapper.toEntity
import it.unibo.psm.ricettasi.data.sync.SyncAction
import it.unibo.psm.ricettasi.data.sync.SyncWriter
import it.unibo.psm.ricettasi.domain.model.Ingredient
import it.unibo.psm.ricettasi.domain.repository.IngredientRepository
import it.unibo.psm.ricettasi.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class IngredientRepositoryImpl(
    private val ingredientDao: IngredientDao,
    private val syncWriter: SyncWriter,
    private val session: SessionRepository,
) : IngredientRepository {

    override fun observeAll(): Flow<List<Ingredient>> =
        ingredientDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun search(query: String, limit: Int): List<Ingredient> {
        if (query.isBlank()) return emptyList()
        // FTS4 prefix search: append * for prefix matching ("pomo" -> "pomodoro")
        val ftsQuery = "\"${query.trim().replace("\"", "")}\"*"
        return ingredientDao.search(ftsQuery, limit).map { it.toDomain() }
    }

    override suspend fun getById(id: String): Ingredient? = ingredientDao.getById(id)?.toDomain()

    override suspend fun createPersonal(name: String): Ingredient {
        // The id is generated client-side: the ingredients uuid PK accepts a caller-provided value,
        // so we can reference it from the pantry item right away without waiting for the next sync.
        val ingredient = Ingredient(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            parentIngredientId = null,
            createdByUser = true,
        )
        val dto = ingredient.toDto(session.currentUserId())
        syncWriter.write(SyncAction.UPSERT, dto) { ingredientDao.upsertAll(listOf(ingredient.toEntity())) }
        return ingredient
    }
}
