package cn.yml.note

import android.app.Application
import cn.yml.note.extensions.toJson
import cn.yml.note.model.Note
import cn.yml.note.utils.GsonUtil
import cn.yml.note.utils.database
import com.google.gson.reflect.TypeToken
import org.jetbrains.anko.db.classParser
import org.jetbrains.anko.db.parseList
import org.jetbrains.anko.db.rowParser
import org.jetbrains.anko.db.select

class App : Application() {
    companion object {
        var note: Note? = null
    }
    override fun onCreate() {
        super.onCreate()
    }
}