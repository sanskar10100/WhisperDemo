package dev.sanskar.whisperdemo

import android.content.ClipData
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sanskar.whisperdemo.ui.theme.WhisperDemoTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WhisperDemoTheme {
                MainContent()
            }
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun MainContent() {
        val audioPermissionState = rememberPermissionState(
            android.Manifest.permission.RECORD_AUDIO
        )

        if (audioPermissionState.status.isGranted) {
            RecordAndDisplay()
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { audioPermissionState.launchPermissionRequest() },
                ) {
                    Text("Grant Audio Permission")
                }
            }
        }
    }

    @Composable
    @OptIn(ExperimentalAnimationApi::class)
    private fun RecordAndDisplay(
        viewModel: MainViewModel = viewModel(),
    ) {
        val context = LocalContext.current

        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { viewModel.toggleRecording(context.externalCacheDir) },
                shape = RoundedCornerShape(8.dp),
            ) {
                AnimatedContent(viewModel.isRecording) {
                    if (!it) Icon(
                        painter = painterResource(R.drawable.baseline_play_circle_48),
                        contentDescription = "Play/Stop Button"
                    ) else Icon(
                        painter = painterResource(R.drawable.baseline_stop_circle_48),
                        contentDescription = "Play/Stop Button"
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            AnimatedContent(
                viewModel.textState,
                transitionSpec = {
                    slideInVertically { it } + fadeIn() with
                            slideOutVertically { -it } + fadeOut()
                },
                modifier = Modifier.padding(8.dp)
            ) { targetText ->
                Text(
                    targetText,
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily(Font(R.font.montserrat)),
                    fontSize = 24.sp,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(),
                        onClick = {
                            copyToClipboardAndToast(targetText)
                        }
                    )
                )
            }
        }
    }

    private fun copyToClipboardAndToast(text: CharSequence){
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = ClipData.newPlainText("label",text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Copied to Clipboard!", Toast.LENGTH_SHORT).show()
    }

}