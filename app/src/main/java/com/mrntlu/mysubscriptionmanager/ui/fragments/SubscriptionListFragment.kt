package com.mrntlu.mysubscriptionmanager.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.mrntlu.mysubscriptionmanager.R
import com.mrntlu.mysubscriptionmanager.adapters.SubscriptionListAdapter
import com.mrntlu.mysubscriptionmanager.interfaces.CoroutinesHandler
import com.mrntlu.mysubscriptionmanager.interfaces.SubscriptionManager
import com.mrntlu.mysubscriptionmanager.models.Subscription
import com.mrntlu.mysubscriptionmanager.models.SubscriptionViewState
import com.mrntlu.mysubscriptionmanager.utils.dateFormatLong
import com.mrntlu.mysubscriptionmanager.utils.printLog
import com.mrntlu.mysubscriptionmanager.utils.setGone
import com.mrntlu.mysubscriptionmanager.utils.setVisible
import com.mrntlu.mysubscriptionmanager.viewmodels.SubscriptionViewModel
import kotlinx.android.synthetic.main.fragment_subscription_list.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SubscriptionListFragment : Fragment(), SubscriptionManager {

    private lateinit var adapter:SubscriptionListAdapter
    private lateinit var navController:NavController
    private lateinit var viewModel:SubscriptionViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_subscription_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        navController=Navigation.findNavController(view)
        viewModel=ViewModelProviders.of(context as AppCompatActivity).get(SubscriptionViewModel::class.java)
        viewModel.getAllSubscriptions(10)

        //view.context.deleteDatabase(SubscriptionDatabase.DATABASE_NAME)

        setupToolbar()
        setupRecyclerView()
        setupObservers(10)
    }

    private fun setupObservers(limit:Int) {
        viewModel.getAllSubscriptions(limit).observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.setSubscriptionList(it)
                printLog(message = it.toString())
            }
        })
    }

    private fun setupToolbar(){
        toolbarSubList?.title = "Subscriptions"
        (activity as AppCompatActivity).setSupportActionBar(toolbarSubList)

    }

    private fun setupRecyclerView(){
        adapter= SubscriptionListAdapter()
        adapter.setSubscriptionManager(this)
        subscriptionsRV.apply {
            layoutManager=LinearLayoutManager(context)
            adapter=this@SubscriptionListFragment.adapter
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.subscription_toolbar_menu,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.addSubscriptionMenu) {
            viewModel.setViewState(SubscriptionViewState.INSERT)
            navController.navigate(R.id.action_subsList_to_sub)
        }
        return super.onOptionsItemSelected(item)
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
        (activity as AppCompatActivity).setSupportActionBar(null) //To prevent memory leaks
        subscriptionsRV.adapter=null
        super.onDestroyView()
    }
}
