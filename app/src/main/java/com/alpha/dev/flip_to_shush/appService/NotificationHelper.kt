/*
 * Copyright (c) 2020, Shashank Verma <shashank.verma2002@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 */

package com.alpha.dev.flip_to_shush.appService

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.alpha.dev.flip_to_shush.R
import com.alpha.dev.flip_to_shush.activities.MainActivity

class NotificationHelper(ctx: Context) : ContextWrapper(ctx) {

    private val channelID = "Running Service"
    private val channelID2 = "General Notification"
    private var mManager: NotificationManager? = null

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val channel = NotificationChannel(channelID, "Running Service", NotificationManager.IMPORTANCE_LOW)
        channel.lightColor = Color.BLUE
        channel.setSound(null, null)
        channel.enableVibration(false)

        val channel2 = NotificationChannel(channelID2, "General Notification", NotificationManager.IMPORTANCE_HIGH)
        channel2.lightColor = Color.BLUE
        channel2.setSound(null, null)
        channel2.enableVibration(true)

        getManager().createNotificationChannel(channel)
        getManager().createNotificationChannel(channel2)
    }

    fun getManager(): NotificationManager {
        if (mManager == null) {
            mManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }

        return mManager!!
    }

    fun runningNotification(): NotificationCompat.Builder {
        val intent = Intent(applicationContext, HideNotificationAction::class.java)
        val actionIntent = PendingIntent.getBroadcast(applicationContext, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val actIntent = Intent(applicationContext, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val openIntent = PendingIntent.getActivity(applicationContext, 0, actIntent, 0)

        return NotificationCompat.Builder(applicationContext, channelID)
            .setContentTitle("Running Service")
            .setSmallIcon(R.drawable.ic_phone_android_black_24dp)
            .setColorized(true)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setSummaryText("Running Service")
                    .bigText("Android OS wants any application running service other than system application should show notification.\n\nUnless if you want, tap on \'Hide Notification\' and uncheck the \"Running Service\" notification or turn off")
                    .setBigContentTitle("Running Service")
            )
            .setColor(Color.parseColor("#2C7DE8"))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setSound(null)
            .setContentIntent(openIntent)
            .addAction(R.drawable.ic_close_black_24dp, "Stop", PendingIntent.getBroadcast(applicationContext, 2, Intent(applicationContext, StopServiceAction::class.java), PendingIntent.FLAG_UPDATE_CURRENT))
            .addAction(R.drawable.ic_notifications_off_black_24dp, "Hide Notification", actionIntent)
    }

    fun lowerRunningNotification(): Notification.Builder {
        val intent = Intent(applicationContext, HideNotificationAction::class.java)
        val actionIntent = PendingIntent.getBroadcast(applicationContext, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val actIntent = Intent(applicationContext, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val openIntent = PendingIntent.getActivity(applicationContext, 0, actIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Notification.Builder(applicationContext)
                .setContentTitle("Running Service")
                .setSmallIcon(R.drawable.ic_phone_android_black_24dp)
                .setColor(Color.parseColor("#2C7DE8"))
                .setStyle(
                    Notification.BigTextStyle()
                        .bigText("Android OS wants any application running service other than system application should show notification.\n\nUnless if you want, tap on \'Hide Notification\' and uncheck the \"Running Service\" notification, or turn off")
                        .setBigContentTitle("Running Service")
                        .setSummaryText("Running Service")
                )
                .setPriority(Notification.PRIORITY_LOW)
                .setOngoing(true)
                .setSound(null)
                .setContentIntent(openIntent)
                .addAction(R.drawable.ic_close_black_24dp, "Stop", PendingIntent.getBroadcast(applicationContext, 2, Intent(applicationContext, StopServiceAction::class.java), PendingIntent.FLAG_UPDATE_CURRENT))
                .addAction(R.drawable.ic_notifications_off_black_24dp, "Hide Notification", actionIntent)
        } else {
            Notification.Builder(applicationContext)
                .setContentTitle("Running Service")
                .setSmallIcon(R.drawable.ic_phone_android_black_24dp)
                .setStyle(
                    Notification.BigTextStyle()
                        .bigText("Android OS wants any application running service other than system application should show notification.\n\nUnless if you want, tap on \'Hide Notification\' and uncheck the \"Running Service\" notification, or turn off")
                        .setBigContentTitle("Running Service")
                        .setSummaryText("Running Service")
                )
                .setPriority(Notification.PRIORITY_LOW)
                .setOngoing(true)
                .setSound(null)
                .setContentIntent(openIntent)
                .addAction(R.drawable.ic_close_black_24dp, "Stop", PendingIntent.getBroadcast(applicationContext, 2, Intent(applicationContext, StopServiceAction::class.java), PendingIntent.FLAG_UPDATE_CURRENT))
                .addAction(R.drawable.ic_notifications_off_black_24dp, "Hide Notification", actionIntent)
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    fun recreateChannel() {
        getManager().deleteNotificationChannel(channelID2)

        val channel2 = NotificationChannel(channelID2, "General Notification", NotificationManager.IMPORTANCE_HIGH)
        channel2.lightColor = Color.BLUE
        channel2.setSound(null, null)
        channel2.enableVibration(true)

        getManager().createNotificationChannel(channel2)
    }

    fun generalNotification(title: String, message: String): NotificationCompat.Builder {
        val actIntent = Intent(applicationContext, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val openIntent = PendingIntent.getActivity(applicationContext, 0, actIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        return NotificationCompat.Builder(applicationContext, channelID2)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.ic_phone_android_black_24dp)
            .setColor(Color.parseColor("#2C7DE8"))
            .setAutoCancel(true)
            .setContentIntent(openIntent)
    }

    fun lowerGeneralNotification(title: String, message: String): Notification.Builder {
        val actIntent = Intent(applicationContext, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val openIntent = PendingIntent.getActivity(applicationContext, 0, actIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Notification.Builder(applicationContext)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_phone_android_black_24dp)
                .setColor(Color.parseColor("#2C7DE8"))
                .setPriority(Notification.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(openIntent)
        } else {
            Notification.Builder(applicationContext)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_phone_android_black_24dp)
                .setPriority(Notification.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(openIntent)
        }
    }

    fun forceStartServiceNotification(): NotificationCompat.Builder {
        val intent = Intent(applicationContext, ForceStartService::class.java)
        val actionIntent = PendingIntent.getBroadcast(applicationContext, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val actIntent = Intent(applicationContext, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val openIntent = PendingIntent.getActivity(applicationContext, 0, actIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        return NotificationCompat.Builder(applicationContext, channelID2)
            .setContentTitle("Error Flip Service")
            .setContentText("User required to restart Flip Service. Tap on \"Force Start\" to try again")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.ic_phone_android_black_24dp)
            .setColor(Color.parseColor("#2C7DE8"))
            .setAutoCancel(true)
            .setContentIntent(openIntent)
            .addAction(R.drawable.ic_fiber_smart_record_black_24dp, "Force Start", actionIntent)
    }

    fun lowerForceStartServiceNotification(): Notification.Builder {
        val intent = Intent(applicationContext, ForceStartService::class.java)
        val actionIntent = PendingIntent.getBroadcast(applicationContext, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val actIntent = Intent(applicationContext, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val openIntent = PendingIntent.getActivity(applicationContext, 0, actIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Notification.Builder(applicationContext)
                .setContentTitle("Error Flip Service")
                .setContentText("User required to restart Flip Service. Tap on \"Force Start\" to try again")
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_phone_android_black_24dp)
                .setColor(Color.parseColor("#2C7DE8"))
                .setAutoCancel(true)
                .setContentIntent(openIntent)
                .addAction(R.drawable.ic_fiber_smart_record_black_24dp, "Force Start", actionIntent)
        } else {
            Notification.Builder(applicationContext)
                .setContentTitle("Error Flip Service")
                .setContentText("User required to restart Flip Service. Tap on \"Force Start\" to try again")
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_phone_android_black_24dp)
                .setAutoCancel(true)
                .setContentIntent(openIntent)
                .addAction(R.drawable.ic_fiber_smart_record_black_24dp, "Force Start", actionIntent)
        }
    }
}