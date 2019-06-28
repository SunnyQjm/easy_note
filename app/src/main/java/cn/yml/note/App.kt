package cn.yml.note

import android.app.Application
import cn.bmob.v3.Bmob
import cn.yml.note.model.Note
import com.zzhoujay.richtext.RichText

class App : Application() {
    companion object {
        var note: Note? = null
        const val APP_NAME = "easy_note"
    }
    override fun onCreate() {
        super.onCreate()

        Bmob.initialize(this, "524302a382e690834ae1521c45fa19b4")
        RichText.initCacheDir(this)
    }
}