package cn.yml.note.extensions

import android.media.MediaRecorder
import android.os.Environment
import cn.yml.note.utils.FileUtils
import com.github.piasy.rxandroidaudio.AudioRecorder
import java.io.File

fun AudioRecorder.easyStartRecord(filePathCallback: (file: File, filePath: String) -> Unit): AudioRecorder {

    val filePath = FileUtils.getRecordPath() +
            File.separator + System.nanoTime() + ".file.m4a"
    val file = File(filePath)
    filePathCallback(file, filePath)
    this.prepareRecord(MediaRecorder.AudioSource.MIC,
        MediaRecorder.OutputFormat.MPEG_4, MediaRecorder.AudioEncoder.AAC,
        file)
    this.startRecord()
    return this
}