package com.mrntlu.mysubscriptionmanager.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.mrntlu.mysubscriptionmanager.models.Subscription
import com.mrntlu.mysubscriptionmanager.persistance.SubscriptionDao
import com.mrntlu.mysubscriptionmanager.persistance.SubscriptionDatabase

class SubscriptionRepository(application: Application) {

    private val subscriptionDao:SubscriptionDao=SubscriptionDatabase.getInstance(application).subscriptionDao

    fun getAllSubscriptions(limit:Int): LiveData<List<Subscription>>{
        return subscriptionDao.getAllSubscriptions(limit)
    }

    suspend fun insertNewSubscription(subscription: Subscription){
        return subscriptionDao.insertNewSubscription(subscription)
    }

    suspend fun updateSubscription(subscription: Subscription){
        return subscriptionDao.updateSubscription(subscription)
    }

    suspend fun deleteSubscription(subscription: Subscription){
        return subscriptionDao.deleteSubscription(subscription)
    }
}