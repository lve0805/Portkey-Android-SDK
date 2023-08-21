package io.aelf.portkey.init

import android.content.Context
import io.aelf.portkey.component.log.PortkeyAndroidLogger
import io.aelf.portkey.component.storage.AndroidStorageHandler
import io.aelf.portkey.storage.StorageProvider
import io.aelf.portkey.utils.log.GLogger
import org.jetbrains.annotations.NotNull

object InitProcessor {
    private var inited = false

    fun hasInit(): Boolean {
        return inited
    }

    internal fun init(config: SDkInitConfig, context: Context) {
        if (hasInit()) {
            GLogger.e("PortkeySDK has been initialized!")
            return
        }
        GLogger.setLogger(config.logger ?: PortkeyAndroidLogger)
        StorageProvider.init(config.storageHandler ?: AndroidStorageHandler(context))
        inited = true
    }
}