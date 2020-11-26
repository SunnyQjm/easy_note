package cn.yml.note.utils

import android.content.Context
import androidx.core.graphics.PathSegment
import com.tencent.aai.AAIClient
import com.tencent.aai.audio.data.AudioRecordDataSource
import com.tencent.aai.auth.LocalCredentialProvider
import com.tencent.aai.exception.ClientException
import com.tencent.aai.exception.ServerException
import com.tencent.aai.listener.AudioRecognizeResultListener
import com.tencent.aai.model.AudioRecognizeRequest
import com.tencent.aai.model.AudioRecognizeResult
import com.tencent.aai.model.type.AudioRecognizeTemplate
import com.tencent.aai.model.type.EngineModelType

class ABSWrapper {
    companion object {
        private const val appId = 1252872558
        private const val projectId = 0
        private const val secretId = "AKIDgjaG1eSdylKgR1yromv3zgEhusa4vZZJ"
        private const val secretKey = "hKq6lDYdT5SDpFRWgmxsa9TfjJmpgIOG"
    }

    var aaiClient: AAIClient? = null
    var audioRecognizeRequest: AudioRecognizeRequest? = null

    fun start(
        context: Context,
        onSegmentSuccess: (text: String) -> Unit = {},
        onSliceSuccess: (text: String) -> Unit = {},
        onSuccess: (text: String) -> Unit = {},
        onFailed: () -> Unit = {}
    ) {
        val credentialProvider = LocalCredentialProvider(secretKey)
        try {
            aaiClient = AAIClient(context, appId, projectId, secretId, credentialProvider)
            audioRecognizeRequest = AudioRecognizeRequest.Builder()
                .pcmAudioDataSource(AudioRecordDataSource())
                .template(AudioRecognizeTemplate(EngineModelType.EngineModelType16KEN, 0, 1))
                .build()
            val audioRecognizeResultListener = object : AudioRecognizeResultListener {
                override fun onSliceSuccess(
                    request: AudioRecognizeRequest?,
                    result: AudioRecognizeResult?,
                    order: Int
                ) {
                    onSliceSuccess(result?.text ?: "")
                }

                override fun onSegmentSuccess(
                    request: AudioRecognizeRequest?,
                    result: AudioRecognizeResult?,
                    order: Int
                ) {
                    onSegmentSuccess(result?.text ?: "")
                }

                override fun onSuccess(request: AudioRecognizeRequest?, result: String?) {
                    onSuccess(result ?: "")
                }

                override fun onFailure(
                    request: AudioRecognizeRequest?,
                    clientException: ClientException?,
                    serverException: ServerException?
                ) {
                    onFailed()
                }

            }
            Thread {
                aaiClient?.startAudioRecognize(
                    audioRecognizeRequest,
                    audioRecognizeResultListener
                )
            }.start()
        } catch (e: ClientException) {
            e.printStackTrace()
        }
    }

    fun stop() {
        val requestId = audioRecognizeRequest?.requestId ?: -1
        Thread {
            aaiClient?.stopAudioRecognize(requestId)
        }.start()
    }

    fun cancel() {
        val requestId = audioRecognizeRequest?.requestId ?: -1
        Thread {
            aaiClient?.cancelAudioRecognize(requestId)
        }.start()
    }
}