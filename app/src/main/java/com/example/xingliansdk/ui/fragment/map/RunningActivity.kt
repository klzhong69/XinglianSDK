package com.example.xingliansdk.ui.fragment.map

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.FrameLayout
import com.amap.api.maps.MapView
import com.amap.api.maps.model.LatLng
import com.example.xingliansdk.Config
import com.example.xingliansdk.R
import com.example.xingliansdk.base.BaseActivity
import com.example.xingliansdk.bean.MapMotionBean
import com.example.xingliansdk.bean.node.ItemExerciseRecordNode
import com.example.xingliansdk.bean.room.AppDataBase
import com.example.xingliansdk.bean.room.RoomExerciseRecordDao
import com.example.xingliansdk.eventbus.SNEvent
import com.example.xingliansdk.eventbus.SNEventBus
import com.example.xingliansdk.ui.fragment.map.view.IRunningContract
import com.example.xingliansdk.ui.fragment.map.view.RunningPresenterImpl
import com.example.xingliansdk.ui.fragment.map.view.RunningSportDataUtil
import com.example.xingliansdk.utils.CountTimerUtil
import com.example.xingliansdk.utils.ShowToast
import com.example.xingliansdk.utils.Utils
import com.example.xingliansdk.view.DateUtil
import com.example.xingliansdk.view.LongClickView
import com.example.xingliansdk.view.PressView
import com.example.xingliansdk.viewmodel.MainViewModel
import com.google.gson.Gson
import com.gyf.barlibrary.ImmersionBar
import com.orhanobut.hawk.Hawk
import com.shon.connector.utils.TLog
import com.sn.map.impl.GaoDeLocationImpl
import com.sn.map.impl.GpsLocationImpl
import com.sn.map.view.SNGaoDeMap
import com.sn.map.view.SNMapHelper
import kotlinx.android.synthetic.main.amap_include_start_pause_layout.*
import kotlinx.android.synthetic.main.amap_running_status_view.*
import kotlinx.android.synthetic.main.include_map.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.DecimalFormat

//运动系数(健走：k=0.8214
//跑步：k=1.036
//自行车：k=0.6142
//轮滑、溜冰：k=0.518
//室外滑雪：k=0.888
class RunningActivity : BaseActivity<MainViewModel>(), View.OnClickListener, IRunningContract.IView  {

    private val tags = "RunningActivity"

    private var mapHelper: SNMapHelper? = null
    var mPresenter: RunningPresenterImpl? = null
    private lateinit var mMapMotionBean: MapMotionBean
    private lateinit var mExerciseDao: RoomExerciseRecordDao
    var date = System.currentTimeMillis()
    var mRecordTime = 0L


    //计算卡里路常量
    var kcalcanstanc = 65.4 //计算卡路里常量


    //是否是暂停状态
    private var isStopStatus = false

    override fun layoutId() = R.layout.include_map
    override fun initView(savedInstanceState: Bundle?) {
        ImmersionBar.with(this)
            .titleBar(titleBar)
            .init()
        SNEventBus.register(this)

        //val iLocation = GaoDeLocationImpl(this,2 * 1000,5);


        mExerciseDao = AppDataBase.instance.getItemExerciseRecordNode()
        mMapMotionBean = intent.getSerializableExtra("MapMotionBean") as MapMotionBean
        Hawk.put(Config.database.AMAP_SPORT_TYPE,mMapMotionBean.type)
        when (mMapMotionBean.type) {
            0 -> RunningSportDataUtil.mExerciseType = Config.exercise.WALK
            1 -> RunningSportDataUtil.mExerciseType = Config.exercise.RUN
            2 -> RunningSportDataUtil.mExerciseType = Config.exercise.BICYCLE
            3 -> RunningSportDataUtil.mExerciseType = Config.exercise.MOUNTAIN_CLIMBING
        }
        mPresenter = RunningPresenterImpl(this)
        mPresenter?.attachView(this)

        amapBackLayout.visibility = View.GONE
        constMap.visibility=View.GONE
        amapRunnLayout.visibility = View.GONE
        //GPS显示
        llRunningGPS.visibility = View.VISIBLE

        statusAmapOperateLayout.visibility = View.GONE

        CountTimerUtil.start(tvNumberAnim, object : CountTimerUtil.AnimationState {
            override fun start() {
            }

            override fun end() {
                 constMap.visibility = View.VISIBLE

                //initMap(savedInstanceState)


                //封面计时器开始计时
                amapStatusTime.base = SystemClock.elapsedRealtime();
                amapStatusTime.start();

                initMap(savedInstanceState)
                mPresenter?.requestWeatherData()
                mPresenter?.requestMapFirstLocation()
                mPresenter?.initDefaultValue()
                timer.base = SystemClock.elapsedRealtime()//计时器清零
                timer.start()
            }

            override fun repeat() {
            }
        })
//        isFirstLatLng = true
        timer.base = SystemClock.elapsedRealtime()//计时器清零
        timer.start()

        statusContinuePressView.setOnClickListener(this)
        statusRunningPressView.setOnClickListener(this)

        tvStop.setOnClickListener(this)
        tvGoOn.setOnClickListener(this)
        tvTest.setOnClickListener(this)
        longSave.setMyClickListener(object : LongClickView.MyClickListener {
            override fun singleClickFinish() {

            }

            override fun longClickFinish() {
                TLog.error("runOnUiThread" + Gson().toJson(mRoomExerciseBean))
                runOnUiThread {
                    TLog.error("===" + Gson().toJson(mRoomExerciseBean))
                    TLog.error("===" + Gson().toJson(mHomeCardBean.addCard))
                    saveExercise()

                    mPresenter!!.requestRetrySaveSportData()

                    if (mRoomExerciseBean == null || mRoomExerciseBean.distance == null || mRoomExerciseBean.distance?.toDouble()!! < 0.2) {
                        finish()
                        ShowToast.showToastLong(" 本次运动距离过短,将不会记录数据")
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
                            //保存
                            mExerciseDao.insert(mRoomExerciseBean!!)
                        } else {
                            if (mRoomExerciseBean?.distance != null && mRoomExerciseBean?.distance?.toDouble()!! > 0.2)
                                mExerciseDao.insert(mRoomExerciseBean!!)
                            ShowToast.showToastLong("数据异常,保存失败" + Gson().toJson(mRoomExerciseBean))
                            ShowToast.showToastLong("数据流card+" + mHomeCardBean.addCard + "  \n---" + mHomeCardBean.addCard.size)
                        }
                        finish()
                    }


                }
                TLog.error("长按结束完成回调  longClickFinish")
            }

        })


        initStatusView()
    }



    private fun initStatusView(){

        //地图模式返回
        amapBackLayout.setOnClickListener {
//            constMap.visibility = View.GONE
//            amapRunnLayout.visibility = View.VISIBLE
//            amapBackLayout.visibility = View.GONE
//            llRunningGPS.visibility = View.GONE
//
//            //判断是否是暂停状态
//            if(isStopStatus){
//                amapDestoryPressView.visibility = View.GONE
//                amapStatusDoubleLayout.visibility = View.VISIBLE
//            }else{
//                amapDestoryPressView.visibility = View.VISIBLE
//                amapStatusDoubleLayout.visibility = View.GONE
//            }

        }

        //暂停按钮，点击后显示结束和继续
        amapDestoryPressView.setOnClickListener {
            amapDestoryPressView.visibility = View.GONE
            amapStatusDoubleLayout.visibility = View.VISIBLE

            operateStop()
        }



        //长按结束按钮
        amapStopPressView.setCircleColor(Color.parseColor("#F43232"))
        amapStopPressView.setCircleTextColor(R.color.white)
        amapStopPressView.setShowProgress(true)
        amapStopPressView.setStartText("结束")


        //长按暂停
        amapStopPressView.setOnSportEndViewOnclick(object :
            PressView.OnSportEndViewOnclick {
            override fun onStartButton() {
                Log.e(tags, "-----开始=")
            }

            override fun onProgressCompetly() {
                Log.e(tags, "------结束了=")
                clickSaveData()
            }

        })

        statusStopPressView.setOnSportEndViewOnclick(object : PressView.OnSportEndViewOnclick{
            override fun onStartButton() {

            }

            override fun onProgressCompetly() {
                clickSaveData()
            }

        })


        //继续按钮
        amapContinuePressView.setOnClickListener {
            amapDestoryPressView.visibility = View.VISIBLE
            amapStatusDoubleLayout.visibility = View.GONE

            operateContinue()
            mPresenter?.requestStartSport()
        }

        //切换地图模式
        aMapStatusTypeImgView.setOnClickListener {
            constMap.visibility = View.VISIBLE
            amapRunnLayout.visibility = View.GONE
            amapBackLayout.visibility = View.VISIBLE
            llRunningGPS.visibility = View.VISIBLE

            //判断是否是暂停状态
            if(isStopStatus){
//                longSave.visibility = View.VISIBLE
//                tvGoOn.visibility = View.VISIBLE
//                tvStop.visibility = View.GONE


                statusAmapOperateLayout.visibility = View.VISIBLE
                statusRunningPressView.visibility = View.GONE

            }else{
//                longSave.visibility = View.GONE
//                tvGoOn.visibility = View.GONE
//                tvStop.visibility = View.VISIBLE


                statusAmapOperateLayout.visibility = View.GONE
                statusRunningPressView.visibility = View.VISIBLE
            }


            //  mPresenter?.requestWeatherData()
//            mPresenter?.requestMapFirstLocation()
//            mPresenter?.initDefaultValue()
//            timer.base = SystemClock.elapsedRealtime()//计时器清零
//            timer.start()
        }

    }


    //长按暂停
    private fun clickSaveData(){
        saveExercise()

        mPresenter!!.requestRetrySaveSportData()

        //|| mRoomExerciseBean.distance?.toDouble()!! < 0.2
        if (mRoomExerciseBean == null || mRoomExerciseBean.distance == null ) {
            ShowToast.showToastLong(" 本次运动距离过短,将不会记录数据")
            finish()
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
                //保存
                mExerciseDao.insert(mRoomExerciseBean!!)
            } else {
                if (mRoomExerciseBean?.distance != null && mRoomExerciseBean?.distance?.toDouble()!! > 0.2)
                    mExerciseDao.insert(mRoomExerciseBean!!)
                ShowToast.showToastLong("数据异常,保存失败" + Gson().toJson(mRoomExerciseBean))
                ShowToast.showToastLong("数据流card+" + mHomeCardBean.addCard + "  \n---" + mHomeCardBean.addCard.size)
            }
            finish()
        }
    }




    var mRoomExerciseBean: ItemExerciseRecordNode = ItemExerciseRecordNode()
    private fun saveExercise() {
        val time = (SystemClock.elapsedRealtime() - timer.base)
        mRoomExerciseBean =
            ItemExerciseRecordNode(
                distances,
                calories,
                averageSpeed,
                pace,
                time,
                RunningSportDataUtil.mExerciseType,
                date,
                DateUtil.getDate(DateUtil.YYYY_MM_DD_AND, date)
            )

//        mRoomExerciseBean =
//            ItemExerciseRecordNode(
//                "0.32",
//                "15.58",
//                "2.82km/h",
//                "21'19\\",
//                342248,
//               0.8214,
//                1624967066056,
//                "2021年06月29日"
//            )

    }



    /**
     * 方法必须重写
     */
    override fun onResume() {
        if (mPresenter != null) {
            mPresenter!!.setUIEnable(true)
            mPresenter!!.onResume()
            mPresenter!!.onVisible()
        }
        super.onResume()
    }

    /**
     * 方法必须重写
     */
    override fun onPause() {
        super.onPause()
        if (mPresenter != null) {
            mPresenter!!.onPause()
        }
//        TLog.error("息屏操作以后==onPause")
    }
    override fun onStop() {
        if (mPresenter != null) {
            //mPresenter.setUIEnable(false);
            mPresenter!!.onStop()
            mPresenter!!.onInvisible()
        }
        super.onStop()
    }
    /**
     * 方法必须重写
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        TLog.error("息屏操作以后==onSaveInstanceState")
    }

    /**
     * 方法必须重写
     */
    override fun onDestroy() {
        super.onDestroy()
        if (mPresenter != null) {
            mPresenter!!.onDestroy()
            mPresenter!!.onDetach()
        }
        if (mapHelper != null) {
            mapHelper!!.stopSport()
        }
        SNEventBus.unregister(this)
    }





    override fun onClick(v: View) {
        when (v.id) {
            R.id.statusRunningPressView->{ //暂停按钮
                statusRunningPressView.visibility = View.GONE
                statusAmapOperateLayout.visibility = View.VISIBLE
                operateStop()
            }
            R.id.statusContinuePressView->{ //继续按钮
                statusRunningPressView.visibility = View.VISIBLE
                statusAmapOperateLayout.visibility = View.GONE

                mPresenter?.requestStartSport()
                operateContinue()
            }


            //点击暂停按钮
            R.id.tvStop -> {

                longSave.visibility = View.VISIBLE
                tvGoOn.visibility = View.VISIBLE
                tvStop.visibility = View.GONE

                operateStop()

            }
            //继续按钮
            R.id.tvGoOn -> {
                mPresenter?.requestStartSport()
                longSave.visibility = View.GONE
                tvGoOn.visibility = View.GONE
                tvStop.visibility = View.VISIBLE
                operateContinue()
            }
            R.id.tvTest -> {
                mPresenter?.requestMapFirstLocation()
            }
        }
    }


    //暂停操作
    private fun operateStop(){
        isStopStatus = true
        timer.stop()
        amapStatusTime.stop()
        mRecordTime = SystemClock.elapsedRealtime()
        mPresenter?.requestStopSport()
    }


    //继续操作
    private fun operateContinue(){
        isStopStatus = false
        if (mRecordTime != 0L){
            timer.base = timer.base + (SystemClock.elapsedRealtime() - mRecordTime)
            amapStatusTime.base = amapStatusTime.base +  (SystemClock.elapsedRealtime() - mRecordTime)
        }else{
            timer.base = SystemClock.elapsedRealtime()
            amapStatusTime.base = SystemClock.elapsedRealtime()
        }
        timer.start()
        amapStatusTime.start()
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventReceived(event: SNEvent<Any>) {
        when (event.code) {
            Config.eventBus.MAP_MOVEMENT_DISSATISFY -> {
                finish()
            }
        }
    }
        //无效返回键
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
         return false
    }

    override fun onUpdateSettingConfig(isKeepScreenEnable: Boolean, isWeatherEnable: Boolean) {

    }

    override fun onUpdateWeatherData(
        weatherType: Int,
        weatherTemperatureRange: String?,
        weatherQuality: String?
    ) {
    }

    private fun initMap(savedInstanceState: Bundle?) {
        TLog.error("savedInstanceState$savedInstanceState")
        val iLocation = GpsLocationImpl(this, 1000, 10)
//        val iLocation = GaoDeLocationImpl(this,2 * 1000,5);
//        TLog.error("iLocation+="+iLocation.lastLocation.latitude)
//        TLog.error("iLocation+="+iLocation.lastLocation.longitude)
//        TLog.error("iLocation+="+iLocation.lastLocation.altitude)
      //  val iLocation = GaoDeLocationImpl(this, 1, 1)
        val mMapView: View
        mMapView = MapView(this)
        mapHelper = SNGaoDeMap(this, savedInstanceState, iLocation, mMapView)
        mMapContent.addView(
            mMapView,
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        mPresenter?.initMapListener(mapHelper)
        mPresenter?.requestStartSport()
    }
    override fun onUpdateMapFirstLocation(mLatitude: Double, mLongitude: Double) {
    }

    override fun onUpdateGpsSignal(signal: Int) {
        if (isFinished()) {
            return
        }
        when (signal) {
            RunningPresenterImpl.SIGNAL_WEAK -> {
            }
            RunningPresenterImpl.SIGNAL_MIDDLE -> {
            }
            RunningPresenterImpl.SIGNAL_STRONG -> {
            }
            RunningPresenterImpl.SIGNAL_STRONG_MAX -> {
            }
            RunningPresenterImpl.SIGNAL_GPS_OFF -> {
            }
        }
    }

 

    var calories:String?=null
    var distances:String?=null
    var pace:String?=null
    var averageSpeed:String?=null


    val decimalFormat  = DecimalFormat("#.#")




    override fun onUpdateSportData(
        distances: String?, calories: String?,
        hourSpeed: String?,
        pace: String?,latLngList : List<LatLng>
    ) {
        TLog.error("distances++$distances")
        TLog.error("calories++$calories")
        TLog.error("hourSpeed++$hourSpeed")
        TLog.error("pace++$pace")

        TLog.error(tags,"-----经纬度集合="+Gson().toJson(latLngList))

        try {
            tvCalories.text=calories
            tvDistance.text=distances
            tvPace.text=pace
            this.calories=calories
            this.distances=distances
            this.pace=pace
            averageSpeed=hourSpeed

            var kValue = Utils.mul(kcalcanstanc, distances?.toDouble())

          //  tvCalories.text = String.format("%.2f",kValue)+""
            amapStatusCaloriesTv.text = calories

            //距离
            amapStatusDistanceTv.text = distances;
            //速度
            amapStatusSpeedTv.text = pace;
        }catch (e:Exception){
            e.printStackTrace()
        }

    }

    override fun onUpdateSportData(spendTime: String?) {
//       TLog.error("时间++$spendTime")
      //  timer.text = spendTime
    }

    override fun onSaveSportDataStatusChange(code: Int) {
        TLog.error("返回值++$code")
        when (code) {
            RunningPresenterImpl.CODE_COUNT_LITTLE -> {
                // finish()
            }
            RunningPresenterImpl.CODE_SUCCESS -> {
                setResult(Activity.RESULT_OK)
            }
        }
    }

    override fun onSportStartAnimationEnable(enable: Boolean) {
    }


}

