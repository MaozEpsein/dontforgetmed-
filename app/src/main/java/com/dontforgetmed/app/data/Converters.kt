package com.dontforgetmed.app.data

import androidx.room.TypeConverter
import com.dontforgetmed.app.data.entity.DoseStatus

class Converters {
    @TypeConverter fun fromStatus(s: DoseStatus): String = s.name
    @TypeConverter fun toStatus(v: String): DoseStatus = DoseStatus.valueOf(v)
}
