package it.unibo.psm.ricettasi.data.repository

import it.unibo.psm.ricettasi.data.local.dao.PantryDao
import it.unibo.psm.ricettasi.data.mapper.toDomain
import it.unibo.psm.ricettasi.data.mapper.toDto
import it.unibo.psm.ricettasi.data.mapper.toEntity
import it.unibo.psm.ricettasi.data.sync.SyncAction
import it.unibo.psm.ricettasi.data.sync.SyncWriter
import it.unibo.psm.ricettasi.domain.model.PantryItem
import it.unibo.psm.ricettasi.domain.repository.PantryRepository
import it.unibo.psm.ricettasi.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

/**
 * UI source of truth = Room. Every write goes through [SyncWriter]: it updates the local table the
 * UI observes and records the same change in the sync queue, in one transaction. The push to
 * Supabase happens later by draining that queue (right away when online), so the online and offline
 * cases share a single write path.
 */
class PantryRepositoryImpl(
    private val pantryDao: PantryDao,
    private val syncWriter: SyncWriter,
    private val session: SessionRepository,
) : PantryRepository {

    override fun observeActive(): Flow<List<PantryItem>> =
        pantryDao.observeActive().map { list -> list.map { it.toDomain() } }

    override fun observeConsumed(): Flow<List<PantryItem>> =
        pantryDao.observeConsumed().map { list -> list.map { it.toDomain() } }

    override suspend fun add(item: PantryItem) {
        val dto = item.toDto(session.currentUserId() ?: return)
        syncWriter.write(SyncAction.UPSERT, dto) { pantryDao.upsert(item.toEntity()) }
    }

    override suspend fun setConsumed(id: String, consumed: Boolean) {
        val current = pantryDao.getById(id)?.toDomain() ?: return
        val userId = session.currentUserId() ?: return
        val updated = current.copy(
            consumed = consumed,
            consumedDate = if (consumed) Instant.now() else null,
        )
        syncWriter.write(SyncAction.UPSERT, updated.toDto(userId)) { pantryDao.upsert(updated.toEntity()) }
    }

    override suspend fun delete(id: String) {
        val item = pantryDao.getById(id)?.toDomain() ?: return
        val dto = item.toDto(session.currentUserId() ?: return)
        syncWriter.write(SyncAction.DELETE, dto) { pantryDao.deleteById(id) }
    }
}
