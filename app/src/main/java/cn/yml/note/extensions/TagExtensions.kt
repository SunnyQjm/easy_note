package cn.yml.note.extensions

import android.graphics.Color
import com.cunoraz.tagview.Tag
import com.cunoraz.tagview.TagView

val colors = arrayOf(
    "#ff8d41", "#01b3ee", "#f26d7e", "#91c607", "#f87d8a",
    "#fbab4a", "#fbab4a", "#36b1c0", "#b3ce1d", "#f36861", "#ffba07",
    "#66bee3", "#89deb5", "#927ff4",
    "#22ac97", "#62b07a", "#62b07a", "#f7d26c", "#ff8841", "#f791ac"
)

fun randomColor(): String {
    return colors[(Math.random() * colors.size).toInt()]
}

fun Any.randomTag(content: String = "", color: String = ""): Tag {
    val tag = Tag(content)
    if (color.isEmpty()) {
        tag.layoutColor = Color.parseColor(randomColor())
    } else {
        tag.layoutColor = Color.parseColor(color)
    }
    return tag
}

/**
 * 随机设置一个颜色
 */
fun Tag.autoRandomColor(): Tag {
    this.layoutColor = Color.parseColor(randomColor())
    return this
}

fun Tag.setDeleteAble(deleteAble: Boolean): Tag {
    this.isDeletable = deleteAble
    return this
}

/**
 * 切换所有标签的删除状态
 */
fun TagView.setDeleteAble(deleteAble: Boolean): TagView {
    val tags = this.tags.map {
        it.isDeletable = deleteAble
        return@map it
    }
    this.removeAll()
    this.addTags(tags)
    return this
}
