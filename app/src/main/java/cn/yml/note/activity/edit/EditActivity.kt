package cn.yml.note.activity.edit

import android.Manifest
import android.content.DialogInterface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.marginTop
import cn.yml.note.App
import cn.yml.note.R
import cn.yml.note.extensions.*
import cn.yml.note.model.Note
import cn.yml.note.model.toContentValues
import cn.yml.note.utils.ContentUriUtil
import cn.yml.note.utils.MyImagePicker
import cn.yml.note.utils.database
import com.qingmei2.rximagepicker.core.RxImagePicker
import com.qingmei2.rximagepicker_extension.MimeType
import com.qingmei2.rximagepicker_extension_wechat.WechatConfigrationBuilder
import com.zzhoujay.richtext.RichText
import kotlinx.android.synthetic.main.activity_edit.*
import kotlinx.android.synthetic.main.bar.*
import com.tbruyelle.rxpermissions2.RxPermissions
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.util.*


/**
 * 便签编辑界面
 */
class EditActivity : AppCompatActivity() {

    companion object {
        const val PARAM_MODE = "PARAM_MODE"
    }

    private var note = Note()
    private val rxPermissions = RxPermissions(this)
    private lateinit var tagDialog: DialogInterface

    /**
     * mode = 0  ==> 编辑模式
     * mode = 1  ==> 预览模式
     * mode = 2  ==> 编辑时预览模式
     */
    private var mode = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        initView()
    }

    private fun initView() {
        imgBack.setOnClickListener {
            onBackPressed()
        }

        // 获取显示模式
        mode = intent.getIntExtra(PARAM_MODE, 0)
        when (mode) {
            1 -> {
                note = App.note!!
                //            etCategory.setText(note.category)
                etContent.setText(note.noteContent)
                RichText.fromMarkdown(note.noteContent)
                    .into(tvPreview)
                tagView.addTags(note.tags)
                tvTime.text = note.createTime.toYMD_HM()
            }
            0 -> {
                note.createTime = System.currentTimeMillis()
                tvTime.text = note.createTime.toYMD_HM()
                val tag = randomTag("小记")
                    .setDeleteAble(true)
                tagView.addTag(tag)
                note.tags.add(tag)
            }
            else -> {

            }
        }

        changeMode(mode)

        tvRight.setOnClickListener {
            saveNote()
            when (mode) {
                0, 2 -> {
                    changeMode(1)
                }
                1 -> {
                    changeMode(0)
                }
            }
        }

        // 添加图片
        llAddPicture.setOnClickListener {
            llAddPicture.scaleXY(1f, 1.2f, 1f)

            // 先获取相机权限和读取手机相册的权限
            rxPermissions
                .request(
                    Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .subscribe { granted ->
                    if (granted) {
                        selector("选择获取图片方式", listOf("拍照", "相册")) { _, i ->
                            when (i) {
                                0 -> {          // 拍照
                                    // 使用摄像头拍摄
                                    RxImagePicker.create()
                                        .openCamera(this)
                                        .subscribe { result ->
                                            insetPicture(result.uri)
                                        }
                                }
                                1 -> {          // 相册选择
                                    val result = mutableListOf<Uri>()
                                    // 打开相册选择
                                    RxImagePicker.create(MyImagePicker::class.java)
                                        .openGallery(
                                            this,
                                            WechatConfigrationBuilder(MimeType.ofImage(), false)
                                                .maxSelectable(9)
                                                .countable(true)
                                                .spanCount(4)
                                                .countable(false)
                                                .build()
                                        )
                                        .subscribe({ res ->
                                            // onNext
                                            result.add(res.uri)
                                            insetPicture(res.uri)
                                        }, {
                                            // onError

                                        }, {
                                            // onComplete
//                                if(result.size <= 0) {
//                                    valueCallback2?.onReceiveValue(null)
//                                } else {
//                                    valueCallback2?.onReceiveValue(result.toTypedArray())
//                                }
                                        })

                                }
                            }
                        }
                    }
                }
        }

        // 添加录音
        llAddSoundRecord.setOnClickListener {
            llAddSoundRecord.scaleXY(1f, 1.2f, 1f)
        }

        // 预览便签
        llPreview.setOnClickListener {
            llPreview.scaleXY(1f, 1.2f, 1f)
            when (mode) {
                0 -> {
                    etContent.transXY(floatArrayOf(0f, 1000f), floatArrayOf(0f, 0f))
                    tvPreview.scaleXY(0.5f, 1f)
                    tvPreviewBtnText.text = "查看原文"
                    changeMode(2)
                }
                1 -> {

                }
                2 -> {
                    etContent.transXY(floatArrayOf(1000f, 0f), floatArrayOf(0f, 0f))
                    tvPreview.scaleXY(1f, 0.5f)
                    tvPreviewBtnText.text = "预览便签"
                    changeMode(0)
                }
            }
        }

        // 添加标签
        llAddTag.setOnClickListener {
            llAddTag.scaleXY(1f, 1.2f, 1f)
            tagDialog = alert {
                customView {
                    verticalLayout {
                        padding = dip(10)

                        textView("输入标签") {
                            gravity = Gravity.CENTER
                            textSize = 18f
                        }.lparams {
                            gravity = Gravity.CENTER
                            width = wrapContent
                            height = wrapContent
                            topMargin = dip(10)
                        }
                        val et = editText() {
                        }.lparams {
                            topMargin = dip(10)
                            width = matchParent
                            height = wrapContent
                            gravity = Gravity.CENTER
                        }
                        et.hint = "在此输入标签名称"
                        button("确认") {
                            onClick {
                                val tag = randomTag(et.text.toString())
                                    .setDeleteAble(true)
                                tagView.addTag(tag)
                                note.tags.add(tag)
                                tagDialog.dismiss()
                            }
                        }.lparams {
                            topMargin = dip(10)
                            gravity = Gravity.CENTER
                        }
                    }
                }
            }.show()
        }

        etContent.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                // 根据便签内容用Markdown渲染组件渲染到预览视图当中
                s?.let {
                    RichText.fromMarkdown(it.toString())
                        .into(tvPreview)
                    note.noteContent = it.toString()
                }
            }

        })

//        etCategory.addTextChangedListener(object : TextWatcher {
//            override fun afterTextChanged(s: Editable?) {
//
//            }
//
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                s?.let {
//                    note.category = it.toString()
//                }
//            }
//
//        })

        tvPreview.setOnClickListener {
            //            changeMode(1)
        }


        tagView.setOnTagDeleteListener { view, tag, position ->
            view.remove(position)
            note.tags.removeAt(position)
        }
    }

    override fun onBackPressed() {
        // 返回的时候先保存便签
        saveNote()
        super.onBackPressed()
    }


    /**
     * 插入图片到光标所在位置
     */
    fun insetPicture(uri: Uri) {
        val absolutePath = ContentUriUtil.getPath(this, uri)
        note.noteImages.add(absolutePath)
        val index = etContent.selectionStart
        val insertText = "\n![]($absolutePath)\n"
        val editable = etContent.editableText
        if (index < 0 || index >= editable.length) {
            editable.append(insertText)
        } else {
            editable.insert(index, insertText)
        }
    }

    /**
     * 保存note
     */
    fun saveNote() {
        database.use {
            if (note.id.isEmpty()) {         // 新建的便签，则插入到数据库中
                note.id = UUID.randomUUID().toString()
                insert(
                    "note",
                    null,
                    note.toContentValues()
                )
            } else {                       // 已有的便签，则更新数据库
                note.createTime = System.currentTimeMillis()
                update("note", note.toContentValues(), "id = ?", arrayOf(note.id))
            }
        }
    }

    /**
     * 改变显示模式
     */
    fun changeMode(m: Int) {
        // 保存当前模式
        mode = m
        when (m) {
            0, 2 -> {      // 编辑模式
                llPreview.visibility = View.VISIBLE
                llAddSoundRecord.visibility = View.VISIBLE
                llAddPicture.visibility = View.VISIBLE
                llAddTag.visibility = View.VISIBLE
                etContent.visibility = View.VISIBLE
                tvRight.text = "完成"
                tagView.setDeleteAble(true)
                tvTitle.text = "编辑标签"
            }
            1 -> {      // 预览模式
                llPreview.visibility = View.GONE
                llAddSoundRecord.visibility = View.GONE
                llAddPicture.visibility = View.GONE
                llAddTag.visibility = View.GONE
                etContent.visibility = View.GONE
                tvRight.text = "编辑"
                tvPreviewBtnText.text = "预览便签"
                tvTitle.text = "查看标签"
                tagView.setDeleteAble(false)
            }
        }
    }
}