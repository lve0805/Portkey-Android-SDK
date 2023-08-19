package io.aelf.portkey

import io.aelf.portkey.component.log.PortkeyAndroidLogger
import io.aelf.portkey.utils.log.GLogger
import io.aelf.utils.AElfException
import org.junit.Before
import org.junit.Test

class LoggerTest {
    @Before
    fun setUp() {
        GLogger.setLogger(PortkeyAndroidLogger)
    }

    @Test
    fun test() {
        GLogger.i("test")
        GLogger.e("test")
        GLogger.w("test")
        GLogger.t("test")
        GLogger.t()
        GLogger.w()
        GLogger.e("test", AElfException())
    }
}