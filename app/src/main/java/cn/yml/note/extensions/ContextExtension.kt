package cn.yml.note.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.view.Gravity
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import cn.yml.note.R
import cn.yml.note.model.Record
import cn.yml.note.model.params.IntentParam
import cn.yml.note.utils.FileUtils
import com.github.piasy.rxandroidaudio.PlayConfig
import com.github.piasy.rxandroidaudio.RxAudioPlayer
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.*
import org.jetbrains.anko.imageView
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.textView
import java.io.File
import java.lang.Exception
//import cn.smssdk.EventHandler
//import cn.smssdk.SMSSDK
//import cn.smssdk.gui.RegisterPage
import java.util.*
import java.util.concurrent.TimeUnit

fun Context.toast(info: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, info, duration)
        .show()
}

fun Context.toast(@StringRes stringRes: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, stringRes, duration)
        .show()
}


fun Context.jumpTo(cls: Class<*>, vararg flags: Int) {
    val intent = Intent(this, cls)
    flags.forEach {
        intent.addFlags(it)
    }
    startActivity(intent)
}

fun Activity.jumpForResult(cls: Class<*>, requestCode: Int) {
    val intent = Intent(this, cls)
//    intentParam?.applyParam(intent)
    startActivityForResult(intent, requestCode)
}

fun Context.jumpTo(cls: Class<*>, intentParam: IntentParam? = null, vararg flags: Int) {
    val intent = Intent(this, cls)
    flags.forEach {
        intent.addFlags(it)
    }
    intentParam?.applyParam(intent)
    startActivity(intent)
}

fun Activity.jumpForResult(cls: Class<*>, requestCode: Int = 0, intentParam: IntentParam? = null) {
    val intent = Intent(this, cls)
    intentParam?.applyParam(intent)
    startActivityForResult(intent, requestCode)
}

//
//fun Fragment.jumpForResult(cls: Class<*>, requestCode: Int, intentParam: IntentParam? = null) {
//    val intent = Intent(activity, cls)
//    intentParam?.applyParam(intent)
//    startActivityForResult(intent, requestCode)
//}

/////////////////////////////////////////////////////////
///////// 下面是软键盘相关的扩展
////////////////////////////////////////////////////////

/**
 * 切换状态，打开则关闭，关闭则打开
 */
fun Context.changeSoftKeyboard() {
    val im = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    if (!im.isActive)
        return
    im.toggleSoftInput(0, 0)
}

/**
 * 隐藏软键盘
 */
fun Context.hideSoftKeyboard(view: View) {
    val im = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    if (!im.isActive)
        return
    //隐藏软键盘 //
    im.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Context.showSoftKeyboard(view: View) {
    val im = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    if (im.isActive)
        return
    im.showSoftInputFromInputMethod(view.windowToken, 0)
}


fun Context.showSelectDateDialog(
    @StyleRes style: Int = android.R.style.Theme_DeviceDefault_Dialog,
    listener: (View, Int, Int, Int) -> Unit =
        { view, year, month, dayOfMonth -> }
) {
    val c = Calendar.getInstance()
    DatePickerDialog(
        this, style, listener, c.get(Calendar.YEAR),
        c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
    )
        .show()
}

fun Context.showSelectDateDialog(
    @StyleRes style: Int = android.R.style.Theme_DeviceDefault_Dialog,
    listener: (Long) -> Unit =
        { time -> }
) {
    val c = Calendar.getInstance()
    DatePickerDialog(
        this, style, { view, year, month, dayOfMonth ->
            c.timeInMillis = 0
            c.set(Calendar.YEAR, year)
            c.set(Calendar.MONTH, month)
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            listener(c.timeInMillis)
        }, c.get(Calendar.YEAR),
        c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
    )
        .show()
}


fun Context.checkMyPermission(permission: String, granted: () -> Unit = {}, deny: () -> Unit = {}) {
    if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
        granted()
    } else {
        deny()
    }
}

@SuppressLint("CheckResult")
fun Context.beginPlayRecord(filePath: String, error: (errMsg: String) -> Unit = {}) {
    val file = File(filePath)
    if (!file.exists()) {
        error("file $filePath not exists")
        return
    }
    RxAudioPlayer.getInstance()
        .play(
            PlayConfig.file(file)
                .looping(true)
                .build()
        )
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({

        }, {
            it.printStackTrace()
        }, {

        })
}

fun Context.stopPlayRecord() {
    try {
        RxAudioPlayer.getInstance()
            .stopPlay()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Context.pausePlayRecord() {
    try {
        RxAudioPlayer.getInstance()
            .pause()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Context.resumePlayRecord() {
    try {
        RxAudioPlayer.getInstance()
            .resume()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}


fun Context.playRecord(record: Record): DialogInterface {
    val playRecordDialog: DialogInterface
    beginPlayRecord(record.filePath) {
        Log.e("playRecord", it)
        Log.e("playRecord", FileUtils.getRecordPath())
    }
    var tv: TextView? = null
    var progressBar: ProgressBar? = null
    playRecordDialog = alert {
        customView {
            linearLayout {
                gravity = Gravity.CENTER_VERTICAL
                orientation = LinearLayout.HORIZONTAL
                padding = dip(10)

                var playState = 1
                imageView {
                    setImageResource(R.drawable.pause)
                    onClick {
                        if (playState == 0) {        // 开始播放
                            playState = 1
                            resumePlayRecord()
                            setImageResource(R.drawable.pause)
                        } else {
                            playState = 0
                            pausePlayRecord()
                            setImageResource(R.drawable.play)
                        }
                    }
                }.lparams {
                    width = dip(30)
                    height = dip(30)
                    weight = 0f
                }

                progressBar = horizontalProgressBar {

                }.lparams {
                    weight = 1f
                    leftMargin = dip(10)
                    rightMargin = dip(10)
                }

                tv = textView {

                }.lparams {
                    weight = 0f
                    width = wrapContent
                    height = wrapContent
                }
            }
        }

        val disposable = Observable.interval(500, TimeUnit.MILLISECONDS)
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe {
                val progress = RxAudioPlayer.getInstance().progress()
                val second = progress.rem(60)
                val minutes = progress / 60
                tv?.post {
                    tv?.text =
                        "${autoGenericCode(
                            minutes.toString(),
                            2
                        )} : ${autoGenericCode(second.toString(), 2)}"
                }
                progressBar?.post {
                    progressBar?.progress = (progress * 100 / record.recordDuration)
                }
            }
        onCancelled {
            stopPlayRecord()
            if (!disposable.isDisposed) {
                disposable.dispose()
            }
        }
    }.show()
    return playRecordDialog
}

/**
 * 不够位数的在前面补0，保留num的长度位数字
 * @param code
 * @return
 */
private fun autoGenericCode(code: String, num: Int): String {
    return String.format("%0" + num + "d", Integer.parseInt(code))
}

///**
// * 打开发送短信验证界面
// */
//fun Context.sendCode(fail: () -> Unit = {},
//                     success: (phone: String, country: String) -> Unit = { phone, country -> }) {
//    val page = RegisterPage()
//    page.setTempCode(null)
//    page.setRegisterCallback(object : EventHandler() {
//        override fun afterEvent(event: Int, result: Int, data: Any?) {
//            if (result == SMSSDK.RESULT_COMPLETE) {
//                // 处理成功的结果
//                val phoneMap = data as HashMap<*, *>
//                val country = phoneMap["country"] as String // 国家代码，如“86”
//                val phone = phoneMap["phone"] as String // 手机号码，如“13800138000”
//                // TODO 利用国家代码和手机号码进行后续的操作
//                success(phone, country)
//            } else {
//                // TODO 处理错误的结果
//                fail()
//            }
//        }
//    })
//    page.show(this)
//}
