package com.iti.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Example only — Room requires at least one entity to compile.
 * Delete this once the first real entity is added.
 */
@Entity(tableName = "example_entity")
data class ExampleEntity(
    @PrimaryKey val id: String
)
