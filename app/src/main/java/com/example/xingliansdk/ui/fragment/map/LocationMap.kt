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

    //??????????????????
    private var oldLatLng: LatLng? = null
    private var newLatLng: LatLng? = null //????????????

    //    private lateinit var myLocationStyle: MyLocationStyle
    //????????????????????????
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
        mapView!!.onCreate(savedInstanceState) // ?????????????????????
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
        timer.base = SystemClock.elapsedRealtime()//???????????????
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
                        ShowToast.showToastLong(" ????????????????????????,?????????????????????")
                   //     AllGenJIDialog.mapDialog(supportFragmentManager)
                    } else {
                        if (mHomeCardBean.addCard != null && mHomeCardBean.addCard.size > 0) {

                            mHomeCardBean.addCard.forEachIndexed { index, addCardDTO ->
                                if (addCardDTO.type == 0) {
                                    mHomeCardBean.addCard[index].time =
                                        System.currentTimeMillis() / 1000
                                    mHomeCardBean.addCard[index].dayContent =
                                        mRoomExerciseBean?.distance
                                    mHomeCardBean.addCard[index].dayContentString = "??????"
                                    Hawk.put(Config.database.HOME_CARD_BEAN, mHomeCardBean)
                                    SNEventBus.sendEvent(Config.eventBus.MAP_MOVEMENT_SATISFY)
                                }
                            }
                            mExerciseDao.insert(mRoomExerciseBean!!)

                        } else
                            ShowToast.showToastLong("????????????,????????????")
                        finish()
                    }


                }
                TLog.error("????????????????????????  longClickFinish")
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
        //??????
        aMap!!.setLocationSource(this) // ??????????????????
        aMap!!.uiSettings.isMyLocationButtonEnabled = true // ????????????????????????????????????
        aMap!!.isMyLocationEnabled = true // ?????????true??????????????????????????????????????????false??????????????????????????????????????????????????????false
        // ???????????????????????????????????? ?????????????????? LOCATION_TYPE_LOCATE????????? LOCATION_TYPE_MAP_FOLLOW ????????????????????????????????? LOCATION_TYPE_MAP_ROTATE
//        aMap!!.setMyLocationType(AMap.LOCATION_TYPE_MAP_FOLLOW)

        //??????
        // ???????????????zoom????????????????????????????????????4-20?????????????????????????????????
        aMap!!.moveCamera(CameraUpdateFactory.zoomTo(18f))
        //?????? aMap.setMapTextZIndex(2) ????????????????????????????????????????????????????????????
        aMap!!.mapTextZIndex = 2

//        myLocationStyle =
//            MyLocationStyle()
//        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW)
        //???????????????
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
                        //??????????????????
                        val gpsStatus =
                            locationManager!!.getGpsStatus(null)
                        //????????????????????????????????????
                        val maxSatellites = gpsStatus!!.maxSatellites
                        //???????????????????????????????????????
                        val iters: Iterator<GpsSatellite> =
                            gpsStatus!!.satellites.iterator()
                        while (iters.hasNext() && count <= maxSatellites) {
                            val s: GpsSatellite = iters.next()
                            val snr: Float = s.snr
                            if (snr > 30) {
                                canUse++//?????????????????????

                                //???????????????
                                when {
                                    canUse >= 10 -> {
                                        //?????????
                                    }
                                    canUse >= 6 -> {
                                        //???
                                    }
                                    canUse >= 4 -> {
                                        //???
                                    }
                                    canUse < 4 -> {
                                        //???
                                    }
                                }
                                if (canUse >= 4) {
                                    //???????????????
                                } else {
                                    //?????????????????????
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

    /**????????????????????????????????????,?????????????????????????????? */
    private fun setUpMap(oldData: LatLng?, newData: LatLng) {
        // ????????????????????????
        TLog.error("??????")
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
     * ???????????????????????????
     */
    override fun onLocationChanged(amapLocation: AMapLocation) {
//        TLog.error("???????????????amapLocation+"+amapLocation)

        if (mListener != null && amapLocation != null) {
            if (amapLocation != null
                && amapLocation.errorCode == 0
            ) {
                mListener!!.onLocationChanged(amapLocation) // ?????????????????????
                if(difference<3) {  //????????????????????? ????????????
                    difference++
                    return
                }
                TLog.error("???????????????+" + amapLocation)
                newLatLng =
                    LatLng(amapLocation.latitude, amapLocation.longitude) //?????????????????????
                if (isFirstLatLng) {
                    //??????????????????????????????
                    val markerOptions = MarkerOptions()
                        .position(newLatLng)
                        .draggable(false)
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.start))
                    aMap!!.addMarker(markerOptions)
                    oldLatLng = newLatLng
                    isFirstLatLng = false
                }
                //???????????????
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
                    TLog.error("??????++$distance2  ")
                    //           TLog.error("????????????+=$distance3")
                    getExercise()
                    if (distance2 > 0) {
                        // getBig()
                        setUpMap(oldLatLng, newLatLng!!)
                    }
                    oldLatLng = newLatLng
                }
            } else {
                val errText =
                    "????????????," + amapLocation.errorCode + ": " + amapLocation.errorInfo
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
//        TLog.error("????????????????????????++$time")
        var type = Config.exercise.RUN

        when (mMapMotionBean.type) {
            0 -> type = Config.exercise.WALK
            1 -> type = Config.exercise.RUN
            2 -> type = Config.exercise.BICYCLE
            3 -> type = Config.exercise.MOUNTAIN_CLIMBING
            //????????????(?????????k=0.8214
            //?????????k=1.036
            //????????????k=0.6142
            //??????????????????k=0.518
            //???????????????k=0.888
        }

        //??????
        var distance = df.format(distance3 / 1000)
        var weight = mDeviceInformationBean.weight.toInt()
        if (weight <= 0)
            weight = 60
        val calories = df.format(distance3 * weight * type / 1000)
        val averageSpeed = df.format(distance3 / time * 3600L)
        val pace = df.format((time / 1000L / (distance3)))
//           TLog.error("??????time++$time  ??????++$distance3")
//          TLog.error("??????++$pace")

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
        //tvAverageSpeed.text = "${averageSpeed} km/h \n????????????"
        val paceList = pace.toString().split(".")
        for (i in paceList.indices) {
//           TLog.error("paceList+="+paceList[i])
        }
        if (paceList.size > 1)
            tvPace.text = "${paceList[0]}'${paceList[1]}"
        else
            tvPace.text = "00'00\""
        tvCalories.text = HelpUtil.getSpan(calories.toString(), "??????", 11)
    }

    /**
     * ????????????
     */
    override fun activate(listener: OnLocationChangedListener) {
        mListener = listener
        if (mlocationClient == null) {
            TLog.error("??????????????????")
            mlocationClient = AMapLocationClient(this)
            mLocationOption = AMapLocationClientOption()
            //??????????????????
            mlocationClient!!.setLocationListener(this)
            //??????????????????????????????
            mLocationOption!!.locationMode =
                AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
            //??????????????????
            mlocationClient!!.setLocationOption(mLocationOption)
            mLocationOption!!.isOnceLocation = true
            /**
             * ????????????????????????GPS?????????????????????30??????GPS?????????????????????????????????????????????
             * ??????????????????????????????????????????????????????????????????????????????
             */
            mLocationOption!!.isGpsFirst = true
            // ???????????????????????????????????????,????????????1000ms,1???????????????????????????
            mLocationOption!!.interval = 10000
            // ????????????????????????????????????????????????????????????????????????????????????????????????????????????
            // ??????????????????????????????????????????????????????????????????2000ms?????????????????????????????????stopLocation()???????????????????????????
            // ???????????????????????????????????????????????????onDestroy()??????
            // ?????????????????????????????????????????????????????????????????????stopLocation()???????????????????????????sdk???????????????
            mlocationClient!!.startLocation()
        }
    }

    /**
     * ????????????
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
     * ??????????????????
     */
    override fun onResume() {
        super.onResume()
        mapView!!.onResume()
        TLog.error("??????????????????==onResume")
    }

    /**
     * ??????????????????
     */
    override fun onPause() {
        super.onPause()
        //   mapView!!.onPause()
        // deactivate()
        TLog.error("??????????????????==onPause")
    }

    /**
     * ??????????????????
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView!!.onSaveInstanceState(outState)
        TLog.error("??????????????????==onSaveInstanceState")
    }

    /**
     * ??????????????????
     */
    override fun onDestroy() {
        super.onDestroy()
        TLog.error("??????????????????==onDestroy")
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
//                        ShowToast.showToastLong("????????????,?????????????????????")
//                        mHomeCardBean.addCard.forEachIndexed { index, addCardDTO ->
//                            if (addCardDTO.type == 0) {
//                                mHomeCardBean.addCard[index].time =
//                                    System.currentTimeMillis() / 1000
//                                mHomeCardBean.addCard[index].dayContent =
//                                    mRoomExerciseBean?.distance
//                                mHomeCardBean.addCard[index].dayContentString = "??????"
//                                Hawk.put(Config.database.HOME_CARD_BEAN, mHomeCardBean)
//                                SNEventBus.sendEvent(Config.eventBus.MAP_MOVEMENT_SATISFY)
//                            }
//                        }
//                        mExerciseDao.insert(mRoomExerciseBean!!)
//
//                    } else
//                        ShowToast.showToastLong("????????????,????????????")
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
//        //locations ??????????????????????????????rectifications ?????????????????????errorInfo ????????????????????????
//    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventReceived(event: SNEvent<Any>) {
        when (event.code) {
            Config.eventBus.MAP_MOVEMENT_DISSATISFY -> {
                TLog.error("????????????")
                finish()
            }
        }
    }
        //???????????????
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
         return false
    }
}