package com.mrntlu.mysubscriptionmanager.utils

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import org.joda.time.DateTime
import java.text.DateFormat
import java.util.*
import androidx.core.graphics.ColorUtils
import com.mrntlu.mysubscriptionmanager.R
import com.mrntlu.mysubscriptionmanager.models.Currency
import com.mrntlu.mysubscriptionmanager.models.Exchange
import com.mrntlu.mysubscriptionmanager.service.response.ExchangeRateResponse

object Constants{
    const val THEME_PREF_NAME="theme_code"
    const val ORDER_PREF_NAME="order_type"
    const val SORT_PREF_NAME="sort_type"
    const val ALARM_PREF_NAME="alarm_type"
    const val CURRENCY_PREF_NAME="default_currency"
    const val DARK_THEME=0
    const val LIGHT_THEME=1
}

fun View.setGone(){
    this.visibility=View.GONE
}

fun View.setVisible(){
    this.visibility=View.VISIBLE
}

fun ExchangeRateResponse.toExchange(): Exchange {
    return Exchange("0",this.EUR,this.RUB,this.GBP,this.KRW,this.JPY,this.TRY,Date())
}

fun Exchange.toExchangeRateResponse():ExchangeRateResponse{
    return ExchangeRateResponse(this.EUR,this.RUB,this.GBP,this.KRW,this.JPY,this.TRY)
}

fun String.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this)

fun Double.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this.toString())

fun Int.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this.toString())

fun Date.dateFormatLong(): String=DateFormat.getDateInstance(DateFormat.LONG).format(this)

fun DateTime.dateFormatLong(): String=DateFormat.getDateInstance(DateFormat.LONG).format(this.toDate())

fun TextView.getAsString():String=this.text.toString()

fun TextView.isEmptyOrBlank():Boolean = this.text.isBlank() || this.text.isEmpty()

fun String.isEmptyOrBlank():Boolean=this.isBlank() || this.isEmpty()

fun ImageButton.setTintColor(color: Int)=this.setColorFilter(ContextCompat.getColor(this.context,color))

fun showToast(context:Context?,message: String) = Toast.makeText(context,message,Toast.LENGTH_SHORT).show()

fun printLog(tag: String = "Test",message:String)=Log.d(tag,message)

fun createDialog(context: Context,message: String): AlertDialog.Builder {
    val dialogBuilder = AlertDialog.Builder(context)

    dialogBuilder.setTitle(context.getString(R.string.important_))
    dialogBuilder.setMessage(message).setCancelable(true)

    dialogBuilder.setNegativeButton("No"){dialog, _ ->
        dialog.dismiss()
    }

    return dialogBuilder
}

fun setIntPrefs(sharedPreferences:SharedPreferences,prefName:String,value:Int){
    val editor=sharedPreferences.edit()
    editor.putInt(prefName,value)
    editor.apply()
}

fun removeIntPrefs(sharedPreferences:SharedPreferences,prefName:String){
    val editor=sharedPreferences.edit()
    editor.remove(prefName)
    editor.apply()
}

fun getDateWithoutTime():Date{
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.time
}

fun isDark(color: Int): Boolean {
    return ColorUtils.calculateLuminance(color) < 0.5
}

fun isInternetAvailable(context: Context): Boolean {
    var result = false
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    cm?.run {
        cm.getNetworkCapabilities(cm.activeNetwork)?.run {
            result = when {
                hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        }
    }
    return result
}

fun getCurrencyRateFromExchange(currency: Currency,exchange: Exchange):Double{
    return when(currency){
        Currency.EUR -> exchange.EUR
        Currency.USD -> 1.0
        Currency.TRY -> exchange.TRY
        Currency.JPY -> exchange.JPY
        Currency.KRW -> exchange.KRW
        Currency.GBP -> exchange.GBP
        Currency.RUB -> exchange.RUB
    }
}

fun getExchangeRateOfCurrency(targetCurrency: Currency,defaultCurrency: Currency,exchange: Exchange):Double{
    val targetRate= getCurrencyRateFromExchange(targetCurrency,exchange)
    val defaultCurrencyRate= getCurrencyRateFromExchange(defaultCurrency,exchange)

    return targetRate/defaultCurrencyRate
}
