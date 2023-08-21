package io.aelf.portkey

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.aelf.portkey.component.log.PortkeyAndroidLogger
import io.aelf.portkey.utils.log.GLogger
import io.aelf.utils.AElfException
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
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