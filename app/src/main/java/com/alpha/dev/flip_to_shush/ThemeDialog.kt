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

package com.alpha.dev.flip_to_shush

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDialog
import kotlinx.android.synthetic.main.theme_dialog.*

class ThemeDialog(context: Context) : AppCompatDialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.theme_dialog)
        window!!.setBackgroundDrawableResource(R.drawable.bg_recent)

        val q = Build.VERSION.SDK_INT == Build.VERSION_CODES.Q

        batteryCheck.visibility = if (q) View.GONE else View.VISIBLE
        defaultCheck.visibility = if (q) View.VISIBLE else View.GONE

        when (PreferenceHelper(context).getInt(THEME, defTheme)) {
            AppCompatDelegate.MODE_NIGHT_YES -> darkCheck.isChecked = true
            AppCompatDelegate.MODE_NIGHT_NO -> lightCheck.isChecked = true
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> defaultCheck.isChecked = true
            AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY -> batteryCheck.isChecked = true
        }

        lightCheck.setOnCheckedChangeListener { compoundButton, _ ->
            if (compoundButton.isChecked) {
                saveTheme(AppCompatDelegate.MODE_NIGHT_NO)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                /*if (fromSet) {
                    context.startActivity(Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
                    ownerActivity!!.overridePendingTransition(0, 0)
                }*/
                dismiss()
            }
        }

        darkCheck.setOnCheckedChangeListener { compoundButton, _ ->
            if (compoundButton.isChecked) {
                saveTheme(AppCompatDelegate.MODE_NIGHT_YES)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
               /* if (fromSet) {
                    context.startActivity(Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
                    ownerActivity!!.overridePendingTransition(0, 0)
                }*/
                dismiss()
            }
        }

        batteryCheck.setOnCheckedChangeListener { compoundButton, _ ->
            if (compoundButton.isChecked) {
                saveTheme(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
               /* if (fromSet) {
                    context.startActivity(Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
                    ownerActivity!!.overridePendingTransition(0, 0)
                }*/
                dismiss()
            }
        }

        defaultCheck.setOnCheckedChangeListener { compoundButton, _ ->
            if (compoundButton.isChecked) {
                saveTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                /*if (fromSet) {
                    context.startActivity(Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
                    ownerActivity!!.overridePendingTransition(0, 0)
                }*/
                dismiss()
            }
        }
    }

    private fun saveTheme(int: Int) {
        PreferenceHelper(context).putInt(THEME, int)
    }
}