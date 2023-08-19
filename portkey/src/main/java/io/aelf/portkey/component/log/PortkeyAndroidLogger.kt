package io.aelf.portkey.component.log

import android.annotation.SuppressLint
import android.util.Log
import io.aelf.portkey.utils.log.ILogger
import io.aelf.utils.AElfException

@SuppressLint("LogNotTimber")

internal object PortkeyAndroidLogger : ILogger {
    private const val tag = "Portkey_SDK"
    override fun e(msg: String) {
        Log.e(tag, msg)
    }

    override fun e(msg: String, exception: AElfException) {
        Log.e(tag, msg, exception)
    }

    override fun i(vararg msg: String?) {
        if (msg.isEmpty()) {
            return
        }
        msg.forEach {
            Log.i(tag, it ?: "")
        }
    }

    override fun w(vararg msg: String?) {
        if (msg.isEmpty()) {
            return
        }
        msg.forEach {
            Log.w(tag, it ?: "")
        }
    }
}