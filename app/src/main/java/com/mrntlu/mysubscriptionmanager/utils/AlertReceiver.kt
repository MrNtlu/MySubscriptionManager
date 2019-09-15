package com.mrntlu.mysubscriptionmanager.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.*

class AlertReceiver:BroadcastReceiver() {

    override fun onReceive(context: Context?, i: Intent?) {
        val message=i?.getStringExtra("message")
        val hashCode=i?.getIntExtra("hastCode",Random().nextInt(9999 - 1000) + 1000)

        context?.let {
            val notificationHelper=NotificationHelper(message!!,it)
            val nb=notificationHelper.getChannelNotification()

            notificationHelper.getManager().notify(hashCode!!,nb.build())
        }
    }
}