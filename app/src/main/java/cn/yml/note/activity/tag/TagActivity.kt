package cn.yml.note.activity.tag

import android.Manifest
import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
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
import com.cunoraz.tagview.Tag
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bar.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.appcompat.v7.Appcompat
import org.jetbrains.anko.db.parseList
import org.jetbrains.anko.db.rowParser
import org.jetbrains.anko.db.select
import org.jetbrains.anko.noButton
import org.jetbrains.anko.yesButton
import java.util.*

class TagActivity : AppCompatActivity() {

    private var noteAdapter: NoteAdapter? = null
    private val rxPermissions = RxPermissions(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tag)

        initView()
    }

    @SuppressLint("SetTextI18n")
    private fun initView() {
        tvRight.visibility = View.INVISIBLE

        imgBack.setOnClickListener {
            onBackPressed()
        }

        noteAdapter = NoteAdapter(mutableListOf())
        recyclerView.layoutManager = LinearLayoutManager(this)
        noteAdapter?.bindToRecyclerView(recyclerView)

        when(App.selectMode) {
            0 -> {      // 点击标签
                App.selectedTag?.let {
                    tvTitle.text = it.text
                    rxPermissions
                        .request(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                        .subscribe { granted ->
                            if (granted) {
                                getNotesByTag(it)
                            } else {
                                toast("没有读写权限")
                            }
                        }
                }
            }
            1 -> {      // 点击日历中的某一天
                val instance = Calendar.getInstance()
                instance.timeInMillis = App.selectDay
                val year = instance.get(Calendar.YEAR)
                val month = instance.get(Calendar.MONTH) + 1
                val day = instance.get(Calendar.DAY_OF_MONTH)

                tvTitle.text = "${year}年${month}月${day}日"
                rxPermissions
                    .request(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    .subscribe { granted ->
                        if (granted) {
                            getNotesByCalendar(App.selectDay)
                        } else {
                            toast("没有读写权限")
                        }
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
                    }, this@TagActivity)
                }
                noButton {}
            }.show()
            return@setOnItemLongClickListener true
        }

        smartRefreshView.setOnRefreshListener {
            // 下拉刷新
            App.selectedTag?.let {
                getNotesByTag(it)
            }
            smartRefreshView.finishRefresh()
        }

    }


    private fun getNotesByCalendar(selectDay: Long) {
        val instance = Calendar.getInstance()
        instance.timeInMillis = selectDay
        val year = instance.get(Calendar.YEAR)
        val month = instance.get(Calendar.MONTH) + 1
        val day = instance.get(Calendar.DAY_OF_MONTH)

        database.use {
            select("note")
                .exec {
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
                        }).filter {
//                            if(it.reminder < 0)
//                                return@filter false
                            val i = Calendar.getInstance()
                            i.timeInMillis = it.createTime
                            val y = i.get(Calendar.YEAR)
                            val m = i.get(Calendar.MONTH) + 1
                            val d = i.get(Calendar.DAY_OF_MONTH)
                            println("$y-$m-$d")
                            return@filter y == year && m == month && d == day
                        }
                    println(result.toJson())
                    noteAdapter?.data?.clear()
                    noteAdapter?.addData(result)
                }
        }
    }

    /**
     * 获取指定标签的Note
     */
    fun getNotesByTag(t: Tag) {
        database.use {
            select("note")
                .exec {
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
                        }).filter {
                            var need = false
                            it.tags.forEach { tag ->
                                println(tag.toJson())
                                if (tag.text == t.text)
                                    need = true
                                println("${tag.text} <> ${t.text}")
                            }
                            return@filter need
                        }
                    println(result.toJson())
                    noteAdapter?.data?.clear()
                    noteAdapter?.addData(result)
                }
        }
    }
}
