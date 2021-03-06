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
     * ?????????  ????????????ble??????
     */
    public static void startBleConnectActivity(Context context) {
        context.startActivity(new Intent(context, BleConnectActivity.class)
                //.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK)
        );
    }

    /**
     * ?????????  ??????????????????
     */
    public static void startOtherSettingActivity(Context context) {
        context.startActivity(new Intent(context, OtherSettingActivity.class)
        );
    }

    /**
     * ?????????????????????
     *
     * @param context ?????????????????????
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
     * ?????????????????????
     *
     * @param context ?????????????????????
     */
    public static void startBloodPressureActivity(Context context) {
        context.startActivity(new Intent(context, BloodPressureActivity.class));
    }

    /**
     * ??????
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
     * ??????????????????
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
     * ????????????
     *
     * @param context
     */
    public static void startBloodOxygenActivity(Context context) {
        context.startActivity(new Intent(context, BloodOxygenActivity.class)
        );
    }

    /**
     * ??????
     */
    public static void startPressureActivity(Context context) {
        context.startActivity(new Intent(context, PressureActivity.class));
    }


    /**
     * ??????????????????
     *
     * @param context
     */
    public static void startRealTimeHeartRateActivity(Context context) {
        context.startActivity(new Intent(context, RealTimeHeartRateActivity.class));
    }

    /**
     * ?????????OTA????????????
     *
     * @param context ?????????????????????
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
     * ???????????????????????????
     *
     * @param context ?????????????????????
     */
    public static void startDeviceInformationActivity(Context context, boolean register) {
        context.startActivity(new Intent(context, DeviceInformationActivity.class)
                        .putExtra("register", register)
//                .putExtra("name", name)
        );
    }

    /**
     * ????????????
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
     * ???????????????????????????
     *
     * @param context ?????????????????????
     */
    public static void startMyDeviceActivity(Context context, int electricity) {
        context.startActivity(new Intent(context, MyDeviceActivity.class)
                .putExtra("electricity", electricity)
        );
    }

    /**
     * ???????????????????????????
     *
     * @param context
     */
    public static void startModuleMeasurementListActivity(Context context) {
        context.startActivity(new Intent(context, ModuleMeasurementListActivity.class)
        );
    }


    /**
     * ?????????????????????????????????
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
     * ???????????????????????????
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
     * ????????????????????????????????????????????????
     *
     * @param context ?????????????????????
     */
    public static void startAlarmClockListActivity(Context context) {
        context.startActivity(new Intent(context, AlarmClockListActivity.class)
        );
    }

    /**
     * ????????????
     *
     * @param context ?????????????????????
     */
    public static void startTakeMedicineIndexActivity(Context context) {
        context.startActivity(new Intent(context, TakeMedicineIndexActivity.class)
        );
    }

    /**
     * ??????????????????
     *
     * @param context ?????????????????????
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
     * @param type    ????????????
     */
    public static void startTakeMedicineRepeatActivity(Context context, int type) {
        context.startActivity(new Intent(context, TakeMedicineRepeatActivity.class)
                .putExtra("ReminderPeriod", type)
        );
    }

    /**
     * ????????????
     *
     * @param context
     */
    public static void startDoNotDisturbActivity(Context context) {
        context.startActivity(new Intent(context, DoNotDisturbActivity.class));
    }

    /**
     * ????????????????????????
     *
     * @param context
     */
    public static void startSportsGoalActivity(Context context) {
        context.startActivity(new Intent(context, SportsGoalActivity.class)
        );
    }

    /**
     * ????????????????????????
     *
     * @param context
     */
    public static void startSleepGoalActivity(Context context) {
        context.startActivity(new Intent(context, SleepGoalActivity.class)
        );
    }

    /**
     * ?????? ??????
     */
    public static void startTempActivity(Context context) {
        context.startActivity(new Intent(context, TempActivity.class)
        );
    }

    /**
     * ??????
     */
    public static void startWeightActivity(Context context) {
        context.startActivity(new Intent(context, WeightActivity.class)
        );
    }

    /**
     * ????????????
     *
     * @param context
     */
    public static void startUnitActivity(Context context) {
        context.startActivity(new Intent(context, UnitActivity.class));
    }

    /**
     * ???????????????
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
     * ??????
     *
     * @param context
     */
    public static void startScheduleListActivity(Context context) {
        context.startActivity(new Intent(context, ScheduleListActivity.class)
        );

    }

    /**
     * ????????????
     */
    public static void startCardEditActivity(Context context) {
        context.startActivity(new Intent(context, CardEditActivity.class)
        );

    }


    /**
     * ???????????????????????????
     *
     * @param context
     */
    public static void startSettingWeatherActivity(Context context) {
        context.startActivity(new Intent(context, SettingWeatherActivity.class)
        );

    }

    /**
     * ???????????????????????????
     */
    public static void startBigDataIntervalActivity(Context context) {
        context.startActivity(new Intent(context, BigDataIntervalActivity.class)
        );

    }

    /**
     * ??????
     */
    public static void startLocationMap(Context context, MapMotionBean mMapMotionBean) {
        context.startActivity(new Intent(context, RunningActivity.class)
                .putExtra("MapMotionBean", (Serializable) mMapMotionBean)
        );
    }

    /**
     * ??????app
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
     * ????????????
     */
    public static void startExerciseRecordActivity(Context context) {
        //context.startActivity(new Intent(context, ExerciseRecordActivity.class));

        context.startActivity(new Intent(context, AmapSportRecordActivity.class).putExtra("sportType",-1));
    }




    /**
     * ??????????????????
     *
     * @param context
     */
    public static void startDeviceSportChartActivity(Context context) {
        context.startActivity(new Intent(context, DeviceSportChartActivity.class));
    }

    /**
     * ??????
     */
    public static void startAboutActivity(Context context) {
        context.startActivity(new Intent(context, AboutActivity.class));
    }

    /**
     * flash??????
     */
    public static void startFlashActivity(Context context) {
        context.startActivity(new Intent(context, FlashActivity.class));
    }

    /**
     * ????????????
     */
    public static void startLoginActivity(Context context) {
        context.startActivity(new Intent(context, LoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        );
    }

    /**
     * ????????????
     */
    public static void startForgetPasswordActivity(Context context) {
        context.startActivity(new Intent(context, ForgetPasswordActivity.class)
        );
    }

    /**
     * ????????????
     */
    public static void startLogOutActivity(Context context) {
        context.startActivity(new Intent(context, LogOutActivity.class)
        );
    }

    /**
     * ???????????? ?????????
     */
    public static void startLogOutCodeActivity(Context context) {
        context.startActivity(new Intent(context, LogOutCodeActivity.class)
        );
    }

    /**
     * ???????????? ??????
     */
    public static void startSureLogOutActivity(Context context, String code) {
        context.startActivity(new Intent(context, SureLogOutActivity.class)
                .putExtra("code", code)
        );
    }

    /**
     * ????????????
     */
    public static void startHeartRateAlarmActivity(Context context) {
        context.startActivity(new Intent(context, HeartRateAlarmActivity.class));
    }

    /**
     * ?????????????????????????????????
     */
    public static void startGoalActivity(Context context) {
        context.startActivity(new Intent(context, GoalActivity.class));
    }

    public static void startSettingActivity(Context context) {
        context.startActivity(new Intent(context, SettingActivity.class));
    }

    /**
     * ??????
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
     * ??????
     */
    public static void startDialMarketActivity(Context context) {
        context.startActivity(new Intent(context, DialMarketActivity.class));
    }

    /**
     * ??????
     */
    public static void startDialDetailsActivity(Context context, String bean) {
        context.startActivity(new Intent(context, DialDetailsActivity.class)
                .putExtra("TypeList", bean));
    }

    /**
     * ???????????????
     *
     * @param context
     */
    public static void startCustomizeDialActivity(Context context) {
        context.startActivity(new Intent(context, CustomizeDialActivity.class));
    }
}
