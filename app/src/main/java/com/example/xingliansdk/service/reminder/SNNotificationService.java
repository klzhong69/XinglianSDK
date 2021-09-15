package com.example.xingliansdk.service.reminder;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;

import com.example.xingliansdk.bean.MessageBean;
import com.example.xingliansdk.utils.PermissionUtils;
import com.example.xingliansdk.utils.SystemUtil;
import com.example.xingliansdk.view.IF;
import com.google.gson.Gson;
import com.orhanobut.hawk.Hawk;
import com.shon.connector.BleWrite;
import com.shon.connector.utils.TLog;

import java.util.ArrayList;

import static com.example.xingliansdk.Config.database.MESSAGE_CALL;
import static com.example.xingliansdk.Config.database.OTHER;

/**
 * 功能：通知栏信息监听服务
 * 监听各个App在通知栏推送的信息
 * 1.别重写super,  否则NotificationListenerService不兼容4.3,报抽象错误, 具体原因看源码
 * 2.别重写onBind(), 否则服务不会创建 除非你super.onBind()
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class SNNotificationService extends NotificationListenerService implements BleWrite.MessageInterface {
    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        TLog.Companion.error("通知监听:NotificationService#onCreate");
    }


    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null) return;
        int other = Hawk.get(OTHER, 2);
        if (other == 1)
            return;
        Notification notification = sbn.getNotification();
        if (notification == null) return;
        try {
            String packName = sbn.getPackageName();
            String title = null;
            String content = null;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && !packName.equals("com.example.xingliansdk")) {
                Bundle extras = notification.extras;
                if (extras != null) {
                    try {
                        title = extras.getString(Notification.EXTRA_TITLE, null);
                        content = extras.getString(Notification.EXTRA_TEXT, null);
//                        TLog.Companion.error("title+="+title);
//                        TLog.Companion.error("content+="+content);
                        if (IF.isEmpty(content)) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                content = extras.getString(Notification.EXTRA_BIG_TEXT, null);
                            }
                        }
                    } catch (Exception ignored) {
                    }
                    //小米推送
                    if (PermissionUtils.isPhone(PermissionUtils.MANUFACTURER_XIAOMI)) {
                        if (SystemUtil.isMIUI12()) {
                            try {
                                ApplicationInfo info = (ApplicationInfo) extras.get("android.appInfo");
                                packName = info.packageName;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (extras.containsKey("target_package") && !TextUtils.isEmpty(extras.getCharSequence("target_package", null))) {
                            packName = extras.getCharSequence("target_package", null).toString();
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                            String group = notification.getGroup();
                            if (!TextUtils.isEmpty(group)) {
                                packName = group;
                            }
                        }
                    }
                }
                if (IF.isEmpty(content) && !IF.isEmpty(notification.tickerText)) {
                    content = notification.tickerText.toString();
                }
//                TLog.Companion.error("SNNotificationService+=" + content);
                //   BleWrite.writeMessageCall(1, "测试", "测试内容",this);
                SNNotificationPushHelper.getInstance().handleMessage(SNNotificationPushHelper.TYPE_NOTIFICATION_LISTENER_SERVICE, packName, title, content, this);

            }


        } catch (
                Exception ignored) {
            //try住防止解析闪退 导致服务挂掉
        }

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

    }


    @Override
    public void onDestroy() {
//        TLog.Companion.error("通知监听:NotificationService#onDestroy");
        PermissionUtils.requestRebindNotificationListenerService(this);
        SNNotificationPushHelper.getInstance().recycle();
        super.onDestroy();
    }


    @Override
    public void onResult() {
        TLog.Companion.error("SNNotificationService 返回了++List");
        ArrayList<MessageBean> messageList = Hawk.get(MESSAGE_CALL, new ArrayList<>());
        TLog.Companion.error("List=="+ new Gson().toJson(messageList));
        messageList.remove(0);
        Hawk.put(MESSAGE_CALL,messageList);
        TLog.Companion.error("List=="+ new Gson().toJson(messageList));
        if (messageList.size() > 0) {
            String mContent = messageList.get(0).getContent();
            String mTitle = messageList.get(0).getTitle();
            int mType = messageList.get(0).getType();
            if ((mContent.length() + mTitle.length()) >= 100) {
                if (mTitle.length() >= 100) {
                    BleWrite.writeMessageCall(mType, mTitle, "", this);
                } else
                    BleWrite.writeMessageCall(mType, mTitle, mContent.substring(0, 100 - mTitle.length()), this);
            } else
                BleWrite.writeMessageCall(mType, mTitle, mContent, this);
        }
}
}
