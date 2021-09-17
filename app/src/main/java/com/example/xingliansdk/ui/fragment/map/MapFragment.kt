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
import com.example.xingliansdk.Config.eventBus.LOCATION_INFO
import com.example.xingliansdk.R
import com.example.xingliansdk.base.BaseFragment
import com.example.xingliansdk.base.viewmodel.BaseViewModel
import com.example.xingliansdk.bean.MapMotionBean
import com.example.xingliansdk.blecontent.BleConnection
import com.example.xingliansdk.eventbus.SNEventBus
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
        arguments?.let {
            mMapMotionBean = it.getParcelable("MapMotionBean")
            mStr =
                SpannableString(mMapMotionBean?.Distance?.let { it1 -> HelpUtil.getFormatter(it1) } + "公里")


            homeSportTypeTv.text= "累计"+mMapMotionBean?.let { it1 -> typeSport(it1.type) } +"距离>"

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
        tvDistance.setOnClickListener(this)


        tvGo.setOnLongClickListener {
            startActivity(Intent(context, AmapSportRecordActivity::class.java))
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
                TLog.error("点击+=${Gson().toJson(mMapMotionBean)}")
                JumpUtil.startLocationMap(activity, mMapMotionBean)


//                    startActivity(Intent(context, TestOnActivity::class.java))
            }
            R.id.tvDistance -> {
            }
            R.id.tvGoal -> {
                TLog.error("头大的点击事件")
                nav().navigateAction(R.id.action_MapFragment_to_GoalFragment)
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
        //定位
        aMap!!.setLocationSource(this) // 设置定位监听
        aMap!!.isMyLocationEnabled = true // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap!!.moveCamera(CameraUpdateFactory.zoomTo(18f))
        myLocationStyle = MyLocationStyle()
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW)
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.mipmap.gps_point))
        myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0))// 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0))// 设置圆形的填充颜色
        myLocationStyle.anchor(0.5f, 0.576f) // 这个数值是根据我的图片显示的时候计算的，最好是0.5 0.5
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
            //设置定位监听
            mlocationClient!!.setLocationListener(this)
            //设置为高精度定位模式
            mLocationOption!!.locationMode =
                AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
            //设置定位参数
            mlocationClient!!.setLocationOption(mLocationOption)
            mLocationOption!!.isOnceLocation = true
            mLocationOption!!.isGpsFirst = true
            mlocationClient!!.startLocation()

        }
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
        TLog.error("回来触发 onResume")
    }

    override fun onPause() {
        super.onPause()
        TLog.error("回来触发 onPause")
        mapView?.onPause()
    }

    override fun onDestroy() {
        //setCameraPosition(aMap?.cameraPosition)//保存地图状态
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onLocationChanged(amapLocation: AMapLocation?) {
        if (mListener != null && amapLocation != null) {
            if (amapLocation != null
                && amapLocation.errorCode == 0
            ) {
//                TLog.error("定位成功以后$amapLocation")
                mListener!!.onLocationChanged(amapLocation) // 显示系统小蓝点
                mlocationClient?.stopLocation()
//                amapLocation.city

                //发送位置信息
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
                    "定位失败," + amapLocation.errorCode + ": " + amapLocation.errorInfo
                Log.e("AmapErr", errText)
            }
        }

    }


    private fun typeSport(type: Int): String? {
        if (type == 1) return "步行"
        if (type == 2) return "跑步"
        return if (type == 3) "骑行" else "跑步"
    }

}