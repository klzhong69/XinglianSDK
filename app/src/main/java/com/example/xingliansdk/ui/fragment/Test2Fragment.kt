package com.example.xingliansdk.ui.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.example.xingliansdk.Config.database.HOME_CARD_BEAN
import com.example.xingliansdk.Config.database.PERSONAL_INFORMATION
import com.example.xingliansdk.Config.eventBus.HOME_HISTORICAL_BIG_DATA_WEEK
import com.example.xingliansdk.R
import com.example.xingliansdk.XingLianApplication
import com.example.xingliansdk.XingLianApplication.Companion.getSelectedCalendar
import com.example.xingliansdk.adapter.HomeAdapter
import com.example.xingliansdk.base.BaseFragment
import com.example.xingliansdk.bean.HomeCardBean
import com.example.xingliansdk.bean.room.*
import com.example.xingliansdk.blecontent.BleConnection
import com.example.xingliansdk.eventbus.SNEvent
import com.example.xingliansdk.eventbus.SNEventBus
import com.example.xingliansdk.network.api.weather.WeatherApi
import com.example.xingliansdk.network.api.weather.testWeather
import com.example.xingliansdk.utils.*
import com.example.xingliansdk.view.DateUtil
import com.example.xingliansdk.viewmodel.HomeViewModel
import com.google.gson.Gson
import com.gyf.barlibrary.ImmersionBar
import com.orhanobut.hawk.Hawk
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.shon.bluetooth.util.ByteUtil
import com.shon.connector.BleWrite
import com.shon.connector.Config
import com.shon.connector.bean.*
import com.shon.connector.utils.TLog
import com.shon.connector.utils.TLog.Companion.error
import kotlinx.android.synthetic.main.activity_device_information.*
import kotlinx.android.synthetic.main.activity_flash.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.roundToLong

class Test2Fragment : BaseFragment<HomeViewModel>(), OnRefreshListener, View.OnClickListener

{

    override fun layoutId() = R.layout.activity_big_data_interval


    override fun initView(savedInstanceState: Bundle?) {
        TLog.error("test页面")
        intView()
    }

    override fun onDestroy() {
        super.onDestroy()
        SNEventBus.unregister(this)
    }



    override fun onResume() {
        super.onResume()
//        TLog.error("onResume")

    }
    private fun intView() {
        ImmersionBar.setTitleBar(activity, toolbar)
//        mSwipeRefreshLayout.setOnRefreshListener(this)
//        tvGoal.text = "${mDeviceInformationBean.exerciseSteps}步"
//        rvAll.layoutManager =
//            GridLayoutManager(activity, 2)
//
//        onClickListener()
    }

    private fun onClickListener() {
//        tvEdit.setOnClickListener(this)
//        circleSports.setOnClickListener(this)
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        TLog.error("进行刷新不判断")
//        if(!isRefresh) {
//            TLog.error("进行刷新")
//        }
        //isRefresh = true
//        TLog.error("mList+${Gson().toJson(mAddList)}")
    }


    override fun onClick(v: View) {
        when (v.id) {
//            R.id.tvEdit -> {
//                JumpUtil.startCardEditActivity(activity)
//            }
//            R.id.circleSports -> {
//                JumpUtil.startDeviceSportChartActivity(activity)
//            }
        }
    }


}

