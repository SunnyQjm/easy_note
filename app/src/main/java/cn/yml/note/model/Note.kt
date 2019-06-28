package cn.yml.note.model

import android.content.ContentValues
import android.util.Log
import cn.bmob.v3.BmobObject
import cn.bmob.v3.datatype.BmobFile
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.BmobUpdateListener
import cn.bmob.v3.listener.SaveListener
import cn.bmob.v3.listener.UpdateListener
import cn.bmob.v3.listener.UploadBatchListener
import cn.yml.note.extensions.toJson
import com.cunoraz.tagview.Tag
import kotlin.reflect.full.memberProperties


/**
 * 本地Note对象
 */
data class Note(
    var id: String = "",
    var noteTitle: String = "默认标题",                           // 便签标题
    var noteContent: String = "默认内容",                         // 便签内容
    var noteImages: MutableList<String> = mutableListOf(),      // 便签图片
    var noteRecording: MutableList<String> = mutableListOf(),   // 录音
    var tags: MutableList<Tag> = mutableListOf(),            // 标签
    var createTime: Long = System.currentTimeMillis()           // 创建时间
)

data class NetworkNote(
    var id: String = "",
    var noteTitle: String = "默认标题",                               // 便签标题
    var noteContent: String = "默认内容",                             // 便签内容
    var noteImages: MutableList<String> = mutableListOf(),        // 便签图片
    var noteRecording: MutableList<BmobFile> = mutableListOf(),     // 录音
    var tags: MutableList<Tag> = mutableListOf(),                   // 标签
    var createTime: Long = System.currentTimeMillis()               // 创建时间
): BmobObject()


/**
 * 保存
 */
fun Note.save(saveListener: SaveListener<String>) {
    val networkNote = NetworkNote(
        noteTitle = this.noteTitle,
        noteContent =  this.noteContent,
        tags = this.tags,
        noteImages = this.noteImages,
        createTime = this.createTime
    )

    val save = {
        networkNote.save(object : SaveListener<String>() {
            override fun done(p0: String?, p1: BmobException?) {
                if(p1 == null && p0 != null) {
                    this@save.id = p0
                } else {
                    Log.e("Note", p1?.message ?: "")
                }
                saveListener.done(p0, p1)
            }
        })
    }

    val records = this.noteRecording.toTypedArray()
    if(records.isNotEmpty()) {
        BmobFile.uploadBatch(records, object : UploadBatchListener {
            override fun onSuccess(p0: MutableList<BmobFile>?, p1: MutableList<String>?) {
                // 录音上传成功
                networkNote.noteRecording = p0 ?: mutableListOf()
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

fun Note.update(updateListener: UpdateListener) {
    val networkNote = NetworkNote(
        id = this.id,
        noteTitle = this.noteTitle,
        noteContent =  this.noteContent,
        tags = this.tags,
        noteImages = this.noteImages,
        createTime = this.createTime
    )
    networkNote.objectId = this.id

    val save = {
        networkNote.update(updateListener)
    }

    val records = this.noteRecording.toTypedArray()
    if(records.isNotEmpty()) {
        BmobFile.uploadBatch(records, object : UploadBatchListener {
            override fun onSuccess(p0: MutableList<BmobFile>?, p1: MutableList<String>?) {
                // 录音上传成功
                networkNote.noteRecording = p0 ?: mutableListOf()
                save()
            }

            override fun onProgress(p0: Int, p1: Int, p2: Int, p3: Int) {
            }

            override fun onError(p0: Int, p1: String?) {
                updateListener.done(BmobException(p1 ?: "上传录音失败"))
            }

        })
    } else {
        save()
    }
}



fun Note.toContentValues(): ContentValues {
    val values = ContentValues()

    this.javaClass.kotlin.memberProperties.forEach {
        println(it.name)
        when(it.name) {
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