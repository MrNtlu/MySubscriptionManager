package com.mrntlu.mysubscriptionmanager.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
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
import com.mrntlu.mysubscriptionmanager.utils.dateFormatLong
import com.mrntlu.mysubscriptionmanager.utils.isDark
import com.mrntlu.mysubscriptionmanager.utils.printLog
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

    override fun getItemCount(): Int = if (isAdapterSet)(if (subscriptionList.size==0) 1 else subscriptionList.size) else 1


    override fun getItemViewType(position: Int): Int = if (isAdapterSet)(if (subscriptionList.size==0) NO_ITEM_HOLDER else SUBSCRIPTION_HOLDER) else LOADING_ITEM_HOLDER


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is SubscriptionViewHolder){
            val subscription=subscriptionList.get(position)

            val priceTag="${subscription.price} ${subscription.currency.symbol}"
            var paymentDate=DateTime(subscription.paymentDate)
            var daysLeft= Days.daysBetween(DateTime(Date()),paymentDate).days

            val countDown:String
            if (daysLeft<0){
                while (daysLeft<0) {
                    paymentDate = when (subscription.frequencyType) {
                        FrequencyType.DAY -> paymentDate + subscription.frequency.days()
                        FrequencyType.MONTH -> paymentDate + subscription.frequency.months()
                        FrequencyType.YEAR -> paymentDate + subscription.frequency.years()
                    }
                    daysLeft= Days.daysBetween(DateTime(Date()),paymentDate).days
                }
                val totalPaid=subscription.totalPaid+subscription.price
                subscriptionManager.resetPaymentDate(subscription.copy(totalPaid =totalPaid ,paymentDate = paymentDate.toDate()))
                countDown=countDownController(daysLeft)
            }else{
                countDown=countDownController(daysLeft)
            }

            if (isDark(subscription.color)){
                holder.itemView.nameText.setTextColor(ContextCompat.getColor(holder.itemView.context,R.color.White))
                holder.itemView.priceText.setTextColor(ContextCompat.getColor(holder.itemView.context,R.color.White))
            } else{
                holder.itemView.nameText.setTextColor(ContextCompat.getColor(holder.itemView.context,R.color.Black))
                holder.itemView.priceText.setTextColor(ContextCompat.getColor(holder.itemView.context,R.color.Black))
            }

            holder.itemView.subscriptionCard.setCardBackgroundColor(subscription.color)
            holder.itemView.nameText.text=subscription.name
            holder.itemView.priceText.text=priceTag
            holder.itemView.countdownText.text=countDown

            holder.itemView.setOnClickListener {
                subscriptionManager.onClicked(subscription)
            }
        }else if (holder is NoItemViewHolder){

        }
    }

    private fun countDownController(daysLeft:Int): String=
        when (daysLeft) {
            0 -> "Today"
            1 -> "Tomorrow"
            else -> "$daysLeft days"
        }
}