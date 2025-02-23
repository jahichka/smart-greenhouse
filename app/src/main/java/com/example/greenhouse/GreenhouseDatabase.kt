package com.example.greenhouse

import androidx.room.*

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val greenhouseId: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "photos")
data class Photo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val greenhouseId: String,
    val uri: String
)

@Dao
interface GreenhouseDao {
    @Query("SELECT * FROM notes WHERE greenhouseId = :greenhouseId")
    fun getNotes(greenhouseId: String): List<Note>

    @Insert
    fun insertNote(note: Note)

    @Update
    fun updateNote(note: Note)

    @Delete
    fun deleteNote(note: Note) // DELETE function

    @Query("SELECT * FROM photos WHERE greenhouseId = :greenhouseId")
    fun getPhotos(greenhouseId: String): List<Photo>

    @Insert
    fun insertPhoto(photo: Photo)

    @Delete
    fun deletePhoto(photo: Photo)
}

@Database(entities = [Note::class, Photo::class], version = 2)
abstract class GreenhouseDatabase : RoomDatabase() {
    abstract fun greenhouseDao(): GreenhouseDao

    companion object {
        @Volatile
        private var INSTANCE: GreenhouseDatabase? = null

        fun getDatabase(context: android.content.Context): GreenhouseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GreenhouseDatabase::class.java,
                    "greenhouse_database"
                ).fallbackToDestructiveMigration().allowMainThreadQueries().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
