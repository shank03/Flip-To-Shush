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
import android.provider.Settings

class HideNotificationAction : BroadcastReceiver() {

    override fun onReceive(ctx: Context, int: Intent) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, ctx.applicationContext.packageName)
            intent.putExtra(Settings.EXTRA_CHANNEL_ID, "Running Service")
            ctx.startActivity(intent)

        } else {
            val intent = Intent("android.settings.APP_NOTIFICATION_SETTINGS").setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("app_package", ctx.applicationInfo.packageName)
            intent.putExtra("app_uid", ctx.applicationInfo.uid)
            ctx.startActivity(intent)
        }
    }
}