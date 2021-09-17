package com.example.xingliansdk.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.utils.overlay.SmoothMoveMarker;
import com.amap.api.trace.LBSTraceClient;
import com.amap.api.trace.TraceListener;
import com.amap.api.trace.TraceLocation;
import com.amap.api.trace.TraceOverlay;
import com.example.db.AmapSportBean;
import com.example.xingliansdk.R;
import com.example.xingliansdk.widget.TitleBarLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * 显示运动轨迹页面
 * Created by Admin
 *
 */
public class AmapHistorySportActivity extends AppCompatActivity implements LocationSource,
        AMapLocationListener, TraceListener {

    private static final String TAG = "AmapHistorySportActivit";


    private MapView amapMapView;

    private Marker startMark, endMark;
    Polyline polyline;

    private AMap aMap;

    private OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption = null;



    private ConcurrentMap<Integer, TraceOverlay> mOverlayList = new ConcurrentHashMap<>();
    private int mSequenceLineID = 1000;
    private LBSTraceClient mTraceClient;

    private int mCoordinateType = LBSTraceClient.TYPE_AMAP;

    private NestedScrollView amapDetailSV;
    private CusMapContainerView cusMapContainerView;


    DecimalFormat decimalFormat = new DecimalFormat("#.##");


    private TitleBarLayout titleBarLayout;

    //类型
    private TextView itemAmapSportTypeTv;
    //公里
    private TextView itemSportDetailDistanceTv;
    //时间
    private TextView itemSportDetailDateTv;
    //运动时间
    private TextView itemAmapSportDetailDurationTv;
    //热量
    private TextView itemAmapSportDurationTv;

    //平均心率
    private TextView itemAmapSportDetailHeartTv;
    //步数
    private TextView itemAmapSportStepTv;
    //平均配速
    private TextView itemAmapSportDetailPeisuTv;
    //平均速度
    private TextView itemAmapSportSpeedTv;




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amap_sport_detail_layout);
        amapMapView = findViewById(R.id.amapDetailMapView);
        amapMapView.onCreate(savedInstanceState);

        initViews();

        if (aMap == null) {
            aMap = amapMapView.getMap();
            setUpMap();
        }

        showAMapData();

    }

    private void showAMapData() {
        try {
            Intent intent = getIntent();
            AmapSportBean amapSportBean = (AmapSportBean) intent.getSerializableExtra("sport_position");

            Log.e(TAG,"-----beanStr="+amapSportBean.toString());
            if(amapSportBean == null){
                showEmptyView();
                return;
            }

            showSportData(amapSportBean);

            String latStr = amapSportBean.getLatLonArrayStr();
            if(latStr == null)
                return;
            List<LatLng> latLngList = new Gson().fromJson(latStr,new TypeToken<List<LatLng>>(){}.getType());


            //  List<LatLng> latLngList = getLanList(traceLocationList);

            //开始和结束的位置标记
            LatLng startLng = latLngList.get(0);

            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLng, 18));

            MarkerOptions markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(typeSportImg(amapSportBean.getSportType())))
                    .position(startLng)
                    .draggable(false);
            startMark = aMap.addMarker(markerOptions);
            startMark.setDraggable(false);


            LatLng endLng = latLngList.get(latLngList.size() - 1);
            MarkerOptions markerOption = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_amap_sport_end))
                    .position(endLng)
                    .draggable(false);
            endMark = aMap.addMarker(markerOption);
            endMark.setDraggable(false);
            polyline = aMap.addPolyline(new PolylineOptions().addAll(latLngList).color(Color.parseColor("#00FF01")).width(18f));


            SmoothMoveMarker smoothMoveMarker = new SmoothMoveMarker(aMap);
            smoothMoveMarker.setDescriptor(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.gps_point)));

            smoothMoveMarker.setPoints(latLngList);
            smoothMoveMarker.setTotalDuration(3);
            smoothMoveMarker.startSmoothMove();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    smoothMoveMarker.setVisible(false);
                }
            }, 3* 1000);

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private String getPaceStr(String str){
        try {
            if(str == null)
                return "--";
            Integer integer = Integer.valueOf(str);
            if(integer == 0)
                return "00'00''";
            int hour = integer / 60;
            int minute = integer % 60;

            return hour+"'"+minute+"''";
        }catch (Exception e){
            e.printStackTrace();
            return "--";
        }

    }

    private void showSportData(AmapSportBean amapSportBean){
        try {
            itemAmapSportTypeTv.setText(typeSport(amapSportBean.getSportType()));
            itemSportDetailDistanceTv.setText(decimalFormat.format(Float.valueOf(amapSportBean.getDistance())/1000));
            itemSportDetailDateTv.setText(amapSportBean.getEndSportTime());
            itemAmapSportDetailDurationTv.setText(amapSportBean.getCurrentSportTime());
            itemAmapSportDurationTv.setText(amapSportBean.getCalories());
            itemAmapSportDetailPeisuTv.setText(getPaceStr(amapSportBean.getPace()));
            itemAmapSportSpeedTv.setText(decimalFormat.format(Double.valueOf(amapSportBean.getAverageSpeed())));
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    private void showEmptyView(){
        itemAmapSportTypeTv.setText("--");
        itemSportDetailDistanceTv.setText("--");
        itemSportDetailDateTv.setText("--");
        itemAmapSportDetailDurationTv.setText("--");
        itemAmapSportDurationTv.setText("--");
        itemAmapSportDetailPeisuTv.setText("--");
        itemAmapSportSpeedTv.setText("--");
    }



    private void initViews() {
        amapDetailSV = findViewById(R.id.amapDetailSV);
        cusMapContainerView = findViewById(R.id.cusMapContainerView);

        cusMapContainerView.setNestedScrollView(amapDetailSV);

        itemAmapSportTypeTv = findViewById(R.id.itemAmapSportTypeTv);
        itemSportDetailDistanceTv = findViewById(R.id.itemSportDetailDistanceTv);
        itemSportDetailDateTv = findViewById(R.id.itemSportDetailDateTv);
        itemAmapSportDetailDurationTv = findViewById(R.id.itemAmapSportDetailDurationTv);
        itemAmapSportDurationTv = findViewById(R.id.itemAmapSportDurationTv);
        itemAmapSportDetailHeartTv = findViewById(R.id.itemAmapSportDetailHeartTv);
        itemAmapSportStepTv = findViewById(R.id.itemAmapSportStepTv);
        itemAmapSportDetailPeisuTv = findViewById(R.id.itemAmapSportDetailPeisuTv);
        itemAmapSportSpeedTv = findViewById(R.id.itemAmapSportSpeedTv);




        titleBarLayout = findViewById(R.id.sportDetailTB);
        titleBarLayout.setTitleBarListener(new TitleBarLayout.TitleBarListener() {
            @Override
            public void onBackClick() {
                finish();
            }

            @Override
            public void onActionImageClick() {

            }

            @Override
            public void onActionClick() {

            }
        });

    }




//
//    }

//    //开始轨迹纠偏
//    private void startTrace() {
//        if (mOverlayList.containsKey(mSequenceLineID)) {
//            TraceOverlay overlay = mOverlayList.get(mSequenceLineID);
//            overlay.zoopToSpan();
//            int status = overlay.getTraceStatus();
//            String tipString = "";
//            if (status == TraceOverlay.TRACE_STATUS_PROCESSING) {
//                tipString = "该线路轨迹纠偏进行中...";
//                //setDistanceWaitInfo(overlay);
//            } else if (status == TraceOverlay.TRACE_STATUS_FINISH) {
//                //setDistanceWaitInfo(overlay);
//                tipString = "该线路轨迹已完成";
//            } else if (status == TraceOverlay.TRACE_STATUS_FAILURE) {
//                tipString = "该线路轨迹失败";
//            } else if (status == TraceOverlay.TRACE_STATUS_PREPARE) {
//                tipString = "该线路轨迹纠偏已经开始";
//            }
//            Toast.makeText(this.getApplicationContext(), tipString,
//                    Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        TraceOverlay mTraceOverlay = new TraceOverlay(aMap);
//        mOverlayList.put(mSequenceLineID, mTraceOverlay);
//        List<LatLng> mapList = getLanList(traceLocationList);
//        mTraceOverlay.setProperCamera(mapList);
////        mResultShow.setText(mDistanceString);
////        mLowSpeedShow.setText(mStopTimeString);
//        mTraceClient = new LBSTraceClient(AmapHistorySportActivity.this);
//        mTraceClient.queryProcessedTrace(mSequenceLineID, getTraceLocationList(),
//                mCoordinateType, this);
//
//
//    }


//    private List<TraceLocation> getTraceLocationList() {
//        List<TraceLocation> lt = new ArrayList<>();
//        for (AmapTraceLocation at : traceLocationList) {
//            TraceLocation tt = new TraceLocation(at.getLatitude(), at.getLongitude(),
//                    at.getBearing(), at.getSpeed(), at.getTime());
//            lt.add(tt);
//        }
//        return lt;
//
//
//    }

//    private List<LatLng> getLanList(List<AmapTraceLocation> tl) {
//        List<LatLng> latLngList = new ArrayList<>();
//        for (AmapTraceLocation traceLocation : tl) {
//            latLngList.add(new LatLng(traceLocation.getLatitude(), traceLocation.getLongitude()));
//        }
//        return latLngList;
//    }


    //设置属性
    private void setUpMap() {
//        String language = Locale.getDefault().getLanguage();
//
//        aMap.setMapLanguage(language.equals("zh") ? AMap.CHINESE : AMap.ENGLISH);
        // 自定义系统定位小蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory
                .fromResource(R.mipmap.gps_point));// 设置小蓝点的图标
        myLocationStyle.strokeColor(Color.BLACK);// 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.argb(100, 0, 0, 180));// 设置圆形的填充颜色
        // myLocationStyle.anchor(int,int)//设置小蓝点的锚点
        myLocationStyle.strokeWidth(1.0f);// 设置圆形的边框粗细
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setZoomControlsEnabled(false);
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (amapMapView != null)
            amapMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (amapMapView != null)
            amapMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (amapMapView != null)
            amapMapView.onDestroy();
    }



    //定位成功回调
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (mListener != null && aMapLocation != null) {
            mListener.onLocationChanged(aMapLocation);
            LatLng latLng = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
        }
    }

    //激活定位
    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        this.mListener = onLocationChangedListener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(AmapHistorySportActivity.this);
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mlocationClient.setLocationListener(this);
            //定位参数
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            mLocationOption.setNeedAddress(true);
            mLocationOption.setInterval(50 * 1000);
            mLocationOption.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.Sport);
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            //mlocationClient.stopLocation();
            mlocationClient.startLocation();
        }
    }

    //销毁定位
    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }


    //轨迹纠偏失败
    @Override
    public void onRequestFailed(int lineID, String s) {
        if (mOverlayList.containsKey(lineID)) {
            TraceOverlay overlay = mOverlayList.get(lineID);
            overlay.setTraceStatus(TraceOverlay.TRACE_STATUS_FAILURE);
            //wsetDistanceWaitInfo(overlay);
        }
    }

    //轨迹纠偏进行中
    @Override
    public void onTraceProcessing(int lineID, int i1, List<LatLng> list) {
        if (list == null)
            return;
        if (mOverlayList.containsKey(lineID)) {
            TraceOverlay overlay = mOverlayList.get(lineID);
            overlay.setTraceStatus(TraceOverlay.TRACE_STATUS_PROCESSING);
            overlay.add(list);
        }
    }

    //轨迹纠偏结束
    @Override
    public void onFinished(int lineID, List<LatLng> linepoints, int distance,
                           int watingtime) {
        Toast.makeText(this.getApplicationContext(), "onFinished",
                Toast.LENGTH_SHORT).show();
        if (mOverlayList.containsKey(lineID)) {
            TraceOverlay overlay = mOverlayList.get(lineID);
            overlay.setTraceStatus(TraceOverlay.TRACE_STATUS_FINISH);
            overlay.setDistance(distance);
            overlay.setWaitTime(watingtime);
            //setDistanceWaitInfo(overlay);
        }
    }


    private String typeSport(int type){
        if(type == 1)
            return "步行";
        if(type == 2)
            return "跑步";
        if(type == 3)
            return "骑行";
        return "跑步";
    }

    private int typeSportImg(int type){
        if(type == 1)
            return R.mipmap.icon_amap_walk;
        if(type == 2)
            return R.mipmap.icon_amap_run;
        if(type == 3)
            return R.mipmap.icon_amap_ride;
        return R.mipmap.icon_amap_walk;
    }

}
