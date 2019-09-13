package com.mrntlu.mysubscriptionmanager.utils

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.core.app.NotificationCompat
import com.mrntlu.mysubscriptionmanager.R

class NotificationHelper(var message:String,val context: Context):ContextWrapper(context) {

    companion object{
        const val channelID="channelID"
        const val channelName="Subscription Reminder"
    }

    private var mManager:NotificationManager?=null

    init {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            createChannels()
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createChannels() {
        val channel1=NotificationChannel(channelID, channelName,NotificationManager.IMPORTANCE_DEFAULT)
        channel1.enableVibration(true)
        channel1.enableLights(true)
        channel1.lightColor=getColor(R.color.colorPrimary)
        channel1.lockscreenVisibility=Notification.VISIBILITY_PRIVATE

        getManager().createNotificationChannel(channel1)
    }

    fun getManager():NotificationManager{
        if (mManager==null){
            mManager=getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }
        return mManager!!
    }

    fun getChannelNotification():NotificationCompat.Builder{
        return NotificationCompat.Builder(context, channelID)
            .setContentTitle("Payment Time!")
            .setContentText(message)
            .setSmallIcon(R.mipmap.ic_launcher)
    }
}