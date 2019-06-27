package cn.yml.note.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.RecyclerView;

/** * 支持监听空白区域点击事件的RecycleView
 * 解决卧谈会点击图片右侧空白区域无响应问题
 * created by WRX in 180704 */

public class BlankRecyclerView extends RecyclerView {

    private GestureDetectorCompat gestureDetector;
    private BlankListener listener;

    public BlankRecyclerView(Context context) {
        super(context);
    }

    public BlankRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BlankRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setBlankListener(BlankListener listener) {
        this.listener = listener;
        this.gestureDetector = new GestureDetectorCompat(getContext(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        return true;
                    }
                });
    }

    public interface BlankListener {
        void onBlankClick();
    }

    /**
     * 加了一些null测试以防报空指针错误
     * by WRX in 2018.07.16*/
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (gestureDetector == null) {
            Log.d("TEST", "onTouchEvent: gestureDetector == null");
            this.gestureDetector = new GestureDetectorCompat(getContext(),
                    new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public boolean onSingleTapUp(MotionEvent e) {
                            return true;
                        }
                    });
        }
        if (gestureDetector.onTouchEvent(e)) {
            View childView = findChildViewUnder(e.getX(), e.getY());
            if (childView == null) {
                if (listener != null)
                    listener.onBlankClick();
                else
                    Log.d("TEST", "onTouchEvent: listener == null");
                return true;
            }
        }
        return super.onTouchEvent(e);
    }
}
