package cn.yml.note.activity.search

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.recyclerview.widget.LinearLayoutManager
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.UpdateListener
import cn.yml.note.App
import cn.yml.note.R
import cn.yml.note.activity.edit.EditActivity
import cn.yml.note.activity.main.NoteAdapter
import cn.yml.note.extensions.jumpTo
import cn.yml.note.extensions.toJson
import cn.yml.note.extensions.toast
import cn.yml.note.model.Image
import cn.yml.note.model.Note
import cn.yml.note.model.delete
import cn.yml.note.model.params.IntentParam
import cn.yml.note.utils.GsonUtil
import cn.yml.note.utils.database
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_search.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.appcompat.v7.Appcompat
import org.jetbrains.anko.db.parseList
import org.jetbrains.anko.db.rowParser
import org.jetbrains.anko.db.select
import org.jetbrains.anko.noButton
import org.jetbrains.anko.yesButton

class SearchActivity : AppCompatActivity() {

    private var noteAdapter: NoteAdapter? = null
    private val rxPermissions = RxPermissions(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        initView()
    }

    private fun initView() {
        imgBack.setOnClickListener {
            onBackPressed()
        }
        noteAdapter = NoteAdapter(mutableListOf())
        recyclerView.layoutManager = LinearLayoutManager(this)
        noteAdapter?.bindToRecyclerView(recyclerView)


        tvRight.setOnClickListener {
            rxPermissions
                .request(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .subscribe { granted ->
                    if (granted) {
                        getAllNotes(etSearch.text.toString())
                    } else {
                        toast("没有读写权限")
                    }
                }
        }

        // 点击便签Item进入便签预览界面
        noteAdapter?.setOnItemClickListener { adapter, view, position ->
            App.note = noteAdapter!!.getItem(position)
            jumpTo(
                EditActivity::class.java, IntentParam()
                    .add(EditActivity.PARAM_MODE, 1)
            )
        }
        noteAdapter?.onBlankAreaClick = object : NoteAdapter.OnBlankAreaClickListener {
            override fun onItemClick(adapter: NoteAdapter, item: Note) {
                App.note = item
                jumpTo(
                    EditActivity::class.java, IntentParam()
                        .add(EditActivity.PARAM_MODE, 1)
                )
            }
        }

        // 长按删除
        noteAdapter?.setOnItemLongClickListener { adapter, view, position ->
            val item = noteAdapter!!.getItem(position)
            alert(Appcompat, "", "确认要删除当前便签？") {
                yesButton {
                    item?.delete(object : UpdateListener() {
                        override fun done(p0: BmobException?) {
                            noteAdapter?.remove(position)
                        }
                    }, this@SearchActivity)
                }
                noButton {}
            }.show()
            return@setOnItemLongClickListener true
        }

    }


    private fun getAllNotes(key: String) {
        println("key: $key")
        database.use {
            select("note")
                .whereArgs("noteContent like {nc}",
                    "nc" to "%$key%")
                .exec {
                    println("fuck")
                    val result =
                        parseList(rowParser { id: String, noteTitle: String, noteContent: String, noteImages: String,
                                              noteRecording: String, tags: String, createTime: Long, reminder: Long, objectId: String ->
                            return@rowParser Note(
                                id,
                                noteTitle,
                                noteContent,
                                GsonUtil.json2ImageList(noteImages) as MutableList<Image>,
                                GsonUtil.json2RecordList(noteRecording),
                                GsonUtil.json2TagList(tags),
                                createTime,
                                reminder,
                                objectId
                            )
                        })
                    println(result.toJson())
                    noteAdapter?.data?.clear()
                    noteAdapter?.addData(result)
                }
        }
    }
}
