package io.aelf.portkey

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.afollestad.materialdialogs.ModalDialog
import io.aelf.portkey.component.stub.PortkeySDKViewStub
import io.aelf.portkey.demo.ui.theme.PortkeyAndroidSDKTheme
import io.aelf.portkey.entity.social_recovery.ModalPreview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ModalPreview()
        }
    }
}
