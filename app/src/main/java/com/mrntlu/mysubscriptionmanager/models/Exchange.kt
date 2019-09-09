package com.mrntlu.mysubscriptionmanager.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
@Entity(
    tableName = "exchanges"
)
data class Exchange(
    @PrimaryKey(autoGenerate = false) val id: String,
    val EUR:Double,
    val RUB:Double,
    val GBP:Double,
    val KRW:Double,
    val JPY:Double,
    val TRY:Double,
    @ColumnInfo(name="cache_date") val cacheDate: Date
): Parcelable
