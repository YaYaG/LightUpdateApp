package com.jackwang.updateapk;

/**
 * 姓名: Jack
 * 时间： 2020-03-28
 * 描述：
 */
public interface UpdateListener {
    void start();
    void progress(int progress);
    void downFinish();
    void downFail(Throwable throwable);
}
