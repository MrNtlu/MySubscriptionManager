package com.mrntlu.mysubscriptionmanager.utils

import android.content.Context
import android.content.SharedPreferences
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

object Constants{
    val THEME_PREF_NAME="theme_code"
    val ORDER_PREF_NAME="order_type"
    val SORT_PREF_NAME="sort_type"
    val DARK_THEME=0
    val LIGHT_THEME=1
}

fun View.setGone(){
    this.visibility=View.GONE
}

fun View.setVisible(){
    this.visibility=View.VISIBLE
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

    dialogBuilder.setTitle("Important!")
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

fun isDark(color: Int): Boolean {
    return ColorUtils.calculateLuminance(color) < 0.5
}