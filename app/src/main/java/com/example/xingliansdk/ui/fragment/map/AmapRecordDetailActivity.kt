package com.example.xingliansdk.ui.fragment.map

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.LocationSource
import com.amap.api.maps.MapView
import com.amap.api.maps.model.*
import com.example.db.AmapSportBean
import com.example.xingliansdk.R
import com.example.xingliansdk.widget.TitleBarLayout
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_amap_sport_detail_layout.*
import java.io.Serializable


/**
 * Created by Admin
 *Date 2021/9/13
 */
class AmapRecordDetailActivity : AppCompatActivity() , LocationSource, AMapLocationListener {

    private var amapView : MapView ? = null
    private var amap : AMap ? = null

    private var mListener: LocationSource.OnLocationChangedListener? = null
    private var mlocationClient: AMapLocationClient? = null
    private var mLocationOption: AMapLocationClientOption? = null


    private var sportDetailTB : TitleBarLayout ?= null

    private var startMark: Marker? = null
    private  var endMark:Marker? = null

    var polyline: Polyline? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_amap_sport_detail_layout)

        amapView = findViewById(R.id.amapDetailMapView)

        amapView?.onCreate(savedInstanceState)

        if(amap == null){
            amap = amapView?.map;
            setUpMap()
        }


        initViews();

        showData();
    }


    private fun showData(){
        try {
            val amapSportInfo: Serializable? = intent.getSerializableExtra(" sport_position") ?: return

            val gson  = Gson()
            val amapSportBean = gson.fromJson(amapSportInfo, AmapSportBean::class.java)

            val latLngStr = amapSportBean.latLonArrayStr;

            val lanList = gson.fromJson<List<LatLng>>(latLngStr)

            //找到第一个和最后一个点，标记开始和结束的位置
            //开始和结束的位置标记

            //开始和结束的位置标记
            val startLng = lanList[0]

            //将地图视图移动到运动的轨迹点
            amap?.moveCamera(CameraUpdateFactory.newLatLngZoom(startLng, 18F))

            val markerOptions: MarkerOptions =
                MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.start))
                    .position(startLng)
                    .draggable(true)
            startMark = amap?.addMarker(markerOptions)
            startMark?.isDraggable = false


            val endLng = lanList[lanList.size - 1]
            val markerOption: MarkerOptions =
                MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.end))
                    .position(endLng)
                    .draggable(true)
            endMark = amap?.addMarker(markerOption)
            endMark?.isDraggable = false

            polyline = amap?.addPolyline(PolylineOptions().addAll(lanList).color(Color.GREEN).width(8f))
        }catch (e : Exception){
            e.printStackTrace()
        }


    }




    private fun initViews(){
        sportDetailTB = findViewById(R.id.sportDetailTB)

        sportDetailTB?.setTitleBarListener(object : TitleBarLayout.TitleBarListener {
            override fun onBackClick() {
                finish()
            }

            override fun onActionImageClick() {
            }

            override fun onActionClick() {
            }
        })
    }


    //设置属性
    private fun setUpMap() {

        // 自定义系统定位小蓝点
        val myLocationStyle = MyLocationStyle()
        myLocationStyle.myLocationIcon(
            BitmapDescriptorFactory
                .fromResource(R.mipmap.gps_point)
        ) // 设置小蓝点的图标
        myLocationStyle.strokeColor(Color.BLACK) // 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.argb(100, 0, 0, 180)) // 设置圆形的填充颜色
        // myLocationStyle.anchor(int,int)//设置小蓝点的锚点
        myLocationStyle.strokeWidth(1.0f) // 设置圆形的边框粗细
        amap?.myLocationStyle = myLocationStyle
        amap?.setLocationSource(this) // 设置定位监听
        amap?.uiSettings?.isZoomControlsEnabled  = false
        amap?.uiSettings?.isMyLocationButtonEnabled   = true // 设置默认定位按钮是否显示
        amap?.isMyLocationEnabled = true // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
    }


    override fun onResume() {
        super.onResume()
        amapView?.onResume()
    }


    override fun onPause() {
        super.onPause()
        amapView?.onPause()
    }


    override fun onStop() {
        super.onStop()

    }


    override fun onDestroy() {
        super.onDestroy()
        amapView?.onDestroy()

    }

    //激活定位
    override fun activate(p0: LocationSource.OnLocationChangedListener?) {
        mListener = p0
        if (mlocationClient == null) {
            mlocationClient = AMapLocationClient(this)
            mLocationOption = AMapLocationClientOption()
            //设置定位监听
            mlocationClient!!.setLocationListener(this)
            //定位参数
            mLocationOption!!.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
            mLocationOption!!.isNeedAddress = true
            mLocationOption!!.interval = (50 * 1000).toLong()
            mLocationOption!!.locationPurpose = AMapLocationClientOption.AMapLocationPurpose.Sport
            mlocationClient!!.setLocationOption(mLocationOption)
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            //mlocationClient.stopLocation();
            mlocationClient!!.startLocation()
        }
    }

    //销毁定位
    override fun deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient!!.stopLocation();
            mlocationClient!!.onDestroy();
        }
        mlocationClient = null;
    }

    //定位成功回调
    override fun onLocationChanged(p0: AMapLocation?) {
        if (mListener != null && p0 != null) {
            mListener!!.onLocationChanged(p0)
            val latLng = LatLng(p0.latitude, p0.longitude)
            amap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18F))
        }
    }

}

private fun <T> Gson.fromJson(latLngStr: String?): List<LatLng> {

    return Gson().fromJson<List<LatLng>>(latLngStr)
}

private fun Gson.fromJson(amapSportInfo: Serializable?, java: Class<AmapSportBean>): AmapSportBean {

    return Gson().fromJson(amapSportInfo, AmapSportBean::class.java)
}

