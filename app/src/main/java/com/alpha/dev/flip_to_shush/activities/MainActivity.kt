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
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import com.alpha.dev.flip_to_shush.*
import com.alpha.dev.flip_to_shush.appService.FlipService
import com.alpha.dev.flip_to_shush.appService.FlipServiceCompat
import com.alpha.dev.flip_to_shush.appService.NotificationHelper
import com.alpha.dev.flip_to_shush.appService.ServiceObserver
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var manager: NotificationManager

    private var running = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        try {
            NotificationHelper(this).getManager().cancel(2)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (!sdkAboveM()) ab_des.text = ABOUT_DES_M

        ServiceObserver.serviceRunning.observe(this, Observer {
            running = it
            if (it) {
                enable.text = "Running"
                enable.isClickable = false
                stop.visibility = View.VISIBLE
                if (sdkAboveM()) {
                    enable.setTextColor(getColor(R.color.colorPrimary))
                    enable.backgroundTintList = ColorStateList.valueOf(getColor(R.color.green))
                } else {
                    enable.setTextColor(resources.getColor(R.color.colorPrimary))
                    enable.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.green))
                }
            } else {
                enable.text = "Enable"
                enable.isClickable = true
                stop.visibility = View.GONE
                if (sdkAboveM()) {
                    enable.setTextColor(getColor(R.color.colorPrimary))
                    enable.backgroundTintList = ColorStateList.valueOf(getColor(R.color.colorAccent))
                } else {
                    enable.setTextColor(resources.getColor(R.color.colorPrimary))
                    enable.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.colorAccent))
                }
            }
        })

        enable.setOnClickListener {
            if (sdkAboveM()) {
                if (manager.isNotificationPolicyAccessGranted) {
                    if (!running) {
                        try {
                            NotificationHelper(this).getManager().cancel(3)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        saveStartPref(true)
                        startService(Intent(this@MainActivity, FlipService::class.java))
                    } else if (enable.text != "Running") {
                        makeToast(this, "Something went wrong.\n\nPlease force close app and try again", Toast.LENGTH_LONG)
                        //Toast.makeText(this@MainActivity, "Something went wrong.\n\nPlease force close app and try again", Toast.LENGTH_LONG).show()
                    }
                } else {
                    makeToast(this, "We need permission", Toast.LENGTH_LONG)
                    //Toast.makeText(this@MainActivity, "We need permission", Toast.LENGTH_LONG).show()
                    startActivityForResult(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS), 102)
                }
            } else {
                if (!running) {
                    try {
                        NotificationHelper(this).getManager().cancel(3)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    saveStartPref(true)
                    startService(Intent(this@MainActivity, FlipServiceCompat::class.java))
                } else if (enable.text != "Running") {
                    makeToast(this, "Something went wrong.\n\nPlease force close app and try again", Toast.LENGTH_LONG)
                    //Toast.makeText(this@MainActivity, "Something went wrong.\n\nPlease force close app and try again", Toast.LENGTH_LONG).show()
                }
            }
        }

        stop.setOnClickListener {
            if (running) {
                stopService(Intent(this@MainActivity, if (sdkAboveM()) FlipService::class.java else FlipServiceCompat::class.java))

                Handler().postDelayed({
                    if (!running) saveStartPref(false)
                }, 100)
            }
        }

        bar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.contact_dev -> {
                    val intent = Intent(Intent.ACTION_SENDTO).setData(Uri.parse("mailto:shashank.verma2002@gmail.com"))

                    if (intent.resolveActivity(packageManager) != null) startActivity(intent)
                    true
                }

                R.id.sensor_test -> {
                    if (running) {
                        Snackbar.make(findViewById(R.id.m_c), "Can't test sensor while service is Running", Snackbar.LENGTH_LONG).show()
                    } else startActivity(Intent(this@MainActivity, SensorTesting::class.java))
                    true
                }

                R.id.privacy_viewer -> {
                    startActivity(Intent(this@MainActivity, PrivacyViewer::class.java))
                    true
                }

                R.id.theme -> {
                    ThemeDialog(this@MainActivity).show()
                    true
                }

                R.id.rate -> {
                    val goToMarket = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
                    goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY /*or Intent.FLAG_ACTIVITY_NEW_DOCUMENT*/ or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)

                    try {
                        startActivity(goToMarket)
                    } catch (e: ActivityNotFoundException) {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=$packageName")))
                    }

                    true
                }

                R.id.share -> {
                    val shareIntent = Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_TEXT, "Hey check out this app at: https://play.google.com/store/apps/details?id=$packageName").setType("text/plain")
                    startActivity(shareIntent)

                    true
                }

                R.id.help -> {
                    startActivity(Intent(this@MainActivity, HelpActivity::class.java))
                    true
                }

                else -> false
            }
        }

        bar.setNavigationOnClickListener {
            startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
        }

        if (PreferenceHelper(this).getBoolean("f", true)) {
            PreferenceHelper(this).putBoolean("f", false)
            guideUser()
        }
    }

    private fun saveStartPref(bool: Boolean) {
        PreferenceHelper(this).putBoolean(SERVICE_STARTED, bool)
    }

    private fun guideUser() {
        val sequence = TapTargetSequence(this@MainActivity)
                .targets(
                        TapTarget.forToolbarMenuItem(findViewById<Toolbar>(R.id.bar), R.id.help, "Get Help & FAQ", "You can find your answers to common doubts you get :)")
                                .textTypeface(Typeface.createFromAsset(assets, "google_sans.ttf"))
                                .titleTextColorInt(Color.WHITE)
                                .descriptionTextColorInt(Color.parseColor("#FAFAFA"))
                                .transparentTarget(true)
                                .dimColorInt(Color.parseColor("#F7F7F7"))
                                .outerCircleColorInt(Color.parseColor("#2C7DE8"))
                                .cancelable(false),

                        TapTarget.forToolbarMenuItem(findViewById<Toolbar>(R.id.bar), R.id.sensor_test, "Sensor ", "You can test your sensors here anytime :)")
                                .textTypeface(Typeface.createFromAsset(assets, "google_sans.ttf"))
                                .dimColorInt(Color.parseColor("#F7F7F7"))
                                .titleTextColorInt(Color.WHITE)
                                .descriptionTextColorInt(Color.parseColor("#FAFAFA"))
                                .transparentTarget(true)
                                .outerCircleColorInt(Color.parseColor("#2C7DE8"))
                                .cancelable(false),

                        TapTarget.forToolbarNavigationIcon(findViewById<Toolbar>(R.id.bar), "Settings", "You can select the way for Flip detection to work :)")
                                .textTypeface(Typeface.createFromAsset(assets, "google_sans.ttf"))
                                .dimColorInt(Color.parseColor("#F7F7F7"))
                                .titleTextColorInt(Color.WHITE)
                                .descriptionTextColorInt(Color.parseColor("#FAFAFA"))
                                .transparentTarget(true)
                                .outerCircleColorInt(Color.parseColor("#2C7DE8"))
                                .cancelable(false)
                )

        sequence.start()
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (sdkAboveM()) {
            if (requestCode == 102 && manager.isNotificationPolicyAccessGranted) {
                if (!running) {
                    saveStartPref(true)
                    startService(Intent(this@MainActivity, FlipService::class.java))
                } else makeToast(this, "Something went wrong.\n\nPlease force close app and try again", Toast.LENGTH_LONG)
            } else makeToast(this, "We need this Permission", Toast.LENGTH_SHORT)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_file, menu)
        return true
    }

    override fun onStart() {
        mRunning = true
        super.onStart()
    }

    override fun onPause() {
        mRunning = false
        super.onPause()
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        mRunning = true
        super.onResume()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        recreate()
    }
}