package com.mrntlu.mysubscriptionmanager.ui.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mrntlu.mysubscriptionmanager.R
import com.mrntlu.mysubscriptionmanager.adapters.SubscriptionListAdapter
import com.mrntlu.mysubscriptionmanager.interfaces.CoroutinesHandler
import com.mrntlu.mysubscriptionmanager.interfaces.SubscriptionManager
import com.mrntlu.mysubscriptionmanager.models.Subscription
import com.mrntlu.mysubscriptionmanager.models.SubscriptionViewState
import com.mrntlu.mysubscriptionmanager.ui.MainActivity
import com.mrntlu.mysubscriptionmanager.utils.*
import com.mrntlu.mysubscriptionmanager.viewmodels.SubscriptionViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_subscription_list.*
import kotlinx.android.synthetic.main.navigation_bottom_sheet.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SubscriptionListFragment : Fragment(), SubscriptionManager {

    private lateinit var adapter:SubscriptionListAdapter
    private lateinit var navController:NavController
    private lateinit var viewModel:SubscriptionViewModel
    private lateinit var prefs:SharedPreferences
    private lateinit var navigationSheetDialog:BottomSheetDialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_subscription_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        navController=Navigation.findNavController(view)
        viewModel=ViewModelProviders.of(context as AppCompatActivity).get(SubscriptionViewModel::class.java)
        viewModel.getAllSubscriptions(10)
        prefs=view.context.getSharedPreferences(Constants.PREFS_NAME,0)
        //view.context.deleteDatabase(SubscriptionDatabase.DATABASE_NAME)

        setupRecyclerView()
        setupObservers(10)
        setBottomSheetDialog(view)
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
                R.id.appbarSort -> showToast(context!!,"Sort clicked")
            }
            true
        }
        activity.bottomAppBar.setNavigationOnClickListener { navigationSheetDialog.show() }
    }

    private fun setBottomSheetDialog(view: View) {
        navigationSheetDialog= BottomSheetDialog(view.context)
        navigationSheetDialog.setContentView(R.layout.navigation_bottom_sheet)

        navigationSheetDialog.themeChangerText.text=if (prefs.getInt(Constants.PREFS_NAME,Constants.LIGHT_THEME)==Constants.LIGHT_THEME) "Enable Dark Theme" else "Disable Dark Theme"

        navigationSheetDialog.themeChangeSheet.setOnClickListener {
            if(prefs.getInt(Constants.PREFS_NAME,Constants.LIGHT_THEME)==Constants.LIGHT_THEME){
                setIntPrefs(prefs,Constants.PREFS_NAME,Constants.DARK_THEME)
            }else{
                setIntPrefs(prefs,Constants.PREFS_NAME,Constants.LIGHT_THEME)
            }
            val intent= Intent(context,MainActivity::class.java)
            startActivity(intent)
            (context as AppCompatActivity).finish()
            navigationSheetDialog.dismiss()
        }

        navigationSheetDialog.deleteAllSheet.setOnClickListener {
            showToast(it.context,"Delete all pressed.")
            navigationSheetDialog.dismiss()
        }
    }

    private fun setupObservers(limit:Int) {
        viewModel.getAllSubscriptions(limit).observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.setSubscriptionList(it)
                printLog(message = it.toString())
            }
        })
    }

    private fun setupRecyclerView(){
        adapter= SubscriptionListAdapter()
        adapter.setSubscriptionManager(this)
        subscriptionsRV.apply {
            layoutManager=LinearLayoutManager(context)
            adapter=this@SubscriptionListFragment.adapter
        }
    }

//Subscription Manager
    override fun onClicked(subscription: Subscription) {
        val bundle= bundleOf("subscription" to subscription)
        viewModel.setViewState(SubscriptionViewState.VIEW)
        navController.navigate(R.id.action_subsList_to_sub,bundle)
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
