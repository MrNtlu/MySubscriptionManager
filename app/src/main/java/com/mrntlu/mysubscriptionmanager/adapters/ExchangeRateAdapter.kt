package com.mrntlu.mysubscriptionmanager.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mrntlu.mysubscriptionmanager.R
import com.mrntlu.mysubscriptionmanager.adapters.viewholders.ExchangeRateViewHolder
import com.mrntlu.mysubscriptionmanager.ui.fragments.ExchangeRate
import kotlinx.android.synthetic.main.cell_exchange_rate.view.*
import java.util.*
import kotlin.collections.ArrayList

class ExchangeRateAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var exchangeRateList:ArrayList<ExchangeRate> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.cell_exchange_rate,parent,false)
        return ExchangeRateViewHolder(view)
    }

    override fun getItemCount()=exchangeRateList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val exchangeRate=exchangeRateList[position]

        Glide.with(holder.itemView.context).load(exchangeRate.flag).into(holder.itemView.cellExchangeFlag)
        holder.itemView.cellExchangeNameText.text=exchangeRate.name
        holder.itemView.cellExchangeRateText.text=String.format(Locale.ENGLISH,"%.5f",exchangeRate.rate)
    }

    fun setSubscriptionList(exchangeRateList:ArrayList<ExchangeRate>){
        this.exchangeRateList.apply {
            this.clear()
            this.addAll(exchangeRateList)
        }
        notifyDataSetChanged()
    }
}