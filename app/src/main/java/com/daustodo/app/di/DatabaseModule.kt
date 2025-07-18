package com.daustodo.app.di

import android.content.Context
import androidx.room.Room
import com.daustodo.app.data.database.DausTodoDatabase
import com.daustodo.app.data.database.PomodoroDao
import com.daustodo.app.data.database.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DausTodoDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            DausTodoDatabase::class.java,
            "daus_todo_database"
        ).build()
    }
    
    @Provides
    fun provideTaskDao(database: DausTodoDatabase): TaskDao {
        return database.taskDao()
    }
    
    @Provides
    fun providePomodoroDao(database: DausTodoDatabase): PomodoroDao {
        return database.pomodoroDao()
    }
}