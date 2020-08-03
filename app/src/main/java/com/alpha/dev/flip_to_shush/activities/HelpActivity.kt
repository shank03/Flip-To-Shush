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

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alpha.dev.flip_to_shush.HelpDialog
import com.alpha.dev.flip_to_shush.R
import kotlinx.android.synthetic.main.activity_help.*

class HelpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        ask.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:alphadeveloper3@gmail.com")

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
        }

        helpList.setHasFixedSize(true)
        helpList.layoutManager = LinearLayoutManager(this)

        val list: ArrayList<HelpData> = ArrayList()

        list.add(
            HelpData(
                "Battery and memory consumption ?",
                "Do not worry about the consumption of battery. There are numerous ways to detect if device is flipped but we have chosen the most efficient way to detect it.\n\nBased on our testings, using it for 5 hours will give 1% battery usage on lower end devices.\n\nFor memory, it uses only 40-60 Mb depending on device."
            )
        )

        list.add(
            HelpData(
                "Why do I need to turn on screen ?",
                "We know that it is something extra to do but there are 2 reasons for it.\n\n" +
                        "${if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) Html.fromHtml("&#8226;", Html.FROM_HTML_MODE_COMPACT) else Html.fromHtml("&#8226;")}" +
                        " First, when your device's screen is off, only system apps are allowed to listen to sensors.\n\n" +
                        "${if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) Html.fromHtml("&#8226;", Html.FROM_HTML_MODE_COMPACT) else Html.fromHtml("&#8226;")}" +
                        " Second, you never know when your device is flipped without your notice and feature got activated and you may miss important calls or messages!.\nThus, turning screen on and flipping is something you would not do unknowingly."
            )
        )

        list.add(
            HelpData(
                "Feature stopping again and again automatically",
                "If you are on Android 9 Pie, make sure your app is not restricted by adaptive battery. Else contact developer for better solving problem with consideration of your device and OS"
            )
        )

        list.add(
            HelpData(
                "Need to flip few times to activate ?",
                "Firstly, there is a half second delay after detecting.\nSecondly, this is totally dependent on device and it's sensor. How well and accurate they can pass the values to the program, is the way how much better it can detect if your device is flipped."
            )
        )

        list.add(
            HelpData(
                "Flip not working on using it after a long time ?",
                "This is a common problem. The story behind this is that android will pause the usage of sensors when it is not being used.\n\nYou can over come this by opening the app and moving the device close and away again and again while facing the screen towards the surface until you hear vibrations and it start working normal.\n\n" +
                        "If this doesn't work then stop and enable the feature again or 'Force close' app and open again or Restart your phone after closing app."
            )
        )

        helpList.adapter = Adapter(this@HelpActivity, list)
    }

    override fun onBackPressed() {
        finish()
    }

    data class HelpData(val head: String, val description: String)

    class Adapter(val context: Context, private val helpList: ArrayList<HelpData>) : RecyclerView.Adapter<Holder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            return Holder(LayoutInflater.from(parent.context).inflate(R.layout.help_lay, parent, false))
        }

        override fun getItemCount(): Int {
            return helpList.size
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val data = helpList[position]

            holder.title.text = data.head

            holder.itemView.setOnClickListener {
                HelpDialog(context, data.head, data.description).show()
            }
        }
    }

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val title: AppCompatTextView = view.findViewById(R.id.title)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        recreate()
    }
}
