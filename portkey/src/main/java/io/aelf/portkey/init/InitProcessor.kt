package io.aelf.portkey.init

import android.content.Context
import io.aelf.portkey.component.log.PortkeyAndroidLogger
import io.aelf.portkey.component.storage.AndroidStorageHandler
import io.aelf.portkey.component.stub.initPortkeySDKViewStub
import io.aelf.portkey.storage.StorageProvider
import io.aelf.portkey.utils.log.GLogger

object InitProcessor {
    private var inited = false

    internal fun hasInit(): Boolean {
        return inited
    }

     fun init(config: SDkInitConfig, context: Context) {
        if (hasInit()) {
            GLogger.e("PortkeySDK has been initialized!")
            return
        }
        GLogger.setLogger(config.logger ?: PortkeyAndroidLogger)
        StorageProvider.init(config.storageHandler ?: AndroidStorageHandler(context))
        if (!config.isUseOutsideComposeStub) {
            initPortkeySDKViewStub(context)
        }
        inited = true
    }
}