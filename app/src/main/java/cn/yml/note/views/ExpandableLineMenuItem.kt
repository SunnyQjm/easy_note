package cn.yml.note.views


import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import cn.yml.note.R

/**
 * Created by Sunny on 2017/10/5 0005.
 */

class ExpandableLineMenuItem @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr), LineMenu {

    private var lineMenuItem: LineMenuItem? = null
    private var openedAble = true
    private var open = false
    private var menuHeight: Int = 0
    @DrawableRes
    private var menuBG = R.drawable.ripple_bg_white
    private var menuWidth = LinearLayout.LayoutParams.MATCH_PARENT

    companion object {
        private const val DEFAULT_MENU_HEIGHT = 50f
    }
    init {
        init(context, attrs, defStyleAttr)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {

        val ta = context.obtainStyledAttributes(attrs, R.styleable.ExpandableLineMenuItem)
        open = ta.getBoolean(R.styleable.ExpandableLineMenuItem_open, false)
        openedAble = ta.getBoolean(R.styleable.ExpandableLineMenuItem_openedAble, true)
        menuHeight = ta.getDimensionPixelOffset(R.styleable.ExpandableLineMenuItem_menu_height,
                dip2px(context, DEFAULT_MENU_HEIGHT))
        menuWidth = ta.getDimensionPixelOffset(R.styleable.ExpandableLineMenuItem_menu_width,
                LinearLayout.LayoutParams.MATCH_PARENT)
        menuBG = ta.getResourceId(R.styleable.ExpandableLineMenuItem_menu_bg, menuBG)
        ta.recycle()

        lineMenuItem = LineMenuItem(context, attrs, defStyleAttr)
        lineMenuItem!!.setBackgroundResource(menuBG)
        lineMenuItem!!.setOnClickListener { v ->
            if (open) {       //关闭操作
                close()
            } else {       //打开操作
                open()
            }
        }


    }

    /**
     * 菜单栏展开
     */
    fun open() {
        if (!openedAble)
            return
        open = true
        ObjectAnimator.ofFloat(lineMenuItem!!.right_icon,
                "rotation", 0f, 90f)
                .setDuration(300)
                .start()
        //除了菜单本身，全部隐藏
        for (i in 0 until childCount - 1) {
            getChildAt(i).visibility = View.VISIBLE
        }
    }

    /**
     * 菜单栏关闭
     */
    fun close() {
        if (!openedAble)
            return
        open = false
        ObjectAnimator.ofFloat(lineMenuItem!!.right_icon,
                "rotation", 90f, 0f)
                .setDuration(300)
                .start()
        for (i in 0 until childCount - 1) {
            getChildAt(i).visibility = View.GONE
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        println("onMeasure")
        val childCount = childCount
        for (i in 0 until childCount) {
            val childView = getChildAt(i)
            //为每一个子控件计算大小
            measureChild(childView, widthMeasureSpec, heightMeasureSpec)
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        val layoutParams = LinearLayout.LayoutParams(menuWidth, menuHeight)
        addView(lineMenuItem, layoutParams)
        if (open) {
            open()
        } else {
            close()
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        //先部署菜单布局
        lineMenuItem!!.layout(0, 0, width, lineMenuItem!!.measuredHeight)
        var totalHeight = lineMenuItem!!.measuredHeight

        //部署包含的View
        for (i in 0 until childCount - 1) {
            val childView = getChildAt(i)
            val left = if (childView.measuredWidth != width)
                width - childView.measuredWidth
            else
                0
            childView.layout(left, totalHeight, left + childView.measuredWidth,
                    totalHeight + childView.measuredHeight)
            totalHeight += childView.measuredHeight
        }
    }


    override fun getLeft_icon(): ImageView? {
        return lineMenuItem!!.left_icon
    }

    override fun getRight_icon(): ImageView? {
        return lineMenuItem!!.right_icon
    }

    override fun getTv_title(): TextView? {
        return lineMenuItem!!.tv_title
    }

    override fun getTv_value(): TextView? {
        return lineMenuItem!!.tv_value
    }

    override fun setLeftIcon(res: Int) {
        lineMenuItem!!.setLeftIcon(res)
    }

    override fun setRightIcon(res: Int) {
        lineMenuItem!!.setRightIcon(res)
    }

    override fun setRightIconVisible(visible: Int) {
        lineMenuItem!!.setRightIconVisible(visible)
    }

    override fun setLeftIconVisible(visible: Int) {
        lineMenuItem!!.setLeftIconVisible(visible)
    }

    override fun setTitleIconVisible(visible: Int) {
        lineMenuItem!!.setTitleIconVisible(visible)
    }

    override fun setValueIconVisible(visible: Int) {
        lineMenuItem!!.setValueIconVisible(visible)
    }

    override fun setTitle(title: String) {
        lineMenuItem!!.setTitle(title)
    }

    override fun setValue(value: String) {
        lineMenuItem!!.setValue(value)
    }



    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }
}
