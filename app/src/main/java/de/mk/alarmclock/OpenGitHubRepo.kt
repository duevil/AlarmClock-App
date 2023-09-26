package de.mk.alarmclock

import android.content.Context
import android.content.Intent
import android.net.Uri

fun Context.openGitHubRepo() = Intent(
    Intent.ACTION_VIEW,
    Uri.Builder()
        .scheme("https")
        .authority("github.com")
        .appendPath("duevil")
        .appendPath("AlarmClock-Control-App")
        .build()
).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).run(::startActivity)
