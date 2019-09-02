package com.mrntlu.mysubscriptionmanager.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mrntlu.mysubscriptionmanager.interfaces.CoroutinesHandler
import com.mrntlu.mysubscriptionmanager.models.Subscription
import com.mrntlu.mysubscriptionmanager.models.SubscriptionViewState
import com.mrntlu.mysubscriptionmanager.repository.SubscriptionRepository
import kotlinx.coroutines.*

class SubscriptionViewModel(application: Application):AndroidViewModel(application) {

    private val TIME_OUT=3000L
    private var subscriptionRepository: SubscriptionRepository = SubscriptionRepository(application)

    val subscriptionList = MutableLiveData<List<Subscription>>()


    fun getAllSubscriptions(limit: Int): LiveData<List<Subscription>> {
        return subscriptionRepository.getAllSubscriptions(limit)
    }

    fun insertNewSubscription(subscription: Subscription, coroutinesHandler: CoroutinesHandler):Job= viewModelScope.launch(Dispatchers.IO) {
        val job=withTimeoutOrNull(TIME_OUT) {
            subscriptionRepository.insertNewSubscription(subscription)
        }
        withContext(Dispatchers.Main){
            if(job==null){
                coroutinesHandler.onError("Timed out!")
            }else{
                coroutinesHandler.onSuccess()
            }
        }
    }

    fun updateSubscription(subscription: Subscription) = viewModelScope.launch(Dispatchers.IO) {
        subscriptionRepository.updateSubscription(subscription)
    }

    fun deleteSubscription(subscription: Subscription) = viewModelScope.launch(Dispatchers.IO) {
        subscriptionRepository.deleteSubscription(subscription)
    }

    val viewState = MutableLiveData<SubscriptionViewState>()

    fun setViewState(viewState: SubscriptionViewState) {
        this.viewState.value = viewState
    }
}