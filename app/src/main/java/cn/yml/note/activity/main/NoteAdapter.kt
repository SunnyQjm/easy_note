package cn.yml.note.activity.main

import androidx.recyclerview.widget.GridLayoutManager
import cn.yml.note.R
import cn.yml.note.extensions.setDeleteAble
import cn.yml.note.extensions.toYMD_HM
import cn.yml.note.model.Note
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
                adapter.setOnItemClickListener { _, _, _ ->

                }
            }
        }
    }

    interface OnBlankAreaClickListener {
        fun onItemClick(adapter: NoteAdapter, item: Note)
    }
}