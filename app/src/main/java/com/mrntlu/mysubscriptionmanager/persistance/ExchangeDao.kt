package com.mrntlu.mysubscriptionmanager.persistance

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mrntlu.mysubscriptionmanager.models.Exchange

@Dao
interface ExchangeDao {

    @Query("SELECT * FROM exchanges")
    fun getAllExchanges(): LiveData<Exchange>

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun updateExchange(exchange: Exchange)
}