package cn.yml.note.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class ClearAbleEditText @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
                                                  defStyleAttr: Int = android.R.attr.editTextStyle)
    : TinEditText(context, attrs, defStyleAttr) {

    private val rightDrawable: Drawable? = mDrawables[2]

    init {
//        rightDrawable?.setBounds(0, 0,
//                (rightDrawable.intrinsicWidth * 0.65).toInt(),
//                (rightDrawable.intrinsicHeight * 0.65).toInt()
//        )

        changeDrawables(right = null)
        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length ?: 0 > 0) {
                    changeDrawables(right = rightDrawable)
                } else {
                    changeDrawables(right = null)
                }
            }

        })
//        setOnFocusChangeListener { _, hasFocus ->
//
//        }

    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        super.onFocusChange(v, hasFocus)
        if (!hasFocus || text?.isEmpty() == true)
            changeDrawables(right = null)
        else
            changeDrawables(right = rightDrawable)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_UP) {
            //getTotalPaddingRight()图标左边缘至控件右边缘的距离
            //getWidth() - getTotalPaddingRight()表示从最左边到图标左边缘的位置
            //getWidth() - getPaddingRight()表示最左边到图标右边缘的位置
            val touchable = event.x > width - totalPaddingRight && event.x < width - paddingRight

            if (touchable) {
                this.setText("")
            }
        }
        return super.onTouchEvent(event)
    }

    private fun changeDrawables(left: Drawable? = mDrawables[0], top: Drawable? = mDrawables[1], right: Drawable? = mDrawables[2],
                                bottom: Drawable? = mDrawables[3]) {
        setCompoundDrawables(left, top, right, bottom)
    }
}

