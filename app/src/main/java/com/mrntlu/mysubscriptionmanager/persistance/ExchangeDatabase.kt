package com.mrntlu.mysubscriptionmanager.persistance

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mrntlu.mysubscriptionmanager.models.Exchange
import com.mrntlu.mysubscriptionmanager.models.converters.DateConverter

@Database(entities =[Exchange::class],version = 2)
@TypeConverters(DateConverter::class)
abstract class ExchangeDatabase:RoomDatabase() {

    companion object{
        const val DATABASE_NAME="exchanges_db"

        @Volatile
        private var INSTANCE: ExchangeDatabase? = null

        fun getInstance(context: Context): ExchangeDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        ExchangeDatabase::class.java,
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

    abstract val exchangeDao:ExchangeDao
}