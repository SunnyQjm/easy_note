package cn.yml.note.activity.edit

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.TextView
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.SaveListener
import cn.bmob.v3.listener.UpdateListener
import cn.yml.note.App
import cn.yml.note.R
import cn.yml.note.activity.picture_preview.PicturePreviewActivity
import cn.yml.note.extensions.*
import cn.yml.note.model.*
import cn.yml.note.model.params.IntentParam
import cn.yml.note.utils.*
import cn.yml.note.views.TapHoldUpButton
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.listener.OnTimeSelectListener
import com.qingmei2.rximagepicker.core.RxImagePicker
import com.qingmei2.rximagepicker_extension.MimeType
import com.qingmei2.rximagepicker_extension_wechat.WechatConfigrationBuilder
import com.zzhoujay.richtext.RichText
import kotlinx.android.synthetic.main.activity_edit.*
import kotlinx.android.synthetic.main.bar.*
import com.tbruyelle.rxpermissions2.RxPermissions
import com.zzhoujay.richtext.CacheType
import com.zzhoujay.richtext.ImageHolder
import com.zzhoujay.richtext.callback.OnUrlClickListener
import org.jetbrains.anko.*
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.io.File
import java.util.*


/**
 * Note edit activity
 */
class EditActivity : AppCompatActivity() {

    companion object {
        const val PARAM_MODE = "PARAM_MODE"
    }

    private var note = Note()
    private val rxPermissions = RxPermissions(this)
    private lateinit var tagDialog: DialogInterface
    private lateinit var recordDialog: DialogInterface
    private lateinit var recordTime: TextView
    private lateinit var recordFile: File
    private var hidePosition: Float = 2000f


    /**
     * mode = 0  ==> edit mode
     * mode = 1  ==> preview mode
     * mode = 2  ==> preview when edit
     */
    private var mode = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        RichText.initCacheDir(this)
        RichText.debugMode = true
        initView()
    }

    @SuppressLint("SetTextI18n")
    private fun initView() {
        hidePosition = dip(1000).toFloat()
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
                renderRichText(note.noteContent)
                tagView.addTags(note.tags)
                tvTime.text = note.createTime.toYMD_HM()
            }
            0 -> {
                note.createTime = System.currentTimeMillis()
                tvTime.text = note.createTime.toYMD_HM()
                val tag = randomTag("note")
                    .setDeleteAble(true)
                tagView.addTag(tag)
                note.tags.add(tag)
            }
            else -> {

            }
        }

        changeMode(mode)

        tvRight.setOnClickListener {
            hideSoftKeyboard(tvRight)
            saveNote()
            when (mode) {
                0, 2 -> {
                    etContent.transXY(floatArrayOf(0f, hidePosition), floatArrayOf(0f, 0f))
                    tvPreview.transXY(floatArrayOf(-hidePosition, 0f), floatArrayOf(0f, 0f))
                    changeMode(1)
                }
                1 -> {
                    changeMode(0)
                }
            }
        }

        // Add picture
        llAddPicture.setOnClickListener {
            hideSoftKeyboard(llAddPicture)
            llAddPicture.scaleXY(1f, 1.2f, 1f)

            // get permission first
            rxPermissions
                .request(
                    Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .subscribe { granted ->
                    if (granted) {
                        selector("Select get picture way", listOf("photograph", "album")) { _, i ->
                            when (i) {
                                0 -> {          // photograph
                                    RxImagePicker.create()
                                        .openCamera(this)
                                        .subscribe { result ->
                                            insetPicture(result.uri)
                                        }
                                }
                                1 -> {          // album
                                    val result = mutableListOf<Uri>()
                                    // open album
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
                                        })

                                }
                            }
                        }
                    }
                }
        }

        // voice to text
        tapHoldBtn.setOnButtonClickListener(object : TapHoldUpButton.OnButtonClickListener {
            val absWrapper = ABSWrapper()

            @SuppressLint("CheckResult")
            override fun onLongHoldStart(v: View?) {
                // get permission first
                rxPermissions
                    .request(
                        Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    .subscribe { granted ->
                        if (granted) {
                            absWrapper.start(tapHoldBtn.context, onSegmentSuccess = { it ->
                                // insert segment
                                val index = etContent.selectionStart
                                // Delete extra period
                                if (it.last() == '。') {
                                    it.dropLast(1)
                                }
                                val editable = etContent.editableText
                                if (index < 0 || index >= editable.length) {
                                    editable.append(it)
                                } else {
                                    editable.insert(index, it)
                                }
                            })
                        }
                    }
            }

            override fun onLongHoldEnd(v: View?) {
                absWrapper.stop();
            }

            override fun onClick(v: View?) {
                toast("Record time is too short!");
            }
        })

        // preview note
        llPreview.setOnClickListener {
            hideSoftKeyboard(llPreview)
            llPreview.scaleXY(1f, 1.2f, 1f)
            when (mode) {
                0 -> {
                    etContent.transXY(floatArrayOf(0f, hidePosition), floatArrayOf(0f, 0f))
                    tvPreview.transXY(floatArrayOf(-hidePosition, 0f), floatArrayOf(0f, 0f))
//                    tvPreviewBtnText.text = "View source"
                    changeMode(2)
                }
                1 -> {

                }
                2 -> {
                    etContent.transXY(floatArrayOf(hidePosition, 0f), floatArrayOf(0f, 0f))
                    tvPreview.transXY(floatArrayOf(0f, -hidePosition), floatArrayOf(0f, 0f))
//                    tvPreviewBtnText.text = "Preview note"
                    changeMode(0)
                }
            }
        }

        // Add tag
        llAddTag.setOnClickListener {
            hideSoftKeyboard(llAddTag)
            llAddTag.scaleXY(1f, 1.2f, 1f)
            tagDialog = alert {
                customView {
                    verticalLayout {
                        padding = dip(10)

                        textView("Input tag") {
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
                        et.hint = "Input tag name here"
                        button("Confirm") {
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

                // render markdown text
                s?.let {
                    renderRichText(it.toString())
                    note.noteContent = it.toString()
                }
            }

        })


        tagView.setOnTagDeleteListener { view, tag, position ->
            view.remove(position)
            note.tags.removeAt(position)
        }

        renderReminderText(note)
        imgReminder.setOnClickListener {
            rxPermissions
                .request(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)
                .subscribe { granted ->
                    if (granted) {
                        selectReminderTime { date, v ->
                            // 如果存在先删除旧的事项
                            CalendarReminderUtils.deleteCalendarEvent(this, "EasyNote(${note.id})")
                            note.reminder = date.time
                            CalendarReminderUtils.addCalendarEvent(
                                this, "EasyNote(${note.id})", note.noteContent,
                                note.reminder
                            )
                            renderReminderText(note)
                        }
                    }
                }
        }


    }


    private fun renderReminderText(node: Note) {
        if (note.reminder > 0) {
            tvReminder.text = note.reminder.toYMD_HMS()
        }
    }

    override fun onBackPressed() {
        val dialog = indeterminateProgressDialog("Saving") {

        }
        // save note before back
        saveNote {
            dialog.dismiss()
            super.onBackPressed()
        }
    }


    /**
     * Insert picture to current position
     */
    fun insetPicture(uri: Uri) {
        val absolutePath = ContentUriUtil.getPath(this, uri)
        val file = File(absolutePath)
        if (!file.exists()) {
            llAddPicture.snackbar("Picture: $absolutePath not found, insert failed!")
            return
        }

        val dialog = indeterminateProgressDialog("Inserting") {

        }
        // 将选中的图片拷贝到本应用的缓存目录下
        val targetFile = file.copyTo(
            FileUtils.generatePictureFile("${System.currentTimeMillis()}-${file.name}"), true
        )

        if (!targetFile.exists()) {
            llAddPicture.snackbar("Picture: $absolutePath not found, insert failed!")
            return
        }


        note.noteImages.add(
            Image(
                fileName = targetFile.name,
                filePath = targetFile.absolutePath
            )
        )
        val index = etContent.selectionStart
        val insertText = "\n![](${targetFile.absoluteFile})\n"
        val editable = etContent.editableText
        if (index < 0 || index >= editable.length) {
            editable.append(insertText)
        } else {
            editable.insert(index, insertText)
        }
        dialog.dismiss()

    }

    /**
     * save note
     */
    fun saveNote(finishCallback: () -> Unit = {}) {
        if (note.id.isEmpty()) {         // create new note, save to db
            note.id = UUID.randomUUID().toString()
            note.save(object : SaveListener<String>() {
                override fun done(p0: String?, p1: BmobException?) {
                    finishCallback()
                    if (p1 == null) {
//                        toast("save success")
                    } else {
                        toast("save failed: " + p1.message)
                    }
                }
            }, this)
        } else {                       // already exists note, update db
            note.createTime = System.currentTimeMillis()
            note.update(object : UpdateListener() {
                override fun done(p0: BmobException?) {
                    finishCallback()
                    if (p0 == null) {        // update success
//                        toast("update success")
                    } else {
                        toast("update failed: " + p0.message)
                    }
                }

            }, this)
        }

        if (note.reminder > 0) {
            // delete if exists
            CalendarReminderUtils.deleteCalendarEvent(this, "EasyNote(${note.id})")
            note.reminder = note.reminder
            CalendarReminderUtils.addCalendarEvent(
                this, "EasyNote(${note.id})", note.noteContent,
                note.reminder
            )
            renderReminderText(note)
        }
    }

    /**
     * change mode
     */
    fun changeMode(m: Int) {
        // save current mode
        mode = m
        when (m) {
            0, 2 -> {      // edit mode
                llPreview.visibility = View.VISIBLE
                llAddSoundRecord.visibility = View.VISIBLE
                llAddPicture.visibility = View.VISIBLE
                llAddTag.visibility = View.VISIBLE
                etContent.visibility = View.VISIBLE
                tapHoldBtn.visibility = View.VISIBLE
                tvRight.text = getString(R.string.edit_activity_finish)
                tagView.setDeleteAble(true)
                tvTitle.text = getString(R.string.edit_activity_edit_note)
            }
            1 -> {      // preview mode
                llPreview.visibility = View.GONE
                llAddSoundRecord.visibility = View.GONE
                llAddPicture.visibility = View.GONE
                llAddTag.visibility = View.GONE
                etContent.visibility = View.GONE
                tapHoldBtn.visibility = View.GONE
                tvRight.text = getString(R.string.edit_activity_edit)
//                tvPreviewBtnText.text = getString(R.string.edit_activity_preview_note)
                tvTitle.text = getString(R.string.edit_activity_view_note)
                tagView.setDeleteAble(false)
            }
        }
    }


    fun renderRichText(content: String) {
        RichText.fromMarkdown(content)
            .cache(CacheType.layout)
            .scaleType(ImageHolder.ScaleType.fit_center) // picture scale type
//            .imageLongClick(OnImageLongClickListener { imageUrls, position ->
//
//                return@OnImageLongClickListener true
//            })
            .imageClick { imageUrls, position ->
                jumpTo(
                    PicturePreviewActivity::class.java, IntentParam()
                        .add(PicturePreviewActivity.PARAM_PICTURES, imageUrls.toTypedArray())
                        .add(PicturePreviewActivity.PARAM_POSITION, position)
                )
            }
            .urlClick(object : OnUrlClickListener {
                override fun urlClicked(url: String?): Boolean {
                    url?.let { name ->
                        val recording = note.noteRecording.filter { it.fileName == name }
                        if (recording.isNotEmpty()) {
                            playRecord(recording.first())
                        }
                    }
                    return true
                }

            })
            .size(ImageHolder.MATCH_PARENT, dip(100))
            .into(tvPreview)
    }


    /**
     * select reminderTime
     */
    fun selectReminderTime(callback: (date: Date, v: View?) -> Unit) {
        val pvTime = TimePickerBuilder(this, OnTimeSelectListener { date, v ->
            callback(date, v)
        })
            .setType(BooleanArray(6, init = {
                return@BooleanArray true
            }))
            .build()
        pvTime.show()
    }
}