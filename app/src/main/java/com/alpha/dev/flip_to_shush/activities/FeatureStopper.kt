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
import com.alpha.dev.flip_to_shush.appService.ServiceObserver
import kotlinx.android.synthetic.main.activity_feature_stopper.*

class FeatureStopper : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feature_stopper)

        if (!ServiceObserver.getValue()) {
            stat.text = "Already Stopped"
            Toast.makeText(this, "Already Stopped", Toast.LENGTH_LONG).show()
        } else {
            stat.text = "Stopped"
            stopService(Intent(this, FlipService::class.java))
        }

        Handler().postDelayed({
            finish()
        }, 1000)
    }
}
