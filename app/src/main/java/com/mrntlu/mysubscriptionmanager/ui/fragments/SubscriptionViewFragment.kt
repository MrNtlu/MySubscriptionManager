package com.mrntlu.mysubscriptionmanager.ui.fragments

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

import com.mrntlu.mysubscriptionmanager.R
import com.mrntlu.mysubscriptionmanager.models.Subscription
import com.mrntlu.mysubscriptionmanager.models.SubscriptionViewState
import com.mrntlu.mysubscriptionmanager.ui.MainActivity
import com.mrntlu.mysubscriptionmanager.utils.dateFormatLong
import com.mrntlu.mysubscriptionmanager.utils.isEmptyOrBlank
import com.mrntlu.mysubscriptionmanager.utils.printLog
import com.mrntlu.mysubscriptionmanager.utils.setGone
import com.mrntlu.mysubscriptionmanager.viewmodels.SubscriptionViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_subscription_view.*
import java.util.*

class SubscriptionViewFragment : Fragment() {

    private lateinit var mSubscription:Subscription
    private lateinit var navController: NavController
    private lateinit var viewModel:SubscriptionViewModel

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

        initBottomAppBar(view.context as MainActivity)
        setData()
    }

    private fun initBottomAppBar(activity: MainActivity) {
        activity.setBottomAppBar(this)
        activity.addFab.setOnClickListener {
            val bundle= bundleOf("subscription" to mSubscription)
            viewModel.setViewState(SubscriptionViewState.UPDATE)
            navController.navigate(R.id.action_subView_to_sub,bundle)
        }
    }

    private fun setData() {
        nameViewText.text = mSubscription.name
        if (!mSubscription.description.isEmptyOrBlank()) descriptionViewText.text=mSubscription.description else descriptionViewText.setGone()
        priceViewText.text = mSubscription.price.toString()
        currencyViewText.text = mSubscription.currency.name
        frequencyViewText.text=mSubscription.frequency.toString()

        val frequencyText=mSubscription.frequencyType.toString().toLowerCase(Locale.getDefault()).plus(if (mSubscription.frequency>1)"s" else "")
        frequencyTypeViewText.text=frequencyText
        firstPaymentViewText.text=mSubscription.paymentDate.dateFormatLong()
        paidInTotalViewText.text=mSubscription.totalPaid.toString()
        if (!mSubscription.description.isEmptyOrBlank()) paymentMethodViewText.text=mSubscription.paymentMethod
        else{
            paymentMethodTextView.setGone()
            paymentMethodViewText.setGone()
        }
    }
}
