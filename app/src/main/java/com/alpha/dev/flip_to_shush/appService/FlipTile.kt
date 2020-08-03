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

import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
open class FlipTile/* : TileService()*/ {

    /*private lateinit var manager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        manager = NotificationHelper(applicationContext).getManager()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

    override fun onClick() {
        super.onClick()

        val tile = qsTile
        if (!isLocked) {
            processClickEvent(tile)
        } else {
            unlockAndRun {
                processClickEvent(tile)
            }
        }
    }

    override fun onTileAdded() {
        super.onTileAdded()
        super.onStartListening()
    }

    override fun onStartListening() {
        super.onStartListening()

        val tile = qsTile

        if (manager.isNotificationPolicyAccessGranted) {
            if (isMyServiceRunning(FlipService::class.java)) {
                tile.state = Tile.STATE_ACTIVE
                setSubtitle(tile, "Enabled")
            } else {
                tile.state = Tile.STATE_INACTIVE
                setSubtitle(tile, "Off")
            }
        } else {
            tile.state = Tile.STATE_UNAVAILABLE
            setSubtitle(tile, "Permission Denied")
        }


        tile.updateTile()
    }

    open fun processClickEvent(tile: Tile) {
        if (isMyServiceRunning(FlipService::class.java) && tile.state == Tile.STATE_ACTIVE) {
            tile.state = Tile.STATE_INACTIVE
            setSubtitle(tile, "Off")

            try {
                stopService(Intent(applicationContext, FlipService::class.java))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            if (manager.isNotificationPolicyAccessGranted) {
                if (!isMyServiceRunning(FlipService::class.java)) {
                    tile.state = Tile.STATE_ACTIVE
                    setSubtitle(tile, "Enabled")

                    try {
                        startService(Intent(applicationContext, FlipService::class.java))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    tile.state = Tile.STATE_INACTIVE
                    setSubtitle(tile, "Off")

                    try {
                        stopService(Intent(applicationContext, FlipService::class.java))
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }

                    Toast.makeText(applicationContext, "Something went wrong.\n\nForce close app and try again", Toast.LENGTH_LONG).show()
                }
            } else {
                startActivityAndCollapse(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
            }
        }

        tile.updateTile()
    }

    private fun setSubtitle(tile: Tile, text: String) {
        try {
            tile.contentDescription = text
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }*/
}