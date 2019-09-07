package com.mrntlu.mysubscriptionmanager.models

import android.os.Parcelable
import androidx.room.*
import kotlinx.android.parcel.Parcelize
import java.util.*

enum class Currency (val code:Int,val symbol:String){
    EURO(0,"€"),
    USD(1,"$"),
    TRY(2,"₺"),
    YEN(3,"¥"),
    WON(4,"₩"),
    POUND(5,"£"),
    RUBLE(6,"\u20BD")
}

enum class FrequencyType(val code:Int){
    DAY(0),
    MONTH(1),
    YEAR(2)
}

@Parcelize
@Entity(
    tableName = "subscriptions",
    indices = [Index(value = ["name"], unique = false)]
)
data class Subscription(
    @PrimaryKey(autoGenerate = false) val id: String,
    val name:String,
    val description:String,
    @ColumnInfo(name="payment_date") val paymentDate:Date,
    val frequency:Int,
    @ColumnInfo(name="frequency_type") val frequencyType: FrequencyType,
    val price:Double,
    val currency: Currency,
    val color:Int,
    @ColumnInfo(name="payment_method")val paymentMethod:String,
    val totalPaid:Double=0.0
): Parcelable