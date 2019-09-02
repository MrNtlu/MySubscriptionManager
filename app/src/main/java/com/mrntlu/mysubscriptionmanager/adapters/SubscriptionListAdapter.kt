package com.mrntlu.mysubscriptionmanager.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.debop.kodatimes.months
import com.mrntlu.mysubscriptionmanager.R
import com.mrntlu.mysubscriptionmanager.adapters.viewholders.LoadingItemViewHolder
import com.mrntlu.mysubscriptionmanager.adapters.viewholders.NoItemViewHolder
import com.mrntlu.mysubscriptionmanager.adapters.viewholders.SubscriptionViewHolder
import com.mrntlu.mysubscriptionmanager.interfaces.SubscriptionManager
import com.mrntlu.mysubscriptionmanager.models.Currency
import com.mrntlu.mysubscriptionmanager.models.Subscription
import com.mrntlu.mysubscriptionmanager.utils.dateFormatLong
import com.mrntlu.mysubscriptionmanager.utils.printLog
import kotlinx.android.synthetic.main.cell_subscriptions.view.*
import org.joda.time.DateTime
import org.joda.time.Days

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

            val priceTag:String
            priceTag = when(subscription.currency){
                Currency.DOLLAR-> "${subscription.price} $"
                Currency.EURO->"${subscription.price} €"
                Currency.LIRA->"${subscription.price} ₺"
            }

            val paymentDate=DateTime(subscription.paymentDate)
            val upcomingPayment=DateTime(subscription.paymentDate) + subscription.frequency.months()
            printLog(message = "${paymentDate.dateFormatLong()} ${upcomingPayment.dateFormatLong()}}")

            val daysLeft= Days.daysBetween(paymentDate,upcomingPayment).days

            val countDown:String
            if (daysLeft<0){
                //TODO update the
                countDown="Error"
            }else if (daysLeft==0){
                countDown="Today"
            }else if (daysLeft==1){
                countDown="Tomorrow"
            }else{
                countDown="$daysLeft days"
            }

            holder.itemView.nameText.text=subscription.name
            holder.itemView.priceText.text=priceTag
            holder.itemView.countdownText.text=countDown

            holder.itemView.setOnClickListener {
                subscriptionManager.onClicked(subscription)
            }
        }else if (holder is NoItemViewHolder){

        }
    }
}