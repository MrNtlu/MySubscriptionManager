package com.mrntlu.mysubscriptionmanager.models

import android.os.Parcelable
import androidx.room.*
import com.mrntlu.mysubscriptionmanager.R
import kotlinx.android.parcel.Parcelize
import java.util.*

enum class Currency (val code:Int,val symbol:String,val flag:Int){
    EURO(0,"€",R.drawable.eur),
    USD(1,"$",R.drawable.usd),
    TRY(2,"₺",R.drawable.lira),
    YEN(3,"¥",R.drawable.jpy),
    WON(4,"₩",R.drawable.krw),
    POUND(5,"£",R.drawable.gbp),
    RUBLE(6,"\u20BD",R.drawable.rub)
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