package cn.yml.note

import android.app.Application
import android.content.Context
import cn.bmob.v3.Bmob
import cn.yml.note.model.Note
import cn.yml.note.utils.SpUtils
import com.cunoraz.tagview.Tag
import com.zzhoujay.richtext.RichText

class App : Application() {
    companion object {
        var note: Note? = null
        var isAutoSyn = false
        var selectedTag: Tag? = null
        var selectMode: Int = 0             // 0 -> Tag 1 -> Calendar
        var selectDay: Long = 0
        const val APP_NAME = "easy_note"
        const val SP_AUTO_SYN = "SP_AUTO_SYN"
    }
    override fun onCreate() {
        super.onCreate()

        Bmob.initialize(this, "524302a382e690834ae1521c45fa19b4")
        RichText.initCacheDir(this)
        updateAutoSyn(SpUtils.get(this, SP_AUTO_SYN, false))
    }

}

fun Context.updateAutoSyn(isChecked: Boolean) {
    App.isAutoSyn = isChecked
    SpUtils.put(this, App.SP_AUTO_SYN, isChecked)
}