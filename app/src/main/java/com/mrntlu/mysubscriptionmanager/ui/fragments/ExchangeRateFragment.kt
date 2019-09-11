package com.mrntlu.mysubscriptionmanager.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.mrntlu.mysubscriptionmanager.R
import com.mrntlu.mysubscriptionmanager.adapters.ExchangeRateAdapter
import com.mrntlu.mysubscriptionmanager.models.Currency
import com.mrntlu.mysubscriptionmanager.models.Exchange
import com.mrntlu.mysubscriptionmanager.ui.MainActivity
import com.mrntlu.mysubscriptionmanager.utils.getCurrencyRateFromExchange
import com.mrntlu.mysubscriptionmanager.utils.printLog
import com.mrntlu.mysubscriptionmanager.utils.setGone
import com.mrntlu.mysubscriptionmanager.utils.setVisible
import com.mrntlu.mysubscriptionmanager.viewmodels.ExchangeViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_exchange_rate.*

data class ExchangeRate(val flag:Int,val name:String,val rate:Double)

interface ExchangeFetchHandler{
    fun onError()
}

class ExchangeRateFragment : Fragment(),ExchangeFetchHandler {

    override fun onError() {
        if (exchangeProgressLayout.isVisible) exchangeProgressLayout.setGone()
    }

    private var exchangeRateList:ArrayList<ExchangeRate> = arrayListOf()
    private lateinit var adapter:ExchangeRateAdapter
    private lateinit var exchangeRate: Exchange
    private lateinit var defaultCurrency: Currency
    private lateinit var navController: NavController
    private lateinit var viewModel: ExchangeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            defaultCurrency = Currency.getByCode(it.getInt("defaultCurrency"))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_exchange_rate, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController= Navigation.findNavController(view)
        viewModel= ViewModelProviders.of(context as MainActivity).get(ExchangeViewModel::class.java)

        val baseExchange="1 ${defaultCurrency.name} is"
        baseExchangeText.text=baseExchange

        setupObservers()
        initBottomAppBar(context as MainActivity)
    }

    private fun initBottomAppBar(activity: MainActivity) {
        activity.setBottomAppBar(this)
        setBottomAppbarMenuListeners(activity)
        activity.addFab.setOnClickListener {
            navController.popBackStack()
        }
    }

    private fun setupObservers(){
        viewModel.getAllExchanges().observe(this, Observer {
            exchangeRate=it
            setExchangeRateList()
            exchangeProgressLayout.setGone()
        })
    }

    private fun setBottomAppbarMenuListeners(activity: MainActivity) {
        activity.bottomAppBar.setOnMenuItemClickListener {
            if (it.itemId==R.id.viewrateRefresh){
                exchangeProgressLayout.setVisible()
                activity.updateExchangeRate(this)
            }
            true
        }
    }

    private fun setupRecyclerView() {
        adapter= ExchangeRateAdapter()
        adapter.setSubscriptionList(exchangeRateList)
        exchangeRatesRV.apply {
            layoutManager=LinearLayoutManager(context)
            adapter=this@ExchangeRateFragment.adapter
        }
    }

    private fun setExchangeRateList(){
        exchangeRateList.clear()
        for (currency:Currency in Currency.values()){
            if (defaultCurrency!=currency){
                val rate= getCurrencyRateFromExchange(currency,exchangeRate)/ getCurrencyRateFromExchange(defaultCurrency,exchangeRate)
                exchangeRateList.add(ExchangeRate(currency.flag,currency.name,rate))
            }
        }
        setupRecyclerView()
    }

    override fun onDestroyView() {
        exchangeRatesRV.adapter=null
        super.onDestroyView()
    }
}
