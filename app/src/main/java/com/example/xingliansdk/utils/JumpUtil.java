package com.example.xingliansdk.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.xingliansdk.MainHomeActivity;
import com.example.xingliansdk.TestActivity;
import com.example.xingliansdk.bean.MapMotionBean;
import com.example.xingliansdk.bean.SleepTypeBean;
import com.example.xingliansdk.dfu.DFUActivity;
import com.example.xingliansdk.network.api.dialView.RecommendDialBean;
import com.example.xingliansdk.ui.BleConnectActivity;
import com.example.xingliansdk.ui.bloodOxygen.BloodOxygenActivity;
import com.example.xingliansdk.ui.BloodPressureActivity;
import com.example.xingliansdk.ui.device.BigDataIntervalActivity;
import com.example.xingliansdk.ui.dial.CustomizeDialActivity;
import com.example.xingliansdk.ui.dial.DialDetailsActivity;
import com.example.xingliansdk.ui.dial.DialMarketActivity;
import com.example.xingliansdk.ui.fragment.map.AmapSportRecordActivity;
import com.example.xingliansdk.ui.fragment.map.RunningActivity;
import com.example.xingliansdk.ui.login.ForgetPasswordActivity;
import com.example.xingliansdk.ui.login.GoalActivity;
import com.example.xingliansdk.ui.login.LogOutActivity;
import com.example.xingliansdk.ui.login.LogOutCodeActivity;
import com.example.xingliansdk.ui.login.LoginActivity;
import com.example.xingliansdk.ui.login.PasswordActivity;
import com.example.xingliansdk.ui.login.SureLogOutActivity;
import com.example.xingliansdk.ui.problemsFeedback.ProblemsFeedbackActivity;
import com.example.xingliansdk.ui.setting.DoNotDisturbActivity;
import com.example.xingliansdk.ui.device.ModuleMeasurementListActivity;
import com.example.xingliansdk.ui.device.OtherSettingActivity;
import com.example.xingliansdk.ui.device.ReminderPushListActivity;
import com.example.xingliansdk.ui.deviceSport.DeviceSportChartActivity;
import com.example.xingliansdk.ui.exerciseRecord.ExerciseRecordActivity;
import com.example.xingliansdk.ui.fragment.home.CardEditActivity;
import com.example.xingliansdk.ui.heartrate.HeartRateActivity;
import com.example.xingliansdk.ui.heartrate.RealTimeHeartRateActivity;
import com.example.xingliansdk.ui.pressure.PressureActivity;
import com.example.xingliansdk.ui.setting.AboutActivity;
import com.example.xingliansdk.ui.setting.InfRemindActivity;
import com.example.xingliansdk.ui.setting.MyDeviceActivity;
import com.example.xingliansdk.ui.setting.SettingActivity;
import com.example.xingliansdk.ui.setting.SleepGoalActivity;
import com.example.xingliansdk.ui.setting.SportsGoalActivity;
import com.example.xingliansdk.ui.setting.UnitActivity;
import com.example.xingliansdk.ui.setting.account.AccountActivity;
import com.example.xingliansdk.ui.setting.account.AppealActivity;
import com.example.xingliansdk.ui.setting.account.BindNewPhoneActivity;
import com.example.xingliansdk.ui.setting.account.FindPhoneMainActivity;
import com.example.xingliansdk.ui.setting.account.PasswordCheckActivity;
import com.example.xingliansdk.ui.setting.account.UpPasswordActivity;
import com.example.xingliansdk.ui.setting.alarmClock.AlarmClockActivity;
import com.example.xingliansdk.ui.setting.alarmClock.AlarmClockListActivity;
import com.example.xingliansdk.ui.setting.DeviceInformationActivity;
import com.example.xingliansdk.ui.setting.flash.FlashActivity;
import com.example.xingliansdk.ui.setting.heartRateAlarm.HeartRateAlarmActivity;
import com.example.xingliansdk.ui.setting.schedule.ScheduleActivity;
import com.example.xingliansdk.ui.setting.schedule.ScheduleListActivity;
import com.example.xingliansdk.ui.setting.SettingWeatherActivity;
import com.example.xingliansdk.ui.setting.takeMedicine.TakeMedicineActivity;
import com.example.xingliansdk.ui.setting.takeMedicine.TakeMedicineIndexActivity;
import com.example.xingliansdk.ui.setting.takeMedicine.TakeMedicineRepeatActivity;
import com.example.xingliansdk.ui.sleep.SleepDetailsActivity;
import com.example.xingliansdk.ui.sleep.details.SleepNightActivity;
import com.example.xingliansdk.ui.temp.TempActivity;
import com.example.xingliansdk.ui.web.WebActivity;
import com.example.xingliansdk.ui.weight.WeightActivity;
import com.shon.connector.utils.TLog;

import java.io.Serializable;
import java.util.ArrayList;

import static me.hgj.jetpackmvvm.network.NetworkUtil.url;

public class JumpUtil {

    /**
     * 跳转到  扫描链接ble界面
     */
    public static void startBleConnectActivity(Context context) {
        context.startActivity(new Intent(context, BleConnectActivity.class)
                //.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK)
        );
    }

    /**
     * 跳转到  其他设置界面
     */
    public static void startOtherSettingActivity(Context context) {
        context.startActivity(new Intent(context, OtherSettingActivity.class)
        );
    }

    /**
     * 跳转到血压界面
     *
     * @param context 从哪个页面跳转
     */
//    public static void startMainHomeActivity(Context context, String address) {
//        context.startActivity(new Intent(context, MainHomeActivity.class)
//                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK)
//                .putExtra("address", address));
//    }
    public static void startMainHomeActivity(Context context) {
        context.startActivity(new Intent(context, MainHomeActivity.class)
        );
    }

    /**
     * 跳转到血压界面
     *
     * @param context 从哪个页面跳转
     */
    public static void startBloodPressureActivity(Context context) {
        context.startActivity(new Intent(context, BloodPressureActivity.class));
    }

    /**
     * 睡眠
     *
     * @param context
     */
    public static void startSleepDetailsActivity(Context context/*, ArrayList<SleepBean> mList*/) {
        context.startActivity(new Intent(context, SleepDetailsActivity.class)
                // .putExtra("SleepBean", mList)
        );
    }

    public static void startSleepNightActivity(Context context, SleepTypeBean bean) {
        context.startActivity(new Intent(context, SleepNightActivity.class)
                .putExtra("SleepType", bean)
        );
    }

    /**
     * 心率图表页面
     *
     * @param context
     * @param mList
     */
    public static void startHeartRateActivity(Context context, ArrayList<Integer> mList, int type) {
        context.startActivity(new Intent(context, HeartRateActivity.class)
                .putExtra("HeartRate", mList)
                .putExtra("HistoryType", type)
        );
    }

    /**
     * 血氧图表
     *
     * @param context
     */
    public static void startBloodOxygenActivity(Context context) {
        context.startActivity(new Intent(context, BloodOxygenActivity.class)
        );
    }

    /**
     * 压力
     */
    public static void startPressureActivity(Context context) {
        context.startActivity(new Intent(context, PressureActivity.class));
    }


    /**
     * 查看实时心率
     *
     * @param context
     */
    public static void startRealTimeHeartRateActivity(Context context) {
        context.startActivity(new Intent(context, RealTimeHeartRateActivity.class));
    }

    /**
     * 跳转到OTA升级界面
     *
     * @param context 从哪个页面跳转
     */
    public static void startOTAActivity(Context context, String address, String name, String productNumber, int version
            , Boolean status) {
        context.startActivity(new Intent(context, DFUActivity.class)
                .putExtra("address", address)
                .putExtra("name", name)
                .putExtra("productNumber", productNumber)
                .putExtra("version", version)
                .putExtra("writeOTAUpdate", status)
        );

    }

    /**
     * 跳转到设备设置界面
     *
     * @param context 从哪个页面跳转
     */
    public static void startDeviceInformationActivity(Context context, boolean register) {
        context.startActivity(new Intent(context, DeviceInformationActivity.class)
                        .putExtra("register", register)
//                .putExtra("name", name)
        );
    }

    /**
     * 密码设置
     */
    public static void startPasswordActivity(Context context, String phone, String code, String areaCode, int type) {
        context.startActivity(new Intent(context, PasswordActivity.class)
                .putExtra("phone", phone)
                .putExtra("code", code)
                .putExtra("areaCode", areaCode)
                .putExtra("type", type)
        );
    }

    /**
     * 跳转到设备设置界面
     *
     * @param context 从哪个页面跳转
     */
    public static void startMyDeviceActivity(Context context, int electricity) {
        context.startActivity(new Intent(context, MyDeviceActivity.class)
                .putExtra("electricity", electricity)
        );
    }

    /**
     * 跳转到测量状态模块
     *
     * @param context
     */
    public static void startModuleMeasurementListActivity(Context context) {
        context.startActivity(new Intent(context, ModuleMeasurementListActivity.class)
        );
    }


    /**
     * 跳转到消息提醒列表模块
     *
     * @param context
     */
    public static void startReminderPushListActivity(Context context) {
        context.startActivity(new Intent(context, ReminderPushListActivity.class)
        );
    }

    public static void startInfRemindActivity(Context context) {
        context.startActivity(new Intent(context, InfRemindActivity.class)
        );
    }

    /**
     * 跳转到设备设置界面
     */
    public static void startAlarmClockActivity(Context context, int type) {
        context.startActivity(new Intent(context, AlarmClockActivity.class)
                .putExtra("type", type)
        );
    }

    public static void startScheduleActivity(Context context) {
        context.startActivity(new Intent(context, ScheduleActivity.class)
        );
    }

    public static void startAlarmClockActivity(Context context, int type, int position) {
        context.startActivity(new Intent(context, AlarmClockActivity.class)
                .putExtra("type", type)
                .putExtra("update", position)
        );
    }

    public static void startScheduleActivity(Context context, int position) {
        context.startActivity(new Intent(context, ScheduleActivity.class)
                .putExtra("update", position)
        );
    }

    /**
     * 设置界面跳转到闹钟列表和日程列表
     *
     * @param context 从哪个页面跳转
     */
    public static void startAlarmClockListActivity(Context context) {
        context.startActivity(new Intent(context, AlarmClockListActivity.class)
        );
    }

    /**
     * 吃药提醒
     *
     * @param context 从哪个页面跳转
     */
    public static void startTakeMedicineIndexActivity(Context context) {
        context.startActivity(new Intent(context, TakeMedicineIndexActivity.class)
        );
    }

    /**
     * 添加吃药提醒
     *
     * @param context 从哪个页面跳转
     */
    public static void startTakeMedicineActivity(Context context) {
        context.startActivity(new Intent(context, TakeMedicineActivity.class)
        );
    }

    public static void startTakeMedicineActivity(Context context, int position) {
        context.startActivity(new Intent(context, TakeMedicineActivity.class)
                .putExtra("update", position)
        );
    }

    /**
     * @param context
     * @param type    提醒周期
     */
    public static void startTakeMedicineRepeatActivity(Context context, int type) {
        context.startActivity(new Intent(context, TakeMedicineRepeatActivity.class)
                .putExtra("ReminderPeriod", type)
        );
    }

    /**
     * 勿扰模式
     *
     * @param context
     */
    public static void startDoNotDisturbActivity(Context context) {
        context.startActivity(new Intent(context, DoNotDisturbActivity.class));
    }

    /**
     * 设置达标运动界面
     *
     * @param context
     */
    public static void startSportsGoalActivity(Context context) {
        context.startActivity(new Intent(context, SportsGoalActivity.class)
        );
    }

    /**
     * 设置达标睡眠界面
     *
     * @param context
     */
    public static void startSleepGoalActivity(Context context) {
        context.startActivity(new Intent(context, SleepGoalActivity.class)
        );
    }

    /**
     * 体温 温度
     */
    public static void startTempActivity(Context context) {
        context.startActivity(new Intent(context, TempActivity.class)
        );
    }

    /**
     * 体重
     */
    public static void startWeightActivity(Context context) {
        context.startActivity(new Intent(context, WeightActivity.class)
        );
    }

    /**
     * 单位设置
     *
     * @param context
     */
    public static void startUnitActivity(Context context) {
        context.startActivity(new Intent(context, UnitActivity.class));
    }

    /**
     * 账户与安全
     *
     * @param context
     */
    public static void startAccountActivity(Context context) {
        context.startActivity(new Intent(context, AccountActivity.class));
    }

    public static void startUpPasswordActivity(Context context) {
        context.startActivity(new Intent(context, UpPasswordActivity.class));
    }

    public static void startFindPhoneActivity(Context context) {
        context.startActivity(new Intent(context, FindPhoneMainActivity.class));
    }

    public static void startBindNewPhoneActivity(Context context, String oldVerifyCode, String password) {
        context.startActivity(new Intent(context, BindNewPhoneActivity.class)
                .putExtra("oldVerifyCode", oldVerifyCode)
                .putExtra("password", password));
    }

    public static void startPasswordCheckActivity(Context context) {
        context.startActivity(new Intent(context, PasswordCheckActivity.class));
    }

    public static void startAppealActivity(Context context) {
        context.startActivity(new Intent(context, AppealActivity.class));
    }

    /**
     * 日程
     *
     * @param context
     */
    public static void startScheduleListActivity(Context context) {
        context.startActivity(new Intent(context, ScheduleListActivity.class)
        );

    }

    /**
     * 卡片编辑
     */
    public static void startCardEditActivity(Context context) {
        context.startActivity(new Intent(context, CardEditActivity.class)
        );

    }


    /**
     * 设置天气预报的跳转
     *
     * @param context
     */
    public static void startSettingWeatherActivity(Context context) {
        context.startActivity(new Intent(context, SettingWeatherActivity.class)
        );

    }

    /**
     * 查看数据间隔与修改
     */
    public static void startBigDataIntervalActivity(Context context) {
        context.startActivity(new Intent(context, BigDataIntervalActivity.class)
        );

    }

    /**
     * 地图
     */
    public static void startLocationMap(Context context, MapMotionBean mMapMotionBean) {
        context.startActivity(new Intent(context, RunningActivity.class)
                .putExtra("MapMotionBean", (Serializable) mMapMotionBean)
        );
    }

    /**
     * 重启app
     *
     * @param context
     */
    public static void restartApp(Context context) {
        PackageManager packageManager = context.getPackageManager();
        if (null == packageManager) {
            TLog.Companion.error("null == packageManager");
            return;
        }
        final Intent intent = packageManager.getLaunchIntentForPackage(context.getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
        }
    }

    /**
     * 运动记录
     */
    public static void startExerciseRecordActivity(Context context) {
        //context.startActivity(new Intent(context, ExerciseRecordActivity.class));

        context.startActivity(new Intent(context, AmapSportRecordActivity.class).putExtra("sportType",-1));
    }




    /**
     * 设备运动记录
     *
     * @param context
     */
    public static void startDeviceSportChartActivity(Context context) {
        context.startActivity(new Intent(context, DeviceSportChartActivity.class));
    }

    /**
     * 关于
     */
    public static void startAboutActivity(Context context) {
        context.startActivity(new Intent(context, AboutActivity.class));
    }

    /**
     * flash页面
     */
    public static void startFlashActivity(Context context) {
        context.startActivity(new Intent(context, FlashActivity.class));
    }

    /**
     * 登录页面
     */
    public static void startLoginActivity(Context context) {
        context.startActivity(new Intent(context, LoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        );
    }

    /**
     * 忘记密码
     */
    public static void startForgetPasswordActivity(Context context) {
        context.startActivity(new Intent(context, ForgetPasswordActivity.class)
        );
    }

    /**
     * 注销账号
     */
    public static void startLogOutActivity(Context context) {
        context.startActivity(new Intent(context, LogOutActivity.class)
        );
    }

    /**
     * 注销账号 验证码
     */
    public static void startLogOutCodeActivity(Context context) {
        context.startActivity(new Intent(context, LogOutCodeActivity.class)
        );
    }

    /**
     * 注销账号 确认
     */
    public static void startSureLogOutActivity(Context context, String code) {
        context.startActivity(new Intent(context, SureLogOutActivity.class)
                .putExtra("code", code)
        );
    }

    /**
     * 心率报警
     */
    public static void startHeartRateAlarmActivity(Context context) {
        context.startActivity(new Intent(context, HeartRateAlarmActivity.class));
    }

    /**
     * 第一次进入页面进行设置
     */
    public static void startGoalActivity(Context context) {
        context.startActivity(new Intent(context, GoalActivity.class));
    }

    public static void startSettingActivity(Context context) {
        context.startActivity(new Intent(context, SettingActivity.class));
    }

    /**
     * 测试
     */
    public static void startTest(Context context) {
        context.startActivity(new Intent(context, TestActivity.class));
    }

    public static void startWeb(Context context, String url) {
        context.startActivity(new Intent(context, WebActivity.class)
                .putExtra("url", url));

    }

    public static void startProblemsFeedbackActivity(Context context) {
        context.startActivity(new Intent(context, ProblemsFeedbackActivity.class));
    }


    /**
     * 表盘
     */
    public static void startDialMarketActivity(Context context) {
        context.startActivity(new Intent(context, DialMarketActivity.class));
    }

    /**
     * 表盘
     */
    public static void startDialDetailsActivity(Context context, String bean) {
        context.startActivity(new Intent(context, DialDetailsActivity.class)
                .putExtra("TypeList", bean));
    }

    /**
     * 自定义表盘
     *
     * @param context
     */
    public static void startCustomizeDialActivity(Context context) {
        context.startActivity(new Intent(context, CustomizeDialActivity.class));
    }
}
