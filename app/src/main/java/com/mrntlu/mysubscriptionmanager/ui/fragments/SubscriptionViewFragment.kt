package com.mrntlu.mysubscriptionmanager.ui.fragments

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.github.debop.kodatimes.toDateTime
import com.mrntlu.mysubscriptionmanager.R
import com.mrntlu.mysubscriptionmanager.interfaces.CoroutinesHandler
import com.mrntlu.mysubscriptionmanager.models.FrequencyType
import com.mrntlu.mysubscriptionmanager.models.Subscription
import com.mrntlu.mysubscriptionmanager.models.SubscriptionViewState
import com.mrntlu.mysubscriptionmanager.ui.MainActivity
import com.mrntlu.mysubscriptionmanager.utils.*
import com.mrntlu.mysubscriptionmanager.viewmodels.SubscriptionViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_subscription_view.*
import java.util.*

class SubscriptionViewFragment : Fragment(), CoroutinesHandler {

    private lateinit var mSubscription:Subscription
    private lateinit var navController: NavController
    private lateinit var viewModel:SubscriptionViewModel
    private lateinit var alarmPref:SharedPreferences
    private var alarmStatus=0 //0==alarm not set 1==alarm set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mSubscription = it.getParcelable("subscription")!!
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_subscription_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController= Navigation.findNavController(view)
        viewModel= ViewModelProviders.of(context as AppCompatActivity).get(SubscriptionViewModel::class.java)

        alarmPref=view.context.getSharedPreferences(Constants.ALARM_PREF_NAME,0)
        alarmStatus=alarmPref.getInt(mSubscription.id,0)
        setAlarmButtonText()

        setOnClickListener()
        initBottomAppBar(view.context as MainActivity)
        setData()
    }

    private fun setOnClickListener() {
        alarmButton.setOnClickListener {
            setAlarm(it.context,alarmStatus==0)
            alarmStatus=if (alarmStatus==0) 1 else 0
            setIntPrefs(alarmPref,mSubscription.id,alarmStatus)
            setAlarmButtonText()
        }
    }

    private fun setAlarmButtonText(){
        alarmButton.text=if (alarmStatus==0) getString(R.string.set_alarm) else getString(R.string.cancel_alarm)
        alarmButton.setCompoundDrawablesWithIntrinsicBounds(if (alarmStatus==0) R.drawable.ic_notifications_black_24dp else R.drawable.ic_notifications_off_black_24dp,0,0,0)
    }

    private fun setAlarm(context: Context,isStarting:Boolean) {
        val alarmManager=context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent=Intent(context,AlertReceiver::class.java)
        intent.putExtra("message","${mSubscription.name}'s payment.")
        intent.putExtra("hashCode",mSubscription.id.hashCode())
        val pendingIntent=PendingIntent.getBroadcast(context,mSubscription.id.hashCode(),intent,0)

        if (isStarting) {
            val paymentInterval = when (mSubscription.frequencyType) {
                FrequencyType.DAY -> mSubscription.frequency * 86400000L
                FrequencyType.MONTH -> mSubscription.frequency * 2592000000L
                FrequencyType.YEAR -> mSubscription.frequency * 31556952000L
            }
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                mSubscription.paymentDate.toDateTime().millis,
                paymentInterval,
                pendingIntent
            )
        }else alarmManager.cancel(pendingIntent)

    }


    private fun initBottomAppBar(activity: MainActivity) {
        activity.setBottomAppBar(this)
        activity.addFab.setOnClickListener {
            val bundle= bundleOf("subscription" to mSubscription)
            viewModel.setViewState(SubscriptionViewState.UPDATE)
            navController.navigate(R.id.action_subView_to_sub,bundle)
        }
        setBottomAppbarMenuListeners(activity)
    }

    private fun setBottomAppbarMenuListeners(activity: MainActivity) {
        activity.bottomAppBar.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.appbarDelete->{
                    val dialogBuilder = createDialog(activity,getString(R.string.do_you_want_to_delete))
                    dialogBuilder.setPositiveButton("Yes") { _, _ ->
                        removeIntPrefs(alarmPref,mSubscription.name)
                        viewModel.deleteSubscription(mSubscription,this)
                    }
                    val alert = dialogBuilder.create()
                    alert.show()
                }
            }
            true
        }
    }

    private fun setData() {
        if (!mSubscription.description.isEmptyOrBlank()) descriptionViewText.text=mSubscription.description else descriptionViewText.setGone()

        nameViewText.text = mSubscription.name
        priceViewText.text = mSubscription.price.toString()
        currencyViewText.text = mSubscription.currency.name
        frequencyViewText.text=mSubscription.frequency.toString()
        firstPaymentViewText.text=mSubscription.paymentDate.dateFormatLong()

        val frequencyText=mSubscription.frequencyType.toString().toLowerCase(Locale.getDefault()).plus(if (mSubscription.frequency>1)"s" else "")
        frequencyTypeViewText.text=frequencyText
        val paidInTotal="${mSubscription.totalPaid} ${mSubscription.currency.symbol}"
        paidInTotalViewText.text=paidInTotal

        if (!mSubscription.description.isEmptyOrBlank()) paymentMethodViewText.text=mSubscription.paymentMethod
        else{
            paymentMethodTextView.setGone()
            paymentMethodViewText.setGone()
        }
    }

//Coroutines Handler
    override fun onSuccess(message:String) {
        context?.let {
            showToast(it, message)
            navController.popBackStack()
        }
    }

    override fun onError(exception: String) {
        context?.let {
            showToast(it, exception)
            navController.popBackStack()
        }
    }

    override fun onStop() {
        viewModel.cancelJobs()
        super.onStop()
    }
}
