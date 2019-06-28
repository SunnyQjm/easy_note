package cn.yml.note.activity.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import cn.yml.note.App
import cn.yml.note.R
import cn.yml.note.activity.edit.EditActivity
import cn.yml.note.activity.register_login.RegisterLoginActivity
import cn.yml.note.activity.setting.SettingActivity
import cn.yml.note.extensions.jumpTo
import cn.yml.note.extensions.toJson
import cn.yml.note.model.Note
import cn.yml.note.model.params.IntentParam
import cn.yml.note.utils.GsonUtil
import cn.yml.note.utils.database
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.appcompat.v7.Appcompat
import org.jetbrains.anko.db.*
import org.jetbrains.anko.noButton
import org.jetbrains.anko.yesButton

/**
 * 主界面
 *
 * 显示便签列表，添加便签按钮
 */
class MainActivity : AppCompatActivity() {

    private var noteAdapter: NoteAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
    }

    private fun initView() {
        imgEdit.setOnClickListener {
            jumpTo(EditActivity::class.java)
        }
        imgSetting.setOnClickListener {
//            jumpTo(SettingActivity::class.java)
            jumpTo(RegisterLoginActivity::class.java)
        }

        noteAdapter = NoteAdapter(mutableListOf())
        recyclerView.layoutManager = LinearLayoutManager(this)
        noteAdapter?.bindToRecyclerView(recyclerView)

        // 点击便签Item进入便签预览界面
        noteAdapter?.setOnItemClickListener { adapter, view, position ->
            App.note = noteAdapter!!.getItem(position)
            jumpTo(EditActivity::class.java, IntentParam()
                .add(EditActivity.PARAM_MODE, 1))
        }
        noteAdapter?.onBlankAreaClick = object : NoteAdapter.OnBlankAreaClickListener {
            override fun onItemClick(adapter: NoteAdapter, item: Note) {
                App.note = item
                jumpTo(EditActivity::class.java, IntentParam()
                    .add(EditActivity.PARAM_MODE, 1))
            }
        }

        // 长按删除
        noteAdapter?.setOnItemLongClickListener { adapter, view, position ->
            val item = noteAdapter!!.getItem(position)
            alert(Appcompat,"", "确认要删除当前便签？") {
                yesButton {
                    database.use {
                        delete("note",
                            "id = ?", arrayOf(item!!.id))
                        getDataFromStorage()
                    }
                }
                noButton {}
            }.show()
            return@setOnItemLongClickListener true
        }
    }

    override fun onResume() {
        super.onResume()
        getDataFromStorage()
    }


    private fun getDataFromStorage() {
        database.use {
            select("note")
                .orderBy("createTime", SqlOrderDirection.DESC)
                .exec {
                    val result = parseList(rowParser { id: String, noteTitle: String, noteContent: String, noteImages: String,
                                                       noteRecording: String, tags: String, createTime: Long ->
                        return@rowParser Note(id, noteTitle, noteContent, GsonUtil.json2Bean(noteImages),
                            GsonUtil.json2Bean(noteRecording), GsonUtil.json2TagList(tags), createTime)
                    })
                    println(result.toJson())
                    noteAdapter?.data?.clear()
                    noteAdapter?.addData(result)
                }
        }
    }
}
