package cn.yml.note.activity.main

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
                            .add(PicturePreviewActivity.PARAM_PICTURES, adapter.data.toTypedArray())
                            .add(PicturePreviewActivity.PARAM_POSITION, position)
                    )
                }
            }
        }
    }

    fun getContext() = mContext
    interface OnBlankAreaClickListener {
        fun onItemClick(adapter: NoteAdapter, item: Note)
    }
}

/**
 * 自动与云端的数据同步
 */
fun NoteAdapter.autoSyn() {
    if (BmobUser.isLogin()) {    // 已登录则尝试同步
        BmobQuery<NetworkNote>()
            .addWhereEqualTo("user", BmobUser.getCurrentUser(User::class.java))
            .findObjects(object : FindListener<NetworkNote>() {
                override fun done(p0: MutableList<NetworkNote>?, p1: BmobException?) {
                    if (p1 == null) {
                        if (p0 != null) {
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


                            // 处理需要上传的便签
                            needUpload.forEach {
                                it.save(object : SaveListener<String>() {
                                    override fun done(p0: String?, p1: BmobException?) {

                                    }
                                }, this@autoSyn.getContext(), false)
                            }

                            // 处理需要从云端同步到本地的便签
                            needDownload.forEach {
                                it.saveToLocal(this@autoSyn.getContext())
                            }
                        }
                        println(p0?.toJson() ?: "")
                    } else {
                        this@autoSyn.getContext().toast("自动同步失败")
                    }
                }

            })
    } else {                    // 未登陆则不同步

    }
}