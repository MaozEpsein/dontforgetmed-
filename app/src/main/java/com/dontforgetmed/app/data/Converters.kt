package com.dontforgetmed.app.data

import androidx.room.TypeConverter
import com.dontforgetmed.app.data.entity.DoseStatus
import com.dontforgetmed.app.data.entity.FrequencyType

class Converters {
    @TypeConverter fun fromStatus(s: DoseStatus): String = s.name
    @TypeConverter fun toStatus(v: String): DoseStatus = DoseStatus.valueOf(v)
    @TypeConverter fun fromFrequency(f: FrequencyType): String = f.name
    @TypeConverter fun toFrequency(v: String): FrequencyType = FrequencyType.valueOf(v)
}
