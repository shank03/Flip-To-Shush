package com.alpha.dev.flip_to_shush.appService;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.alpha.dev.flip_to_shush.AppHelperKt;
import com.alpha.dev.flip_to_shush.PreferenceHelper;

import static com.alpha.dev.flip_to_shush.AppHelperKt.makeToast;

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

public class FlipServiceCompat extends Service {

    private static final String TAG = "FlipServiceCompat";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static boolean mStarted = false;

    /* Sensor */
    SensorManager sensorManager;
    SensorEventListener eventListener, proximityListener;
    Sensor accelerometer, proximity;
    Vibrator v;

    /* -- */
    NotificationHelper helper;
    PowerManager powerManager;
    AudioManager audioManager;

    HandlerThread thread;

    /* Values */
    float mGZ = 0, mZ = 0, prx = 5;
    int mEventCountSinceGZChanged = 0, mATDEventCountChange = 0, maxCountGZChange = 5, maxChange = 10, mIntensity = 0, mRinger = AudioManager.RINGER_MODE_SILENT;
    boolean updatable = false, isStock = true, enabledSilent = false;

    @Override
    public void onCreate() {
        helper = new NotificationHelper(getApplicationContext());
        new Handler().post(() -> makeToast(getApplicationContext(), "Flip Service Started", Toast.LENGTH_SHORT));

        PreferenceHelper pr = new PreferenceHelper(getApplicationContext());
        isStock = pr.getBoolean(AppHelperKt.SENSE_TYPE, true);
        mIntensity = pr.getInt(AppHelperKt.INTENSITY, 250);
        mRinger = pr.getInt(AppHelperKt.RINGER_MODE, AudioManager.RINGER_MODE_SILENT);

        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        /* Defining Sensor Listeners */
        eventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    if (isStock) {
                        processStockDetection(event);
                    } else {
                        processAllTimeDetection(event);
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };

        proximityListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                Log.d(TAG, "onSensorChanged: PROXIMITY: " + event.values[0]);
                if (updatable) {
                    prx = event.values[0];
                    updatable = prx != 0;
                }
                Log.d(TAG, "onSensorChanged: UPDATABLE : " + updatable);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!mStarted) {
            mStarted = true;
            ServiceObserver.INSTANCE.getServiceRunning().postValue(true);
            v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

            if (sensorManager != null) {
                accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

                if (accelerometer == null || proximity == null) {
                    throwError();
                } else {
                    /* Processing in a new thread */
                    thread = new HandlerThread("Flip Service Sensor Thread", Thread.MAX_PRIORITY);
                    thread.start();
                    Handler sensorHandler = new Handler(thread.getLooper());

                    sensorManager.registerListener(eventListener, accelerometer, Sensor.TYPE_ACCELEROMETER, sensorHandler);
                    sensorManager.registerListener(proximityListener, proximity, Sensor.TYPE_PROXIMITY, sensorHandler);
                }
            } else {
                throwError();
            }
        }
        return START_STICKY;
    }

    private void processStockDetection(final SensorEvent event) {
        float gz = event.values[2];

        if (gz >= 0) {
            if (enabledSilent && !isDisplayOFF()) {
                disableRingerMode();
            }
        }

        if (mGZ == 0) {
            mGZ = gz;
        } else {
            if ((mGZ * gz) < 0) {
                updatable = true;
                mEventCountSinceGZChanged++;

                if (mEventCountSinceGZChanged == maxCountGZChange) {
                    mGZ = gz;
                    mEventCountSinceGZChanged = 0;

                    if (gz < 0) {
                        Log.d(TAG, "processStockDetection: Acceleration : " + gz);

                        if (deviceIsFlat(event, true)) {
                            new Handler().postDelayed(() -> {
                                Log.d(TAG, "run: LTS PROXIMITY : " + prx);
                                if (deviceFacingBackUp(event) && prx == 0) {
                                    if (!isRingerEnabled()) {
                                        if (v.hasVibrator()) {
                                            v.vibrate(new long[]{0, mIntensity, 1000}, -1);
                                        }
                                        enableRingerMode();
                                    }
                                    prx = 10;
                                }
                            }, 800);
                        }
                    }
                }
            } else {
                if (mEventCountSinceGZChanged > 0) {
                    mGZ = gz;
                    mEventCountSinceGZChanged = 0;
                }
            }
        }
    }

    private void processAllTimeDetection(final SensorEvent event) {
        final float z = event.values[2];

        if (mZ == 0) {
            mZ = z;
        } else {
            if ((mZ * z) < 0) {
                mATDEventCountChange++;
                if (mATDEventCountChange == maxChange) {
                    mZ = z;
                    mATDEventCountChange = 0;

                    if (z < 0) {
                        new Handler().postDelayed(() -> {
                            float x = event.values[0];
                            Log.d(TAG, "run: X : " + x);
                            Log.d(TAG, "run: ACCELERATION : " + z);

                            if (x >= -2.5 && x <= 2.5) {
                                if (deviceIsFlat(event, false)) {
                                    if (enabledSilent) {
                                        disableRingerMode();
                                        new Handler().postDelayed(() -> {
                                            if (v.hasVibrator()) {
                                                v.vibrate(new long[]{0, 320, 1000}, -1);
                                            }
                                        }, 100);
                                    } else {
                                        enableRingerMode();
                                        if (v.hasVibrator()) {
                                            v.vibrate(new long[]{0, mIntensity, 1000}, -1);
                                        }
                                    }
                                }
                            }
                        }, 100);
                    }
                }
            }
        }
    }

    private boolean deviceIsFlat(SensorEvent event, boolean stock) {
        float[] values = event.values;

        /* Movement */
        float x = values[0], y = values[1], z = values[2];
        double mNormalizedG = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));

        /* Normalize the vector */
        //x /= mNormalizedG;
        //y /= mNormalizedG;
        z /= mNormalizedG;

        int inclination = (int) Math.toDegrees(Math.acos(z));
        Log.d(TAG, "deviceIsFlat: ANGLE : " + inclination);
        return stock ? inclination >= 115 : inclination >= 140;
    }

    private boolean isDisplayOFF() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            return !powerManager.isInteractive();
        } else {
            return !powerManager.isScreenOn();
        }
    }

    private boolean deviceFacingBackUp(SensorEvent event) {
        float z = event.values[2];
        Log.d(TAG, "deviceFacingBackUp: GRAVITY" + z);
        return z <= -8 && z >= 10;
    }

    private void enableRingerMode() {
        if (audioManager != null) {
            audioManager.setRingerMode(mRinger);
        }
        enabledSilent = true;
    }

    private void disableRingerMode() {
        if (audioManager != null) {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        }
        enabledSilent = false;
    }

    private boolean isRingerEnabled() {
        return audioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL;
    }

    private void throwError() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder nb = helper.generalNotification("Error Flip Service", "Could not get sensor from your device. Either try again or please contact developer from app.");
            helper.getManager().notify(3, nb.build());
        } else {
            Notification.Builder nb = helper.lowerGeneralNotification("Error Flip Service", "Could not get sensor from your device. Either try again or please contact developer from app.");
            helper.getManager().notify(3, nb.build());
        }
        finish();
    }

    @Override
    public void onDestroy() {
        finish();
    }

    private void finish() {
        mStarted = false;
        ServiceObserver.INSTANCE.getServiceRunning().postValue(false);
        new Handler().post(() -> makeToast(getApplicationContext(), "Flip Service Stopped", Toast.LENGTH_SHORT));

        if (sensorManager != null) {
            sensorManager.unregisterListener(eventListener, accelerometer);
            sensorManager.unregisterListener(proximityListener, proximity);
        }
        if (thread != null) {
            thread.quitSafely();
        }
        super.onDestroy();
    }
}
