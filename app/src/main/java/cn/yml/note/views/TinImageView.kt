package cn.yml.note.views

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.ColorRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat

class TinImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
                                             defStyleAttr: Int = 0)
    : AppCompatImageView(context, attrs, defStyleAttr){

    private var mDrawable = drawable

    fun updateDrawableTinColor(@ColorRes color: Int){
        mDrawable = DrawableCompat.wrap(mDrawable.mutate())
        DrawableCompat.setTintList(mDrawable, ContextCompat.getColorStateList(context, color))
        setImageDrawable(mDrawable)
    }
}