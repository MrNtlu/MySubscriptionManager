package com.mrntlu.mysubscriptionmanager.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.github.debop.kodatimes.days
import com.github.debop.kodatimes.months
import com.github.debop.kodatimes.years
import com.mrntlu.mysubscriptionmanager.R
import com.mrntlu.mysubscriptionmanager.adapters.viewholders.LoadingItemViewHolder
import com.mrntlu.mysubscriptionmanager.adapters.viewholders.NoItemViewHolder
import com.mrntlu.mysubscriptionmanager.adapters.viewholders.SubscriptionViewHolder
import com.mrntlu.mysubscriptionmanager.interfaces.SubscriptionManager
import com.mrntlu.mysubscriptionmanager.models.Currency
import com.mrntlu.mysubscriptionmanager.models.FrequencyType
import com.mrntlu.mysubscriptionmanager.models.Subscription
import com.mrntlu.mysubscriptionmanager.ui.MainActivity
import com.mrntlu.mysubscriptionmanager.utils.*
import kotlinx.android.synthetic.main.cell_subscriptions.view.*
import org.joda.time.DateTime
import org.joda.time.Days
import java.util.*
import kotlin.collections.ArrayList

class SubscriptionListAdapter:RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val NO_ITEM_HOLDER:Int=0
    private val LOADING_ITEM_HOLDER:Int=1
    private val SUBSCRIPTION_HOLDER:Int=2

    private lateinit var subscriptionManager: SubscriptionManager
    private lateinit var defaultCurrency: Currency
    private var isAdapterSet:Boolean=false
    private var subscriptionList:ArrayList<Subscription> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            NO_ITEM_HOLDER -> {
                val view=LayoutInflater.from(parent.context).inflate(R.layout.cell_no_item,parent,false)
                NoItemViewHolder(view)
            }
            LOADING_ITEM_HOLDER -> {
                val view=LayoutInflater.from(parent.context).inflate(R.layout.cell_loading_item,parent,false)
                LoadingItemViewHolder(view)
            }
            else ->{
                val view=LayoutInflater.from(parent.context).inflate(R.layout.cell_subscriptions,parent,false)
                SubscriptionViewHolder(view)
            }
        }
    }

    fun setSubscriptionList(subscriptionList: List<Subscription>){
        this.subscriptionList.apply {
            this.clear()
            this.addAll(subscriptionList)
        }
        isAdapterSet=true
        notifyDataSetChanged()
    }

    fun setSubscriptionManager(subscriptionManager: SubscriptionManager){
        this.subscriptionManager=subscriptionManager
    }

    fun setDefaultCurrency(currency: Currency){
        defaultCurrency=currency
    }

    override fun getItemCount(): Int = if (isAdapterSet)(if (subscriptionList.size==0) 1 else subscriptionList.size) else 1


    override fun getItemViewType(position: Int) = if (isAdapterSet)(if (subscriptionList.size==0) NO_ITEM_HOLDER else SUBSCRIPTION_HOLDER) else LOADING_ITEM_HOLDER


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is SubscriptionViewHolder) {
            val subscription = subscriptionList[position]

            val priceTag = "${subscription.price} ${subscription.currency.symbol}"
            if (defaultCurrency!=subscription.currency && (holder.itemView.context as MainActivity).getExchangeRate()!=null) {
                val equivalentPrice=subscription.price/getExchangeRateOfCurrency(subscription.currency,defaultCurrency,(holder.itemView.context as MainActivity).getExchangeRate()!!)
                val equivalentPriceTag="${String.format(Locale.ENGLISH,"%.2f",equivalentPrice).toDouble()} ${defaultCurrency.symbol}"

                holder.itemView.equivalentPriceText.text=equivalentPriceTag
                holder.itemView.equivalentPriceText.setVisible()
            }else holder.itemView.equivalentPriceText.setGone()

            var paymentDate = DateTime(subscription.paymentDate)
            var daysLeft = Days.daysBetween(DateTime(getDateWithoutTime()), paymentDate).days

            val countDown: String
            var totalPaid = subscription.totalPaid
            if (daysLeft < 0) {
                while (daysLeft < 0) {
                    paymentDate = when (subscription.frequencyType) {
                        FrequencyType.DAY -> paymentDate + subscription.frequency.days()
                        FrequencyType.MONTH -> paymentDate + subscription.frequency.months()
                        FrequencyType.YEAR -> paymentDate + subscription.frequency.years()
                    }
                    daysLeft = Days.daysBetween(DateTime(getDateWithoutTime()), paymentDate).days
                    totalPaid += subscription.price
                }
                subscriptionManager.resetPaymentDate(
                    subscription.copy(
                        totalPaid = totalPaid,
                        paymentDate = paymentDate.toDate()
                    )
                )
                countDown = countDownController(daysLeft)
            } else {
                countDown = countDownController(daysLeft)
            }

            setTextColors(listOf(holder.itemView.nameText,holder.itemView.priceText,holder.itemView.equivalentPriceText),
                isDark(subscription.color))

            holder.itemView.subscriptionCard.setCardBackgroundColor(subscription.color)
            holder.itemView.nameText.text = subscription.name
            holder.itemView.priceText.text = priceTag
            holder.itemView.countdownText.text = countDown

            holder.itemView.setOnClickListener {
                subscriptionManager.onClicked(subscription)
            }
        }
    }

    private fun setTextColors(textList:List<TextView>,isDark:Boolean){
        for (textview in textList){
            textview.setTextColor(
                ContextCompat.getColor(
                    textview.context,
                    if (isDark) R.color.White else R.color.Black
                )
            )
        }
    }

    private fun countDownController(daysLeft:Int): String=
        when (daysLeft) {
            0 -> "Today"
            1 -> "Tomorrow"
            else -> "$daysLeft days"
        }
}