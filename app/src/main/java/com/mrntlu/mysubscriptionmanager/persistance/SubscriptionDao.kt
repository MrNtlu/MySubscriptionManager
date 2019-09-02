package com.mrntlu.mysubscriptionmanager.persistance

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mrntlu.mysubscriptionmanager.models.Subscription

@Dao
interface SubscriptionDao {

    @Query("SELECT * FROM subscriptions LIMIT :limit")
    fun getAllSubscriptions(limit:Int): LiveData<List<Subscription>>

    @Insert
    suspend fun insertNewSubscription(subscription: Subscription)

    @Update
    suspend fun updateSubscription(subscription: Subscription)

    @Delete
    suspend fun deleteSubscription(subscription: Subscription)
}