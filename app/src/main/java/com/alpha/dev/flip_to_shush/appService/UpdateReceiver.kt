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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.alpha.dev.flip_to_shush.OFF_APP_TOAST
import com.alpha.dev.flip_to_shush.PreferenceHelper
import com.alpha.dev.flip_to_shush.SERVICE_STARTED
import com.alpha.dev.flip_to_shush.sdkAboveM

class UpdateReceiver : BroadcastReceiver() {

    override fun onReceive(p0: Context, p1: Intent?) {
        if (p1 != null) {
            if (p1.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
                if (PreferenceHelper(p0).getBoolean(SERVICE_STARTED, false)) {
                    if (!ServiceObserver.getValue()) {
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                p0.startForegroundService(Intent(p0.applicationContext, FlipService::class.java).putExtra(OFF_APP_TOAST, PreferenceHelper(p0).getBoolean(OFF_APP_TOAST, true)))
                            } else {
                                p0.startService(
                                    Intent(p0.applicationContext, if (sdkAboveM()) FlipService::class.java else FlipServiceCompat::class.java).putExtra(
                                        OFF_APP_TOAST,
                                        PreferenceHelper(p0).getBoolean(OFF_APP_TOAST, true)
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            val helper = NotificationHelper(p0)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                helper.recreateChannel()
                                val nb = helper.forceStartServiceNotification()
                                helper.getManager().notify(2, nb.build())
                            } else {
                                val nb = helper.lowerForceStartServiceNotification()
                                helper.getManager().notify(2, nb.build())
                            }
                        }
                    }
                }
            }
        }
    }
}