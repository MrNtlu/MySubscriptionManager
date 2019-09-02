package com.mrntlu.mysubscriptionmanager.utils

import android.content.Context
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.mrntlu.mysubscriptionmanager.R
import com.mrntlu.mysubscriptionmanager.models.Currency
import com.mrntlu.mysubscriptionmanager.models.FrequencyType
import com.mrntlu.mysubscriptionmanager.models.Subscription
import org.joda.time.DateTime
import java.text.DateFormat
import java.util.*

fun printLog(tag: String = "Test",message:String)=Log.d(tag,message)

fun View.setGone(){
    this.visibility=View.GONE
}

fun View.setVisible(){
    this.visibility=View.VISIBLE
}

fun showToast(context:Context?,message: String) = Toast.makeText(context,message,Toast.LENGTH_SHORT).show()

fun String.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this)

fun Double.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this.toString())

fun Int.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this.toString())

fun Date.dateFormatLong(): String=DateFormat.getDateInstance(DateFormat.LONG).format(this)

fun DateTime.dateFormatLong(): String=DateFormat.getDateInstance(DateFormat.LONG).format(this.toDate())

fun TextView.getAsString():String=this.text.toString()

val subscription= Subscription(UUID.randomUUID().toString(),"Test","Desc",Date(),15,FrequencyType.DAY,1.11,
    Currency.DOLLAR,
    R.color.FloralWhite,null)