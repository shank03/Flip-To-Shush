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
import android.content.Context
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatTextView
import com.ironz.binaryprefs.BinaryPreferencesBuilder
import com.ironz.binaryprefs.Preferences
import java.util.*

var mRunning: Boolean = false
var mTestRunning = false

var mTheme = if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM else AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
val defTheme = if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM else AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
val MS2: Spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) Html.fromHtml(" ms<sup>-2</sup>", Html.FROM_HTML_MODE_LEGACY) else Html.fromHtml(" ms<sup>-2</sup>")

const val DND_PERMISSION = 102
const val TESTING = "testing_done"
const val INTENSITY = "intensity"
const val SENSE_TYPE = "original"
const val PREF_NAME = "flip"
const val SERVICE_STARTED = "started"
const val FILTER = "inter_filter"
const val RINGER_MODE = "ringer"
const val THEME = "theme_int"
const val OFF_APP_TOAST = "off_toast"

//const val PRIVACY_URL = "https://flip-to-shush.flycricket.io/privacy.html"
const val ABOUT_DES_M = "When notifications are annoying you or disturbing at work, just turn on screen and flip your phone (facing the back of the phone upwards) and this app will enable silent mode. " +
        "(Do Not Disturb requires Android Marshmallow and above)."
val PRIVACY_STATEMENT_HTML = "<p>I, Shashank Verma. built the Flip-to-shush app as a Free app. This SERVICE is provided by me at no cost and is intended for use as is.</p><br>" +
        "<p>This page is used to inform visitors regarding my policies with the collection, use, and disclosure of Personal Information if anyone decided to use my Service.</p><br>" +
        "<p>No Personal Information is collected.</p><br>" +
        "<p>The terms used in this Privacy Policy have the same meanings as in our Terms and Conditions, which is accessible at Flip-to-Shush unless otherwise defined in this Privacy Policy.</p><br>" +
        "<p><strong>Information Collection and Use</strong></p><br>" +
        "<p>I Do Not Intend To get any Personal Identifiable Information. None of your data is being collected.</p><br>" +
        "<p><strong>Security</strong></p><br>" +
        "<p>Since, none of your information is being collected, you are totally safe :)</p><br>" +
        "<p><B>Changes to This Privacy Policy</B></p><br>" +
        "<p>I may update our Privacy Policy from time to time. Thus, you are advised to review this page periodically for any changes. I will notify you of any changes by posting the new Privacy Policy on this page. These changes are effective immediately after they are posted on this page.</p><br>" +
        "<p><strong>Copyright</strong></p><br>" +
        "Copyright (c) ${Calendar.getInstance().get(Calendar.YEAR)}, Shashank Verma.<br><br>" +
        "<p><strong>Contact Me</strong></p><br>" +
        "<p>If you have any questions or suggestions about my Privacy Policy, do not hesitate to contact me at shashank.verma2002@gmail.com</p>"

@Volatile
var preference: Preferences? = null

@Volatile
var layoutInflater: LayoutInflater? = null

fun getLayoutInflater(context: Context): LayoutInflater {
    if (layoutInflater == null) {
        layoutInflater = context.applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }
    return layoutInflater!!
}

@SuppressLint("InflateParams")
fun makeToast(context: Context, message: String, duration: Int) {
    val view = getLayoutInflater(context.applicationContext).inflate(R.layout.toast_layout, null)
    view.findViewById<AppCompatTextView>(R.id.text).text = message

    val toast = Toast(context.applicationContext)
    toast.duration = duration
    toast.setGravity(Gravity.BOTTOM, 0, 200)
    toast.view = view
    toast.show()
}

fun sdkAboveM(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

/*val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
    if (sdkAboveM()) {
        if (FlipService::class.java.name == service.service.className) {
            return true
        }
    } else {
        if (FlipServiceCompat::class.java.name == service.service.className) {
            return true
        }
    }
}
return false*/

class PreferenceHelper(val context: Context) {

    private fun getPreference(): Preferences {
        if (preference == null) {
            preference = BinaryPreferencesBuilder(context.applicationContext)
                .name(PREF_NAME)
                .build()
        }
        return preference!!
    }

    fun putBoolean(key: String, bool: Boolean) {
        val editor = getPreference().edit()

        editor.putBoolean(key, bool)
        editor.apply()
    }

    fun getBoolean(key: String, def: Boolean): Boolean {
        return getPreference().getBoolean(key, def)
    }

    fun putInt(key: String, int: Int) {
        val editor = getPreference().edit()

        editor.putInt(key, int)
        editor.apply()
    }

    fun getInt(key: String, def: Int): Int {
        return getPreference().getInt(key, def)
    }
}