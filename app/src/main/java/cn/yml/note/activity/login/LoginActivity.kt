package cn.yml.note.activity.login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import cn.bmob.v3.BmobUser
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.LogInListener
import cn.bmob.v3.listener.SaveListener
import cn.yml.note.R
import cn.yml.note.activity.main.MainActivity
import cn.yml.note.activity.register.RegisterActivity
import cn.yml.note.activity.register_login.RegisterLoginActivity
import cn.yml.note.extensions.hideSoftKeyboard
import cn.yml.note.extensions.jumpTo
import cn.yml.note.model.User
import cn.yml.note.utils.AccountValidatorUtil
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.bar.*
import org.jetbrains.anko.design.snackbar

/**
 * 登录页面
 */
class LoginActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initView()
    }

    private fun initView() {
        tvRight.visibility = View.GONE
        tvTitle.text = getString(R.string.login)
        imgBack.setOnClickListener {
            onBackPressed()
        }


        // 执行登录操作
        btnLogin.setOnClickListener {
            hideSoftKeyboard(btnLogin)
            val phone = etPhone.text.toString()
            val password = etPassword.text.toString()
            if(phone.isEmpty() || password.isEmpty()) {
                btnLogin.snackbar(R.string.please_complete_fill_info)
                return@setOnClickListener
            }
            if(!AccountValidatorUtil.isMobile(phone)) {
                btnLogin.snackbar(R.string.please_input_validate_phone_number)
                return@setOnClickListener
            }
            val user = User()
            user.username = phone
            user.setPassword(password)
            BmobUser.loginByAccount(phone, password, object : LogInListener<User>() {
                override fun done(p0: User?, p1: BmobException?) {
                    if(p1 == null) {        // 登录成功
                        jumpTo(MainActivity::class.java)
                    } else {
                        btnLogin.snackbar("Login failed: ${p1.message}")
                    }
                }
            })
        }


        // 跳转到注册
        tvRegisterNow.setOnClickListener {
            jumpTo(RegisterActivity::class.java)
        }

        // 跳转到一键登录
        tvOneClickLogin.setOnClickListener {
            jumpTo(RegisterLoginActivity::class.java)
        }


    }
}
