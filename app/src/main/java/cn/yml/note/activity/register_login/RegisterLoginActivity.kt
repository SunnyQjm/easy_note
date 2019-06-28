package cn.yml.note.activity.register_login

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import cn.bmob.v3.BmobSMS
import cn.bmob.v3.BmobUser
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.LogInListener
import cn.bmob.v3.listener.QueryListener
import cn.yml.note.R
import cn.yml.note.extensions.setStyleText
import cn.yml.note.model.User
import cn.yml.note.utils.AccountValidatorUtil
import cn.yml.note.utils.doInterval
import kotlinx.android.synthetic.main.activity_register_login.*
import kotlinx.android.synthetic.main.bar.*
import org.jetbrains.anko.design.snackbar

class RegisterLoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_login)

        initView()
    }

    private fun initView() {
        imgBack.setOnClickListener {
            onBackPressed()
        }

        tvTitle.text = "一键登录"

        tvRight.visibility = View.GONE

        tvSendCode.setOnClickListener {
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
                        }
                    }

                })
            } else {
                tvSendCode.snackbar(R.string.please_input_validate_phone_number)
            }
        }

        // 登录
        btnLogin.setOnClickListener {
            val phone = etPhone.text.toString()
            val vertifyCode = etCode.text.toString()
            if (!AccountValidatorUtil.isMobile(phone)) {
                tvSendCode.snackbar(R.string.please_input_validate_phone_number)
                return@setOnClickListener
            }
            if (vertifyCode.isEmpty()) {
                tvSendCode.snackbar(R.string.vertify_code_not_allow_empty)
                return@setOnClickListener
            }

            println("phone: $phone")
            println("code: $vertifyCode")
            BmobUser.signOrLoginByMobilePhone(phone, vertifyCode, object : LogInListener<User>() {
                override fun done(p0: User?, p1: BmobException?) {
                    if (p1 == null) {
                        onBackPressed()
                    } else {
                        btnLogin.snackbar("登录失败，请检查验证码是否正确！${p1.message}")
                        println(p1.message?:"")
                    }
                }

            })
        }
    }
}
