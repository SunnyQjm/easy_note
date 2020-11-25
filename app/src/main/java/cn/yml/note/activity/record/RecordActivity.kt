package cn.yml.note.activity.record

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import cn.yml.note.R
import com.zzhoujay.richtext.RichText
import com.zzhoujay.richtext.callback.OnImageClickListener
import com.zzhoujay.richtext.callback.OnUrlClickListener
import kotlinx.android.synthetic.main.activity_record.*

/**
 * 录音页面
 */
class RecordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)

        initView()
    }

    private fun initView() {
        val markdown = "蹭今难\n" +
                "[起风了](276989035900740.file.m4a)\n" +
                "哦送哦\n" +
                "![](http://easy_note.qjm253.cn/2019/06/28/e3e3e241556245848c5fec50931eee25.jpg)\n" +
                "\n111111\n" +
                "![](http://easy_note.qjm253.cn/2019/06/28/e3e3e241556245848c5fec50931eee25.jpg)\n" +
                "\n"
        RichText.fromMarkdown(markdown)

            .imageClick { imageUrls, position -> println(imageUrls.size) }
            .urlClick {
                false
            }
            .into(tvRichText)
    }
}
