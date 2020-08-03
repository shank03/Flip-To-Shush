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

package com.alpha.dev.flip_to_shush.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alpha.dev.flip_to_shush.R
import com.alpha.dev.flip_to_shush.appService.FlipService
import com.alpha.dev.flip_to_shush.appService.NotificationHelper
import com.alpha.dev.flip_to_shush.appService.ServiceObserver
import com.alpha.dev.flip_to_shush.mTestRunning
import com.alpha.dev.flip_to_shush.makeToast
import com.alpha.dev.flip_to_shush.sdkAboveM
import kotlinx.android.synthetic.main.activity_feature_starter.*

class FeatureStarter : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feature_starter)

        val helper = NotificationHelper(this)

        try {
            helper.getManager().cancel(2)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            helper.getManager().cancel(3)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (sdkAboveM()) {
            if (helper.getManager().isNotificationPolicyAccessGranted) {
                if (ServiceObserver.getValue()) {
                    stat.text = "Already Running"
                    makeToast(this, "Already Running", Toast.LENGTH_LONG)
                } else {
                    if (mTestRunning) {
                        stat.text = "Cannot Start Now\nSensor test is Running"
                    } else {
                        stat.text = "Started"
                        startService(Intent(this, FlipService::class.java))
                    }
                }

                Handler().postDelayed({
                    finish()
                }, 1000)
            } else {
                startActivity(Intent(this, PermissionActivity::class.java))
                finish()
            }
        }
    }
}
