package com.example.yesorno.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.view.HapticFeedbackConstants

fun Context.hapticFeedback() {
    val activity = findActivity() ?: return
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        activity.window?.decorView?.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    } else {
        @Suppress("DEPRECATION")
        activity.window?.decorView?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
    }
}

private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
