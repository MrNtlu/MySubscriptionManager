package com.mrntlu.mysubscriptionmanager.ui.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.mrntlu.mysubscriptionmanager.R

/**
 * A simple [Fragment] subclass.
 */
class SubscriptionStatsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_subscription_stats, container, false)
    }


}
