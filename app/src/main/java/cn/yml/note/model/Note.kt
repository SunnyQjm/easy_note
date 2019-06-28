package cn.yml.note.model

import android.content.ContentValues
import android.content.Context
import android.util.Log
import cn.bmob.v3.BmobObject
import cn.bmob.v3.BmobUser
import cn.bmob.v3.datatype.BmobFile
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.BmobUpdateListener
import cn.bmob.v3.listener.SaveListener
import cn.bmob.v3.listener.UpdateListener
import cn.bmob.v3.listener.UploadBatchListener
import cn.yml.note.extensions.toJson
import cn.yml.note.utils.database
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
    var createTime: Long = System.currentTimeMillis(),           // 创建时间
    var objectId: String = ""                                   // Bmob对象Id
)

data class NetworkNote(
    var id: String = "",
    var noteTitle: String = "默认标题",                               // 便签标题
    var noteContent: String = "默认内容",                             // 便签内容
    var noteImages: MutableList<String> = mutableListOf(),        // 便签图片
    var noteRecording: MutableList<BmobFile> = mutableListOf(),     // 录音
    var tags: MutableList<Tag> = mutableListOf(),                   // 标签
    var user: User? = null,                                         // 关联的用户
    var createTime: Long = System.currentTimeMillis()               // 创建时间
): BmobObject()


/**
 * 保存
 */
fun Note.save(saveListener: SaveListener<String>, context: Context, needSaveToLocalStorage: Boolean = true) {
    val networkNote = NetworkNote(
        noteTitle = this.noteTitle,
        noteContent =  this.noteContent,
        tags = this.tags,
        noteImages = this.noteImages,
        createTime = this.createTime
    )
    if(!BmobUser.isLogin()) {
        if(needSaveToLocalStorage) {
            context.database.use {
                insert(
                    "note",
                    null,
                    this@save.toContentValues()
                )
            }
        }
        saveListener.done("", null)
        return
    }

    networkNote.user = BmobUser.getCurrentUser(User::class.java)


    val save = {
        networkNote.save(object : SaveListener<String>() {
            override fun done(p0: String?, p1: BmobException?) {
                if(p1 == null && p0 != null) {
                    this@save.objectId = p0
                    if(needSaveToLocalStorage) {
                        context.database.use {
                            insert(
                                "note",
                                null,
                                this@save.toContentValues()
                            )
                        }
                    }
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

fun Note.update(updateListener: UpdateListener, context: Context) {
    val networkNote = NetworkNote(
        id = this.id,
        noteTitle = this.noteTitle,
        noteContent =  this.noteContent,
        tags = this.tags,
        noteImages = this.noteImages,
        createTime = this.createTime
    )
    networkNote.objectId = this.objectId
    if(!BmobUser.isLogin() || this.objectId.isEmpty()) {
        updateListener.done(null)
        context.database.use {
            update("note", this@update.toContentValues(), "id = ?", arrayOf(this@update.id))
        }
        return
    }

    networkNote.user = BmobUser.getCurrentUser(User::class.java)

    val save = {
        networkNote.update(object : UpdateListener() {
            override fun done(p0: BmobException?) {
                if(p0 == null) {
                    context.database.use {
                        update("note", this@update.toContentValues(), "id = ?", arrayOf(this@update.id))
                    }
                }
                updateListener.done(p0)
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