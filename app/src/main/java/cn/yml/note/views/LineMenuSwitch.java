package cn.yml.note.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import cn.yml.note.R;
import com.android6.doyin.utils.DensityUtil;


/**
 * Created by Sunny on 2017/8/16 0016.
 */

public class LineMenuSwitch extends RelativeLayout{
    private Context context;
    private String menuTitle;
    @ColorInt
    private int menuTitleColor;
    private float menuTitleSize;

    @DrawableRes
    private int switchThumb;
    @DrawableRes
    private int switchTrack;

    private int leftMargin;
    private int rightMargin;

    private TextView tv_title;
    private Switch aSwitch;

    private static final int DEFAULT_TEXT_SIZE = 14;
    private static final float DEFAULT_MARGIN = 13;

    private static final int DEFAULT_TITLE_COLOR = R.color.text_color;
    private boolean checked = false;
    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener;

    public LineMenuSwitch(Context context) {
        this(context, null);
    }

    public LineMenuSwitch(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LineMenuSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        getAttr(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        tv_title = new TextView(context);
        tv_title.setText(menuTitle);
        tv_title.setTextColor(menuTitleColor);
        tv_title.setTextSize(menuTitleSize);
        tv_title.setBackgroundResource(R.color.transparent);
        LayoutParams title_param = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        title_param.addRule(CENTER_VERTICAL);
        title_param.leftMargin = leftMargin;
        tv_title.setLayoutParams(title_param);
        addView(tv_title, title_param);

        aSwitch = new Switch(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            aSwitch.setThumbResource(switchThumb);
            aSwitch.setTrackResource(switchTrack);
        }
        LayoutParams switch_param = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            switch_param.addRule(RelativeLayout.ALIGN_PARENT_END);
        }
        switch_param.addRule(ALIGN_PARENT_RIGHT);
        switch_param.addRule(CENTER_VERTICAL);
        switch_param.rightMargin = rightMargin;
        addView(aSwitch, switch_param);
        aSwitch.setChecked(checked);
        aSwitch.setOnCheckedChangeListener(onCheckedChangeListener);
    }

    private void getAttr(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.LineMenuSwitch);
        menuTitle = ta.getString(R.styleable.LineMenuSwitch_menu_title);
        menuTitleSize = ta.getFloat(R.styleable.LineMenuSwitch_menu_title_size, DEFAULT_TEXT_SIZE);
        menuTitleColor = ta.getColor(R.styleable.LineMenuSwitch_menu_title_color,
                getResources().getColor(DEFAULT_TITLE_COLOR));
        leftMargin = ta.getDimensionPixelSize(R.styleable.LineMenuSwitch_left_margin,
                DensityUtil.INSTANCE.dip2px(context, DEFAULT_MARGIN));
        rightMargin = ta.getDimensionPixelOffset(R.styleable.LineMenuSwitch_right_margin,
                DensityUtil.INSTANCE.dip2px(context, DEFAULT_MARGIN));
        switchThumb = ta.getResourceId(R.styleable.LineMenuSwitch_switch_thumb, R.drawable.switch_thumb);
        switchTrack = ta.getResourceId(R.styleable.LineMenuSwitch_switch_track, R.drawable.switch_track);
        ta.recycle();
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
        if(aSwitch != null)
            aSwitch.setChecked(checked);
    }

    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener onCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener;
        if(aSwitch != null)
            aSwitch.setOnCheckedChangeListener(onCheckedChangeListener);
    }
}
