package com.example.data.database

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "creations")
data class Creation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "image", "video", "music", "chat", "voice", "photo", "logo", "social"
    val title: String,
    val styleOrGenre: String = "",
    val aspectRatioOrMood: String = "",
    val details: String = "",
    val resultUrlOrText: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)

@Entity(tableName = "saved_prompts")
data class SavedPrompt(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String,
    val promptText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface CreationDao {
    @Query("SELECT * FROM creations ORDER BY timestamp DESC")
    fun getAllCreations(): Flow<List<Creation>>

    @Query("SELECT * FROM creations WHERE type = :type ORDER BY timestamp DESC")
    fun getCreationsByType(type: String): Flow<List<Creation>>

    @Query("SELECT * FROM creations WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteCreations(): Flow<List<Creation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreation(creation: Creation)

    @Update
    suspend fun updateCreation(creation: Creation)

    @Delete
    suspend fun deleteCreation(creation: Creation)

    @Query("DELETE FROM creations WHERE id = :id")
    suspend fun deleteCreationById(id: Int)

    // Saved Prompts
    @Query("SELECT * FROM saved_prompts ORDER BY timestamp DESC")
    fun getAllSavedPrompts(): Flow<List<SavedPrompt>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedPrompt(prompt: SavedPrompt)

    @Delete
    suspend fun deleteSavedPrompt(prompt: SavedPrompt)
}

@Database(entities = [Creation::class, SavedPrompt::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun creationDao(): CreationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ai_creator_hub_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
