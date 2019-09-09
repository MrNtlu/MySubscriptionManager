package com.mrntlu.mysubscriptionmanager.service

import com.mrntlu.mysubscriptionmanager.service.response.ExchangeResponse
import retrofit2.http.GET

interface ExchangeService {

    @GET("latest?base=USD&symbols=TRY,GBP,JPY,EUR,KRW,RUB")
    suspend fun getExchangeRates():ExchangeResponse
}