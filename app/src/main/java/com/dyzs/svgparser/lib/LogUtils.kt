package com.dyzs.svgparser.lib

import android.util.Log

object LogUtils {
    private val TAG = LogUtils::class.java.simpleName
    private var isDebugging = true
    fun setDebug(b: Boolean) {
        isDebugging = b
    }

    fun v(tag: String?, msg: String?) {
        if (isDebugging) {
            Log.v(tag ?: TAG, msg ?: "")
        }
    }

    fun d(tag: String?, msg: String?) {
        if (isDebugging) {
            Log.d(tag ?: TAG, msg ?: "")
        }
    }

    fun i(tag: String?, msg: String?) {
        if (isDebugging) {
            Log.i(tag ?: TAG, msg ?: "")
        }
    }

    fun w(tag: String?, msg: String?) {
        if (isDebugging) {
            Log.w(tag ?: TAG, msg ?: "")
        }
    }

    fun e(tag: String?, msg: String?) {
        if (isDebugging) {
            Log.e(tag ?: TAG, msg ?: "")
        }
    }
}