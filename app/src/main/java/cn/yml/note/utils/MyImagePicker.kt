package cn.yml.note.utils

import android.content.Context
import com.qingmei2.rximagepicker.entity.Result
import com.qingmei2.rximagepicker.entity.sources.Camera
import com.qingmei2.rximagepicker.entity.sources.Gallery
import com.qingmei2.rximagepicker.ui.ICustomPickerConfiguration
import com.qingmei2.rximagepicker_extension_wechat.ui.WechatImagePickerActivity
import io.reactivex.Observable

interface MyImagePicker {
    // 打开相册选择图片
    @Gallery(
        componentClazz = WechatImagePickerActivity::class,
        openAsFragment = false
    )
    fun openGallery(context: Context, config: ICustomPickerConfiguration):
            Observable<Result>

    // 打开相机拍照
    @Camera
    fun openCamera(context: Context):
            Observable<Result>
}
