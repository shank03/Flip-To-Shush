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

/**********************************************************************************************************************
 * Alpha Developer<sup>TM</sup> Inc. Copyright (c) 2019.                                                              *
 * This is the work of, from and by individual team member.  Any duplication or publication of this code without our  *
 * consent AND without this Copyrighted Text is subjected to Copyright Infringement and could even lead to court      *
 * cases.                                                                                                             *
 *                                                                                                                    *
 * Contact emails :-                                                                                                  *
 *     (Group) alphadeveloper3@gmail.com                                                                              *
 *     (Team Leader) shashank.verma2002@gmail.com                                                                     *
 **********************************************************************************************************************/

package com.alpha.dev.flip_to_shush.activities

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.alpha.dev.flip_to_shush.*
import com.alpha.dev.flip_to_shush.appService.FlipService
import com.alpha.dev.flip_to_shush.appService.FlipServiceCompat
import com.alpha.dev.flip_to_shush.appService.ServiceObserver
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    private var mIntensity = 0
    private var mUpdate = 0

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val pr = PreferenceHelper(this)
        mIntensity = pr.getInt(INTENSITY, 250)
        val isStock = pr.getBoolean(SENSE_TYPE, true)
        if (sdkAboveM()) {
            filterStat.text = when (pr.getInt(FILTER, NotificationManager.INTERRUPTION_FILTER_NONE)) {
                NotificationManager.INTERRUPTION_FILTER_NONE -> "None"
                NotificationManager.INTERRUPTION_FILTER_ALARMS -> "Alarms"
                else -> "Priority"
            }
        } else {
            in_dnd.text = "On Flip Detected"
            sub_int.text = "Mode to enable : "
            filterStat.text = if (pr.getInt(RINGER_MODE, AudioManager.RINGER_MODE_SILENT) == AudioManager.RINGER_MODE_SILENT) "Silent Mode" else "Vibration Mode"
        }

        val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        stockCheck.isChecked = isStock
        allTimeCheck.isChecked = !isStock

        offToastCheck.isChecked = pr.getBoolean(OFF_APP_TOAST, true)

        stockCheck.setOnCheckedChangeListener { compoundButton, _ ->
            if (compoundButton.isChecked) {
                changeSenseType(true)
                if (ServiceObserver.getValue()) {
                    stopService(Intent(this@SettingsActivity, if (sdkAboveM()) FlipService::class.java else FlipServiceCompat::class.java))

                    Handler().postDelayed({
                        if (!ServiceObserver.getValue()) {
                            startService(Intent(this@SettingsActivity, if (sdkAboveM()) FlipService::class.java else FlipServiceCompat::class.java))
                            Snackbar.make(findViewById(R.id.snack), "Settings Applied\nService Restarted", Snackbar.LENGTH_LONG).show()
                        }
                    }, 100)
                } else {
                    Snackbar.make(findViewById(R.id.snack), "Settings Applied", Snackbar.LENGTH_LONG).show()
                }
            }
        }

        allTimeCheck.setOnCheckedChangeListener { compoundButton, _ ->
            if (compoundButton.isChecked) {
                changeSenseType(false)
                if (ServiceObserver.getValue()) {
                    stopService(Intent(this@SettingsActivity, if (sdkAboveM()) FlipService::class.java else FlipServiceCompat::class.java))

                    Handler().postDelayed({
                        if (!ServiceObserver.getValue()) {
                            startService(Intent(this@SettingsActivity, if (sdkAboveM()) FlipService::class.java else FlipServiceCompat::class.java))
                            Snackbar.make(findViewById(R.id.snack), "Settings Applied\nService Restarted", Snackbar.LENGTH_LONG).show()
                        }
                    }, 100)
                } else {
                    Snackbar.make(findViewById(R.id.snack), "Settings Applied", Snackbar.LENGTH_LONG).show()
                }
            }
        }

        offToastCheck.setOnCheckedChangeListener { _, isChecked ->
            pr.putBoolean(OFF_APP_TOAST, isChecked)
            if (ServiceObserver.getValue()) {
                stopService(Intent(this@SettingsActivity, if (sdkAboveM()) FlipService::class.java else FlipServiceCompat::class.java))

                Handler().postDelayed({
                    if (!ServiceObserver.getValue()) {
                        startService(Intent(this@SettingsActivity, if (sdkAboveM()) FlipService::class.java else FlipServiceCompat::class.java))
                        Snackbar.make(findViewById(R.id.snack), "Settings Applied\nService Restarted", Snackbar.LENGTH_LONG).show()
                    }
                }, 100)
            } else {
                Snackbar.make(findViewById(R.id.snack), "Settings Applied", Snackbar.LENGTH_LONG).show()
            }
        }

        seekBar.progress = if (mIntensity == 65) 0 else mIntensity
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                if (v.hasVibrator()) {
                    val pattern = longArrayOf(0, if (mUpdate != 0) mUpdate.toLong() else 65, 1000)
                    v.vibrate(pattern, -1)
                }
                scrollView.fullScroll(View.FOCUS_DOWN)
            }

            override fun onProgressChanged(p0: SeekBar?, i: Int, p2: Boolean) {
                Log.d("PROGRESS", "$i")

                mUpdate = getProgress(i)
                seekBar.progress = mUpdate

                applyBtn.visibility = if (mUpdate != mIntensity) View.VISIBLE else View.GONE
                scrollView.fullScroll(View.FOCUS_DOWN)
            }
        })

        applyBtn.setOnClickListener {
            mIntensity = mUpdate
            if (mUpdate == 0) {
                mUpdate = 65
            }

            PreferenceHelper(this@SettingsActivity).putInt(INTENSITY, mUpdate)

            if (ServiceObserver.getValue()) {
                stopService(Intent(this@SettingsActivity, if (sdkAboveM()) FlipService::class.java else FlipServiceCompat::class.java))

                Handler().postDelayed({
                    if (!ServiceObserver.getValue()) {
                        startService(Intent(this@SettingsActivity, if (sdkAboveM()) FlipService::class.java else FlipServiceCompat::class.java))
                        Snackbar.make(findViewById(R.id.snack), "Settings Applied\nService Restarted", Snackbar.LENGTH_LONG).show()
                    }
                }, 100)
            } else {
                Snackbar.make(findViewById(R.id.snack), "Settings Applied", Snackbar.LENGTH_LONG).show()
            }

            applyBtn.visibility = View.GONE
        }

        interruptionChooser.setOnClickListener {
            InterruptionDialog(this@SettingsActivity, filterStat).show()
        }
    }

    private fun getProgress(i: Int): Int {
        return if (i in 0..125) {
            if (i > 62.5) 125 else 0

        } else if (i in 125..250) {
            if (i > 187.5) 250 else 125

        } else if (i in 250..375) {
            if (i > 303.5) 375 else 250

        } else /*if (i in 300.0..400.0)*/ {
            if (i > 437.5) 500 else 375
        }
    }

    private fun changeSenseType(bool: Boolean) {
        PreferenceHelper(this).putBoolean(SENSE_TYPE, bool)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        recreate()
    }
}
