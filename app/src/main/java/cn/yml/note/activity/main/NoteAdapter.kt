package cn.yml.note.activity.main

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.yml.note.R
import cn.yml.note.extensions.toYMD_HM
import cn.yml.note.model.Note
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class NoteAdapter(mList: MutableList<Note>) : BaseQuickAdapter<Note, BaseViewHolder>(R.layout.item_note, mList) {
    override fun convert(helper: BaseViewHolder?, item: Note?) {
        helper?.let {
            item?.let {
                helper.setText(R.id.tvContent, item.noteContent)
                    .setText(R.id.tvTime, item.createTime.toYMD_HM())
//                    .setText(R.id.tvCategory, item.category)

                val imgRecyclerView = helper.getView<RecyclerView>(R.id.recyclerView)
                imgRecyclerView.layoutManager = GridLayoutManager(mContext, 3)
                val adapter = ImageAdapter(item.noteImages)
                adapter.bindToRecyclerView(imgRecyclerView)

                // 点击图片
                adapter.setOnItemClickListener { _, _, _ ->

                }
            }
        }
    }
}