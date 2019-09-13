package com.mrntlu.mysubscriptionmanager.persistance

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mrntlu.mysubscriptionmanager.models.Subscription
import com.mrntlu.mysubscriptionmanager.models.converters.DateConverter
import com.mrntlu.mysubscriptionmanager.models.converters.SubscriptionCurrencyConverter
import com.mrntlu.mysubscriptionmanager.models.converters.SubscriptionFrequencyConverter

@Database(entities = [Subscription::class], version = 1)
@TypeConverters(DateConverter::class,SubscriptionCurrencyConverter::class,SubscriptionFrequencyConverter::class)
abstract class SubscriptionDatabase: RoomDatabase() {

    companion object{
        const val DATABASE_NAME="subscriptions_db"

        @Volatile
        private var INSTANCE: SubscriptionDatabase? = null

        fun getInstance(context: Context): SubscriptionDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        SubscriptionDatabase::class.java,
                        DATABASE_NAME
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }

    abstract val subscriptionDao:SubscriptionDao
}