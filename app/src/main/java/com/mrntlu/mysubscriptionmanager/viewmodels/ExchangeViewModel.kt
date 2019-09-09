package com.mrntlu.mysubscriptionmanager.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.mrntlu.mysubscriptionmanager.interfaces.ExchangeRateHandler
import com.mrntlu.mysubscriptionmanager.models.Exchange
import com.mrntlu.mysubscriptionmanager.repository.ExchangeRepository
import com.mrntlu.mysubscriptionmanager.service.response.ExchangeResponse
import com.mrntlu.mysubscriptionmanager.utils.printLog
import kotlinx.coroutines.*

class ExchangeViewModel(application: Application): AndroidViewModel(application) {

    private val TIME_OUT = 3000L
    private var exchangeRepository= ExchangeRepository(application)
    private var mJob: Job? = null

    fun fetchExchangeRates(exchangeRateHandler: ExchangeRateHandler){
        mJob=viewModelScope.launch(Dispatchers.IO + CoroutineExceptionHandler { _, e ->
            exchangeRateHandler.onError(if (e.message==null)"No Internet" else e.message!!)
        }){
            var response: ExchangeResponse?=null
            val job= withTimeoutOrNull(TIME_OUT) {
                response=exchangeRepository.fetchExchangeRates()
            }
            withContext(Dispatchers.Main){
                if (job==null){
                    exchangeRateHandler.onError("Couldn't get the exchange rate.")
                }else {
                    response?.let {
                        exchangeRateHandler.onFetchSucess("Success",it.rates)
                    }
                }
            }
        }
    }

    fun getAllExchanges(): LiveData<Exchange> {
        return exchangeRepository.getAllExchanges()
    }

    fun updateExchange(exchange: Exchange, exchangeRateHandler: ExchangeRateHandler){
        mJob=Job()
        mJob=viewModelScope.launch(Dispatchers.IO){
            val job= withTimeoutOrNull(TIME_OUT){
                exchangeRepository.updateExchange(exchange)
            }
            withContext(Dispatchers.Main){
                if (job==null){
                    exchangeRateHandler.onError("Timed out!")
                }else{
                    exchangeRateHandler.onInsertSucess("Success.", exchange)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mJob?.let {
            if (it.isActive){
                printLog(message = "Canceled")
                it.cancel()
            }
        }
    }
}