package com.android6.doyin.utils

import android.annotation.SuppressLint
import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager

import java.lang.reflect.Field

/**
 * Created by tanshunwang on 2016/10/1 0001.
 */

object DensityUtil {
    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    fun px2dip(context: Context, pxValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     *
     * @param pxValue （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    fun px2sp(context: Context, pxValue: Float): Int {
        val fontScale = context.resources.displayMetrics.scaledDensity
        return (pxValue / fontScale + 0.5f).toInt()
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param spValue （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    fun sp2px(context: Context, spValue: Float): Int {
        val fontScale = context.resources.displayMetrics.scaledDensity
        return (spValue * fontScale + 0.5f).toInt()
    }

    /**
     * 获取屏幕宽度
     *
     * @param context
     * @return
     */
    fun getScreenWidth(context: Context): Int {
        val dm = DisplayMetrics()
        val manager = context
                .getSystemService(Context.WINDOW_SERVICE) as WindowManager
        manager.defaultDisplay.getMetrics(dm)
        return dm.widthPixels
    }

    fun getScreenHeight(context: Context): Int {
        val dm = DisplayMetrics()
        val manager = context
                .getSystemService(Context.WINDOW_SERVICE) as WindowManager
        manager.defaultDisplay.getMetrics(dm)
        return dm.heightPixels
    }

    @SuppressLint("PrivateApi")
    fun getStatusBarHeight(context: Context): Int {
        var c: Class<*>? = null

        var obj: Any? = null

        var field: Field? = null

        var x = 0
        var sbar = 0

        try {

            c = Class.forName("com.android.internal.R\$dimen")

            obj = c!!.newInstance()

            field = c.getField("status_bar_height")

            x = Integer.parseInt(field!!.get(obj).toString())

            sbar = context.resources.getDimensionPixelSize(x)

        } catch (e1: Exception) {

            e1.printStackTrace()

        }

        return sbar
    }

    fun getScreenParams(context: Context): IntArray {
        val manager = context
                .getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = manager.defaultDisplay
        val result = IntArray(2)
        result[0] = display.width
        result[1] = display.height
        return result
    }
}
