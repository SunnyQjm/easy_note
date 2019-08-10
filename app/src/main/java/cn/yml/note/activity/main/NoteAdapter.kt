package cn.yml.note.activity.main

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import cn.bmob.v3.BmobQuery
import cn.bmob.v3.BmobUser
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.FindListener
import cn.bmob.v3.listener.SaveListener
import cn.yml.note.R
import cn.yml.note.activity.picture_preview.PicturePreviewActivity
import cn.yml.note.extensions.*
import cn.yml.note.model.*
import cn.yml.note.model.params.IntentParam
import cn.yml.note.views.BlankRecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.cunoraz.tagview.TagView

class NoteAdapter(mList: MutableList<Note>) : BaseQuickAdapter<Note, BaseViewHolder>(R.layout.item_note, mList) {

    var onBlankAreaClick: OnBlankAreaClickListener? = null
    override fun convert(helper: BaseViewHolder?, item: Note?) {
        helper?.let {
            item?.let {
                helper.setText(R.id.tvContent, item.noteContent)
                    .setText(R.id.tvTime, item.createTime.toYMD_HM())
//                    .setText(R.id.tvCategory, item.category)

                if(item.reminder > 0) {
                    helper.setVisible(R.id.imgReminder, true)
                        .setText(R.id.tvReminder, item.reminder.toYMD_HM())
                } else {
                    helper.setVisible(R.id.imgReminder, false)
                        .setVisible(R.id.tvReminder, false)
                }
                val tagView = helper.getView<TagView>(R.id.tagView)
                tagView.addTags(item.tags)
                tagView.setDeleteAble(false)

                val imgRecyclerView = helper.getView<BlankRecyclerView>(R.id.recyclerView)

                imgRecyclerView.setBlankListener {
                    onBlankAreaClick?.onItemClick(this, item)
                }
                imgRecyclerView.layoutManager = GridLayoutManager(mContext, 3)
                val adapter = ImageAdapter(item.noteImages)
                adapter.bindToRecyclerView(imgRecyclerView)

                // 点击图片
                adapter.setOnItemClickListener { _, view, position ->
                    mContext.jumpTo(
                        PicturePreviewActivity::class.java, IntentParam()
                            .add(PicturePreviewActivity.PARAM_PICTURES, adapter.data.map { it.filePath }.toTypedArray())
                            .add(PicturePreviewActivity.PARAM_POSITION, position)
                    )
                }
            }
        }
    }

    interface OnBlankAreaClickListener {
        fun onItemClick(adapter: NoteAdapter, item: Note)
    }
}

/**
 * 自动与云端的数据同步
 */
fun NoteAdapter.autoSyn(context: Context, finishCallback: (p1: BmobException?) -> Unit = {}) {
    if (BmobUser.isLogin()) {    // 已登录则尝试同步
        BmobQuery<NetworkNote>()
            .addWhereEqualTo("user", BmobUser.getCurrentUser(User::class.java))
            .findObjects(object : FindListener<NetworkNote>() {
                override fun done(p0: MutableList<NetworkNote>?, p1: BmobException?) {
                    if (p1 == null) {
                        if (p0 != null) {
                            println("服务器返回的数据: ${p0.toJson()}")
                            val netIds = p0.map {
                                return@map it.id
                            }
                            val netObjectIds = p0.map {
                                return@map it.objectId
                            }

                            val localIds = this@autoSyn.data.map {
                                return@map it.id
                            }
                            val localObjectIds = this@autoSyn.data.map {
                                return@map it.objectId
                            }

                            val needDownload = p0.filter {
                                return@filter !(localIds.contains(it.id) || localObjectIds.contains(it.objectId))
                            }

                            val needUpload = this@autoSyn.data.filter {
                                return@filter !(netIds.contains(it.id) || netObjectIds.contains(it.objectId))
                            }


                            var isCallCallback = false
                            var alreadyUpload = 0
                            var alreadyDownload = 0
                            // 处理需要上传的便签
                            needUpload.forEach {
                                println("处理上传")
                                it.save(object : SaveListener<String>() {
                                    override fun done(p0: String?, p1: BmobException?) {
                                        alreadyUpload++
                                        if (!isCallCallback && alreadyUpload == needUpload.size
                                            && alreadyDownload == needDownload.size) {
                                            finishCallback(null)
                                            isCallCallback = true
                                        }
                                    }
                                }, context, false)
                            }

                            // 处理需要从云端同步到本地的便签
                            needDownload.forEach {
                                println("处理下载")
                                it.saveToLocal(context) {
                                    alreadyDownload++
                                    if (!isCallCallback && alreadyUpload == needUpload.size
                                        && alreadyDownload == needDownload.size) {
                                        finishCallback(null)
                                        isCallCallback = true
                                    }
                                }
                            }
                            if (!isCallCallback && alreadyUpload == needUpload.size
                                && alreadyDownload == needDownload.size) {
                                finishCallback(null)
                                isCallCallback = true
                            }
                        }
                    } else {
                        context.toast("自动同步失败")
                    }
                }

            })
    } else {                    // 未登陆则不同步

    }
}