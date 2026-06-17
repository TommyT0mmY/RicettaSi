package it.unibo.psm.ricettasi.data.local

import androidx.room.TypeConverter
import it.unibo.psm.ricettasi.data.remote.dto.SyncPayload
import it.unibo.psm.ricettasi.data.sync.SyncAction
import it.unibo.psm.ricettasi.domain.model.Difficulty
import it.unibo.psm.ricettasi.domain.model.MealType
import kotlinx.serialization.json.Json

object Converters {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = false }

    @TypeConverter
    fun fromDifficulty(v: Difficulty): String = v.value

    @TypeConverter
    fun toDifficulty(v: String): Difficulty = Difficulty.fromValue(v)

    @TypeConverter
    fun fromMealType(v: MealType): String = v.value

    @TypeConverter
    fun toMealType(v: String): MealType = MealType.fromValue(v) ?: MealType.PRANZO

    @TypeConverter
    fun fromSyncAction(v: SyncAction): String = v.name

    @TypeConverter
    fun toSyncAction(v: String): SyncAction = SyncAction.valueOf(v)

    @TypeConverter
    fun fromSyncPayload(v: SyncPayload): String = json.encodeToString(v)

    @TypeConverter
    fun toSyncPayload(v: String): SyncPayload = json.decodeFromString(v)
}
