package com.mrntlu.mysubscriptionmanager.ui.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.widget.AdapterView
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mrntlu.mysubscriptionmanager.R
import com.mrntlu.mysubscriptionmanager.adapters.CurrencySpinnerAdapter
import com.mrntlu.mysubscriptionmanager.adapters.SubscriptionListAdapter
import com.mrntlu.mysubscriptionmanager.interfaces.CoroutinesHandler
import com.mrntlu.mysubscriptionmanager.interfaces.SubscriptionManager
import com.mrntlu.mysubscriptionmanager.models.Currency
import com.mrntlu.mysubscriptionmanager.models.Subscription
import com.mrntlu.mysubscriptionmanager.models.SubscriptionViewState
import com.mrntlu.mysubscriptionmanager.ui.MainActivity
import com.mrntlu.mysubscriptionmanager.ui.fragments.OrderType.*
import com.mrntlu.mysubscriptionmanager.ui.fragments.SortingType.*
import com.mrntlu.mysubscriptionmanager.utils.*
import com.mrntlu.mysubscriptionmanager.viewmodels.SubscriptionViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_subscription_list.*
import kotlinx.android.synthetic.main.navigation_bottom_sheet.*
import kotlinx.android.synthetic.main.navigation_sort_bottom_sheet.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

enum class SortingType(val code:Int){
    NAME(0),
    PRICE(1),
    PAYMENT_DATE(2);

    companion object{
        private val values = values()
        fun getByCode(code: Int) = values.first { it.code == code }
    }
}

enum class OrderType(val code:Int){
    ASC(0),
    DESC(1);

    companion object{
        private val values = values()
        fun getByCode(code: Int) = values.first { it.code == code }
    }
}

class SubscriptionListFragment : Fragment(), SubscriptionManager {

    private lateinit var adapter:SubscriptionListAdapter
    private lateinit var navController:NavController
    private lateinit var viewModel:SubscriptionViewModel
    private lateinit var navigationSheetDialog:BottomSheetDialog
    private lateinit var sortSheetDialog:BottomSheetDialog
    private lateinit var themePref:SharedPreferences
    private lateinit var sortPref:SharedPreferences
    private lateinit var orderPref:SharedPreferences
    private lateinit var defaultCurrencyPref:SharedPreferences

    private lateinit var sheetButtons:List<ImageButton>
    private lateinit var sortingType:SortingType
    private lateinit var orderType:OrderType
    private var limit=10
    private lateinit var defaultCurrency:Currency


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_subscription_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController=Navigation.findNavController(view)
        viewModel=ViewModelProviders.of(context as AppCompatActivity).get(SubscriptionViewModel::class.java)

        themePref=view.context.getSharedPreferences(Constants.THEME_PREF_NAME,0)
        sortPref=view.context.getSharedPreferences(Constants.SORT_PREF_NAME,0)
        orderPref=view.context.getSharedPreferences(Constants.ORDER_PREF_NAME,0)
        defaultCurrencyPref=view.context.getSharedPreferences(Constants.CURRENCY_PREF_NAME,0)

        orderType=OrderType.getByCode(orderPref.getInt(Constants.ORDER_PREF_NAME,ASC.code))
        sortingType=SortingType.getByCode(sortPref.getInt(Constants.SORT_PREF_NAME,NAME.code))
        defaultCurrency=Currency.getByCode(defaultCurrencyPref.getInt(Constants.CURRENCY_PREF_NAME,Currency.USD.code))
        viewModel.setDefaultCurrency(defaultCurrency)
        //view.context.deleteDatabase(SubscriptionDatabase.DATABASE_NAME)

        setupRecyclerView()
        setBottomSheetDialog(view)
        setSortSheetDialog(view)
        initBottomAppBar(context as MainActivity)
    }

    private fun initBottomAppBar(activity: MainActivity) {
        activity.setBottomAppBar(this)
        activity.addFab.setOnClickListener {
            viewModel.setViewState(SubscriptionViewState.INSERT)
            navController.navigate(R.id.action_subsList_to_sub)
        }
        activity.bottomAppBar.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.appbarSort -> sortSheetDialog.show()
            }
            true
        }
        activity.bottomAppBar.setNavigationOnClickListener { navigationSheetDialog.show() }
    }

    private fun setBottomSheetDialog(view: View) {
        navigationSheetDialog= BottomSheetDialog(view.context)
        navigationSheetDialog.setContentView(R.layout.navigation_bottom_sheet)

        navigationSheetDialog.themeChangerText.text=if (themePref.getInt(Constants.THEME_PREF_NAME,Constants.LIGHT_THEME)==Constants.LIGHT_THEME) getString(R.string.enable_dark_theme) else getString(R.string.disable_dark_theme)

        navigationSheetDialog.defaultCurrencySpinner.let {
            val sheetColor = TypedValue()
            val textColor = TypedValue()
            view.context.theme.resolveAttribute(R.attr.sheetbackgroundcolor,sheetColor,true)
            view.context.theme.resolveAttribute(R.attr.textcolor,textColor,true)

            val arrayAdapter = CurrencySpinnerAdapter(
                view.context,
                Currency.values().toList(),
                sheetColor.resourceId,
                textColor.resourceId)
            it.adapter = arrayAdapter
            it.setSelection(defaultCurrency.code)
        }

        navigationSheetDialog.defaultCurrencySpinner.onItemSelectedListener=object :AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {}

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                if (defaultCurrency!=Currency.getByCode(position)) {
                    defaultCurrency = Currency.getByCode(position)
                    viewModel.setDefaultCurrency(defaultCurrency)
                    setIntPrefs(
                        defaultCurrencyPref,
                        Constants.CURRENCY_PREF_NAME,
                        defaultCurrency.code
                    )
                    if (::adapter.isInitialized) {
                        adapter.setDefaultCurrency(defaultCurrency)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }

        navigationSheetDialog.themeChangeSheet.setOnClickListener {
            if(themePref.getInt(Constants.THEME_PREF_NAME,Constants.LIGHT_THEME)==Constants.LIGHT_THEME){
                setIntPrefs(themePref,Constants.THEME_PREF_NAME,Constants.DARK_THEME)
            }else{
                setIntPrefs(themePref,Constants.THEME_PREF_NAME,Constants.LIGHT_THEME)
            }
            val intent= Intent(context,MainActivity::class.java)
            startActivity(intent)
            (context as AppCompatActivity).finish()
            navigationSheetDialog.dismiss()
        }

        if ((view.context as MainActivity).getExchangeRate()!=null) {
            navigationSheetDialog.updateExchangeSheet.setOnClickListener {
                val bundle= bundleOf("defaultCurrency" to defaultCurrency.code)
                navController.navigate(R.id.action_subsList_to_rate,bundle)
                navigationSheetDialog.dismiss()
            }
        }else navigationSheetDialog.updateExchangeSheet.setGone()
    }

    private fun setSortSheetDialog(view:View){
        sortSheetDialog= BottomSheetDialog(view.context)
        sortSheetDialog.setContentView(R.layout.navigation_sort_bottom_sheet)
        sheetButtons= listOf(sortSheetDialog.nameAscBtn,sortSheetDialog.nameDescBtn,
            sortSheetDialog.priceAscBtn,sortSheetDialog.priceDescBtn,
            sortSheetDialog.paymentDateAscBtn,sortSheetDialog.paymentDateDescBtn)

        sortAndOrderController()

        setSheetDialogClickListener(sortSheetDialog.nameAscBtn,NAME,ASC)
        setSheetDialogClickListener(sortSheetDialog.nameDescBtn,NAME,DESC)
        setSheetDialogClickListener(sortSheetDialog.priceAscBtn,PRICE,ASC)
        setSheetDialogClickListener(sortSheetDialog.priceDescBtn,PRICE,DESC)
        setSheetDialogClickListener(sortSheetDialog.paymentDateAscBtn,PAYMENT_DATE,ASC)
        setSheetDialogClickListener(sortSheetDialog.paymentDateDescBtn,PAYMENT_DATE,DESC)
    }

    private fun setSheetDialogClickListener(imageButton: ImageButton,sortingType: SortingType,orderType: OrderType){
        imageButton.setOnClickListener {
            if (!(this.sortingType==sortingType && this.orderType==orderType)){
                setOrderSortValues(orderType,sortingType)
                sortAndOrderController()
            }
            sortSheetDialog.dismiss()
        }
    }

    private fun sortAndOrderController(){
        setupObservers()
        when(sortingType){
            NAME -> if (orderType== ASC) setSheetButtonColors(sortSheetDialog.nameAscBtn) else setSheetButtonColors(sortSheetDialog.nameDescBtn)
            PRICE -> if (orderType== ASC) setSheetButtonColors(sortSheetDialog.priceAscBtn) else setSheetButtonColors(sortSheetDialog.priceDescBtn)
            PAYMENT_DATE -> if (orderType== ASC) setSheetButtonColors(sortSheetDialog.paymentDateAscBtn) else setSheetButtonColors(sortSheetDialog.paymentDateDescBtn)
        }
    }

    private fun setOrderSortValues(orderType: OrderType,sortingType: SortingType){
        this.orderType= orderType
        this.sortingType= sortingType
        setIntPrefs(orderPref,Constants.ORDER_PREF_NAME,orderType.code)
        setIntPrefs(sortPref,Constants.SORT_PREF_NAME,sortingType.code)
    }

    private fun setSheetButtonColors(selectedButton:ImageButton){
        for (button in sheetButtons){
            if (button!= selectedButton) button.setTintColor(if (themePref.getInt(Constants.THEME_PREF_NAME,Constants.LIGHT_THEME)==Constants.LIGHT_THEME) R.color.Black else R.color.White)
        }
        selectedButton.setTintColor(R.color.Green)
    }

    private fun setupObservers() {
        viewModel.getAllSubscriptions(sortingType,orderType,limit).observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.setSubscriptionList(it)
            }
        })
    }

    private fun setupRecyclerView(){
        adapter= SubscriptionListAdapter()
        adapter.setSubscriptionManager(this)
        adapter.setDefaultCurrency(defaultCurrency)
        subscriptionsRV.apply {
            layoutManager=LinearLayoutManager(context)
            adapter=this@SubscriptionListFragment.adapter
        }
    }

//Subscription Manager
    override fun onClicked(subscription: Subscription) {
        val bundle= bundleOf("subscription" to subscription)
        navController.navigate(R.id.action_subsList_to_subView,bundle)
    }

    override fun resetPaymentDate(subscription: Subscription) {
        GlobalScope.launch(Dispatchers.Main){
            progressbarLayout.setVisible()
        }
        viewModel.updateSubscription(subscription,object:CoroutinesHandler{
            override fun onSuccess(message: String) {
                progressbarLayout.setGone()
            }

            override fun onError(exception: String) {
                progressbarLayout.setGone()
            }
        })
    }

    override fun onStop() {
        viewModel.cancelJobs()
        super.onStop()
    }

    override fun onDestroyView() {
        subscriptionsRV.adapter=null
        super.onDestroyView()
    }
}
