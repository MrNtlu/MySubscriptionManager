package com.mrntlu.mysubscriptionmanager.interfaces

import com.mrntlu.mysubscriptionmanager.models.Exchange
import com.mrntlu.mysubscriptionmanager.service.response.ExchangeRateResponse

interface ExchangeRateHandler {
    fun onFetchSucess(message:String,exchangeResponse: ExchangeRateResponse)
    fun onInsertSucess(message:String,exchange: Exchange)
    fun onError(exception:String)
}