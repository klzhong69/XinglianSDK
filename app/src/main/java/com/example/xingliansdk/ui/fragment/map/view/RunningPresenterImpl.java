package com.example.xingliansdk.ui.fragment.map.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import com.amap.api.maps.model.LatLng;
import com.example.db.AmapSportBean;
import com.example.db.DbManager;
import com.example.xingliansdk.Config;
import com.example.xingliansdk.Constant;
import com.example.xingliansdk.R;
import com.example.xingliansdk.network.api.login.LoginBean;
import com.example.xingliansdk.ui.fragment.map.task.SNAsyncTask;
import com.example.xingliansdk.ui.fragment.map.task.SNVTaskCallBack;
import com.example.xingliansdk.utils.CountTimer;
import com.example.xingliansdk.utils.HawkUtil;
import com.example.xingliansdk.utils.ResUtil;
import com.example.xingliansdk.utils.Utils;
import com.example.xingliansdk.view.DateUtil;
import com.google.gson.Gson;
import com.orhanobut.hawk.Hawk;
import com.shon.connector.utils.TLog;
import com.sn.map.bean.SNLocation;
import com.sn.map.interfaces.OnMapLocationAddressListener;
import com.sn.map.interfaces.OnMapScreenShotListener;
import com.sn.map.interfaces.OnSportMessageListener;
import com.sn.map.utils.GPSUtil;
import com.sn.map.view.SNMapHelper;
import com.tencent.bugly.crashreport.biz.UserInfoBean;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * 功能:运动轨迹
 */
public class RunningPresenterImpl extends BasePresenter<IRunningContract.IView> implements IRunningContract.IPresenter, CountTimer.OnCountTimerListener {

    private static final String TAG = "RunningPresenterImpl";
    
    private IRunningContract.IView view;
    private LinkedList<SNLocation> locations;

    private final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    /**
     * 超强
     */
    public static final int SIGNAL_STRONG_MAX = 3;
    /**
     * 强
     */
    public static final int SIGNAL_STRONG = 2;
    /**
     * 中
     */
    public static final int SIGNAL_MIDDLE = 1;
    /**
     * 弱
     */
    public static final int SIGNAL_WEAK = 0;

    /**
     * GPS已关闭
     */
    public static final int SIGNAL_GPS_OFF = -1;

    public static final int CODE_SUCCESS = 0x00;
    public static final int CODE_ERROR = -0x10;
    public static final int CODE_COUNT_LITTLE = -0x20;
    public static final int CODE_TIME_OUT = -0x30;
    private SNMapHelper mMapHelper;
     private CountTimer countTimer = new CountTimer(1000, this);
    /**
     * 截图
     */
    private Bitmap mMapBitmap;
    /**
     * 位置具体名称
     */
    private String mMapAddress;
    private long millisecond;
    private String millisecondStr;

    public RunningPresenterImpl(IRunningContract.IView view) {
        this.view = view;

    }


    @Override
    public void initMapListener(SNMapHelper mapHelper) {
        this.mMapHelper = mapHelper;
        mapHelper.requestLastLocation();
        mapHelper.setOnSportMessageListener(onSportMessageListener);
        onSportMessageListener.onSignalChanged(0);

        // 判断GPS模块是否开启，如果没有则开启
        if (!GPSUtil.isGpsEnable((Context) getView())) {
            view.onUpdateGpsSignal(SIGNAL_GPS_OFF);
        }
    }

    @Override
    public void initDefaultValue() {
        String distances;
        String hourSpeed;
        //  UnitConfig unitConfig = AppUnitUtil.getUnitConfig();
        //如果单位是英里,则需要转一下
//        if (unitConfig.distanceUnit == UnitConfig.DISTANCE_MILES) {
//            distances = (ResUtil.format("%.2f %s", UnitConversion.kmToMiles((float) 0), ResUtil.getString(R.string.unit_mile)));
//            hourSpeed = (ResUtil.format("%.2f %s", UnitConversion.kmToMiles((float) 0), ResUtil.getString(R.string.unit_mile_h)));
//        } else {
        distances = (ResUtil.format("%.2f", 0.0));
        hourSpeed = (ResUtil.format("%.2f %s", 0.0, "km/h"));
//        }
        String calories = (ResUtil.format("%.2f ", 0.0));
        String pace = (ResUtil.format("%02d'%02d\"", 0, 0));
        if (!isUIEnable()) return;
        TLog.Companion.error("更新地理位置了 initDefaultValue");
        view.onUpdateSportData(distances, calories, hourSpeed, pace,new ArrayList<>());
    }

    @Override
    public void requestSettingConfig() {
        if (!isUIEnable()) return;
        //  view.onUpdateSettingConfig(MapSettingStorage.isKeepScreenEnable(), MapSettingStorage.isWeatherEnable());
    }

    @Override
    public void requestWeatherData() {
//        SNAsyncTask.execute(new SNVTaskCallBack() {
//            String weatherQuality = ResUtil.getString(R.string.content_general);
//            int weatherType = 0;
//            String weatherTemperatureRange;
//
//            @Override
//            public void run() throws Throwable {
////                WeatherListBean weatherListBean = WeatherStorage.getWeatherListBean();
////                if (weatherListBean == null) return;
////                List<WeatherBean.DataBean> dataList = weatherListBean.getData();
////                if (IF.isEmpty(dataList)) return;
////                //从历史天气列表中取出今天的天气
////                WeatherBean.DataBean name = null;
////                for (WeatherBean.DataBean bean : dataList) {
////                    if (DateUtil.equalsToday(bean.getDate())) {
////                        name = dataList.get(0);
////                    }
////                }
////                //如果天气历史中仍然没有今天的天气,就拿最后一次的天气 先顶顶
////                if (name == null && dataList.size() > 0) {
////                    name = dataList.get(dataList.size() - 1);
////                }
////                if (name == null) return;
////
////                weatherType = name.getCond_code_type();
////                int temperatureMin = name.getTmp_min();
////                int temperatureMax = name.getTmp_max();
////                UnitConfig unitConfig = AppUnitUtil.getUnitConfig();
////                String format = "%d℃~%d℃";
////                if (unitConfig != null && unitConfig.getTemperatureUnit() == UnitConfig.TEMPERATURE_F) {
////                    format = "%d℉~%d℉";
////                    temperatureMin = (int) UnitConversion.CToF(temperatureMin);
////                    temperatureMax = (int) UnitConversion.CToF(temperatureMax);
////                }
//                //   weatherTemperatureRange = ResUtil.format(format, temperatureMin, temperatureMax);
//            }
//
//            @Override
//            public void done() {
//                if (!isUIEnable()) return;
//                view.onUpdateWeatherData(weatherType, weatherTemperatureRange, weatherQuality);
//            }
//        });

    }


    @Override
    public void requestMapFirstLocation() {
        double latitude = 0;
        double longitude = 0;
        SNLocation lastLocation = mMapHelper.getLastLocation();
        if (lastLocation != null) {
            latitude = lastLocation.getLatitude();
            longitude = lastLocation.getLongitude();
        }
        TLog.Companion.error("初始化位置latitude++"+latitude);
        TLog.Companion.error("初始化位置longitude++"+longitude);
        //初始化地图时 显示默认上次的经纬度
        if (latitude > 0 && longitude > 0) {
            view.onUpdateMapFirstLocation(latitude, longitude);
        } else {
            //如果没有 那就默认北京
            latitude = 0;
            longitude = 0;
            if (latitude > 0 && longitude > 0) {
                if (!isUIEnable()) return;
                view.onUpdateMapFirstLocation(latitude, longitude);
            }
        }
    }

    private static Handler mHandler = new Handler();

    private void saveSportData() {
        LinkedList<SNLocation> locations = mMapHelper.getLocations();
        if(locations == null || locations.isEmpty())
            return;
//        if (locations.size() <= 1) {
//            onCallSaveSportDataStatusChange(CODE_COUNT_LITTLE);
//            return;
//        }

        mMapBitmap = null;
        mMapAddress = null;
        //mMapHelper.screenCapture(true, onMapScreenShotListener);
        mMapHelper.requestGetLocationAddress(locations.getLast(), onMapLocationAddressListener);
//        TimeOutUtil.setTimeOut(mHandler, 10 * 1000L, new TimeOutUtil.OnTimeOutListener() {
//            @Override
//            public void onTimeOut() {
//                onCallSaveSportDataStatusChange(CODE_TIME_OUT);
//            }
//        });
    }

    private void onCallSaveSportDataStatusChange(int codeCountLittle) {
        view.onSaveSportDataStatusChange(codeCountLittle);
        //   TimeOutUtil.removeTimeOut(mHandler);
    }

    @Override
    public void requestStartSport() {

        if (!mMapHelper.isStarted()) {
            // if (MapSettingStorage.isBeginVibrationEnable()) {
            requestDeviceVibrator();
            // }

            mMapHelper.startSport();
            if (isUIEnable()) {
                view.onSportStartAnimationEnable(true);
            }
            countTimer.start();
        }

    }

    private void requestDeviceVibrator() {
        // 尴尬了  手环只支持振动开和关, 而需求是要实现振动一下 只能这样先顶顶 看看以后手环有没有加协议
//        SNAsyncTask.execute(new SNVTaskCallBack() {
//            @Override
//            public void run() throws Throwable {
//                //    SNBLEHelper.sendCMD(SNCMD.get().setFindDeviceStatus(true));
//                sleep(1000);
//                //    SNBLEHelper.sendCMD(SNCMD.get().setFindDeviceStatus(false));
//            }
//        });
    }

    @Override
    public void requestStopSport() {
//        if (MapSettingStorage.isEndVibrationEnable()) {
//            requestDeviceVibrator();
//        }
        if (mMapHelper != null && mMapHelper.isStarted()) {
            mMapHelper.stopSport();
            countTimer.stop();
          // saveSportData();
        } else {
            if (!isUIEnable()) return;
            onCallSaveSportDataStatusChange(CODE_SUCCESS);
        }

    }

    @Override
    public void requestRetrySaveSportData() {
        saveSportData();
    }


    private final OnSportMessageListener onSportMessageListener = new OnSportMessageListener() {


        @Override
        public void onSignalChanged(int level) {
            if (!isUIEnable()) return;
            if (level >= 10) {
                view.onUpdateGpsSignal(SIGNAL_STRONG_MAX);
            } else if (level >= 6) {
                view.onUpdateGpsSignal(SIGNAL_STRONG);
            } else if (level >= 4) {
                view.onUpdateGpsSignal(SIGNAL_MIDDLE);
            } else if (level < 4) {
                view.onUpdateGpsSignal(SIGNAL_WEAK);
            }
        }

        @Override
        public void onSportChanged(final LinkedList<SNLocation> locations) {
            RunningPresenterImpl.this.locations = locations;
            updateSportMessage(locations);
        }
    };



    private void updateSportMessage(final LinkedList<SNLocation> locations) {

        SNAsyncTask.execute(new SNVTaskCallBack() {
            private List<LatLng> latLngs = new ArrayList<>();
            private String distances;
            private String calories;
            private String hourSpeed;
            private String pace;

            @Override
            public void run() throws Throwable {
                RunningSportDataUtil.SportData sportData = RunningSportDataUtil.calculateSportData(RunningSportDataUtil.calculateBaseSportData(mMapHelper, locations));
                distances = (ResUtil.format("%.2f", sportData.distances));
                hourSpeed = (ResUtil.format("%.2f%s", sportData.hourSpeed, ResUtil.getString(R.string.unit_km_h)));
//                hourSpeed = (ResUtil.format("%.2f%s", sportData.hourSpeed));
                calories = (ResUtil.format("%.2f", sportData.calories));
                pace = (ResUtil.format("%02d'%02d\"", sportData.speed_minutes, sportData.speed_seconds));
                TLog.Companion.error("更新地理位置了 run"+new Gson().toJson(locations)+" 大小="+locations.size());

                for(SNLocation snLocation : locations){
                    latLngs.add(new LatLng(snLocation.getLatitude(),snLocation.getLongitude()));
                }
            }

            @Override
            public void done() {
                if (!isUIEnable()) return;
                TLog.Companion.error("更新地理位置了 done");
                view.onUpdateSportData(distances, calories, hourSpeed, pace,latLngs);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (locations != null && !locations.isEmpty()) {
            updateSportMessage(locations);
        }
    }

    /**
     * 地图截图和储存
     */
    private OnMapScreenShotListener onMapScreenShotListener = new OnMapScreenShotListener() {


        @Override
        public void onMapScreenShot(final Bitmap bitmap) {
            mMapBitmap = bitmap;
            save();

        }
    };

    /**
     * 获取地理位置
     */
    private OnMapLocationAddressListener onMapLocationAddressListener = new OnMapLocationAddressListener() {

        @Override
        public void onLocationAddress(String address) {
            mMapAddress = address;
            save();
        }

        @Override
        public void onLocationAddressFailed(int code) {
            if (!isUIEnable()) return;
            onCallSaveSportDataStatusChange(CODE_ERROR);
        }

    };


    double  kcalcanstanc = 65.4; //计算卡路里常量


    private void save() {

        Log.e(TAG,"---111--save="+mMapAddress);

        //条件: 需要 截图完成 和地理编码转换完成 才进行下一步
        //两个条件都必须满足
        if (mMapAddress == null ) {
            return;
        }
        SNAsyncTask.execute(new SNVTaskCallBack() {
            private String mLocationJsonData;
            private RunningSportDataUtil.SportData sportData;
            private RunningSportDataUtil.BaseSportData baseSportData;
          //  private File file = new File(Constant.Path.CACHE_MAP, "map_capture_image.jpg");
            private String dateTime;

            @Override
            public void run() throws Throwable {
                dateTime = DateUtil.getCurrentDate(DateUtil.YYYY_MM_DD_HH_MM_SS);
                LinkedList<SNLocation> locations = mMapHelper.getLocations();
             //   StravaTool.saveToGpxFile(RunTrackUtil.convertToGPXs(locations), dateTime);
           //     mLocationJsonData = RunTrackUtil.convertToJson(locations);
                //计算
                baseSportData = RunningSportDataUtil.calculateBaseSportData(mMapHelper, locations);
                sportData = RunningSportDataUtil.calculateSportData(baseSportData);
                //取截图的中间部分
            //    int screenWidth = DIYViewUtil.getScreenWidth(App.getContext().getResources());
              //  Bitmap bitmapCenterRange = BitmapUtil.getBitmapCenterRange(mMapBitmap, screenWidth, screenWidth);
            //    bitmapCenterRange.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(file));

            }

            @Override
            public void done() {


                Log.e(TAG,"----保存运动=");
                
                if (baseSportData == null ) {
                    onCallSaveSportDataStatusChange(CODE_ERROR);
                } else {
                    Log.e(TAG,"--22--保存运动=");
                    LinkedList<SNLocation> locations = mMapHelper.getLocations();

                    MultipartBody.Builder builder = new MultipartBody.Builder();
                    builder.setType(MultipartBody.FORM);
                 //   builder.addFormDataPart("access_token", UserStorage.getAccessToken());
                 //   builder.addFormDataPart("user_id", String.valueOf(AppUserUtil.getUser().getUser_id()));
                    //日期 yyyy-MM-dd HH:mm:ss

                    builder.addFormDataPart("date", dateTime);
                    //距离 米
                    builder.addFormDataPart("distance", String.valueOf(Math.round(baseSportData.distanceTotal)));
                    //时长 秒
                    builder.addFormDataPart("duration", String.valueOf(millisecond / 1000));
                    //平均速度 米/秒
                    builder.addFormDataPart("average_speed", String.valueOf(baseSportData.speedAvg));
                    //配速 单位 分秒(00'00") 转成 秒 传到服务器               ,到时候获取服务器数据时需要 把秒转成 分秒  单位
                    builder.addFormDataPart("average_pace", String.valueOf((sportData.speed_minutes * 60) + sportData.speed_seconds));
                    //最快速度 米/秒
                    builder.addFormDataPart("fast_speed", String.valueOf(String.valueOf(baseSportData.speedMax)));
                    //卡路里 单位 卡
                    builder.addFormDataPart("cal", String.valueOf(Math.round(sportData.calories * 1000)));
                    //位置
                    builder.addFormDataPart("location", mMapAddress);
                    //轨迹
                    if (mLocationJsonData != null) {
                        builder.addFormDataPart("data", mLocationJsonData);
                    }
                    //图片
                   // builder.addFormDataPart("track_image", file.getName(), RequestBody.create(MediaType.parse("image/jpeg"), file));


                    Log.e(TAG,"----时分="+(sportData.speed_minutes+"-"+sportData.speed_seconds));

                    //保存数据到数据库中

                    //先判断是否有用户id,无用户id不保存   var userInfo = Hawk.get<LoginBean>(Config.database.USER_INFO)

                    LoginBean loginBean = Hawk.get(Config.database.USER_INFO);
                    Log.e(TAG,"--22--loginBean="+loginBean.toString());
                    if(loginBean == null)
                        return;
                    String userId = loginBean.getUser().getUserId();
                    if(userId == null)
                        return;
                    Log.e(TAG,"----保存Userid="+userId);
                    //总距离
                    String countDistance = Math.round(baseSportData.distanceTotal)+"";
                    //总时长 ，秒
                    String countTime = (millisecond / 1000)+"";
                    //结束运动的时间
                    long endTime = System.currentTimeMillis()/1000;
                    //卡路里
                    String countCalories = decimalFormat.format(Utils.mul(kcalcanstanc,baseSportData.distanceTotal)/1000) +"";
                    //平均速度 米/秒
                    String avgSpeed = baseSportData.speedAvg+"";
                    //平均配速
                    String paceStr = (sportData.speed_minutes * 60 + sportData.speed_seconds)+"";
                    //经纬度集合
                    List<LatLng> resultLat = new ArrayList<>();
                    if(locations != null && locations.size()>0){
                        for(SNLocation snLocation : locations){
                            resultLat.add(new LatLng(snLocation.getLatitude(),snLocation.getLongitude()));
                        }
                    }
                    String latStr = new Gson().toJson(resultLat);

                    //mac地址
                    String macAddress = Hawk.get("address");
                    //运动类型
                    int sportType = Hawk.get(Config.database.AMAP_SPORT_TYPE,1);

                    AmapSportBean amapSportBean = new AmapSportBean();
                    amapSportBean.setUserId(userId);
                    amapSportBean.setDeviceMac(macAddress);
                    amapSportBean.setDayDate(Utils.getCurrentDate());
                    amapSportBean.setYearMonth(Utils.getCurrentDateByFormat("yyyy-MM"));
                    amapSportBean.setSportType(sportType);
                    amapSportBean.setMapType(1);
                    amapSportBean.setCurrentSportTime(millisecondStr);
                    amapSportBean.setEndSportTime(Utils.getCurrentDate1());
                    amapSportBean.setCurrentSteps(0);
                    amapSportBean.setDistance(countDistance);
                    amapSportBean.setCalories(countCalories);
                    amapSportBean.setAverageSpeed(avgSpeed);
                    amapSportBean.setPace(paceStr);
                    amapSportBean.setLatLonArrayStr(latStr);
                    amapSportBean.setHeartArrayStr("");
                    Log.e(TAG,"-----保存cans="+new Gson().toJson(amapSportBean));

                    boolean isSave = DbManager.getDbManager().saveAmapSportData(amapSportBean);
                    //保存成功后查询下累计的里程
                    if(isSave)
                        DbManager.getDbManager().queryALlTotalDistance(userId);


//                    AppNetReq.getApi().uploadTrack(builder.build())
//                            .enqueue(new OnResponseListener<DefResponseBean>() {
//                                @Override
//                                public void onResponse(DefResponseBean body) throws Throwable {
//                                    SNLog.i("uploadTrack:" + body.isSuccessful());
//                                    if (!isUIEnable()) return;
//                                    onCallSaveSportDataStatusChange(CODE_SUCCESS);
//                                }
//
//                                @Override
//                                public void onFailure(int ret, String msg) {
//                                    if (!isUIEnable()) return;
//                                    onCallSaveSportDataStatusChange(CODE_ERROR);
//                                }
//                            });
                }
            }

            @Override
            public void error(Throwable e) {
                super.error(e);
                if (!isUIEnable()) return;
                onCallSaveSportDataStatusChange(CODE_ERROR);
            }
        });
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (mMapBitmap != null && !mMapBitmap.isRecycled()) {
                mMapBitmap.recycle();
            }
        } catch (Exception ignored) {
        }
          countTimer.stop();
    }

    @Override
    public void onCountTimerChanged(long millisecond) {
        this.millisecond = millisecond;
        DateUtil.HMS hms = DateUtil.getHMSFromMillis(millisecond);
        if (!isUIEnable()) return;
        this.millisecondStr = ResUtil.format("%02d:%02d:%02d", hms.getHour(), hms.getMinute(), hms.getSecond());
        view.onUpdateSportData(ResUtil.format("%02d:%02d:%02d", hms.getHour(), hms.getMinute(), hms.getSecond()));
    }
}
