package cn.yml.note.activity.register_login

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import cn.bmob.v3.BmobSMS
import cn.bmob.v3.BmobUser
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.LogInListener
import cn.bmob.v3.listener.QueryListener
import cn.yml.note.R
import cn.yml.note.activity.main.MainActivity
import cn.yml.note.extensions.hideSoftKeyboard
import cn.yml.note.extensions.jumpTo
import cn.yml.note.extensions.setStyleText
import cn.yml.note.model.User
import cn.yml.note.utils.AccountValidatorUtil
import cn.yml.note.utils.doInterval
import kotlinx.android.synthetic.main.activity_register_login.*
import kotlinx.android.synthetic.main.bar.*
import org.jetbrains.anko.design.snackbar

/**
 * One click login page
 */
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

        tvTitle.text = getString(R.string.one_click_login)

        tvRight.visibility = View.GONE

        tvSendCode.setOnClickListener {
            hideSoftKeyboard(tvSendCode)
            //发送验证码回调
            val phone = etPhone.text.toString()
            if (AccountValidatorUtil.isMobile(phone)) {      // send verify code
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

        // do login
        btnLogin.setOnClickListener {
            hideSoftKeyboard(btnLogin)
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

            BmobUser.signOrLoginByMobilePhone(phone, vertifyCode, object : LogInListener<User>() {
                override fun done(p0: User?, p1: BmobException?) {
                    if (p1 == null) {
                        jumpTo(MainActivity::class.java)
                    } else {
                        btnLogin.snackbar("Login failed, please check whether verify code is correct. ${p1.message}")
                        println(p1.message?:"")
                    }
                }

            })
        }
    }
}
