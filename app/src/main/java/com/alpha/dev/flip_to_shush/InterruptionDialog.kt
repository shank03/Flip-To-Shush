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

package com.alpha.dev.flip_to_shush

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.widget.AppCompatTextView
import com.alpha.dev.flip_to_shush.appService.FlipService
import com.alpha.dev.flip_to_shush.appService.FlipServiceCompat
import com.alpha.dev.flip_to_shush.appService.ServiceObserver
import kotlinx.android.synthetic.main.interruption_dialog.*

class InterruptionDialog(context: Context, private val filterStat: AppCompatTextView) : AppCompatDialog(context) {

    private val ctx = context

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.interruption_dialog)
        window!!.setBackgroundDrawableResource(R.drawable.bg_recent)

        if (sdkAboveM()) {
            when (PreferenceHelper(ctx).getInt(FILTER, NotificationManager.INTERRUPTION_FILTER_NONE)) {
                NotificationManager.INTERRUPTION_FILTER_NONE -> noneCheck.isChecked = true
                NotificationManager.INTERRUPTION_FILTER_ALARMS -> alarmCheck.isChecked = true
                else -> priorityCheck.isChecked = true
            }
        } else {
            int_head.text = "Select Mode"
            priorityCheck.text = "Vibration Mode"
            noneCheck.text = "Silent Mode"
            when (PreferenceHelper(ctx).getInt(RINGER_MODE, AudioManager.RINGER_MODE_SILENT)) {
                AudioManager.RINGER_MODE_VIBRATE -> priorityCheck.isChecked = true
                else -> noneCheck.isChecked = true
            }
        }

        if (!sdkAboveM()) {
            disclaimer.visibility = View.GONE
            alarmCheck.visibility = View.GONE
        }

        noneCheck.setOnCheckedChangeListener { compoundButton, _ ->
            if (compoundButton.isChecked) {
                if (sdkAboveM()) {
                    setFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
                    filterStat.text = "None"
                } else {
                    setRinger(AudioManager.RINGER_MODE_SILENT)
                    filterStat.text = "Silent Mode"
                }
                reRunService()
                dismiss()
            }
        }

        priorityCheck.setOnCheckedChangeListener { compoundButton, _ ->
            if (compoundButton.isChecked) {
                if (sdkAboveM()) {
                    setFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
                    filterStat.text = "Priority"
                } else {
                    setRinger(AudioManager.RINGER_MODE_VIBRATE)
                    filterStat.text = "Vibration Mode"
                }
                reRunService()
                dismiss()
            }
        }

        alarmCheck.setOnCheckedChangeListener { compoundButton, _ ->
            if (compoundButton.isChecked) {
                if (sdkAboveM()) {
                    setFilter(NotificationManager.INTERRUPTION_FILTER_ALARMS)
                    filterStat.text = "Alarms"
                    reRunService()
                    dismiss()
                }
            }
        }
    }

    private fun reRunService() {
        if (ServiceObserver.getValue()) {
            ctx.stopService(Intent(ctx, if (sdkAboveM()) FlipService::class.java else FlipServiceCompat::class.java))

            if (!ServiceObserver.getValue()) {
                ctx.startService(Intent(ctx, if (sdkAboveM()) FlipService::class.java else FlipServiceCompat::class.java))
                //Snackbar.make(findViewById(R.id.snack), "Settings Applied\nService Restarted", Snackbar.LENGTH_LONG).show()
            }
        } /*else {
            Snackbar.make(findViewById(R.id.snack), "Settings Applied", Snackbar.LENGTH_LONG).show()
        }*/
    }

    private fun setFilter(int: Int) {
        PreferenceHelper(ctx).putInt(FILTER, int)
    }

    private fun setRinger(int: Int) {
        PreferenceHelper(ctx).putInt(RINGER_MODE, int)
    }
}