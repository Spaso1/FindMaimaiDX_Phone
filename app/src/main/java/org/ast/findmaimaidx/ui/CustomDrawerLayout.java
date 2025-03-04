package org.ast.findmaimaidx.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.drawerlayout.widget.DrawerLayout;

public class CustomDrawerLayout extends DrawerLayout {

    private static final float TOUCH_SLOP_SENSITIVITY = 0.2f; // 调整这个值来改变灵敏度

    public CustomDrawerLayout(Context context) {
        super(context);
    }

    public CustomDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomDrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return super.onTouchEvent(ev);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    public void openDrawer(int gravity) {
        super.openDrawer(gravity);
    }

    @Override
    public void closeDrawer(int gravity) {
        super.closeDrawer(gravity);
    }

    @Override
    public boolean isDrawerOpen(int drawerGravity) {
        return super.isDrawerOpen(drawerGravity);
    }

    @Override
    public boolean isDrawerVisible(int drawerGravity) {
        return super.isDrawerVisible(drawerGravity);
    }

    @Override
    public void setDrawerLockMode(int lockMode) {
        super.setDrawerLockMode(lockMode);
    }

    @Override
    public void setDrawerLockMode(int lockMode, int edgeGravity) {
        super.setDrawerLockMode(lockMode, edgeGravity);
    }

    @Override
    public int getDrawerLockMode(int edgeGravity) {
        return super.getDrawerLockMode(edgeGravity);
    }

    @Override
    public void setScrimColor(int color) {
        super.setScrimColor(color);
    }
    @Override
    public void setDrawerElevation(float elevation) {
        super.setDrawerElevation(elevation);
    }

    @Override
    public float getDrawerElevation() {
        return super.getDrawerElevation();
    }

    @Override
    public void setStatusBarBackground(int resId) {
        super.setStatusBarBackground(resId);
    }

    @Override
    public void setStatusBarBackground(Drawable background) {
        super.setStatusBarBackground(background);
    }

    @Override
    public void setDrawerShadow(Drawable shadowDrawable, int edgeGravity) {
        super.setDrawerShadow(shadowDrawable, edgeGravity);
    }

    @Override
    public void setDrawerShadow(int shadowResId, int edgeGravity) {
        super.setDrawerShadow(shadowResId, edgeGravity);
    }

    @Override
    public void setDrawerListener(DrawerListener listener) {
        super.setDrawerListener(listener);
    }

    @Override
    public void addDrawerListener(DrawerListener listener) {
        super.addDrawerListener(listener);
    }

    @Override
    public void removeDrawerListener(DrawerListener listener) {
        super.removeDrawerListener(listener);
    }

    @Override
    public void setDrawerLockMode(int lockMode, View drawerView) {
        super.setDrawerLockMode(lockMode, drawerView);
    }

    @Override
    public int getDrawerLockMode(View drawerView) {
        return super.getDrawerLockMode(drawerView);
    }

    @Override
    public void openDrawer(View drawerView) {
        super.openDrawer(drawerView);
    }

    @Override
    public void closeDrawer(View drawerView) {
        super.closeDrawer(drawerView);
    }

    @Override
    public boolean isDrawerOpen(View drawerView) {
        return super.isDrawerOpen(drawerView);
    }

    @Override
    public boolean isDrawerVisible(View drawerView) {
        return super.isDrawerVisible(drawerView);
    }
}
