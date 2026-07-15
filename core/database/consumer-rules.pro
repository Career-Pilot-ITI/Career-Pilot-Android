# Keep all classes in the database package
-keep class com.iti.core.database.** { *; }

# Keep Room database classes
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
