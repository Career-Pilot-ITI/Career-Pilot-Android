package com.iti.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ExampleEntity::class], // features register their @Entity classes here
    version = 1,
    exportSchema = true,
)
abstract class CareerPilotDatabase : RoomDatabase()
