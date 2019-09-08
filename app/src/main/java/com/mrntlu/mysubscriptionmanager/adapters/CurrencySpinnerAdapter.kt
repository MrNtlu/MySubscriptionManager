package com.mrntlu.mysubscriptionmanager.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.mrntlu.mysubscriptionmanager.R
import com.mrntlu.mysubscriptionmanager.models.Currency
import com.mrntlu.mysubscriptionmanager.utils.printLog

class CurrencySpinnerAdapter(context: Context,arrayList: List<Currency>):ArrayAdapter<Currency>(context, 0,arrayList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position,convertView,parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position,convertView,parent)
    }

    private fun initView(position: Int,_convertView: View?,parent: ViewGroup):View{
        var convertView=_convertView
        if (convertView==null){
            convertView=LayoutInflater.from(context).inflate(R.layout.currency_spinner_row,parent,false)
        }
        val flagImage=convertView?.findViewById<ImageView>(R.id.flagImage)
        val currencyCodeText=convertView?.findViewById<TextView>(R.id.currencyCodeText)

        getItem(position)?.let {
            Glide.with(context).load(it.flag).into(flagImage!!)
            currencyCodeText!!.text=it.name
            printLog(message = "${it.name} ${currencyCodeText!!}")
        }

        return convertView!!
    }
}