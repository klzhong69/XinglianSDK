package com.example.xingliansdk.ui.fragment.map

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.util.Log
import android.view.View
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.*
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.MyLocationStyle
import com.example.db.DbManager
import com.example.xingliansdk.Config
import com.example.xingliansdk.Config.eventBus.LOCATION_INFO
import com.example.xingliansdk.R
import com.example.xingliansdk.base.BaseFragment
import com.example.xingliansdk.base.viewmodel.BaseViewModel
import com.example.xingliansdk.bean.MapMotionBean
import com.example.xingliansdk.blecontent.BleConnection
import com.example.xingliansdk.eventbus.SNEventBus
import com.example.xingliansdk.network.api.login.LoginBean
import com.example.xingliansdk.ui.fragment.map.view.RunningPresenterImpl
import com.example.xingliansdk.utils.HelpUtil
import com.example.xingliansdk.utils.JumpUtil
import com.google.gson.Gson
import com.orhanobut.hawk.Hawk
import com.shon.connector.utils.TLog
import kotlinx.android.synthetic.main.fragment_movement_type.*
import me.hgj.jetpackmvvm.ext.nav
import me.hgj.jetpackmvvm.ext.navigateAction


class MapFragment : BaseFragment<BaseViewModel>(), View.OnClickListener, LocationSource,
    AMapLocationListener {

    private val tags = "MapFragment"

    override fun layoutId(): Int = R.layout.fragment_movement_type
    lateinit var mStr: SpannableString
    var mMapMotionBean: MapMotionBean? = null
    private var aMap: AMap? = null
    private var mapView: TextureMapView? = null
    var mUiSettings: UiSettings? = null
    private var mListener: LocationSource.OnLocationChangedListener? = null
    private var mlocationClient: AMapLocationClient? = null
    private var mLocationOption: AMapLocationClientOption? = null
    private lateinit var myLocationStyle: MyLocationStyle
    private var tempStatus = false




    override fun initView(savedInstanceState: Bundle?) {
        Log.e(tag,"----111111-----")

        arguments?.let {
            mMapMotionBean = it.getParcelable("MapMotionBean")
            Log.e(tags,"mMapMotionBean="+Gson().toJson(mMapMotionBean))
            mStr =
                SpannableString(mMapMotionBean?.Distance?.let { it1 -> HelpUtil.getFormatter(it1) } + "??????")



        }
        mapView = view?.findViewById<View>(R.id.map) as TextureMapView
        mapView!!.onCreate(savedInstanceState)
        //init()
        mStr.setSpan(
            AbsoluteSizeSpan(20, true),
            mStr.length - 2,
            mStr.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        tvDistance.text = mStr
        tvGo.setOnClickListener(this)
        tvGoal.setOnClickListener(this)
        homeSportLayout.setOnClickListener(this)
        tvDistance.setOnClickListener(this)


        tvGo.setOnLongClickListener {
           //startActivity(Intent(context, AmapSportRecordActivity::class.java))

            //???????????????????????????

            //????????????????????????id,?????????id?????????   var userInfo = Hawk.get<LoginBean>(Config.database.USER_INFO)
            val loginBean = Hawk.get<LoginBean>(Config.database.USER_INFO)
            val userId = loginBean!!.user.userId
            DbManager.getDbManager().queryALlTotalDistance(userId)
            true
        }
    }

    override fun createObserver() {
        super.createObserver()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

    companion object {
        fun newInstance(cid: Int, mDistance: MapMotionBean): MapFragment {
            val args = Bundle()
            args.putInt("cid", cid)
            args.putSerializable("MapMotionBean", mDistance)
            val fragment = MapFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tvGo -> {
                TLog.error("??????+=${Gson().toJson(mMapMotionBean)}")
                JumpUtil.startLocationMap(activity, mMapMotionBean)


//                    startActivity(Intent(context, TestOnActivity::class.java))
            }
            R.id.tvDistance -> {
            }
            R.id.tvGoal -> {
                TLog.error("?????????????????????")
                nav().navigateAction(R.id.action_MapFragment_to_GoalFragment)
            }


            //???????????????
            R.id.homeSportLayout->{
//                startActivity(Intent(context,AmapSportRecordActivity::class.java))

               val intent = Intent(context,AmapSportRecordActivity::class.java)
                mMapMotionBean?.let { intent.putExtra("sportType", it.type) }
                startActivity(intent)
            }
        }
    }

    private fun init() {
        if (aMap == null) {
            aMap = mapView!!.map
            mUiSettings = aMap?.uiSettings
        }
        mUiSettings?.isZoomControlsEnabled = false
        mUiSettings?.setAllGesturesEnabled(false)
        mUiSettings?.isZoomGesturesEnabled = false
        //??????
        aMap!!.setLocationSource(this) // ??????????????????
        aMap!!.isMyLocationEnabled = true // ?????????true??????????????????????????????????????????false??????????????????????????????????????????????????????false
        aMap!!.moveCamera(CameraUpdateFactory.zoomTo(18f))
        myLocationStyle = MyLocationStyle()
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW)
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.mipmap.gps_point))
        myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0))// ???????????????????????????
        myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0))// ???????????????????????????
        myLocationStyle.anchor(0.5f, 0.576f) // ?????????????????????????????????????????????????????????????????????0.5 0.5
        aMap!!.myLocationStyle = myLocationStyle
    }

    override fun deactivate() {
//        mListener = null
//        mlocationClient?.stopLocation()
//        mlocationClient?.onDestroy()

    }

    override fun activate(listener: LocationSource.OnLocationChangedListener?) {
        mListener = listener
        if (mlocationClient == null) {
            mlocationClient = AMapLocationClient(activity)
            mLocationOption = AMapLocationClientOption()
            //??????????????????
            mlocationClient!!.setLocationListener(this)
            //??????????????????????????????
            mLocationOption!!.locationMode =
                AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
            //??????????????????
            mlocationClient!!.setLocationOption(mLocationOption)
            mLocationOption!!.isOnceLocation = true
            mLocationOption!!.isGpsFirst = true
            mlocationClient!!.startLocation()

        }
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
        TLog.error("???????????? onResume")
        Log.e(tags,"-----mMapMotionBean="+Gson().toJson(mMapMotionBean))

        //????????????
        homeSportTypeTv.text= "??????"+mMapMotionBean?.let { it1 -> typeSport(it1.type) } +"??????>"
        //????????????
        homeSportCountTv.text = mMapMotionBean?.let { it1 -> typeSportDistance(it1.type) }
    }

    override fun onPause() {
        super.onPause()
        TLog.error("???????????? onPause")
        mapView?.onPause()
    }

    override fun onDestroy() {
        //setCameraPosition(aMap?.cameraPosition)//??????????????????
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onLocationChanged(amapLocation: AMapLocation?) {
        if (mListener != null && amapLocation != null) {
            if (amapLocation != null
                && amapLocation.errorCode == 0
            ) {
//                TLog.error("??????????????????$amapLocation")
                mListener!!.onLocationChanged(amapLocation) // ?????????????????????
                mlocationClient?.stopLocation()
//                amapLocation.city

                //??????????????????
                Hawk.put("city", amapLocation.district)
                if (BleConnection.iFonConnectError && !tempStatus) {
                    tempStatus = true
                    SNEventBus.sendEvent(
                        LOCATION_INFO,
                        "${amapLocation.longitude},${amapLocation.latitude}"
                    )
                }
            } else {
                val errText =
                    "????????????," + amapLocation.errorCode + ": " + amapLocation.errorInfo
                Log.e("AmapErr", errText)
            }
        }

    }


    private fun typeSport(type: Int): String? {
        if (type == 0) return "??????"
        if (type == 1) return "??????"
        return if (type == 2) "??????" else "??????"
    }

    //??????????????????????????????
    private fun typeSportDistance(type : Int):String?{
        if(type == 0) return Hawk.get(Config.database.WALK_DISTANCE_KEY,"0.0")
        if(type == 1) return Hawk.get(Config.database.RUN_DISTANCE_KEY,"0.0")
        return if(type == 2) Hawk.get(Config.database.BIKE_DISTANCE_KEY,"0.0") else Hawk.get(Config.database.WALK_DISTANCE_KEY,"0.0")
    }

}