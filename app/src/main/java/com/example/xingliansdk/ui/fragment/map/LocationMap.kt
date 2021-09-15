package com.example.xingliansdk.ui.fragment.map

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.location.GpsSatellite
import android.location.GpsStatus
import android.location.LocationManager
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import com.amap.api.location.*
import com.amap.api.maps.*
import com.amap.api.maps.LocationSource.OnLocationChangedListener
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.PolylineOptions
import com.example.xingliansdk.Config
import com.example.xingliansdk.R
import com.example.xingliansdk.base.BaseActivity
import com.example.xingliansdk.bean.HomeCardBean
import com.example.xingliansdk.bean.MapMotionBean
import com.example.xingliansdk.bean.node.ItemExerciseRecordNode
import com.example.xingliansdk.bean.room.AppDataBase
import com.example.xingliansdk.bean.room.RoomExerciseRecordDao
import com.example.xingliansdk.eventbus.SNEvent
import com.example.xingliansdk.eventbus.SNEventBus
import com.example.xingliansdk.utils.AllGenJIDialog
import com.example.xingliansdk.utils.CountTimerUtil
import com.example.xingliansdk.utils.HelpUtil
import com.example.xingliansdk.utils.ShowToast
import com.example.xingliansdk.view.DateUtil
import com.example.xingliansdk.view.LongClickView
import com.example.xingliansdk.viewmodel.MainViewModel
import com.google.gson.Gson
import com.gyf.barlibrary.ImmersionBar
import com.orhanobut.hawk.Hawk
import com.shon.connector.utils.TLog
import kotlinx.android.synthetic.main.include_map.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList


class LocationMap : BaseActivity<MainViewModel>(), View.OnClickListener, LocationSource,
    AMapLocationListener {
    private var aMap: AMap? = null
    private var mapView: MapView? = null

    //以前的定位点
    private var oldLatLng: LatLng? = null
    private var newLatLng: LatLng? = null //新的定位

    //    private lateinit var myLocationStyle: MyLocationStyle
    //是否是第一次定位
    private var isFirstLatLng = false
    private var mListener: OnLocationChangedListener? = null
    private var mlocationClient: AMapLocationClient? = null
    private var mLocationOption: AMapLocationClientOption? = null
    var mRecordTime = 0L
    var mUiSettings: UiSettings? = null
    private lateinit var mMapMotionBean: MapMotionBean

    //gps
    private var locationManager: LocationManager? = null
    private lateinit var mExerciseDao: RoomExerciseRecordDao
    var date = System.currentTimeMillis()
    override fun layoutId() = R.layout.include_map

    override fun initView(savedInstanceState: Bundle?) {
        mapView =
            findViewById<View>(R.id.map) as MapView
        mapView!!.onCreate(savedInstanceState) // 此方法必须重写
        ImmersionBar.with(this)
            .titleBar(titleBar)
            .init()
        SNEventBus.register(this)
        mExerciseDao = AppDataBase.instance.getItemExerciseRecordNode()
        mMapMotionBean = intent.getSerializableExtra("MapMotionBean") as MapMotionBean
        constMap.visibility=View.GONE
        CountTimerUtil.start(tvNumberAnim,object: CountTimerUtil.AnimationState {
            override fun start() {
            }

            override fun end() {
                constMap.visibility=View.VISIBLE
            }

            override fun repeat() {
            }
        })
        isFirstLatLng = true
        timer.base = SystemClock.elapsedRealtime()//计时器清零
        timer.start()
        init()
        getGps()
        tvStop.setOnClickListener(this)
        tvGoOn.setOnClickListener(this)
//        img_start.setOnClickListener {
//            val markerOptions = MarkerOptions()
//                .position(oldLatLng)
//                .draggable(false)
//                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.end))
//            aMap!!.addMarker(markerOptions)
//            if (mRecordTime != 0L)
//                timer.base = timer.base + (SystemClock.elapsedRealtime() - mRecordTime)
//            else
//                timer.base = SystemClock.elapsedRealtime()
//            timer.start()
//            TLog.error("SystemClock+=" + SystemClock.elapsedRealtime() + ",   getBase+=" + timer.base)
//        }
//        longSave.setOnClickListener(this)
        longSave.setMyClickListener(object : LongClickView.MyClickListener {
            override fun singleClickFinish() {

            }

            override fun longClickFinish() {
                TLog.error("runOnUiThread" + Gson().toJson(mRoomExerciseBean))
                runOnUiThread {
                    TLog.error("===" + Gson().toJson(mRoomExerciseBean))
                //    mExerciseDao.insert(mRoomExerciseBean!!)
                    if (mRoomExerciseBean == null ||mRoomExerciseBean?.distance==null|| mRoomExerciseBean?.distance?.toDouble()!! < 0.2) {
                        finish()
                        ShowToast.showToastLong(" 本次运动距离过短,将不会记录数据")
                   //     AllGenJIDialog.mapDialog(supportFragmentManager)
                    } else {
                        if (mHomeCardBean.addCard != null && mHomeCardBean.addCard.size > 0) {

                            mHomeCardBean.addCard.forEachIndexed { index, addCardDTO ->
                                if (addCardDTO.type == 0) {
                                    mHomeCardBean.addCard[index].time =
                                        System.currentTimeMillis() / 1000
                                    mHomeCardBean.addCard[index].dayContent =
                                        mRoomExerciseBean?.distance
                                    mHomeCardBean.addCard[index].dayContentString = "公里"
                                    Hawk.put(Config.database.HOME_CARD_BEAN, mHomeCardBean)
                                    SNEventBus.sendEvent(Config.eventBus.MAP_MOVEMENT_SATISFY)
                                }
                            }
                            mExerciseDao.insert(mRoomExerciseBean!!)

                        } else
                            ShowToast.showToastLong("数据异常,保存失败")
                        finish()
                    }


                }
                TLog.error("长按结束完成回调  longClickFinish")
            }

        })
//        img_stop.setOnClickListener {
//            TLog.error("????")
//            timer.stop()
//            mRecordTime = SystemClock.elapsedRealtime()
//            mapView!!.onPause()
//            deactivate()
//        }

    }

    private fun init() {
        if (aMap == null) {
            aMap = mapView!!.map
            mUiSettings = aMap?.uiSettings
            mUiSettings?.isZoomControlsEnabled = false
        }
        //定位
        aMap!!.setLocationSource(this) // 设置定位监听
        aMap!!.uiSettings.isMyLocationButtonEnabled = true // 设置默认定位按钮是否显示
        aMap!!.isMyLocationEnabled = true // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // 设置定位的类型为定位模式 ，可以由定位 LOCATION_TYPE_LOCATE、跟随 LOCATION_TYPE_MAP_FOLLOW 或地图根据面向方向旋转 LOCATION_TYPE_MAP_ROTATE
//        aMap!!.setMyLocationType(AMap.LOCATION_TYPE_MAP_FOLLOW)

        //画线
        // 缩放级别（zoom）：地图缩放级别范围为【4-20级】，值越大地图越详细
        aMap!!.moveCamera(CameraUpdateFactory.zoomTo(18f))
        //使用 aMap.setMapTextZIndex(2) 可以将地图底图文字设置在添加的覆盖物之上
        aMap!!.mapTextZIndex = 2

//        myLocationStyle =
//            MyLocationStyle()
//        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW)
        //自定义图标
//        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.mipmap.gps_point))
//        aMap!!.myLocationStyle =myLocationStyle

//        lbsTraceClient= LBSTraceClient.getInstance(this)
//        lbsTraceClient?.startTrace(this)
    }

    @SuppressLint("MissingPermission")
    private fun getGps() {
        if (locationManager == null) {
            locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        }
        locationManager?.addGpsStatusListener(GpsStatus.Listener {
            when (it) {
                GpsStatus.GPS_EVENT_FIRST_FIX -> {
                }
                GpsStatus.GPS_EVENT_SATELLITE_STATUS -> {
                    var count = 0
                    var canUse = 0
                    try {
                        //获取当前状态
                        val gpsStatus =
                            locationManager!!.getGpsStatus(null)
                        //获取卫星颗数的默认最大值
                        val maxSatellites = gpsStatus!!.maxSatellites
                        //创建一个迭代器保存所有卫星
                        val iters: Iterator<GpsSatellite> =
                            gpsStatus!!.satellites.iterator()
                        while (iters.hasNext() && count <= maxSatellites) {
                            val s: GpsSatellite = iters.next()
                            val snr: Float = s.snr
                            if (snr > 30) {
                                canUse++//信号弱或无信号

                                //表示有信号
                                when {
                                    canUse >= 10 -> {
                                        //满信号
                                    }
                                    canUse >= 6 -> {
                                        //强
                                    }
                                    canUse >= 4 -> {
                                        //中
                                    }
                                    canUse < 4 -> {
                                        //弱
                                    }
                                }
                                if (canUse >= 4) {
                                    //表示有信号
                                } else {
                                    //信号弱或无信号
                                }
                            }
                            count++
                        }
                    } catch (e: SecurityException) {
                        e.printStackTrace()
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                }
                GpsStatus.GPS_EVENT_STARTED -> {
                }
                GpsStatus.GPS_EVENT_STOPPED -> {
                }
            }
        })
    }

    /**绘制两个坐标点之间的线段,从以前位置到现在位置 */
    private fun setUpMap(oldData: LatLng?, newData: LatLng) {
        // 绘制一个大地曲线
        TLog.error("绘制")
        aMap!!.addPolyline(
            PolylineOptions()
                .add(oldData, newData)
                .geodesic(true).color(Color.GREEN)
        )
    }

    private var mPoint: DPoint? = null
    private var newPoint: DPoint? = null
    var distance2 = 0f
    var distance3 = 0f
    var difference=0
    /**
     * 定位成功后回调函数
     */
    override fun onLocationChanged(amapLocation: AMapLocation) {
//        TLog.error("回调过后的amapLocation+"+amapLocation)

        if (mListener != null && amapLocation != null) {
            if (amapLocation != null
                && amapLocation.errorCode == 0
            ) {
                mListener!!.onLocationChanged(amapLocation) // 显示系统小蓝点
                if(difference<3) {  //排除前面三次的 误差性能
                    difference++
                    return
                }
                TLog.error("位置有变化+" + amapLocation)
                newLatLng =
                    LatLng(amapLocation.latitude, amapLocation.longitude) //高德自己的算法
                if (isFirstLatLng) {
                    //记录第一次的定位信息
                    val markerOptions = MarkerOptions()
                        .position(newLatLng)
                        .draggable(false)
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.start))
                    aMap!!.addMarker(markerOptions)
                    oldLatLng = newLatLng
                    isFirstLatLng = false
                }
                //位置有变化
                if (oldLatLng !== newLatLng) {
                    mPoint = DPoint(oldLatLng!!.latitude, oldLatLng!!.longitude)
                    newPoint = DPoint(newLatLng!!.latitude, newLatLng!!.longitude)
                    // var distance=CoordinateConverter.calculateLineDistance(mPoint,newPoint)
                    distance2 = AMapUtils.calculateLineDistance(oldLatLng, newLatLng)
                    if(distance2>5&&mMapMotionBean.type==0
                        ||distance2>20&&mMapMotionBean.type==1
                        ||distance2>15&&mMapMotionBean.type==2
                    )
                        return
                    distance3 += distance2
                    TLog.error("距离++$distance2  ")
                    //           TLog.error("累计距离+=$distance3")
                    getExercise()
                    if (distance2 > 0) {
                        // getBig()
                        setUpMap(oldLatLng, newLatLng!!)
                    }
                    oldLatLng = newLatLng
                }
            } else {
                val errText =
                    "定位失败," + amapLocation.errorCode + ": " + amapLocation.errorInfo
                Log.e("AmapErr", errText)
                //                Toast.makeText(this, errText, Toast.LENGTH_SHORT).show();
                if (isFirstLatLng) {
                    Toast.makeText(this, errText, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    var mRoomExerciseBean: ItemExerciseRecordNode = ItemExerciseRecordNode()
    private fun getExercise() {
        var df = DecimalFormat("######0.00")
        val time = (SystemClock.elapsedRealtime() - timer.base)
//        TLog.error("获取当前运动时间++$time")
        var type = Config.exercise.RUN

        when (mMapMotionBean.type) {
            0 -> type = Config.exercise.WALK
            1 -> type = Config.exercise.RUN
            2 -> type = Config.exercise.BICYCLE
            3 -> type = Config.exercise.MOUNTAIN_CLIMBING
            //运动系数(健走：k=0.8214
            //跑步：k=1.036
            //自行车：k=0.6142
            //轮滑、溜冰：k=0.518
            //室外滑雪：k=0.888
        }

        //距离
        var distance = df.format(distance3 / 1000)
        var weight = mDeviceInformationBean.weight.toInt()
        if (weight <= 0)
            weight = 60
        val calories = df.format(distance3 * weight * type / 1000)
        val averageSpeed = df.format(distance3 / time * 3600L)
        val pace = df.format((time / 1000L / (distance3)))
//           TLog.error("配速time++$time  距离++$distance3")
//          TLog.error("配速++$pace")

//        TLog.error("=="+HelpUtil.getFormatter(60.toDouble()/10.toDouble()))
        mRoomExerciseBean =
            ItemExerciseRecordNode(
                distance,
                calories,
                averageSpeed,
                pace,
                time,
                type,
                date,
                DateUtil.getDate(DateUtil.YYYY_MM_DD_AND, date)
            )
        tvDistance.text = "$distance  km"
        //tvAverageSpeed.text = "${averageSpeed} km/h \n平均速度"
        val paceList = pace.toString().split(".")
        for (i in paceList.indices) {
//           TLog.error("paceList+="+paceList[i])
        }
        if (paceList.size > 1)
            tvPace.text = "${paceList[0]}'${paceList[1]}"
        else
            tvPace.text = "00'00\""
        tvCalories.text = HelpUtil.getSpan(calories.toString(), "千卡", 11)
    }

    /**
     * 激活定位
     */
    override fun activate(listener: OnLocationChangedListener) {
        mListener = listener
        if (mlocationClient == null) {
            TLog.error("只走了一次吧")
            mlocationClient = AMapLocationClient(this)
            mLocationOption = AMapLocationClientOption()
            //设置定位监听
            mlocationClient!!.setLocationListener(this)
            //设置为高精度定位模式
            mLocationOption!!.locationMode =
                AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
            //设置定位参数
            mlocationClient!!.setLocationOption(mLocationOption)
            mLocationOption!!.isOnceLocation = true
            /**
             * 设置是否优先返回GPS定位结果，如果30秒内GPS没有返回定位结果则进行网络定位
             * 注意：只有在高精度模式下的单次定位有效，其他方式无效
             */
            mLocationOption!!.isGpsFirst = true
            // 设置发送定位请求的时间间隔,最小值为1000ms,1秒更新一次定位信息
            mLocationOption!!.interval = 10000
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient!!.startLocation()
        }
    }

    /**
     * 停止定位
     */
    override fun deactivate() {
        mListener = null
        if (mlocationClient != null) {
            mlocationClient!!.stopLocation()
            mlocationClient!!.onDestroy()
        }
        mlocationClient = null
    }

    /**
     * 方法必须重写
     */
    override fun onResume() {
        super.onResume()
        mapView!!.onResume()
        TLog.error("息屏操作以后==onResume")
    }

    /**
     * 方法必须重写
     */
    override fun onPause() {
        super.onPause()
        //   mapView!!.onPause()
        // deactivate()
        TLog.error("息屏操作以后==onPause")
    }

    /**
     * 方法必须重写
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView!!.onSaveInstanceState(outState)
        TLog.error("息屏操作以后==onSaveInstanceState")
    }

    /**
     * 方法必须重写
     */
    override fun onDestroy() {
        super.onDestroy()
        TLog.error("息屏操作以后==onDestroy")
        mapView!!.onDestroy()
        mapView!!.onPause()
        deactivate()
        if (null != mlocationClient) {
            mlocationClient!!.onDestroy()
        }
        SNEventBus.unregister(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tvStop -> {
                timer.stop()
                mRecordTime = SystemClock.elapsedRealtime()
                //   mapView!!.onPause()
                mlocationClient?.stopLocation()
                longSave.visibility = View.VISIBLE
                tvGoOn.visibility = View.VISIBLE
                tvStop.visibility = View.GONE
            }
            R.id.tvGoOn -> {
                longSave.visibility = View.GONE
                tvGoOn.visibility = View.GONE
                tvStop.visibility = View.VISIBLE
                mlocationClient?.startLocation()
                // mapView?.
//                            val markerOptions = MarkerOptions()
//                .position(oldLatLng)
//                .draggable(false)
//                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.end))
//            aMap!!.addMarker(markerOptions)
                if (mRecordTime != 0L)
                    timer.base = timer.base + (SystemClock.elapsedRealtime() - mRecordTime)
                else
                    timer.base = SystemClock.elapsedRealtime()
                timer.start()
            }
//            R.id.longSave->
//            {
//                TLog.error("===" + Gson().toJson(mRoomExerciseBean))
//                if (mRoomExerciseBean == null ||mRoomExerciseBean?.distance==null|| mRoomExerciseBean?.distance?.toDouble()!! < 0.2) {
//                 //   AllGenJIDialog.mapDialog(supportFragmentManager)
//                    finish()
//                } else {
//                    if (mHomeCardBean.addCard != null && mHomeCardBean.addCard.size > 0) {
//                        ShowToast.showToastLong("运动结束,以为你保存数据")
//                        mHomeCardBean.addCard.forEachIndexed { index, addCardDTO ->
//                            if (addCardDTO.type == 0) {
//                                mHomeCardBean.addCard[index].time =
//                                    System.currentTimeMillis() / 1000
//                                mHomeCardBean.addCard[index].dayContent =
//                                    mRoomExerciseBean?.distance
//                                mHomeCardBean.addCard[index].dayContentString = "公里"
//                                Hawk.put(Config.database.HOME_CARD_BEAN, mHomeCardBean)
//                                SNEventBus.sendEvent(Config.eventBus.MAP_MOVEMENT_SATISFY)
//                            }
//                        }
//                        mExerciseDao.insert(mRoomExerciseBean!!)
//
//                    } else
//                        ShowToast.showToastLong("数据异常,保存失败")
//                    finish()
//                }
//            }

        }
    }

//    override fun onRequestFailed(lineID: Int, errorInfo: String?) {
//        Toast.makeText(
//            this.applicationContext, errorInfo,
//            Toast.LENGTH_SHORT
//        ).show()
//        if (mOverlayList.containsKey(lineID)) {
//            val overlay: TraceOverlay = mOverlayList.get(lineID)
//            overlay.traceStatus = TraceOverlay.TRACE_STATUS_FAILURE
//            setDistanceWaitInfo(overlay)
//        }
//    }
//
//    override fun onTraceProcessing(lineID: Int, index: Int, segments: MutableList<LatLng>?) {
//       TLog.log( "onTraceProcessing")
//        if (segments == null) {
//            return
//        }
//        if (mOverlayList.containsKey(lineID)) {
//            val overlay: TraceOverlay = mOverlayList.get(lineID)
//            overlay.traceStatus = TraceOverlay.TRACE_STATUS_PROCESSING
//            overlay.add(segments)
//        }
//    }
//
//    override fun onFinished(lineID: Int, linepoints: MutableList<LatLng>?, distance: Int, waitingtime: Int) {
//        Toast.makeText(
//            this.applicationContext, "onFinished",
//            Toast.LENGTH_SHORT
//        ).show()
//        if (mOverlayList.containsKey(lineID)) {
//            val overlay: TraceOverlay = mOverlayList.get(lineID)
//            overlay.traceStatus = TraceOverlay.TRACE_STATUS_FINISH
//            overlay.distance = distance
//            overlay.waitTime = waitingtime
//            setDistanceWaitInfo(overlay)
//        }
//    }
//
//    override fun onTraceStatus(
//        locations: MutableList<TraceLocation>?,
//        rectifications: MutableList<LatLng>?,
//        errorInfo: String?
//    ) {
//        //locations 定位得到的轨迹点集，rectifications 纠偏后的点集，errorInfo 轨迹纠偏错误信息
//    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventReceived(event: SNEvent<Any>) {
        when (event.code) {
            Config.eventBus.MAP_MOVEMENT_DISSATISFY -> {
                TLog.error("来了老弟")
                finish()
            }
        }
    }
        //无效返回键
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
         return false
    }
}