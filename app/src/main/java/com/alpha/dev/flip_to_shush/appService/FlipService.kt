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

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.display.DisplayManager
import android.os.*
import android.util.Log
import android.view.Display
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.alpha.dev.flip_to_shush.*
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

@RequiresApi(Build.VERSION_CODES.M)
class FlipService : Service() {

    override fun onBind(p0: Intent?): IBinder? = null

    private var mStarted = false

    private lateinit var helper: NotificationHelper
    private var sensorManager: SensorManager? = null
    private lateinit var eventListener: SensorEventListener
    private lateinit var proximityListener: SensorEventListener
    private lateinit var proximity: Sensor
    private lateinit var accelerometer: Sensor
    private lateinit var v: Vibrator
    private var updatable = false
    private var isStock = true
    private var enabledDND = false

    private var mGZ: Float = 0f    /*gravity acceleration along the z axis*/
    private var mEventCountSinceGZChanged = 0
    private var mZ: Float = 0f
    private var mATDEventCountChange = 0
    private val maxCountGzChange = 5
    private var prx = 5f
    private val maxChange = 10
    private var mIntensity = 0
    private var mFilter = 0
    private var mToast = true

    private lateinit var pm: PowerManager
    private lateinit var displayManager: DisplayManager

    var handler: Handler? = null
    private var runnable: Runnable? = null
    private var thread: HandlerThread? = null

    override fun onCreate() {
        helper = NotificationHelper(applicationContext)
        val pr = PreferenceHelper(applicationContext)
        isStock = pr.getBoolean(SENSE_TYPE, true)
        mIntensity = pr.getInt(INTENSITY, 250)
        mFilter = pr.getInt(FILTER, NotificationManager.INTERRUPTION_FILTER_NONE)

        pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager

        /*Starting foreground as per Android rules and guidelines*/
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val nb = helper.runningNotification()
                startForeground(1, nb.build())
            } else {
                val nb = helper.lowerRunningNotification()
                startForeground(1, nb.build())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        /* Defining sensor listeners */
        eventListener = object : SensorEventListener {
            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

            override fun onSensorChanged(p0: SensorEvent) {
                if (p0.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    if (isStock) processStockDetection(p0) else processAllTimeDetection(p0)
                }
            }
        }

        proximityListener = object : SensorEventListener {
            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

            override fun onSensorChanged(p0: SensorEvent) {
                Log.d("VAL PROXIMITY", "${p0.values[0]}")
                if (updatable) {
                    prx = p0.values[0]
                    updatable = prx != 0f
                }
                Log.d("UPDATE", "$updatable")
            }
        }

        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!mStarted) {
            mStarted = true
            ServiceObserver.serviceRunning.postValue(true)
            val data = intent?.extras
            if (data != null) {
                mToast = data.getBoolean(OFF_APP_TOAST, true)
            }
            if (mToast) {
                Handler().post {
                    makeToast(applicationContext, "Flip Service Started", Toast.LENGTH_SHORT)
                }
            }
            v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

            if (sensorManager != null) {
                accelerometer = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                proximity = sensorManager!!.getDefaultSensor(Sensor.TYPE_PROXIMITY)

                /* Processing in new thread*/
                thread = HandlerThread("Flip Service Sensor Thread", Thread.MAX_PRIORITY)
                thread?.start()
                val sensorHandler = Handler(thread!!.looper)

                sensorManager?.registerListener(eventListener, accelerometer, Sensor.TYPE_ACCELEROMETER, sensorHandler)
                sensorManager?.registerListener(proximityListener, proximity, Sensor.TYPE_PROXIMITY, sensorHandler)

                /*Keeping track of notification policy access in background*/
                handler = Handler()
                runnable = object : Runnable {
                    override fun run() {
                        try {
                            if (!helper.getManager().isNotificationPolicyAccessGranted) {
                                finish()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            handler?.postDelayed(this, 700)
                        }
                    }
                }
                handler?.post(runnable!!)
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val nb = helper.generalNotification("Error Flip Service", "Could not get sensor from your device. Either try again or please contact developer from app.")
                    helper.getManager().notify(3, nb.build())
                } else {
                    val nb = helper.lowerGeneralNotification("Error Flip Service", "Could not get sensor from your device. Either try again or please contact developer from app.")
                    helper.getManager().notify(3, nb.build())
                }
                finish()
            }
        }
        return START_STICKY
    }

    private fun processStockDetection(p0: SensorEvent) {
        val gz = p0.values[2]
        /*val x = p0.values[0]
        val y = p0.values[1]
        val angle = tan2(x, y) / (PI / 180)*/

        if (gz >= 0) {
            if (enabledDND && !isDisplayOFF()) disableDND()
        }

        if (mGZ == 0.toFloat()) {
            mGZ = gz
        } else {
            if ((mGZ * gz) < 0) {
                updatable = true
                mEventCountSinceGZChanged++
                if (mEventCountSinceGZChanged == maxCountGzChange) {
                    mGZ = gz
                    mEventCountSinceGZChanged = 0

                    if (gz < 0) {
                        /*screen facing down*/
                        Log.d("ACCELERATION", "$gz")

                        if (deviceIsFlat(p0, true)) {
                            Handler().postDelayed({
                                Log.d("VAL PROXIMITY", "$prx")
                                if (deviceFacingBackUp(p0) && prx == 0f) {
                                    if (!isDNDOn()) {
                                        if (v.hasVibrator()) {
                                            v.vibrate(longArrayOf(0, mIntensity.toLong(), 1000), -1)
                                        }
                                        enableDND()
                                    }
                                    prx = 10f
                                }
                            }, 800)
                        }
                    }
                }
            } else {
                if (mEventCountSinceGZChanged > 0) {
                    mGZ = gz
                    mEventCountSinceGZChanged = 0
                }
            }
        }
    }

    private fun processAllTimeDetection(event: SensorEvent) {
        val z = event.values[2]

        if (mZ == 0.toFloat()) {
            mZ = z
        } else {
            if ((mZ * z) < 0) {
                mATDEventCountChange++
                if (mATDEventCountChange == maxChange) {
                    mZ = z
                    mATDEventCountChange = 0

                    if (z < 0) {
                        Handler().postDelayed({
                            val x = event.values[0]
                            Log.d("X", "$x")
                            Log.d("ACCELERATION", "$z")

                            if (x in -2.5..2.5) {
                                if (deviceIsFlat(event, false)) {
                                    if (enabledDND) {
                                        disableDND()
                                        Handler().postDelayed({
                                            if (v.hasVibrator()) {
                                                v.vibrate(longArrayOf(0, 320, 1000), -1)
                                            }
                                        }, 100)
                                    } else {
                                        enableDND()
                                        if (v.hasVibrator()) {
                                            v.vibrate(longArrayOf(0, mIntensity.toLong(), 1000), -1)
                                        }
                                    }
                                }
                            }
                        }, 100)
                    }
                }
            } else {
                if (mATDEventCountChange > 0) {
                    mZ = z
                    mATDEventCountChange = 0
                }
            }
        }
    }

    /* Returns whether device is flat */
    private fun deviceIsFlat(event: SensorEvent, stock: Boolean): Boolean {
        val values = event.values

        /* Movement */
        var x = values[0]
        var y = values[1]
        var z = values[2]
        val mNormalizedG = sqrt(x.pow(2) + y.pow(2) + z.pow(2))

        /* Normalize the vector */
        x /= mNormalizedG
        y /= mNormalizedG
        z /= mNormalizedG

        val inclination = Math.toDegrees(acos(z.toDouble())).roundToInt()
        Log.d("ANGLE", "$inclination")
        return if (stock) inclination >= 115 else inclination >= 140
    }

    private fun isDisplayOFF(): Boolean = (displayManager.displays[0].state == Display.STATE_OFF || !pm.isInteractive)

    private fun deviceFacingBackUp(event: SensorEvent): Boolean {
        val x = event.values[2]
        Log.d("CURRENT GRAVITY", "$x")
        return x <= -8.6 && x >= -10.0
    }

    private fun enableDND() {
        if (helper.getManager().isNotificationPolicyAccessGranted) {
            helper.getManager().setInterruptionFilter(mFilter)
        }
        enabledDND = true
    }

    private fun disableDND() {
        if (helper.getManager().isNotificationPolicyAccessGranted) {
            helper.getManager().setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        }
        enabledDND = false
    }

    private fun isDNDOn(): Boolean = helper.getManager().currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL

    override fun onDestroy() {
        finish()
    }

    private fun finish() {
        mStarted = false
        ServiceObserver.serviceRunning.postValue(false)
        Handler().post {
            makeToast(applicationContext, "Flip Service Stopped", Toast.LENGTH_SHORT)
        }

        disableDND()
        if (handler != null && runnable != null) {
            handler?.removeCallbacks(runnable!!)
        }
        if (sensorManager != null) {
            sensorManager?.unregisterListener(eventListener, accelerometer)
            sensorManager?.unregisterListener(proximityListener, proximity)
        }
        if (thread != null) {
            thread?.quitSafely()
        }
        super.onDestroy()
    }
}