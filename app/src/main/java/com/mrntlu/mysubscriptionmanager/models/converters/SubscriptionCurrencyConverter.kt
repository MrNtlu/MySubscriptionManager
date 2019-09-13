package com.mrntlu.mysubscriptionmanager.models.converters

import androidx.room.TypeConverter
import com.mrntlu.mysubscriptionmanager.models.Currency
import com.mrntlu.mysubscriptionmanager.models.Currency.*

class SubscriptionCurrencyConverter {
    @TypeConverter
    fun toCurrency(code: Int): Currency {
        return if (code == EUR.code) {
            EUR
        } else if (code == USD.code) {
            USD
        } else if (code == TRY.code) {
            TRY
        }else if (code == KRW.code) {
            KRW
        }else if (code == JPY.code) {
            JPY
        }else if (code == RUB.code) {
            RUB
        }else if (code == GBP.code) {
            GBP
        }else {
            throw IllegalArgumentException("Couldn't recognize status")
        }
    }

    @TypeConverter
    fun toInteger(currency: Currency): Int {
        return currency.code
    }
}