package cn.yml.note.model

import android.content.ContentValues
import android.content.Context
import android.util.Log
import cn.bmob.v3.BmobObject
import cn.bmob.v3.BmobUser
import cn.bmob.v3.datatype.BmobFile
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.*
import cn.yml.note.extensions.toJson
import cn.yml.note.utils.FileUtils
import cn.yml.note.utils.database
import com.cunoraz.tagview.Tag
import java.io.File
import kotlin.reflect.full.memberProperties


data class Record(
    val recordDuration: Int = 0,
    val fileName: String = "",
    var filePath: String = "",
    var url: String = ""
)

/**
 * 本地Note对象
 */
data class Note(
    var id: String = "",
    var noteTitle: String = "默认标题",                           // 便签标题
    var noteContent: String = "默认内容",                         // 便签内容
    var noteImages: MutableList<String> = mutableListOf(),      // 便签图片
    var noteRecording: MutableList<Record> = mutableListOf(),   // 录音
    var tags: MutableList<Tag> = mutableListOf(),            // 标签
    var createTime: Long = System.currentTimeMillis(),           // 创建时间
    var objectId: String = ""                                   // Bmob对象Id
)

data class NetworkNote(
    var id: String = "",
    var noteTitle: String = "默认标题",                               // 便签标题
    var noteContent: String = "默认内容",                             // 便签内容
    var noteImages: MutableList<String> = mutableListOf(),        // 便签图片
    var noteRecording: MutableList<Record> = mutableListOf(),     // 录音
    var tags: MutableList<Tag> = mutableListOf(),                   // 标签
    var user: User? = null,                                         // 关联的用户
    var createTime: Long = System.currentTimeMillis()               // 创建时间
) : BmobObject()


fun NetworkNote.saveToLocal(context: Context, finishCallback: () -> Unit) {
    val note = Note(
        id = this.id,
        objectId = this.objectId,
        noteTitle = this.noteTitle,
        noteContent = this.noteContent,
        noteImages = this.noteImages,
        createTime = this.createTime,
        tags = this.tags,
        noteRecording = this.noteRecording
    )

    println("正在同步: ${this.toJson()}")
    var needDownloadCount = note.noteRecording.size
    if (needDownloadCount == 0) {
        note.saveLocal(context)
        finishCallback()
        return
    }
    // 将远程的录音下载到本地
    note.noteRecording.forEach { record ->
        println("do download File: ${record.toJson()}")
        BmobFile(record.fileName, "", record.url)
            .download(FileUtils.generateRecordFile(record.fileName), object : DownloadFileListener() {
                override fun done(savePath: String?, p1: BmobException?) {
                    if (p1 == null) {
                        record.filePath = savePath ?: ""
                    } else {
                        Log.e("NetworkNote.saveToLocal", "下载失败: ${record.toJson()}")
                    }
                    needDownloadCount--
                    if (needDownloadCount == 0) {
                        note.saveLocal(context)
                        finishCallback()
                    }

                }

                override fun onProgress(p0: Int?, p1: Long) {

                }

            })
    }
}

fun Note.saveLocal(context: Context, finish: () -> Unit = {}) {
    println("do saveLocal: ${this.toString()}")
    context.database.use {
        insert(
            "note",
            null,
            this@saveLocal.toContentValues()
        )
        finish()
    }
}

fun Note.updateLocal(context: Context, finish: () -> Unit = {}) {
    println("do updateLocal: ${this.toString()}")

    context.database.use {
        update("note", this@updateLocal.toContentValues(), "id = ?", arrayOf(this@updateLocal.id))
        finish()
    }
}

fun Note.deleteLocal(context: Context, finish: () -> Unit = {}) {
    println("do deleteLocal: ${this.toString()}")

    context.database.use {
        delete(
            "note",
            "id = ?", arrayOf(this@deleteLocal.id)
        )
        finish()
    }
}

fun Note.delete(updateListener: UpdateListener, context: Context) {
    println("do delete: ${this.toString()}")
    if (!BmobUser.isLogin() || this.objectId.isEmpty()) {
        deleteLocal(context)
        updateListener.done(null)
        return
    }
    val networkNote = NetworkNote(
        id = this.id,
        noteTitle = this.noteTitle,
        noteContent = this.noteContent,
        tags = this.tags,
        noteImages = this.noteImages,
        createTime = this.createTime
    )
    networkNote.objectId = this.objectId
    networkNote.delete(object : UpdateListener() {
        override fun done(p0: BmobException?) {
            deleteLocal(context)
            updateListener.done(p0)
        }

    })
}

/**
 * 保存
 */
fun Note.save(saveListener: SaveListener<String>, context: Context, needSaveToLocalStorage: Boolean = true) {
    println("do save: ${this.toJson()}")
    val networkNote = NetworkNote(
        id = this.id,
        noteTitle = this.noteTitle,
        noteContent = this.noteContent,
        tags = this.tags,
        noteImages = this.noteImages,
        createTime = this.createTime
    )
    if (!BmobUser.isLogin()) {
        if (needSaveToLocalStorage) {
            saveLocal(context)
        }
        saveListener.done("", null)
        return
    }

    networkNote.user = BmobUser.getCurrentUser(User::class.java)


    val save = {
        networkNote.save(object : SaveListener<String>() {
            override fun done(p0: String?, p1: BmobException?) {
                if (p1 == null && p0 != null) {
                    this@save.objectId = p0
                    if (needSaveToLocalStorage) {
                        saveLocal(context)
                    }
                } else {
                    Log.e("Note", p1?.message ?: "")
                }
                saveListener.done(p0, p1)
            }
        })
    }

    val records = this.noteRecording.map { it.filePath }.toTypedArray()
    if (records.isNotEmpty()) {
        BmobFile.uploadBatch(records, object : UploadBatchListener {
            override fun onSuccess(p0: MutableList<BmobFile>?, p1: MutableList<String>?) {
                if (p0 != null && p0.size > 0) {
                    // 录音上传成功
                    networkNote.noteRecording = p0.map { bf ->
                        val record = this@save.noteRecording.filter {
                            it.fileName == bf.filename
                        }[0]
                        record.url = bf.fileUrl
                        return@map Record(
                            recordDuration = record.recordDuration,
                            fileName = record.fileName,
                            url = bf.fileUrl
                        )
                    }.toMutableList()
                }
                save()
            }

            override fun onProgress(p0: Int, p1: Int, p2: Int, p3: Int) {
            }

            override fun onError(p0: Int, p1: String?) {
                saveListener.done("", BmobException(p1 ?: "上传录音失败"))
            }

        })
    } else {
        save()
    }
}

fun Note.update(updateListener: UpdateListener, context: Context) {
    println("do update: ${this.toJson()}")
    val networkNote = NetworkNote(
        id = this.id,
        noteTitle = this.noteTitle,
        noteContent = this.noteContent,
        tags = this.tags,
        noteImages = this.noteImages,
        createTime = this.createTime
    )
    networkNote.objectId = this.objectId
    if (!BmobUser.isLogin() || this.objectId.isEmpty()) {
        updateListener.done(null)
        updateLocal(context)
        return
    }

    networkNote.user = BmobUser.getCurrentUser(User::class.java)

    val save = {
        networkNote.update(object : UpdateListener() {
            override fun done(p0: BmobException?) {
                if (p0 == null) {
                    updateLocal(context)
                }
                updateListener.done(p0)
            }

        })
    }

    val records = this.noteRecording.filter { it.url.isEmpty() }.map { it.filePath }.toTypedArray()
    if (records.isNotEmpty()) {
        BmobFile.uploadBatch(records, object : UploadBatchListener {
            override fun onSuccess(p0: MutableList<BmobFile>?, p1: MutableList<String>?) {
                if (p0 != null) {
                    val newRecords: MutableList<Record> = p0.map { bf ->
                        val record = this@update.noteRecording.filter {
                            it.fileName == bf.filename
                        }[0]
                        return@map Record(
                            recordDuration = record.recordDuration,
                            fileName = record.fileName,
                            url = bf.fileUrl
                        )
                    }.toMutableList()
                    newRecords.addAll(this@update.noteRecording.filter { it.url.isNotEmpty() })
                    // 录音上传成功
                    networkNote.noteRecording = newRecords
                }
                save()
            }

            override fun onProgress(p0: Int, p1: Int, p2: Int, p3: Int) {
            }

            override fun onError(p0: Int, p1: String?) {
                updateListener.done(BmobException(p1 ?: "上传录音失败"))
            }

        })
    } else {
        networkNote.noteRecording = this.noteRecording
        save()
    }
}


fun Note.toContentValues(): ContentValues {
    val values = ContentValues()

    this.javaClass.kotlin.memberProperties.forEach {
        println(it.name)
        when (it.name) {
            "createTime" -> {
                values.put(it.name, it.get(this) as Long)
            }
            "noteImages", "noteRecording", "tags" -> {
                values.put(it.name, it.get(this)?.toJson())
            }
            else -> {
                values.put(it.name, it.get(this).toString())
            }
        }
    }
    return values
}