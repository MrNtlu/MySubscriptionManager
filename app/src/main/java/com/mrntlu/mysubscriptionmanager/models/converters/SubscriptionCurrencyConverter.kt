package com.mrntlu.mysubscriptionmanager.models.converters

import androidx.room.TypeConverter
import com.mrntlu.mysubscriptionmanager.models.Currency
import com.mrntlu.mysubscriptionmanager.models.Currency.*

class SubscriptionCurrencyConverter {
    @TypeConverter
    fun toCurrency(code: Int): Currency {
        return if (code == EURO.code) {
            EURO
        } else if (code == DOLLAR.code) {
            DOLLAR
        } else if (code == LIRA.code) {
            LIRA
        }else {
            throw IllegalArgumentException("Couldn't recognize status")
        }
    }

    @TypeConverter
    fun toInteger(currency: Currency): Int {
        return currency.code
    }
}