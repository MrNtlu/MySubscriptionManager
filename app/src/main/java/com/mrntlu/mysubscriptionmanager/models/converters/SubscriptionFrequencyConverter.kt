package com.mrntlu.mysubscriptionmanager.models.converters

import androidx.room.TypeConverter
import com.mrntlu.mysubscriptionmanager.models.FrequencyType
import com.mrntlu.mysubscriptionmanager.models.FrequencyType.*

class SubscriptionFrequencyConverter {
    @TypeConverter
    fun toFrequency(code: Int): FrequencyType {
        return if (code == DAY.code) {
            DAY
        } else if (code == MONTH.code) {
            MONTH
        } else if (code == YEAR.code) {
            YEAR
        }else {
            throw IllegalArgumentException("Couldn't recognize status")
        }
    }

    @TypeConverter
    fun toInteger(frequency: FrequencyType): Int {
        return frequency.code
    }
}