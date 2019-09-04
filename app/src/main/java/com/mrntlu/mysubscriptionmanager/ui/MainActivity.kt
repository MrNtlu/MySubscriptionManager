package com.mrntlu.mysubscriptionmanager.ui

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.mrntlu.mysubscriptionmanager.R
import com.mrntlu.mysubscriptionmanager.ui.fragments.SubscriptionFragment
import com.mrntlu.mysubscriptionmanager.ui.fragments.SubscriptionListFragment
import com.mrntlu.mysubscriptionmanager.utils.Constants
import com.mrntlu.mysubscriptionmanager.utils.createDialog
import com.mrntlu.mysubscriptionmanager.utils.printLog
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onBackPressed() {
        when (val f=nav_host_fragment.childFragmentManager.findFragmentById(R.id.nav_host_fragment)) {
            is SubscriptionListFragment -> {
                val dialogBuilder = createDialog(this,"Do you want to exit?")
                dialogBuilder.setPositiveButton("Yes") { _, _ ->
                    super.onBackPressed()
                }.create().show()
            }
            is SubscriptionFragment -> {
                f.onBackPressedCallback()
            }
            else -> super.onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs=getSharedPreferences(Constants.PREFS_NAME,0)
        val themeCode=prefs.getInt(Constants.PREFS_NAME,Constants.LIGHT_THEME)

        if(themeCode==Constants.DARK_THEME){
            setTheme(R.style.DarkTheme)
            setStatusBarColor(R.color.Black)
        }else {
            setTheme(R.style.AppTheme)
            window.decorView.systemUiVisibility= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            setStatusBarColor(R.color.White)
        }
    }

    private fun setStatusBarColor(color:Int){
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor=resources.getColor(color,theme)
    }
}
