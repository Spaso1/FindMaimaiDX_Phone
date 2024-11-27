package org.ast.findmaimaidx.utill;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.FrameLayout;

public class ZoomableViewGroup extends FrameLayout {
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    private Matrix matrix = new Matrix();
    private float scaleFactor = 1.0f;
    public static float lastX, lastY;
    private static final long REFRESH_RATE = 11; // 11ms 对应大约 90fps
    private Handler mHandler;
    private Runnable mRefreshRunnable;

    public ZoomableViewGroup(Context context) {
        super(context);
        init(context);
    }

    public ZoomableViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ZoomableViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
        gestureDetector = new GestureDetector(context, new GestureListener());
        mHandler = new Handler(Looper.getMainLooper());
        mRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                // 在这里执行刷新操作
                invalidate(); // 重新绘制视图
                mHandler.postDelayed(this, REFRESH_RATE);
            }
        };
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f)); // 限制缩放范围

            matrix.reset();
            matrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
            invalidate(); // 重新绘制

            return true;
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            float dx = e2.getX() - lastX;
            float dy = e2.getY() - lastY;

            matrix.postTranslate(dx, dy);
            invalidate(); // 重新绘制

            lastX = e2.getX();
            lastY = e2.getY();

            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            lastX = e.getX();
            lastY = e.getY();
            return true;
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.save();
        canvas.concat(matrix);
        super.dispatchDraw(canvas);
        canvas.restore();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startRefreshing();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopRefreshing();
    }

    private void startRefreshing() {
        mHandler.post(mRefreshRunnable);
    }

    private void stopRefreshing() {
        mHandler.removeCallbacks(mRefreshRunnable);
    }
}
