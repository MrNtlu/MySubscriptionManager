package com.mrntlu.mysubscriptionmanager.models.converters

import androidx.room.TypeConverter
import com.mrntlu.mysubscriptionmanager.models.Currency
import com.mrntlu.mysubscriptionmanager.models.Currency.*

class SubscriptionCurrencyConverter {
    @TypeConverter
    fun toCurrency(code: Int): Currency {
        return if (code == EURO.code) {
            EURO
        } else if (code == USD.code) {
            USD
        } else if (code == TRY.code) {
            TRY
        }else if (code == WON.code) {
            WON
        }else if (code == YEN.code) {
            YEN
        }else if (code == RUBLE.code) {
            RUBLE
        }else if (code == POUND.code) {
            POUND
        }else {
            throw IllegalArgumentException("Couldn't recognize status")
        }
    }

    @TypeConverter
    fun toInteger(currency: Currency): Int {
        return currency.code
    }
}