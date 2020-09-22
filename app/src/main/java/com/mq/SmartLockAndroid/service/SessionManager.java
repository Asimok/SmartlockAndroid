package com.mq.SmartLockAndroid.service;

import android.util.Log;

import org.apache.mina.core.session.IoSession;

/**
 * Created by Layne_Yao on 2017/10/8.
 * CSDN:http://blog.csdn.net/Jsagacity
 */
public class SessionManager {

    private static SessionManager mInstance = null;

    private IoSession mSession;

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        if (mInstance == null) {
            synchronized (SessionManager.class) {
                if (mInstance == null) {
                    mInstance = new SessionManager();
                }
            }
        }
        return mInstance;
    }

    public void setSession(IoSession session) {
        this.mSession = session;
    }

    public void writeToServer(Object msg) {
        if (mSession != null) {
            Log.d("SendAsyncTask", "客户端准备发送消息");
            mSession.write(msg);
        }
    }

    public void closeSession() {
        if (mSession != null) {
            mSession.getCloseFuture().setClosed();
        }
    }

    public void removeSession() {
        this.mSession = null;
    }
}
