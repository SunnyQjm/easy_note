package cn.yml.note.activity.setting

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import cn.yml.note.R
import kotlinx.android.synthetic.main.bar.*

class SettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        initView()
    }

    private fun initView() {
        imgBack.setOnClickListener {
            onBackPressed()
        }

        tvTitle.text = "设置"
        tvRight.visibility = View.GONE
        
    }
}
