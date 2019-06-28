package cn.yml.note.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import cn.yml.note.R

class TinTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
                                             defStyleAttr: Int = 0)
    : DrawableTextView(context, attrs, defStyleAttr){

    companion object {
        const val LEFT = 0x0001
        const val TOP = 0x0002
        const val RIGHT = 0x0004
        const val BOTTOM = 0x0008
        const val ALL = 0x000f
    }

    var tints: Int = LEFT
    protected lateinit var mDrawables: Array<Drawable?>

    init {
        if (!isInEditMode) {
            mDrawables = compoundDrawables
                loadFromAttribute(context, attrs, defStyleAttr)
        }
    }

    private fun loadFromAttribute(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        attrs?.let {
            val ta = context.obtainStyledAttributes(it, R.styleable.TinTextView)
            tints = ta.getInt(R.styleable.TinEditText_tints, ALL)
            ta.recycle()
        }
    }

    fun updateDrawablesTinColor(@ColorRes colorPrimary: Int) {
        //设置icon
        if(mDrawables[0] != null && (tints and LEFT) == LEFT){
            mDrawables[0] = DrawableCompat.wrap(mDrawables[0]!!.mutate())
            DrawableCompat.setTintList(mDrawables[0]!!, ContextCompat.getColorStateList(context, colorPrimary))
        }

        if(mDrawables[1] != null && (tints and TOP) == TOP){
            mDrawables[1] = DrawableCompat.wrap(mDrawables[1]!!.mutate())
            DrawableCompat.setTintList(mDrawables[1]!!, ContextCompat.getColorStateList(context, colorPrimary))
        }

        if(mDrawables[2] != null && (tints and RIGHT) == RIGHT){
            mDrawables[2] = DrawableCompat.wrap(mDrawables[2]!!.mutate())
            DrawableCompat.setTintList(mDrawables[2]!!, ContextCompat.getColorStateList(context, colorPrimary))
        }

        if(mDrawables[3] != null && (tints and BOTTOM) == BOTTOM){
            mDrawables[3] = DrawableCompat.wrap(mDrawables[3]!!.mutate())
            DrawableCompat.setTintList(mDrawables[3]!!, ContextCompat.getColorStateList(context, colorPrimary))
        }
        setCompoundDrawables(mDrawables[0], mDrawables[1], mDrawables[2], mDrawables[3])

    }
}