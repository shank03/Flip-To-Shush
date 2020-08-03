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
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Vibrator
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.alpha.dev.flip_to_shush.*
import com.alpha.dev.flip_to_shush.appService.FlipService
import com.alpha.dev.flip_to_shush.appService.ServiceObserver
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_sensor_testing.*

class SensorTesting : AppCompatActivity() {

    private var sensorManager: SensorManager? = null
    private lateinit var eventListener: SensorEventListener
    private lateinit var proximityListener: SensorEventListener
    private var proximity: Sensor? = null
    private var accelerometer: Sensor? = null
    private lateinit var v: Vibrator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor_testing)

        mTestRunning = true

        v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        declareListeners()
        initSensorManager()

        acc_start.setOnClickListener {
            if (ServiceObserver.getValue()) {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Hold Up !")
                    .setMessage("Flip service is running. Sensor test cannot be run without service being turned off.\nWould you like to turn it off and continue for test ?")
                    .setOnCancelListener {
                        it.dismiss()
                        finish()
                    }
                    .setPositiveButton("Yes") { dialogInterface, _ ->
                        stopService(Intent(this, FlipService::class.java))
                        accelerometerTests()
                        acc_start.visibility = View.GONE
                        accReading.visibility = View.VISIBLE

                        dialogInterface.dismiss()
                    }.setNegativeButton("Cancel") { dialogInterface, _ ->
                        dialogInterface.dismiss()
                        finish()
                    }.show()

            } else {
                accelerometerTests()
                acc_start.visibility = View.GONE
                accReading.visibility = View.VISIBLE
            }
        }

        pro_start.setOnClickListener {
            if (ServiceObserver.getValue()) {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Hold Up !")
                    .setMessage("Flip service is running. Sensor test cannot be run without service being turned off.\nWould you like to turn it off and continue for test ?")
                    .setOnCancelListener {
                        it.dismiss()
                        finish()
                    }
                    .setPositiveButton("Yes") { dialogInterface, _ ->
                        stopService(Intent(this, FlipService::class.java))
                        proximityTests()
                        pro_start.visibility = View.GONE
                        proReading.visibility = View.VISIBLE
                        proReading.text = "0.0"

                        dialogInterface.dismiss()
                    }.setNegativeButton("Cancel") { dialogInterface, _ ->
                        dialogInterface.dismiss()
                        finish()
                    }.show()
            } else {
                proximityTests()
                pro_start.visibility = View.GONE
                proReading.visibility = View.VISIBLE
                proReading.text = "0.0"
            }
        }

        finish.setOnClickListener {
            PreferenceHelper(this).putBoolean(TESTING, true)
            startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
            finish()
        }
    }

    private fun initSensorManager() {
        if (sensorManager != null) {
            accelerometer = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            proximity = sensorManager!!.getDefaultSensor(Sensor.TYPE_PROXIMITY)

            if (accelerometer == null) {
                val dialog = HelpDialog(this, "FATAL ERROR!", "Your device does not have accelerometer.")
                dialog.setCanceledOnTouchOutside(false)
                dialog.setOnCancelListener {
                    try {
                        finish()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                dialog.setOnDismissListener {
                    try {
                        finish()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                dialog.show()
                return
            }

            if (proximity == null) {
                val dialog = HelpDialog(this, "FATAL ERROR!", "Your device does not have proximity sensor.")
                dialog.setCanceledOnTouchOutside(false)
                dialog.setOnCancelListener {
                    try {
                        finish()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                dialog.setOnDismissListener {
                    try {
                        finish()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                dialog.show()
                return
            }
        }
    }

    private fun declareListeners() {
        var accTimeOver = false
        var s = false

        eventListener = object : SensorEventListener {
            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

            @SuppressLint("SetTextI18n")
            override fun onSensorChanged(p0: SensorEvent) {
                if (p0.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    val z = p0.values[2]
                    if (!accTimeOver) {
                        accReading.text = "$z $MS2"
                        if (!s) {
                            if (z >= 8) {
                                Handler().postDelayed({
                                    accTimeOver = true
                                }, 5 * 1000)
                                s = true
                            }
                        }
                    } else {
                        sensorManager!!.unregisterListener(eventListener, accelerometer)
                        accReading.text = "${z.toInt()} $MS2 (Completed)"
                        if (v.hasVibrator()) {
                            val pattern = longArrayOf(0, 300, 1000)
                            v.vibrate(pattern, -1)
                        }
                        proximityTesting.visibility = View.VISIBLE
                        s = false
                    }
                }
            }
        }

        var inP = -1f
        var count = -1
        proximityListener = object : SensorEventListener {
            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

            @SuppressLint("SetTextI18n")
            override fun onSensorChanged(p0: SensorEvent) {
                val p = p0.values[0]
                proReading.text = "$p"

                if (inP != p) {
                    inP = p
                    count++

                    if (!s) {
                        Handler().postDelayed({
                            if (count < 5) {
                                sensorManager!!.unregisterListener(proximityListener, proximity)
                                proReading.text = getString(R.string.failed)
                                pro_start.visibility = View.VISIBLE
                                pro_start.text = "Retry"
                                count = -1
                                scView.fullScroll(View.FOCUS_DOWN)
                            }
                        }, 10 * 1000)
                        s = true
                    }
                }

                if (count >= 5) {
                    sensorManager!!.unregisterListener(proximityListener, proximity)
                    proReading.text = "$p (Completed)"
                    finish.visibility = View.VISIBLE
                    scView.fullScroll(View.FOCUS_DOWN)
                    if (v.hasVibrator()) {
                        val pattern = longArrayOf(0, 300, 1000)
                        v.vibrate(pattern, -1)
                    }
                }
            }
        }
    }

    private fun accelerometerTests() {
        acc_des.visibility = View.GONE
        Handler().postDelayed({
            acc_des.text = getString(R.string.acc_test_started)
            acc_des.visibility = View.VISIBLE

            sensorManager!!.registerListener(eventListener, accelerometer, SensorManager.SENSOR_ACCELEROMETER)
        }, 500)
    }

    private fun proximityTests() {
        pro_des.visibility = View.GONE
        Handler().postDelayed({
            pro_des.text = getString(R.string.pro_test_started)
            pro_des.visibility = View.VISIBLE

            sensorManager!!.registerListener(proximityListener, proximity, SensorManager.SENSOR_PROXIMITY)
        }, 500)
    }

    /*private fun isMyServiceRunning(): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (FlipService::class.java.name == service.service.className) {
                return true
            } else if (FlipServiceCompat::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }*/

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        recreate()
    }

    override fun onPause() {
        super.onPause()
        mTestRunning = false
        finish()
    }

    override fun onBackPressed() {
        mTestRunning = false
        try {
            sensorManager!!.unregisterListener(eventListener, accelerometer)
            sensorManager!!.unregisterListener(proximityListener, proximity)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        finish()
    }
}
