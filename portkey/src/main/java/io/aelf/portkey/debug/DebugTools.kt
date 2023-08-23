package io.aelf.portkey.debug

import android.content.Context
import io.aelf.portkey.init.InitProcessor
import io.aelf.portkey.init.SDkInitConfig
import io.aelf.portkey.internal.tools.GlobalConfig
import io.aelf.portkey.network.retrofit.RetrofitProvider

internal fun initDebug(context: Context) {
    InitProcessor.init(SDkInitConfig.Builder().build(), context)
    RetrofitProvider.resetOrInitMainRetrofit("https://localtest-applesign2.portkey.finance")
    GlobalConfig.setTestEnv(true)
}