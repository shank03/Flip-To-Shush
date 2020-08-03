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

import android.annotation.TargetApi
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.alpha.dev.flip_to_shush.*
import kotlinx.android.synthetic.main.activity_permission.*

@RequiresApi(Build.VERSION_CODES.M)
class PermissionActivity : AppCompatActivity() {

    private lateinit var mManager: NotificationManager

    private var handler: Handler? = null
    private var runnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)

        mManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        permissionBtn.setOnClickListener {
            startActivityForResult(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS), DND_PERMISSION)
        }

        continueBtn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        /* Running a background check is policy is granted */
        handler = Handler()
        runnable = object : Runnable {
            @TargetApi(Build.VERSION_CODES.M)
            override fun run() {
                try {
                    if (mManager.isNotificationPolicyAccessGranted) {
                        continueBtn.visibility = View.VISIBLE
                        permissionBtn.isClickable = false
                    } else {
                        permissionBtn.isClickable = true
                        continueBtn.visibility = View.GONE
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    handler!!.postDelayed(this, 1500)
                }
            }
        }
        handler!!.post(runnable!!)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == DND_PERMISSION && mManager.isNotificationPolicyAccessGranted) {
            makeToast(this, "You can now continue", Toast.LENGTH_SHORT)
            continueBtn.visibility = View.VISIBLE
            startActivity(Intent(this, if (PreferenceHelper(this).getBoolean(TESTING, false)) MainActivity::class.java else SensorTesting::class.java))
            finish()
        } else {
            makeToast(this, "We need this permission", Toast.LENGTH_SHORT)
            continueBtn.visibility = View.GONE
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        recreate()
    }
}
