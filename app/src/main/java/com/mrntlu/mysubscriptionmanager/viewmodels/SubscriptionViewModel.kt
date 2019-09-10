package com.mrntlu.mysubscriptionmanager.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mrntlu.mysubscriptionmanager.interfaces.CoroutinesHandler
import com.mrntlu.mysubscriptionmanager.interfaces.ExchangeRateHandler
import com.mrntlu.mysubscriptionmanager.models.Currency
import com.mrntlu.mysubscriptionmanager.models.Subscription
import com.mrntlu.mysubscriptionmanager.models.SubscriptionViewState
import com.mrntlu.mysubscriptionmanager.repository.SubscriptionRepository
import com.mrntlu.mysubscriptionmanager.service.response.ExchangeResponse
import com.mrntlu.mysubscriptionmanager.ui.fragments.OrderType
import com.mrntlu.mysubscriptionmanager.ui.fragments.SortingType
import com.mrntlu.mysubscriptionmanager.utils.printLog
import kotlinx.coroutines.*

class SubscriptionViewModel(application: Application):AndroidViewModel(application) {

    private val TIME_OUT = 3000L
    private var subscriptionRepository: SubscriptionRepository = SubscriptionRepository(application)
    private var mJob: Job? = null

    fun getAllSubscriptions(sortingType: SortingType,orderType: OrderType,limit: Int): LiveData<List<Subscription>> {
        return subscriptionRepository.getAllSubscriptions(sortingType,orderType,limit)
    }

    fun insertSubscription(subscription: Subscription, coroutinesHandler: CoroutinesHandler) {
        mJob=Job()
        mJob=viewModelScope.launch(Dispatchers.IO){
            val job=withTimeoutOrNull(TIME_OUT) {
                delay(1000L)
                subscriptionRepository.insertNewSubscription(subscription)
            }
            withContext(Dispatchers.Main){
                if (job == null) {
                    coroutinesHandler.onError("Timed out!")
                } else {
                    coroutinesHandler.onSuccess("Successfully added.")
                }
            }
        }
    }

    fun updateSubscription(subscription: Subscription, coroutinesHandler: CoroutinesHandler) {
        mJob=Job()
        mJob=viewModelScope.launch(Dispatchers.IO){
            val job=withTimeoutOrNull(TIME_OUT) {
                delay(500L)
                subscriptionRepository.updateSubscription(subscription)
            }
            withContext(Dispatchers.Main){
                if (job == null) {
                    coroutinesHandler.onError("Timed out!")
                } else {
                    coroutinesHandler.onSuccess("Successfully updated.")
                }
            }
        }
    }

    fun deleteSubscription(subscription: Subscription, coroutinesHandler: CoroutinesHandler) {
        mJob=Job()
        mJob=viewModelScope.launch(Dispatchers.IO){
            val job=withTimeoutOrNull(TIME_OUT) {
                subscriptionRepository.deleteSubscription(subscription)
            }
            withContext(Dispatchers.Main){
                if(job==null){
                    coroutinesHandler.onError("Timed out!")
                }else{
                    coroutinesHandler.onSuccess("Successfully deleted.")
                }
            }
        }
    }

    val viewState = MutableLiveData<SubscriptionViewState>()

    fun setViewState(viewState: SubscriptionViewState) {
        this.viewState.value = viewState
    }

    val defaultCurrency=MutableLiveData<Currency>()

    fun setDefaultCurrency(currency: Currency) {
        this.defaultCurrency.value = currency
    }

    fun cancelJobs(){
        mJob?.let {
            if (it.isActive){
                printLog(message = "Canceled")
                it.cancel()
            }
        }
    }
}