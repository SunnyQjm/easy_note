package cn.yml.note.model

import android.content.ContentValues
import cn.yml.note.extensions.toJson
import com.cunoraz.tagview.Tag
import kotlin.reflect.full.memberProperties


data class Note(
    var id: String = "",
    var noteTitle: String = "默认标题",                           // 便签标题
    var noteContent: String = "默认内容",                         // 便签内容
    var noteImages: MutableList<String> = mutableListOf(),      // 便签图片
    var noteRecording: MutableList<String> = mutableListOf(),   // 录音
    var tags: MutableList<Tag> = mutableListOf(),            // 标签
    var createTime: Long = System.currentTimeMillis()           // 创建时间
)

fun Note.toContentValues(): ContentValues {
    val values = ContentValues()

    this.javaClass.kotlin.memberProperties.forEach {
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