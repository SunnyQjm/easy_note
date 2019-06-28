package cn.yml.note.extensions

import android.media.MediaRecorder
import android.os.Environment
import com.github.piasy.rxandroidaudio.AudioRecorder
import java.io.File

fun AudioRecorder.easyStartRecord(): AudioRecorder {
    val file = File(
        Environment.getExternalStorageDirectory().absolutePath +
            File.separator + System.nanoTime() + ".file.m4a")
    this.prepareRecord(MediaRecorder.AudioSource.MIC,
        MediaRecorder.OutputFormat.MPEG_4, MediaRecorder.AudioEncoder.AAC,
        file)
    this.startRecord()
    return this
}