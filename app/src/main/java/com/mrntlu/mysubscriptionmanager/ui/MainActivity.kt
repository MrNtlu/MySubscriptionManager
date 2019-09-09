package com.mrntlu.mysubscriptionmanager.ui

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomappbar.BottomAppBar
import com.mrntlu.mysubscriptionmanager.R
import com.mrntlu.mysubscriptionmanager.interfaces.ExchangeRateHandler
import com.mrntlu.mysubscriptionmanager.models.Exchange
import com.mrntlu.mysubscriptionmanager.service.response.ExchangeRateResponse
import com.mrntlu.mysubscriptionmanager.ui.fragments.SubscriptionFragment
import com.mrntlu.mysubscriptionmanager.ui.fragments.SubscriptionListFragment
import com.mrntlu.mysubscriptionmanager.ui.fragments.SubscriptionStatsFragment
import com.mrntlu.mysubscriptionmanager.ui.fragments.SubscriptionViewFragment
import com.mrntlu.mysubscriptionmanager.utils.Constants
import com.mrntlu.mysubscriptionmanager.utils.createDialog
import com.mrntlu.mysubscriptionmanager.utils.printLog
import com.mrntlu.mysubscriptionmanager.utils.toExchange
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
            is SubscriptionFragment -> {
                f.onBackPressed()
            }
            else -> super.onBackPressed()
        }
    }

    private lateinit var viewModel: ExchangeViewModel
    private lateinit var exchangeRate:Exchange

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel= ViewModelProviders.of(this).get(ExchangeViewModel::class.java)

        viewModel.getAllExchanges().observe(this, Observer {
            when {
                it==null -> {
                    printLog(message = "It is null fetching...")
                    viewModel.fetchExchangeRates(this)
                }
                Days.daysBetween(DateTime(it.cacheDate),DateTime(Date())).days>0 -> {
                    printLog(message = Days.daysBetween(DateTime(Date()),DateTime(it.cacheDate)).days.toString())
                    viewModel.fetchExchangeRates(this)
                }
                else -> {
                    printLog(message = Days.daysBetween(DateTime(Date()),DateTime(it.cacheDate)).days.toString())
                    printLog(message = Days.daysBetween(DateTime(it.cacheDate),DateTime(Date())).days.toString())
                    printLog(message = "DB resource $it ${it.cacheDate}")
                }
            }
        })

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

    fun getExchangeRate(): Exchange {
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
                setBottomAppBarItems(null,R.menu.subs_appbar_edit_menu,R.drawable.ic_edit_black_24dp)
            }
            is SubscriptionStatsFragment->{
                bottomAppBar.fabAlignmentMode= BottomAppBar.FAB_ALIGNMENT_MODE_END
                setBottomAppBarItems(null,R.menu.subs_appbar_edit_menu,R.drawable.ic_clear_black_24dp)
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
        printLog(message="$exchangeResponse onfetch")
        viewModel.updateExchange(exchangeResponse.toExchange(),this)
    }

    override fun onInsertSucess(message: String, exchange: Exchange) {
        printLog(message="$exchange onInsert")
        exchangeRate=exchange
    }

    override fun onError(exception: String) {
        printLog(message = exception)
    }
}
