package com.mrntlu.mysubscriptionmanager.interfaces

import com.mrntlu.mysubscriptionmanager.models.Subscription

interface SubscriptionManager{
    fun onClicked(subscription: Subscription)

    fun resetPaymentDate(subscription: Subscription)
}