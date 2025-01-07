package org.ast.findmaimaidx.been;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CompassView extends View {
    private Paint paint;
    private float direction = 0.0f;

    public CompassView(Context context) {
        super(context);
        init();
    }

    public CompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CompassView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(10);
        paint.setStyle(Paint.Style.STROKE);
    }

    public void setDirection(float direction) {
        this.direction = direction;
        invalidate(); // 重新绘制
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int center = Math.min(width, height) / 2;

        // 绘制指南针中心
        canvas.drawCircle(width / 2, height / 2, center, paint);

        // 绘制指针
        float angle = (direction + 180) * (float) Math.PI / 180;
        float x = (float) (width / 2 + center * 0.8 * Math.cos(angle));
        float y = (float) (height / 2 + center * 0.8 * Math.sin(angle));

        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(5);
        canvas.drawLine(width / 2, height / 2, x, y, paint);
    }
}
