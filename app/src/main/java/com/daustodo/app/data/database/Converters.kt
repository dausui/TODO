package com.daustodo.app.data.database

import androidx.room.TypeConverter
import com.daustodo.app.data.model.Priority
import com.daustodo.app.data.model.PomodoroType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Converters {
    
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    
    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? {
        return dateTime?.format(formatter)
    }
    
    @TypeConverter
    fun toLocalDateTime(dateTimeString: String?): LocalDateTime? {
        return dateTimeString?.let { LocalDateTime.parse(it, formatter) }
    }
    
    @TypeConverter
    fun fromPriority(priority: Priority): String {
        return priority.name
    }
    
    @TypeConverter
    fun toPriority(priorityString: String): Priority {
        return Priority.valueOf(priorityString)
    }
    
    @TypeConverter
    fun fromPomodoroType(type: PomodoroType): String {
        return type.name
    }
    
    @TypeConverter
    fun toPomodoroType(typeString: String): PomodoroType {
        return PomodoroType.valueOf(typeString)
    }
}