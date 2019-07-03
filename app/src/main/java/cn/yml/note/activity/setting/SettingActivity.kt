package cn.yml.note.activity.setting

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import cn.bmob.v3.BmobUser
import cn.yml.note.App
import cn.yml.note.R
import cn.yml.note.activity.login.LoginActivity
import cn.yml.note.activity.register_login.RegisterLoginActivity
import cn.yml.note.extensions.jumpTo
import cn.yml.note.model.User
import cn.yml.note.updateAutoSyn
import kotlinx.android.synthetic.main.activity_setting.*
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



        lmsAutoSyn.setChecked(App.isAutoSyn)

        // 退出
        tvExit.setOnClickListener {
            BmobUser.logOut()
            jumpTo(LoginActivity::class.java)
        }

        // 用户信息
        lmiUser.setOnClickListener {
            if(!BmobUser.isLogin()) {
                jumpTo(LoginActivity::class.java)
            }
        }

        // 是否自动同步
        lmsAutoSyn.setOnCheckedChangeListener { buttonView, isChecked ->
            updateAutoSyn(isChecked)
        }
    }

    override fun onResume() {
        super.onResume()
        val user = BmobUser.getCurrentUser(User::class.java)
        if(!BmobUser.isLogin()) {
            lmiUser.setValue("点此登录")
        } else{
            lmiUser.setValue(
                user.mobilePhoneNumber
            )
        }
    }
}
