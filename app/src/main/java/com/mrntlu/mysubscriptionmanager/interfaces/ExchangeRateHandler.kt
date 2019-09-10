package com.mrntlu.mysubscriptionmanager.interfaces

import com.mrntlu.mysubscriptionmanager.service.response.ExchangeRateResponse

interface ExchangeRateHandler {
    fun onFetchSucess(message:String,exchangeResponse: ExchangeRateResponse)
    fun onError(exception:String)
}