package it.unibo.psm.ricettasi.data.remote.dto

import kotlinx.serialization.Serializable

/** A recipe category recipes can be tagged with. */
@Serializable
data class CategoryDto(
    val id: String,
    val name: String,
) {
    companion object {
        const val TABLE = "categories"
    }
}
