package com.mrntlu.mysubscriptionmanager.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.mrntlu.mysubscriptionmanager.models.Exchange
import com.mrntlu.mysubscriptionmanager.persistance.ExchangeDatabase
import com.mrntlu.mysubscriptionmanager.service.ExchangeService
import com.mrntlu.mysubscriptionmanager.service.RetrofitClient
import com.mrntlu.mysubscriptionmanager.service.response.ExchangeResponse

class ExchangeRepository(application: Application) {

    private val exchangeDao=ExchangeDatabase.getInstance(application).exchangeDao
    private var apiClient= RetrofitClient.getClient().create(ExchangeService::class.java)

    fun getAllExchanges(): LiveData<Exchange>{
        return exchangeDao.getAllExchanges()
    }

    suspend fun updateExchange(exchange: Exchange){
        return exchangeDao.updateExchange(exchange)
    }

    suspend fun fetchExchangeRates(): ExchangeResponse {
        return apiClient.getExchangeRates()
    }
}