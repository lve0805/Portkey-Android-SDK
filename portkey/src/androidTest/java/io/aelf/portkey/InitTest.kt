package io.aelf.portkey

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.aelf.portkey.init.InitProcessor
import io.aelf.portkey.init.SDkInitConfig
import io.aelf.portkey.storage.IStorageBehaviour
import io.aelf.portkey.storage.StorageProvider
import io.aelf.portkey.utils.log.GLogger
import io.aelf.portkey.utils.log.ILogger
import io.aelf.utils.AElfException
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InitTest {
    @Before
    fun setUp() {
        InitProcessor.init(
            SDkInitConfig.Builder()
                .setLogger(MyLogger)
                .build(),
            InstrumentationRegistry.getInstrumentation().targetContext
        )
    }

    @Test
    fun test() {
        assert(InitProcessor.hasInit())
        val handler:IStorageBehaviour = StorageProvider.getHandler()!!
        handler.putValue("test", "test")
        assert(handler.getValue("test") == "test")
        GLogger.i("test")
        assert(MyLogger.recentWord == "test")
    }
}

object MyLogger : ILogger {
    var recentWord: String = "Non"

    override fun e(msg: String) {
        recentWord = msg
    }

    override fun e(msg: String, exception: AElfException) {
        recentWord = msg
    }

    override fun i(vararg msg: String?) {
        recentWord = msg[0]!!
    }

    override fun w(vararg msg: String?) {
        recentWord = msg[0]!!
    }

}