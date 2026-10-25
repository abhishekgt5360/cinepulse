package com.example.data.local

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey
    val subjectId: String,
    val slug: String,
    val title: String,
    val posterUrl: String?,
    val season: Int,
    val episode: Int,
    val positionMs: Long,
    val durationMs: Long,
    val lastWatchedTimestamp: Long = System.currentTimeMillis(),
    val type: String? = null
)

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey
    val subjectId: String,
    val slug: String,
    val title: String,
    val posterUrl: String?,
    val rating: String?,
    val year: String?,
    val genre: String?,
    val type: String?,
    val addedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "episode_progress", primaryKeys = ["subjectId", "season", "episode"])
data class EpisodeProgressEntity(
    val subjectId: String,
    val season: Int,
    val episode: Int,
    val positionMs: Long,
    val durationMs: Long,
    val updatedAt: Long = System.currentTimeMillis(),
    val finished: Boolean = false
)

@Dao
interface WatchDao {
    @Query("SELECT * FROM watch_history ORDER BY lastWatchedTimestamp DESC")
    fun getAllHistory(): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE subjectId = :subjectId LIMIT 1")
    suspend fun getHistory(subjectId: String): WatchHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHistory(entity: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE subjectId = :subjectId")
    suspend fun deleteHistory(subjectId: String)

    @Query("DELETE FROM watch_history")
    suspend fun clearHistory()

    @Query("SELECT * FROM watchlist ORDER BY addedTimestamp DESC")
    fun getAllWatchlist(): Flow<List<WatchlistEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE subjectId = :subjectId)")
    fun isWatchlisted(subjectId: String): Flow<Boolean>

    @Query("SELECT * FROM watchlist WHERE subjectId = :subjectId LIMIT 1")
    suspend fun getWatchlistItem(subjectId: String): WatchlistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToWatchlist(entity: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE subjectId = :subjectId")
    suspend fun removeFromWatchlist(subjectId: String)

    // Episode Progress
    @Query("SELECT * FROM episode_progress WHERE subjectId = :subjectId AND season = :season AND episode = :episode LIMIT 1")
    suspend fun getEpisodeProgress(subjectId: String, season: Int, episode: Int): EpisodeProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEpisodeProgress(entity: EpisodeProgressEntity)

    @Query("DELETE FROM episode_progress WHERE subjectId = :subjectId AND season = :season AND episode = :episode")
    suspend fun deleteEpisodeProgress(subjectId: String, season: Int, episode: Int)

    @Query("DELETE FROM episode_progress WHERE subjectId = :subjectId")
    suspend fun deleteEpisodeProgressForSubject(subjectId: String)

    @Query("DELETE FROM episode_progress")
    suspend fun clearEpisodeProgress()
}

val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `episode_progress` (
                `subjectId` TEXT NOT NULL,
                `season` INTEGER NOT NULL,
                `episode` INTEGER NOT NULL,
                `positionMs` INTEGER NOT NULL,
                `durationMs` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                `finished` INTEGER NOT NULL,
                PRIMARY KEY(`subjectId`, `season`, `episode`)
            )
            """.trimIndent()
        )
    }
}

@Database(
    entities = [WatchHistoryEntity::class, WatchlistEntity::class, EpisodeProgressEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun watchDao(): WatchDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cinepulse.db"
                ).addMigrations(MIGRATION_1_2).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
