package com.mrntlu.mysubscriptionmanager.service.response

data class ExchangeRateResponse(val EUR:Double,val RUB:Double,val GBP:Double,val KRW:Double,val JPY:Double,val TRY:Double)

data class ExchangeResponse(val rates:ExchangeRateResponse)