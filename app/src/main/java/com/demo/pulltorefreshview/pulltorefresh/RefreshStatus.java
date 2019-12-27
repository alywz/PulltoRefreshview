package com.demo.pulltorefreshview.pulltorefresh;

/**
 * Created by WZ on 2019/12/25.
 * description:刷新的状态定义
 */
public enum RefreshStatus {
    NONE,        //原始状态
    PULL,        //下拉状态
    PULLMORE,    //松开刷新
    REFRESHING, //正在刷新
    RELEASE,    //释放状态
    COMPLETE,    //完成刷新
}
