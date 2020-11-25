package cn.yml.note.activity.record

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import cn.yml.note.R
import com.zzhoujay.richtext.RichText
import com.zzhoujay.richtext.callback.OnImageClickListener
import com.zzhoujay.richtext.callback.OnUrlClickListener
import kotlinx.android.synthetic.main.activity_record.*

/**
 * Record page
 */
class RecordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)

        initView()
    }

    private fun initView() {
        val markdown = "> test"
        RichText.fromMarkdown(markdown)

            .imageClick { imageUrls, position -> println(imageUrls.size) }
            .urlClick {
                false
            }
            .into(tvRichText)
    }
}
