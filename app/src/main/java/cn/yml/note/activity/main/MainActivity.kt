package cn.yml.note.activity.main

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import cn.bmob.v3.BmobUser
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.UpdateListener
import cn.yml.note.App
import cn.yml.note.R
import cn.yml.note.activity.edit.EditActivity
import cn.yml.note.activity.login.LoginActivity
import cn.yml.note.activity.register_login.RegisterLoginActivity
import cn.yml.note.activity.setting.SettingActivity
import cn.yml.note.extensions.jumpTo
import cn.yml.note.extensions.rotate
import cn.yml.note.extensions.toJson
import cn.yml.note.extensions.toast
import cn.yml.note.model.Note
import cn.yml.note.model.delete
import cn.yml.note.model.params.IntentParam
import cn.yml.note.utils.GsonUtil
import cn.yml.note.utils.database
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.appcompat.v7.Appcompat
import org.jetbrains.anko.db.*
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.noButton
import org.jetbrains.anko.yesButton

/**
 * 主界面
 *
 * 显示便签列表，添加便签按钮
 */
class MainActivity : AppCompatActivity() {

    private var noteAdapter: NoteAdapter? = null
    private var synIng = false           // 是否正在同步
    private val rxPermissions = RxPermissions(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
    }

    private fun initView() {
        imgEdit.setOnClickListener {
            if (!synIngJudge())
                jumpTo(EditActivity::class.java)
        }
        imgSetting.setOnClickListener {
            if (!synIngJudge())
                jumpTo(SettingActivity::class.java)
//            jumpTo(RegisterLoginActivity::class.java)
//            jumpTo(RecordActivity::class.java)
//            jumpTo(LoginActivity::class.java)
        }

        noteAdapter = NoteAdapter(mutableListOf())
        recyclerView.layoutManager = LinearLayoutManager(this)
        noteAdapter?.bindToRecyclerView(recyclerView)

        // 点击便签Item进入便签预览界面
        noteAdapter?.setOnItemClickListener { adapter, view, position ->
            if (!synIngJudge()) {
                App.note = noteAdapter!!.getItem(position)
                jumpTo(
                    EditActivity::class.java, IntentParam()
                        .add(EditActivity.PARAM_MODE, 1)
                )
            }
        }
        noteAdapter?.onBlankAreaClick = object : NoteAdapter.OnBlankAreaClickListener {
            override fun onItemClick(adapter: NoteAdapter, item: Note) {
                if (!synIngJudge()) {
                    App.note = item
                    jumpTo(
                        EditActivity::class.java, IntentParam()
                            .add(EditActivity.PARAM_MODE, 1)
                    )
                }
            }
        }

        // 长按删除
        noteAdapter?.setOnItemLongClickListener { adapter, view, position ->
            if (!synIngJudge()) {
                val item = noteAdapter!!.getItem(position)
                alert(Appcompat, "", "确认要删除当前便签？") {
                    yesButton {
                        item?.delete(object : UpdateListener() {
                            override fun done(p0: BmobException?) {
                                getDataFromStorage()
                            }
                        }, this@MainActivity)
                    }
                    noButton {}
                }.show()
            }
            return@setOnItemLongClickListener true
        }

        imgRefresh.setOnClickListener {
            println("do permission request")
            // 点击同步按钮强制同步（无论是何网络环境）
            rxPermissions
                .request(Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe { granted ->
                    println("permission request result $granted")
                    if(granted) {
                        autoSyn(true)
                    } else {
                        toast("没有读写权限")
                    }
                }
        }
    }

    @SuppressLint("CheckResult")
    override fun onResume() {
        super.onResume()
        getDataFromStorage()

        rxPermissions
            .request(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .subscribe { granted ->
                if(granted) {
                    autoSyn()
                } else {
                    toast("没有读写权限")
                }
            }
    }

    private fun autoSyn(forceSyn: Boolean = false) {
        println("check do syn")
        if (synIng)
            return
        println("do syn")
        if (!BmobUser.isLogin()) {
            if (forceSyn) {          //忽略自动同步，手动同步时未登录则提示登录
                imgRefresh.snackbar("请先登录", "点此登录", action = {
                    jumpTo(RegisterLoginActivity::class.java)
                })
            }
            return
        }
        // 如果设置开启了自动同步，则尝试自动同步
        if (App.isAutoSyn || forceSyn) {
            val animator = imgRefresh.rotate(
                0f, 360f, duration = 500,
                repeatCount = -1,
                repeatMode = ValueAnimator.INFINITE
            )
            noteAdapter?.autoSyn(this) {
                animator.cancel()
                if (it == null) {        // 自动同步成功
                    if (forceSyn) {
                        imgRefresh.snackbar("同步成功")
                    }
                    getDataFromStorage()
                } else {
                    toast("同步失败")
                }
            }
        } else {
            println("do not syn")
        }
    }

    private fun synIngJudge(): Boolean {
        if (synIng) {
            imgRefresh.snackbar("正在同步便签，请稍后重试")
        }
        return synIng
    }


    private fun getDataFromStorage() {
        database.use {
            select("note")
                .orderBy("createTime", SqlOrderDirection.DESC)
                .exec {
                    val result =
                        parseList(rowParser { id: String, noteTitle: String, noteContent: String, noteImages: String,
                                              noteRecording: String, tags: String, createTime: Long, objectId: String ->
                            return@rowParser Note(
                                id,
                                noteTitle,
                                noteContent,
                                GsonUtil.json2ImageList(noteImages),
                                GsonUtil.json2RecordList(noteRecording),
                                GsonUtil.json2TagList(tags),
                                createTime,
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
