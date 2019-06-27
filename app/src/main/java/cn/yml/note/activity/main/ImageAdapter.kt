package cn.yml.note.activity.main

import android.widget.ImageView
import cn.yml.note.R
import cn.yml.note.extensions.load
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class ImageAdapter(mList: MutableList<String>) : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_image, mList) {
    override fun convert(helper: BaseViewHolder?, item: String?) {
        helper?.let {
            item?.let {
                println(item)
                helper.getView<ImageView>(R.id.imageView)
                    .load(item)
            }
        }
    }
}