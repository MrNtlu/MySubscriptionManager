package com.mrntlu.mysubscriptionmanager.persistance

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mrntlu.mysubscriptionmanager.models.Subscription

@Dao
interface SubscriptionDao {

    @Query("SELECT * FROM subscriptions ORDER BY CASE :sortingType WHEN 0 THEN name WHEN 1 THEN currency AND price WHEN 2 THEN payment_date END ASC LIMIT :limit")
    fun getAllSubscriptionsAsc(sortingType:Int,limit:Int): LiveData<List<Subscription>>

    @Query("SELECT * FROM subscriptions ORDER BY CASE :sortingType WHEN 0 THEN name WHEN 1 THEN price WHEN 2 THEN payment_date END DESC LIMIT :limit")
    fun getAllSubscriptionsDesc(sortingType:Int,limit:Int): LiveData<List<Subscription>>

    @Insert
    suspend fun insertNewSubscription(subscription: Subscription)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun updateSubscription(subscription: Subscription)

    @Delete
    suspend fun deleteSubscription(subscription: Subscription)
}