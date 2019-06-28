package cn.yml.note.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import cn.yml.note.R

open class DrawableEditView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
                                                 defStyleAttr: Int = android.R.attr.editTextStyle)
    : AppCompatEditText(context, attrs, defStyleAttr) {

    companion object {
        const val DEFAULT_SIZE = 20
    }

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.DrawableEditView)

        val width = ta.getDimensionPixelOffset(R.styleable.DrawableEditView_drawable_width, DEFAULT_SIZE)
        val height = ta.getDimensionPixelOffset(R.styleable.DrawableEditView_drawable_height, DEFAULT_SIZE)

        val sizeWrap = SizeWrap()

        val letfD = ta.getDrawable(R.styleable.DrawableEditView_left_drawable)
                ?.apply {
                    val lwidth = ta.getDimensionPixelOffset(R.styleable.DrawableEditView_left_drawable_width, DEFAULT_SIZE)
                    val lheight = ta.getDimensionPixelOffset(R.styleable.DrawableEditView_left_drawable_height, DEFAULT_SIZE)
                    sizeWrap.checkSize(width, height, lwidth, lheight)
                    setBounds(0, 0, sizeWrap.width, sizeWrap.height)
                }
        val rightD = ta.getDrawable(R.styleable.DrawableEditView_right_drawable)
                ?.apply {
                    val lwidth = ta.getDimensionPixelOffset(R.styleable.DrawableEditView_right_drawable_width, DEFAULT_SIZE)
                    val lheight = ta.getDimensionPixelOffset(R.styleable.DrawableEditView_right_drawable_height, DEFAULT_SIZE)
                    sizeWrap.checkSize(width, height, lwidth, lheight)
                    setBounds(0, 0, sizeWrap.width, sizeWrap.height)
                }
        val topD = ta.getDrawable(R.styleable.DrawableEditView_top_drawable)
                ?.apply {
                    val lwidth = ta.getDimensionPixelOffset(R.styleable.DrawableEditView_top_drawable_width, DEFAULT_SIZE)
                    val lheight = ta.getDimensionPixelOffset(R.styleable.DrawableEditView_top_drawable_height, DEFAULT_SIZE)
                    sizeWrap.checkSize(width, height, lwidth, lheight)
                    setBounds(0, 0, sizeWrap.width, sizeWrap.height)
                }
        val bottomD = ta.getDrawable(R.styleable.DrawableEditView_bottom_drawable)
                ?.apply {
                    val lwidth = ta.getDimensionPixelOffset(R.styleable.DrawableEditView_bottom_drawable_width, DEFAULT_SIZE)
                    val lheight = ta.getDimensionPixelOffset(R.styleable.DrawableEditView_bottom_drawable_height, DEFAULT_SIZE)
                    sizeWrap.checkSize(width, height, lwidth, lheight)
                    setBounds(0, 0, sizeWrap.width, sizeWrap.height)
                }

        this.setCompoundDrawables(letfD, topD, rightD, bottomD)

        ta.recycle()
    }


    class SizeWrap(var width: Int = -1, var height: Int = -1) {
        fun checkSize(globalWidth: Int, globalHeight: Int, localWidth: Int, localHeight: Int) {
            width = DEFAULT_SIZE
            height = DEFAULT_SIZE

            //对某一个具体方向图标大小的设置会覆盖对全局图标大小的设置
            if (localHeight > 0 && localWidth > 0) {
                width = localWidth
                height = localHeight
            }

            if (localHeight == DEFAULT_SIZE && localWidth == DEFAULT_SIZE
                    && globalHeight > 0 && globalWidth > 0) {
                width = globalWidth
                height = globalHeight
            }
        }
    }
}