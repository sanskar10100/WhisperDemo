package dev.sanskar.whisperdemo

import android.media.MediaRecorder
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.audio.TranscriptionRequest
import com.aallam.openai.api.file.FileSource
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import kotlinx.coroutines.launch
import okio.source
import java.io.File
import java.io.IOException
import java.lang.Exception


private const val TAG = "MainViewModel"

class MainViewModel : ViewModel() {

    var textState by mutableStateOf("")

    var isRecording by mutableStateOf(false)

    private val openAI = OpenAI(BuildConfig.OPENAI_API_KEY)

    private lateinit var fileLocation: String
    private lateinit var recorder: MediaRecorder

    @OptIn(BetaOpenAI::class)
    fun toggleRecording(externalCacheDir: File?) {
        if (isRecording) {
            recorder.stop()
            isRecording = false

            val request = TranscriptionRequest(
                audio = FileSource(fileLocation, source = File(fileLocation).source()),
                model = ModelId("whisper-1")
            )
            viewModelScope.launch {
                textState = "Generating..."
                try {
                    val transcription = openAI.transcription(request)
                    textState = transcription.text
                } catch (e: Exception) {
                    textState = "Sorry, we couldn't connect to the OpenAI servers right now. Please try again later!"
                }

                File(fileLocation).delete()
            }
        } else {
            fileLocation = externalCacheDir?.absolutePath + "/recording.mp3"

            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFile(fileLocation)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                prepare()
                start()
            }
            isRecording = true
            textState = "Listening..."
            Log.d(TAG, "toggleRecording: Recording started")
        }
    }

    override fun onCleared() {
        super.onCleared()
        recorder.release()
    }
}