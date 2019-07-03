package cn.yml.note.activity.register

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import cn.bmob.v3.BmobSMS
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.QueryListener
import cn.bmob.v3.listener.SaveListener
import cn.yml.note.R
import cn.yml.note.activity.main.MainActivity
import cn.yml.note.extensions.hideSoftKeyboard
import cn.yml.note.extensions.jumpTo
import cn.yml.note.extensions.setStyleText
import cn.yml.note.model.User
import cn.yml.note.utils.AccountValidatorUtil
import cn.yml.note.utils.doInterval
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_register.etPhone
import kotlinx.android.synthetic.main.activity_register.tvSendCode
import kotlinx.android.synthetic.main.bar.*
import org.jetbrains.anko.design.snackbar

/**
 * 注册页面
 */
class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initView()
    }

    private fun initView() {
        tvRight.visibility = View.GONE
        tvTitle.text = "注册"
        imgBack.setOnClickListener {
            onBackPressed()
        }

        // 注册操作
        btnRegister.setOnClickListener {
            val phone = etPhone.text.toString()
            val code = etCode.text.toString()
            val password = etPassword.text.toString()
            val rePassword = etRePassword.text.toString()
            if(phone.isEmpty() || code.isEmpty() ||
                    password.isEmpty() || rePassword.isEmpty()) {
                btnRegister.snackbar(R.string.please_complete_fill_info)
                return@setOnClickListener
            }
            if(!AccountValidatorUtil.isMobile(phone)) {
                btnRegister.snackbar(R.string.please_input_validate_phone_number)
                return@setOnClickListener
            }
            if(password != rePassword) {
                btnRegister.snackbar("两次输入的密码不一致")
                return@setOnClickListener
            }

            val user = User()
            user.mobilePhoneNumber = phone
            user.setPassword(password)
            user.username = phone
            user.signOrLogin(code, object : SaveListener<User>() {
                override fun done(p0: User?, p1: BmobException?) {
                    if(p1 == null) {        // 注册成功
                        jumpTo(MainActivity::class.java)
                    } else {
                        btnRegister.snackbar("注册失败: ${p1.message}")
                    }
                }

            })

        }

        // 发送验证码
        tvSendCode.setOnClickListener {
            hideSoftKeyboard(tvSendCode)
            //发送验证码回调
            val phone = etPhone.text.toString()
            if (AccountValidatorUtil.isMobile(phone)) {      //发送验证码
                BmobSMS.requestSMSCode(phone, "", object : QueryListener<Int>() {
                    @SuppressLint("ResourceAsColor")
                    override fun done(smsId: Int?, e: BmobException?) {
                        if (e == null) {
                            tvSendCode.setTextColor(resources.getColor(R.color.grey))
                            tvSendCode.isClickable = false
                            doInterval(start = 1, count = 60) {
                                if (it < 60L) {
                                    tvSendCode.text =
                                        String.format(getString(R.string.send_code_after_serval_second), 60 - it)
                                } else {
                                    tvSendCode.isClickable = true
                                    tvSendCode.setStyleText(
                                        textRes = R.string.send_vertify_code,
                                        colorRes = R.color.colorPrimary
                                    )
                                }
                            }
                        } else {
                            tvSendCode.snackbar(R.string.send_vertify_code_fail)
                            Log.e("发送验证码", e.message ?: "发送验证码失败")
                        }
                    }

                })
            } else {
                tvSendCode.snackbar(R.string.please_input_validate_phone_number)
            }
        }

    }
}
