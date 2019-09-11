package com.mrntlu.mysubscriptionmanager.ui

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomappbar.BottomAppBar
import com.mrntlu.mysubscriptionmanager.R
import com.mrntlu.mysubscriptionmanager.interfaces.ExchangeRateHandler
import com.mrntlu.mysubscriptionmanager.models.Exchange
import com.mrntlu.mysubscriptionmanager.service.response.ExchangeRateResponse
import com.mrntlu.mysubscriptionmanager.ui.fragments.*
import com.mrntlu.mysubscriptionmanager.utils.*
import com.mrntlu.mysubscriptionmanager.viewmodels.ExchangeViewModel
import kotlinx.android.synthetic.main.activity_main.*
import org.joda.time.DateTime
import org.joda.time.Days
import java.util.*

class MainActivity : AppCompatActivity(), ExchangeRateHandler {

    override fun onBackPressed() {
        when (val f=nav_host_fragment.childFragmentManager.findFragmentById(R.id.nav_host_fragment)) {
            is SubscriptionListFragment -> {
                val dialogBuilder = createDialog(this,"Do you want to exit?")
                dialogBuilder.setPositiveButton("Yes") { _, _ ->
                    super.onBackPressed()
                }.create().show()
            }
            is SubscriptionFragment -> f.onBackPressed()
            is SubscriptionViewFragment -> navController.navigate(R.id.action_subView_to_subsList)
            is ExchangeRateFragment -> navController.navigate(R.id.action_rate_to_subsList)
            else -> super.onBackPressed()
        }
    }

    private lateinit var viewModel: ExchangeViewModel
    private lateinit var navController: NavController
    private var exchangeFetchHandler: ExchangeFetchHandler?=null
    private var exchangeRate:Exchange?=null
    private var isNavSet=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        activityProgressLayout.setVisible()
        viewModel= ViewModelProviders.of(this).get(ExchangeViewModel::class.java)

        val navHostFragment=nav_host_fragment as NavHostFragment
        navController=navHostFragment.navController

        setupObservers()

        val prefs=getSharedPreferences(Constants.THEME_PREF_NAME,0)
        val themeCode=prefs.getInt(Constants.THEME_PREF_NAME,Constants.LIGHT_THEME)

        if(themeCode==Constants.DARK_THEME){
            setTheme(R.style.DarkTheme)
            setStatusBarColor(R.color.Black)
        }else {
            setTheme(R.style.AppTheme)
            window.decorView.systemUiVisibility= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            setStatusBarColor(R.color.White)
        }
    }

    private fun setupNavigation(){
        val navHostFragment=nav_host_fragment as NavHostFragment
        val navInflater = navController.navInflater
        val graph = navInflater.inflate(R.navigation.main_nav_graph)
        navHostFragment.navController.graph = graph
        isNavSet=true
        activityProgressLayout.setGone()
    }

    private fun setupObservers(){
        viewModel.getAllExchanges().observe(this, Observer {
            when {
                it==null -> {
                    printLog(message = "It is null fetching...")
                    if (isInternetAvailable(this)) viewModel.fetchExchangeRates(this)
                    else exchangeRate=null
                }
                !isInternetAvailable(this) ->{
                    printLog(message = "No internet $it ${it.cacheDate}")
                    exchangeRate=it
                    if (!isNavSet) setupNavigation()
                }
                Days.daysBetween(DateTime(it.cacheDate),DateTime(Date())).days>0 -> {
                    printLog(message = "Days remained ${Days.daysBetween(DateTime(it.cacheDate),DateTime(Date())).days}")
                    viewModel.fetchExchangeRates(this)
                }
                else -> {
                    printLog(message = "DB resource $it ${it.cacheDate}")
                    exchangeRate=it
                    if (!isNavSet) setupNavigation()
                }
            }
        })
    }

    fun updateExchangeRate(exchangeFetchHandler:ExchangeFetchHandler){
        this.exchangeFetchHandler=exchangeFetchHandler
        viewModel.fetchExchangeRates(this)
    }

    fun getExchangeRate(): Exchange? {
        return exchangeRate
    }

    private fun setStatusBarColor(color:Int){
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor=resources.getColor(color,theme)
    }

    fun setBottomAppBar(fragment: Fragment) {
        when(fragment){
            is SubscriptionFragment->{
                bottomAppBar.fabAlignmentMode= BottomAppBar.FAB_ALIGNMENT_MODE_END
                setBottomAppBarItems(null,R.menu.subs_appbar_edit_menu,R.drawable.ic_clear_black_24dp)
            }
            is SubscriptionListFragment->{
                bottomAppBar.fabAlignmentMode= BottomAppBar.FAB_ALIGNMENT_MODE_CENTER
                setBottomAppBarItems(R.drawable.ic_dehaze_black_24dp,R.menu.bottom_appbar_menu,R.drawable.ic_add_black_24dp)
            }
            is SubscriptionViewFragment->{
                bottomAppBar.fabAlignmentMode= BottomAppBar.FAB_ALIGNMENT_MODE_END
                setBottomAppBarItems(null,R.menu.subs_appbar_view_menu,R.drawable.ic_edit_black_24dp)
            }
            is SubscriptionStatsFragment->{
                bottomAppBar.fabAlignmentMode= BottomAppBar.FAB_ALIGNMENT_MODE_END
                setBottomAppBarItems(null,R.menu.subs_appbar_edit_menu,R.drawable.ic_clear_black_24dp)
            }
            is ExchangeRateFragment->{
                bottomAppBar.fabAlignmentMode= BottomAppBar.FAB_ALIGNMENT_MODE_END
                setBottomAppBarItems(null,R.menu.subs_appbar_viewrate_menu,R.drawable.ic_clear_black_24dp)
            }
        }
    }

    private fun setBottomAppBarItems(drawable:Int?, menu:Int, fabDrawable:Int){
        invalidateOptionsMenu()
        bottomAppBar.navigationIcon=if (drawable!=null) resources.getDrawable(drawable,theme) else null
        bottomAppBar.replaceMenu(menu)
        addFab.setImageDrawable(resources.getDrawable(fabDrawable,theme))
    }

    override fun onFetchSucess(message: String, exchangeResponse: ExchangeRateResponse) {
        printLog(message="onfetch $exchangeResponse")
        if (exchangeFetchHandler!=null) exchangeFetchHandler=null
        viewModel.updateExchange(exchangeResponse.toExchange(),this)
    }

    override fun onError(exception: String) {
        printLog(message = "Error $exception")
        if (exchangeFetchHandler!=null){
            exchangeFetchHandler!!.onError()
            exchangeFetchHandler=null
        }
    }
}
