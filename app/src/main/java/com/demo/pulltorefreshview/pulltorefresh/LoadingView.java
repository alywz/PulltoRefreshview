package com.demo.pulltorefreshview.pulltorefresh;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

/**
 * Created by WZ on 2019/12/23.
 * description:自定义loading
 */
public class LoadingView extends View {
    private final DashPathEffect dashPathEffect;
    private String TAG = getClass().getSimpleName();
    private Paint mPaint;
    private Path mPath;
    private Path circlePath;
    private Path dst;
    private PathMeasure pathMeasure;
    private float radius = 40;
    private float strokewith = 8;
    private float progress;//画圆的进度
    private int ciclerotation;//旋转动画的进度
    private ObjectAnimator objectAnimator;
    private boolean drawyes = false;//是否是画对号


    public LoadingView(Context context) {
        this(context, null);
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPath = new Path();
        circlePath = new Path();
        dst = new Path();
        pathMeasure = new PathMeasure();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(0xFF525C66);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(5);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        dashPathEffect = new DashPathEffect(new float[]{15, 16}, 0);
        circlePath.addCircle(0, 0, radius, Path.Direction.CCW);

        objectAnimator = ObjectAnimator.ofInt(this, "ciclerotation", 0, 359);
        objectAnimator.setDuration(2000);
        objectAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        objectAnimator.setInterpolator(new LinearInterpolator());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthmode = MeasureSpec.getMode(widthMeasureSpec);
        int widthsize = MeasureSpec.getSize(widthMeasureSpec);
        int heightmode = MeasureSpec.getMode(heightMeasureSpec);
        int heightsize = MeasureSpec.getSize(heightMeasureSpec);
        int with;
        int height;
        switch (widthmode) {
            case MeasureSpec.AT_MOST:
                with = (int) ((radius * 2)+strokewith);
                break;
            default:
                with = widthsize;
                break;
        }
        switch (heightmode) {
            case MeasureSpec.AT_MOST:
                height = (int) ((radius * 2)+strokewith);
                break;
            default:
                height = heightsize;
                break;
        }
        setMeasuredDimension(with, height);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        dst.reset();
        canvas.save();
        canvas.translate(getWidth() / 2, getHeight() / 2);
        pathMeasure.setPath(circlePath, false);
///////////////绘制进度圈/////////////////////////////////////
        float length = pathMeasure.getLength();
        float stop = progress * length;
//        float start = (float) (stop - (0.5 - Math.abs(animateValue - 0.5)) * length);
        float start = 0;
        pathMeasure.getSegment(start, stop, dst, true);

        if (drawyes) {
            //画对号
            mPath.moveTo(-radius, 0);
            mPath.lineTo(-radius * 2 / 3, radius * 2 / 5);
            mPath.lineTo(0, -radius / 3);
            mPaint.setPathEffect(null);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setColor(0xff879099);
            canvas.drawPath(mPath, mPaint);
        } else {
            mPaint.setPathEffect(dashPathEffect);
            if (progress < 1) {
                canvas.rotate(-90);
                canvas.drawPath(dst, mPaint);
            } else {
                canvas.rotate(ciclerotation);
                canvas.drawPath(circlePath, mPaint);
                Log.d("=============", "旋转" + ciclerotation);
            }
        }
        canvas.restore();

    }


    /**
     * 设置画圆的进度
     *
     * @param progress
     */
    public void setProgress(float progress) {
        this.progress = progress;
        drawyes = false;
        invalidate();
    }

    /**
     * 设置画圆的进度
     *
     * @param drawyes
     */
    public void setDrawyes(boolean drawyes) {
        this.drawyes = drawyes;
        invalidate();
    }

    /**
     * 开始旋转动画
     */
    public void startRotation() {
        if (objectAnimator != null) {
            objectAnimator.start();
        }
    }

    /**
     * 结束旋转动画
     */
    public void stopRotation() {
        if (objectAnimator != null) {
            objectAnimator.cancel();
        }
    }

    public int getCiclerotation() {
        return ciclerotation;
    }

    public void setCiclerotation(int ciclerotation) {
        this.ciclerotation = ciclerotation;
        invalidate();
        Log.d(TAG, "ciclerotation=" + ciclerotation);
    }
}
