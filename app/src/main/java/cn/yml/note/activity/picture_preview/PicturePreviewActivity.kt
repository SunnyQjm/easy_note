package cn.yml.note.activity.picture_preview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import cn.yml.note.R
import cn.yml.note.extensions.load
import com.sunny.scrollphotoview.ScrollPhotoView
import kotlinx.android.synthetic.main.activity_picture_preview.*

/**
 * 图片预览视图
 */
class PicturePreviewActivity : AppCompatActivity() {

    companion object {
        const val PARAM_PICTURES = "PARAM_PICTURES"
        const val PARAM_POSITION = "PARAM_POSITION"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture_preview)

        initView()
    }

    private fun initView() {
        imgBack.setOnClickListener {
            onBackPressed()
        }

        val urls = intent.getStringArrayExtra(PARAM_PICTURES)
        val position =  intent.getIntExtra(PARAM_POSITION, 0)

        spv.setUrls(urls)
        if(position >= 0 && position < urls.size)
            spv.setCurrentItem(position)
        spv.imgLoader = {
            url, view ->
            view.load(url)
        }

        spv.onScrollPhotoViewClickListener = object : ScrollPhotoView.OnScrollPhotoViewClickListener {
            override fun onClick(e: MotionEvent?) {
                // 单机返回
                onBackPressed()
            }

            override fun onDoubleTap(e: MotionEvent?) {
            }
        }

    }
}
