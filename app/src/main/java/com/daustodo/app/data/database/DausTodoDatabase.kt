package com.daustodo.app.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.daustodo.app.data.model.Task
import com.daustodo.app.data.model.PomodoroSession

@Database(
    entities = [Task::class, PomodoroSession::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class DausTodoDatabase : RoomDatabase() {
    
    abstract fun taskDao(): TaskDao
    abstract fun pomodoroDao(): PomodoroDao
    
    companion object {
        @Volatile
        private var INSTANCE: DausTodoDatabase? = null
        
        fun getDatabase(context: Context): DausTodoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DausTodoDatabase::class.java,
                    "daus_todo_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}