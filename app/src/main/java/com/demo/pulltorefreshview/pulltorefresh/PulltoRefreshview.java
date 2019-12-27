package com.demo.pulltorefreshview.pulltorefresh;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;

import com.demo.pulltorefreshview.R;

/**
 * Created by WZ on 2019/12/25.
 * description:自定义刷新布局继承自ViewGroup,自己练手写着玩,有坑慢慢踩
 */
public class PulltoRefreshview extends ViewGroup {
    private String TAG = this.getClass().getSimpleName();
    private LoadingView lodaingview;
    private View headerView;
    private TextView tvRefresh;
    private Scroller scroller;
    private int mylastmoveY = 0;
    private int myDownY = 0;
    private int myDownX = 0;
    private int mymoveY = 0;
    private int mymoveX = 0;
    private RefreshListener listener;//刷新监听器
    private int scrollY = 0;  //记录下拉滑动的距离，只有headerview完全展示时才开始刷新
    private RefreshStatus currentstate = RefreshStatus.NONE;//记录当前刷新的状态
    private int herderviewHeight;//刷新布局的高度
    private float progress;//画圆的进度
    private View mChildView;
    private boolean enable = true;

    public void setRefreshListener(RefreshListener listener) {
        this.listener = listener;
    }

    public PulltoRefreshview(Context context) {
        this(context, null);
    }

    public PulltoRefreshview(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PulltoRefreshview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (isInEditMode()) {
            return;
        }
        scroller = new Scroller(context);
    }

    //onMeasure函数，若我们的控件都是固定值或match_parent时，系统提供了默认的实现方式，如果有wrap_content的情况，我们必须要重写该函数
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
//        //开始布局，headerview不显示，下拉时才显示
        headerView = getChildAt(0);
        if (headerView != null) {
            herderviewHeight = headerView.getMeasuredHeight();
            headerView.layout(0, -herderviewHeight, headerView.getMeasuredWidth(), 0);
        }
        View view1 = getChildAt(1);
        if (view1 != null) {
            view1.layout(getPaddingLeft(), getPaddingTop(), view1.getMeasuredWidth() - getPaddingRight(), view1.getMeasuredHeight() - getPaddingBottom());
        }

    }

    //重点，onInterceptTouchEvent负责拦截触摸事件
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (currentstate == RefreshStatus.REFRESHING) return true;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                myDownY = (int) ev.getY();
                myDownX = (int) ev.getX();
                Log.d("tag", "按下");
                break;
            case MotionEvent.ACTION_MOVE:
                mymoveY = (int) ev.getY();
                mymoveX = (int) ev.getX();
                //判断当前内容View是否还能继续向上滑动，利用ViewCompat的canScrollVertically判断view是否还能在竖直方向滑动，
                //如过是下拉，并且内容View不能继续向上滑动，拦截Move事件
                Log.d("tag", canChildScrollUp() + "==" + (myDownY - mymoveY));
                int scrollx = Math.abs(mymoveX - myDownX);
                if (myDownY - mymoveY < -5 && !canChildScrollUp() && enable && scrollx < 50) {
                    Log.d("tag", "move下拉");
                    //Log.d("tag",getChildAt(1).getTop()+"");
                    mylastmoveY = mymoveY;
                    return true;
                } else {
                    Log.d("tag", "move上拉");
                }
                break;
            case MotionEvent.ACTION_UP:
                Log.d("tag", "释放");
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                mymoveY = (int) event.getY();
                int scrolly = mylastmoveY - mymoveY;
                if (scrolly < 0) {
                    scrollY += Math.abs(scrolly / 2);
                } else {
                    scrollY -= Math.abs(scrolly / 2);
                }
                if (scrollY >= herderviewHeight) {
                    //松开可以刷新
                    currentstate = RefreshStatus.PULLMORE;
                    progress = 1;
                } else {
                    //下拉加载更多
                    currentstate = RefreshStatus.PULL;
                    progress = scrollY * 1.0f / herderviewHeight;
                }
                checkRefreshViewState(currentstate);
                Log.d("TAG==", scrolly + "=滑动距离scrolly");
                Log.d("TAG==", scrollY + "=滑动距离scrollY");
                Log.d("tag", herderviewHeight + "第一个view高度");
                scrollBy(0, scrolly / 2);
                mylastmoveY = mymoveY;
                break;
            case MotionEvent.ACTION_UP:
                //判断下拉距离是否大于headerview的高度，是的话开始刷新动画
                if (scrollY > herderviewHeight) {
                    //用户手指抬起时，需要弹回一段距离，回到足够显示headrview的高度即可
                    scroller.startScroll(0, getScrollY(), 0, -getScrollY() - herderviewHeight);
                    //正在刷新状态
                    currentstate = RefreshStatus.REFRESHING;
                } else {
                    //释放回弹状态
                    currentstate = RefreshStatus.RELEASE;
                    scroller.startScroll(0, getScrollY(), 0, -getScrollY());
                }
                checkRefreshViewState(currentstate);
                Log.d("tag", "up" + getScrollY());
                scrollY = 0;
                invalidate();
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * @return Whether it is possible for the child view of this layout to
     * scroll up. Override this if the child view is a custom view.
     */
    public boolean canChildScrollUp() {
        if (mChildView == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT < 14) {
            if (mChildView instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mChildView;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return mChildView.canScrollVertically(-1) || mChildView.getScrollY() > 0;
            }
        } else {
            return mChildView.canScrollVertically(-1);
        }
    }


    /**
     * 改变刷新的状态
     *
     * @param state
     */
    private void checkRefreshViewState(RefreshStatus state) {
        if (lodaingview == null) {
            return;
        }
        switch (state) {
            case NONE:
                //初始化状态
                //设置画圆的进度
                lodaingview.setProgress(0);
                lodaingview.stopRotation();
                lodaingview.setVisibility(VISIBLE);
                tvRefresh.setText("下拉刷新");
                tvRefresh.setVisibility(GONE);
                break;
            case PULL:
                //下拉状态
                //设置画圆的进度
                lodaingview.setProgress(progress);
                Log.d("=========", progress + "");
                tvRefresh.setText("下拉刷新");
                tvRefresh.setVisibility(GONE);
                break;
            case PULLMORE:
            case RELEASE:
                //释放状态
                //松开可以刷新
                tvRefresh.setText("松开刷新");
                tvRefresh.setVisibility(GONE);
                break;
            case REFRESHING:
                //刷新状态
                lodaingview.setProgress(1);
                lodaingview.startRotation();
                tvRefresh.setText("正在刷新");
                listener.onRefresh();
                tvRefresh.setVisibility(GONE);
                break;
            case COMPLETE:
                //刷新完成
                lodaingview.setDrawyes(true);
                lodaingview.stopRotation();
                tvRefresh.setText("刷新完成");
                tvRefresh.setVisibility(VISIBLE);
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        /**
                         * 要执行的操作
                         */
                        scroller.startScroll(0, getScrollY(), 0, -getScrollY());
                        postInvalidate();
                    }
                }, 500);

                break;
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.d(TAG, getChildCount() + "子View的个数");
        mChildView = getChildAt(0);
        if (mChildView == null) {
            return;
        }
        if (getChildCount() > 1) {
            throw new RuntimeException("can only have one child widget");
        }
        addHeader();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        lodaingview = null;
    }

    /**
     * 添加头部
     */
    private void addHeader() {
        headerView = LayoutInflater.from(getContext()).inflate(R.layout.refresh_view, null);
        LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        ((LinearLayout.LayoutParams) params).gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
        headerView.setLayoutParams(params);
        lodaingview = headerView.findViewById(R.id.view_lodaing);
        tvRefresh = headerView.findViewById(R.id.tv_refresh);
        addView(headerView, 0);
        //刷新
        if (currentstate == RefreshStatus.REFRESHING && headerView != null) {
            scroller.startScroll(0, 0, 0, 100);
            checkRefreshViewState(currentstate);
        }
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            invalidate();
        }
    }

    //必须重写该方法。其实就是不做处理即可，因为在recyclerview中，如果recyclerview监听到自己在滑动时，会调用该父View的该方法来禁用父view拦截事件的功能，这样我们就无法监听了，因此在这里我们不做处理，写为空。
    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }


    //更新成功时调用的方法。
    public void complete() {
        currentstate = RefreshStatus.COMPLETE;
        checkRefreshViewState(currentstate);
    }


    /**
     * 更新成功时调用的方法。
     *
     * @param enable
     */
    public void setCanRefresh(boolean enable) {
        this.enable = enable;
    }

    /**
     * 手动打开刷新的方法
     *
     * @param refreshing
     */
    public void setRefreshing(boolean refreshing) {
        if (!refreshing) {
            currentstate = RefreshStatus.COMPLETE;
            checkRefreshViewState(currentstate);
        } else if (currentstate != RefreshStatus.REFRESHING && refreshing) {
            currentstate = RefreshStatus.REFRESHING;
            if (herderviewHeight != 0) {
                scroller.startScroll(0, 0, 0, -herderviewHeight);
                checkRefreshViewState(currentstate);
            }
        }
    }


}
