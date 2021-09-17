package com.example.xingliansdk.ui.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.example.db.DbManager
import com.example.xingliansdk.Config.database.*
import com.example.xingliansdk.Config.eventBus.HOME_HISTORICAL_BIG_DATA_WEEK
import com.example.xingliansdk.R
import com.example.xingliansdk.XingLianApplication
import com.example.xingliansdk.XingLianApplication.Companion.getSelectedCalendar
import com.example.xingliansdk.adapter.HomeAdapter
import com.example.xingliansdk.base.BaseFragment
import com.example.xingliansdk.bean.HomeCardBean
import com.example.xingliansdk.bean.room.*
import com.example.xingliansdk.blecontent.BleConnection
import com.example.xingliansdk.blesend.BleSend
import com.example.xingliansdk.eventbus.SNEvent
import com.example.xingliansdk.eventbus.SNEventBus
import com.example.xingliansdk.network.api.homeView.HomeViewApi
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
import kotlinx.android.synthetic.main.activity_home.toolbar
import kotlinx.android.synthetic.main.fragment_me.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.roundToLong

class HomeFragment : BaseFragment<HomeViewModel>(), OnRefreshListener, View.OnClickListener,
    BleWrite.HistoryCallInterface, //大数据时间列表
    BleWrite.SpecifyDailyActivitiesHistoryCallInterface,  //运动
    BleWrite.SpecifySleepHistoryCallInterface  //睡眠
    , BleWrite.SpecifyHeartRateHistoryCallInterface //心率
    , BleWrite.SpecifyBloodOxygenHistoryCallInterface  //血氧
//    , BleWrite.SpecifyBloodPressureHistoryCallInterface //血压
    , BleWrite.SpecifyStressFatigueHistoryCallInterface //压力
    , BleWrite.SpecifyTemperatureHistoryCallInterface //体温
    , BleWrite.DeviceMotionInterface //实时运动返回
{
    companion object{
        //关于这个页面会重复点击打开俩次的针对性操作
        var PressureOnClick= false
    }
    //    private lateinit var mList: MutableList<HomeCardBean>
    private lateinit var mHomeCardBean: HomeCardBean
    private lateinit var mAddList: MutableList<HomeCardBean.AddCardDTO>

    lateinit var mHomeAdapter: HomeAdapter
    override fun layoutId() = R.layout.activity_home
    var currentStepValue = 0
    private lateinit var sDao: RoomMotionTimeDao
    private var motionTimeList: MutableList<RoomMotionTimeBean> = mutableListOf()

    private lateinit var mMotionListDao: MotionListDao
    private lateinit var mSleepListDao: SleepListDao
    private lateinit var mHeartListDao: HeartListDao
    private lateinit var mBloodOxygenListDao: BloodOxygenListDao
    private lateinit var mTempListDao: TempListDao
    //压力
    private lateinit var mPressureListDao: PressureListDao

//    private var isRefresh = false
    private fun getRoomList() {
        sDao = AppDataBase.instance.getRoomMotionTimeDao()
        mMotionListDao = AppDataBase.instance.getMotionListDao()
        TLog.error("打印运动数据++${Gson().toJson(mMotionListDao.getAllRoomMotionList())}")

        val allRoomTimes = sDao.getAllRoomTimes()
        if (allRoomTimes.size > 0) {
            motionTimeList = allRoomTimes
        }
        mSleepListDao = AppDataBase.instance.getRoomSleepListDao()
        mHeartListDao = AppDataBase.instance.getHeartDao()
        mBloodOxygenListDao = AppDataBase.instance.getBloodOxygenDao()
        mPressureListDao = AppDataBase.instance.getPressureListDao()
        mTempListDao=AppDataBase.instance.getRoomTempListDao()
    }

    override fun initView(savedInstanceState: Bundle?) {
        SNEventBus.register(this)
        setHomeCard()
        intView()
        getRoomList()
//        if (!BleConnection.iFonConnectError&&!BleConnection.Unbind&&!BleConnection.iFOta) {
//            mSwipeRefreshLayout.finishRefresh()
//            mSwipeRefreshLayout.autoRefresh()
//        }
    }
    private fun setHomeCard()
    {
        mHomeCardBean =
            if (Hawk.get<HomeCardBean>(HOME_CARD_BEAN) != null) {
                Hawk.get(HOME_CARD_BEAN)
            } else {
                mAddList = arrayListOf()
                var mList = mutableListOf("运动记录", "心率", "睡眠", "压力", "血氧饱和度", "血压", "体温", "体重")
                var listSubTitle = mutableListOf(
                    "生命在于运动", "守护你的心脏",
                    "科学分析睡眠质量", "测测压力吧", "检测血氧", "记录您的血压", "时刻监测您的体温", "管理体重"
                )
                var imgList = intArrayOf(
                    HelpUtil.getResource("icon_home_sport"),
                    HelpUtil.getResource("icon_home_heart_rate"),
                    HelpUtil.getResource("icon_home_sleep"),
                    HelpUtil.getResource("icon_home_pressure"),
                    HelpUtil.getResource("icon_home_blodoxygen"),
                    HelpUtil.getResource("icon_home_bloodpressure"),
                    HelpUtil.getResource("icon_home_temperature"),//体温
                    HelpUtil.getResource("icon_home_weight")//体重

                )
                for (i in 0 until mList.size) {
                    mHomeCardBean = HomeCardBean()
                    mAddList.add(
                        HomeCardBean.AddCardDTO(
                            mList[i],
                            i,
                            imgList[i],
                            0,
                            "",
                            "",
                            listSubTitle[i]
                        )
                    )
                    mHomeCardBean.addCard = mAddList
                }
                Hawk.put(HOME_CARD_BEAN, mHomeCardBean)
                mHomeCardBean
            }
        mAddList = mHomeCardBean.addCard
    }

    override fun onDestroy() {
        super.onDestroy()
        SNEventBus.unregister(this)
    }

    private fun homeBleWrite() {
        TLog.error("时间==" + (DateUtil.getTodayZero() / 1000 - XingLianApplication.TIME_START))
        Handler(Looper.getMainLooper()).postDelayed({
            if (BleConnection.iFonConnectError) {
                mSwipeRefreshLayout.finishRefresh()
                //isRefresh=false
            } else {
                BleSend.sendDateTime()
                BleWrite.writeSportsUploadModeCall(1)
                val startTime = (DateUtil.getTodayZero() / 1000 - XingLianApplication.TIME_START)
                val endTime = (System.currentTimeMillis() / 1000 - XingLianApplication.TIME_START)
//                BleWrite.writeBigDataHistoryCall(
//                    Config.BigData.APP_HEART_RATE,
//                    this
//                )
                BleWrite.writeSpecifyDailyActivitiesHistoryCall(
                    startTime, endTime,
                    this,true
                )
                BleWrite.writeSpecifyHeartRateHistoryCall(
                    startTime, endTime,
                    this,true
                )
                BleWrite.writeSpecifyBloodOxygenHistoryCall(
                    startTime, endTime,
                    this,true
                )

                BleWrite.writeSpecifyStressFatigueHistoryCall(
                    startTime, endTime,
                    this,true
                )
                BleWrite.writeSpecifyTemperatureHistoryCall(
                    startTime, endTime,
                    this,true
                )
                BleWrite.writeBigDataHistoryCall(Config.BigData.APP_SLEEP, this,true)
            }
        }, 500)
    }

    override fun onResume() {
        super.onResume()
        TLog.error("onResume")

    }
    private fun intView() {
        ImmersionBar.setTitleBar(activity, toolbar)
        mSwipeRefreshLayout.setOnRefreshListener(this)
        tvGoal?.text = "${mDeviceInformationBean.exerciseSteps}步"
        rvAll.layoutManager =
            GridLayoutManager(activity, 2)
        mHomeAdapter = HomeAdapter(mAddList)
        rvAll.adapter = mHomeAdapter
        //刷新可滑动状态
        mHomeAdapter.setOnItemClickListener { adapter, view, position ->
            //     运动记录", "心率", "睡眠", "体重", "血氧饱和度")
            when (mHomeAdapter.data[position].type) {

                0 -> {
                    JumpUtil.startExerciseRecordActivity(activity)
                }
                1 -> {
                    JumpUtil.startHeartRateActivity(activity, mHeartRateList, position)
                }
                2 -> {
                    JumpUtil.startSleepDetailsActivity(activity/*, mStepList*/)
                }
                3 -> {
                    if(!PressureOnClick)
                    {
                        TLog.error("进来了+"+PressureOnClick)
                        PressureOnClick=true
                        JumpUtil.startPressureActivity(activity)
                    }
                    else
                    {
                        PressureOnClick=false
                    }
                }
                4 -> {
                    JumpUtil.startBloodOxygenActivity(activity)
                }
                5 -> {
                    JumpUtil.startBloodPressureActivity(activity)
                }
                6 -> {
                    JumpUtil.startTempActivity(activity)
                }
                7 -> {
                    JumpUtil.startWeightActivity(activity)
                }
            }
        }
        onClickListener()
    }

    private fun onClickListener() {
        tvEdit.setOnClickListener(this)
        circleSports.setOnClickListener(this)
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        TLog.error("进行刷新不判断")
//        if(!isRefresh) {
//            TLog.error("进行刷新")
            homeBleWrite()
//        }
        //isRefresh = true
//        TLog.error("mList+${Gson().toJson(mAddList)}")
        mHomeAdapter.data = mAddList
        mHomeAdapter.notifyDataSetChanged()
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.tvEdit -> {
                JumpUtil.startCardEditActivity(activity)
            }
            R.id.circleSports -> {
                JumpUtil.startDeviceSportChartActivity(activity)
            }
        }
    }


    override fun createObserver() {
        mainViewModel.textValue.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            currentStepValue = mainViewModel.getText()
            circleSports.maxProgress = mDeviceInformationBean.exerciseSteps.toInt()
        })
        mainViewModel.result.observe(this)
        {
            userInfo.user=it.user
            userInfo.userConfig=it.userConfig
            userInfo.permission=it.permission
            Hawk.put(USER_INFO,userInfo)

            //先查询一下汇总的运动数据
            var amapSport = Hawk.get(com.example.xingliansdk.Config.database.WALK_DISTANCE_KEY,"0.0")
            if(amapSport.equals("0.0"))
                DbManager.getDbManager().queryALlTotalDistance(it.user.userId)

            ImgUtil.loadImage(activity,userInfo.user.headPortrait)
            mDeviceInformationBean=DeviceInformationBean(it.user.sex.toInt()
                , it.user.age.toInt(), it.user.height.toInt(), it.user.weight.toDouble().toInt()
                , mDeviceInformationBean.language.toInt(),
                it.userConfig.timeFormat,
                1,
                it.userConfig.distanceUnit,
                mDeviceInformationBean.wearHands.toInt(),
                it.userConfig.temperatureUnit,
                it.userConfig.movingTarget.toLong(),
                DateUtil.convertStringToLong(DateUtil.YYYY_MM_DD, it.user.birthDate),
                it.user.nickname)
            Hawk.put( SLEEP_GOAL, it.userConfig.sleepTarget.toLong())
            mainViewModel.userInfo.postValue(it)
            Hawk.put(PERSONAL_INFORMATION, mDeviceInformationBean)

            tvGoal.text = "${it.userConfig.movingTarget.toLong()}步"
            TLog.error("个人信息++"+Gson().toJson(it))
        }
    }

    var mTimeList: ArrayList<TimeBean> = arrayListOf()
    override fun HistoryCallResult(key: Byte, mList: ArrayList<TimeBean>) {
        TLog.error("时间mList==" + Gson().toJson(mList))
        TLog.error("key==" + key)
//        mSwipeRefreshLayout.sethea
        if (mList?.size <= 0) {
            if (key == Config.BigData.DEVICE_SLEEP) {
                SNEventBus.sendEvent(HOME_HISTORICAL_BIG_DATA_WEEK, true)
                mSwipeRefreshLayout.finishRefresh()
                //isRefresh = false
            }
            return
        }
        mList?.reverse()
        mList?.get(mList.size - 1)?.let {
            when (key) {
                Config.BigData.DEVICE_DAILY_ACTIVITIES -> {
                    error("DEVICE_DAILY_ACTIVITIES")
//                    mTimeList = arrayListOf()
//                    mTimeList.add(it)
                    TLog.error("运动时间++" + Gson().toJson(mTimeList))
                    RoomUtils.updateMovementTime(mTimeList, this)
                    BleWrite.writeSpecifyDailyActivitiesHistoryCall(
                        it.startTime, it.endTime,
                        this
                    )
                }
                Config.BigData.DEVICE_SLEEP -> {
                    error("DEVICE_SLEEP")
                    mTimeList = arrayListOf()
                    mTimeList.add(it)
                  //  RoomUtils.updateSleepTime(mTimeList, this)
                    BleWrite.writeSpecifySleepHistoryCall(
                        it.startTime, it.endTime,
                        this,true
                    )
                }
                Config.BigData.DEVICE_BLOOD_OXYGEN -> {
                    error("DEVICE_BLOOD_OXYGEN")
                    mTimeList = arrayListOf()
                    mTimeList.add(it)
                    RoomUtils.updateBloodOxygenDate(mTimeList, this)
                    //回调拿前面几天的大数据

                }
                Config.BigData.DEVICE_HEART_RATE -> {
                    error("DEVICE_HEART_RATE==心率")
                    mTimeList = arrayListOf()
                    mTimeList.add(it)
//                    BleWrite.writeSpecifyHeartRateHistoryCall(
//                        mTimeList[0].startTime,
//                        mTimeList[0].endTime,
//                        this
//                    )
                    error("DEVICE_HEART_RATE==心率==" + Gson().toJson(mTimeList))
                    //    SNEventBus.sendEvent(HOME_HISTORICAL_BIG_DATA_WEEK, true)
                    RoomUtils.updateHeartRateData(mTimeList, this)
                }
                Config.BigData.DEVICE_STRESS_FATIGUE -> {
                    error("DEVICE_STRESS_FATIGUE==压力")
                    mTimeList = arrayListOf()
                    mTimeList.add(it)
                    RoomUtils.updatePressure(mTimeList, this)
                }
                else -> {
                }
            }
        }

    }

    override fun SpecifyDailyActivitiesHistoryCallResult(
        startTime: Long,
        endTime: Long,
        mList: ArrayList<DailyActiveBean>
    ) {
        if(mList.isNullOrEmpty())
            return
        error("指定日常数据++${Gson().toJson(mList)}")
     //   error("指定日常数据长度++${mList.size}")
        var stepList: MutableList<Int> = mutableListOf()
        var step = 0L
//        var distanceTotal = 0L
//        var caloriesTotal = 0L
        mList?.forEach {
            stepList.add(it.steps)
            step += it.steps
//            distanceTotal += it.distance
//            caloriesTotal += it.calories
        }
        error("30分钟一组的步数++${Gson().toJson(stepList)}")
        val name: String = Gson().toJson(stepList)
        mMotionListDao.insert(
            MotionListBean(
                startTime+XingLianApplication.TIME_START,
                endTime+XingLianApplication.TIME_START,
                name,
                step,
                false,
                DateUtil.getDate(DateUtil.YYYY_MM_DD, (startTime + Config.TIME_START) * 1000L)
            )
        )
        mViewModel.setDailyActive(startTime+XingLianApplication.TIME_START
        ,endTime+XingLianApplication.TIME_START,Gson().toJson(mList))
    }

    override fun SpecifyDailyActivitiesHistoryCallResult(mList: ArrayList<DailyActiveBean>?) {
//        error("指定日常数据 没返回时间++${Gson().toJson(mList)}")

    }

    override fun SpecifySleepHistoryCallResult(
        startTime: Long,
        endTime: Long,
        mList: ArrayList<SleepBean>?, bean: SleepBean
    ) {
        mRefreshHeader?.setReleaseText("睡眠刷新")
        error(" time ${startTime+XingLianApplication.TIME_START}")
        error(" endTime ${endTime+XingLianApplication.TIME_START}")
        error("time睡眠数据${Gson().toJson(mList)}")
        SNEventBus.sendEvent(HOME_HISTORICAL_BIG_DATA_WEEK, true)
            mSwipeRefreshLayout.finishRefresh()
      //  ShowToast.showToastLong("睡眠数据更新完成")
        mSleepListDao.insert(
            SleepListBean(
                bean.startTime+XingLianApplication.TIME_START,
                bean.averageHeartRate,
                bean.maximumHeartRate,
                bean.minimumHeartRate,
                bean.numberOfApnea,
                bean.endTime+XingLianApplication.TIME_START,
                bean.indexOne,
                bean.indexTwo,
                bean.lengthOne,
                bean.lengthTwo,
                bean.totalApneaTime,
                bean.respiratoryQuality,
                Gson().toJson(bean.getmList()), //转成String
                DateUtil.getDate(DateUtil.YYYY_MM_DD, (endTime + Config.TIME_START) * 1000L)
            )
        )
      var value=  HashMap<String,Any>()
        value["startTime"]= bean.startTime+XingLianApplication.TIME_START
        value["endTime"]=bean.endTime+XingLianApplication.TIME_START
        value["apneaTime"]=bean.totalApneaTime
        value["apneaSecond"]=bean.numberOfApnea
        value["avgHeartRate"]=bean.averageHeartRate
        value["minHeartRate"]=bean.minimumHeartRate
        value["maxHeartRate"]=bean.maximumHeartRate
        value["respiratoryQuality"]=bean.respiratoryQuality
        value["sleepList"]= Gson().toJson(bean.getmList())
        mViewModel.setSleep(value)

        var time = bean.endTime - bean.startTime
        if (time <= 0)
            return
        mAddList.forEachIndexed { index, addCardDTO ->
            if (addCardDTO.type == 2) {
                mAddList[index].time = XingLianApplication.TIME_START + endTime
                mAddList[index].dayContent = "${DateUtil.getTextTime(time)}"
                mHomeCardBean.addCard[index] = mAddList[index]
                mHomeAdapter.notifyItemChanged(index)
            }
            Hawk.put(HOME_CARD_BEAN, mHomeCardBean)
//            TLog.error("最后请求完睡眠++")
        }

    }

    override fun SpecifySleepHistoryCallResult(mList: ArrayList<SleepBean>?) {
    }

    var mHeartRateList: ArrayList<Int> = arrayListOf()
    override fun SpecifyHeartRateHistoryCallResult(
        startTime: Long,
        endTime: Long,
        mList: ArrayList<Int>?
    ) {
        mRefreshHeader?.setReleaseText("心率刷新")
        if (mList.isNullOrEmpty())
        {
            return
        }


       // ShowToast.showToastLong("刷新心率了")
        error("time 好家伙 看看心率吧++${Gson().toJson(mList)}")
            mHeartRateList = mList
        val name: String = Gson().toJson(mList)
        mViewModel.setHeartRate((startTime+XingLianApplication.TIME_START).toString()
            ,(endTime+XingLianApplication.TIME_START).toString()
            ,mList.toIntArray())
        mHeartListDao.insert(
            HeartListBean(
                DateUtil.getLongTime(startTime),
                DateUtil.getLongTime(endTime),
                name,
                false,
                DateUtil.getDateTime(startTime)
            )
        )
        var countNum = 0L
        var i = 0
        mList?.forEach {
            if (it > 0) {
                countNum += it
                i++
            }
        }
        if (i == 0) //如果全部为0 则不显示
            return
        mAddList.forEachIndexed { index, addCardDTO ->
            if (addCardDTO.type == 1) {
                mAddList[index].time = XingLianApplication.TIME_START + endTime
                mAddList[index].dayContent = "${countNum / i}"
                mAddList[index].dayContentString = "次/分钟"
                mHomeAdapter.notifyItemChanged(index)
                mHomeCardBean.addCard[index] = mAddList[index]
            }
        }
        TLog.error("mAddList+=" + Gson().toJson(mAddList))
        Hawk.put(HOME_CARD_BEAN, mHomeCardBean)
    }

    override fun SpecifyHeartRateHistoryCallResult(mList: ArrayList<Int>?) {
    }

    override fun SpecifyBloodOxygenHistoryCallResult(
        startTime: Long,
        endTime: Long,
        mList: ArrayList<Int>?
    ) {
        mRefreshHeader?.setReleaseText("血氧刷新")
        if(mList.isNullOrEmpty())
            return
        TLog.error("time 好家伙 血氧++${Gson().toJson(mList)}")
        val name: String = Gson().toJson(mList)

        mViewModel.setBloodOxygen((startTime+XingLianApplication.TIME_START).toString(),
            (endTime+XingLianApplication.TIME_START).toString(),
            mList.toIntArray())
     //   mViewModel.setBloodOxygen(value)
        if (endTime - startTime >= BloodOxygenListBean.day) {
            mBloodOxygenListDao.insert(
                BloodOxygenListBean(
                    startTime+ XingLianApplication.TIME_START,
                    endTime+ XingLianApplication.TIME_START,
                    name,
                    true,
                    DateUtil.getDateTime(startTime)
                )
            )
        } else {
            mBloodOxygenListDao.insert(
                BloodOxygenListBean(
                    startTime+ XingLianApplication.TIME_START,
                    endTime+ XingLianApplication.TIME_START,
                    name,
                    false,
                    DateUtil.getDateTime(startTime)
                )
            )
        }
        var countNum = 0L
        var i = 0
        mList?.forEach {
            if (it > 0) {
                countNum += it
                i++
            }
        }
        if (i == 0)
            return
        mAddList.forEachIndexed { index, addCardDTO ->
            if (addCardDTO.type == 4) {
                mAddList[index].time = XingLianApplication.TIME_START + endTime
                mAddList[index].dayContent = "${countNum / i}"
                mAddList[index].dayContentString = "%"
                mHomeAdapter.notifyItemChanged(index)
                mHomeCardBean.addCard[index] = mAddList[index]
            }
        }
        TLog.error("mAddList+=" + Gson().toJson(mAddList))
        Hawk.put(HOME_CARD_BEAN, mHomeCardBean)

    }

    override fun SpecifyBloodOxygenHistoryCallResult(
        mList: ArrayList<Int>?
    ) {
    }

//    override fun SpecifyBloodPressureHistoryCallResult(
//        startTime: Long,
//        endTime: Long,
//        mList: ArrayList<DataBean>?
//    ) {
//    }

    override fun DeviceMotionResult(mDataBean: DataBean) {
        TLog.error("设备实时运动${Gson().toJson(mDataBean)}")
        TLog.error("exerciseSteps==="+mDeviceInformationBean.exerciseSteps.toInt())

        val forMater = DecimalFormat("#0.00")
        forMater.roundingMode = RoundingMode.DOWN
        mDeviceInformationBean=Hawk.get(PERSONAL_INFORMATION)
        circleSports.maxProgress= mDeviceInformationBean.exerciseSteps.toInt()
        circleSports?.progress = mDataBean.totalSteps.toInt()
        TLog.error("unitSystem==" + Gson().toJson(mDeviceInformationBean))
        if (mDeviceInformationBean?.unitSystem == 1.toByte()) {
            tvKM?.text = "${forMater.format(mDataBean.distance.toDouble() / 1000)} 英里"
        } else
            tvKM?.text = "${forMater.format(mDataBean.distance.toDouble() / 1000)} 公里"
        //  TLog.error("calories==${data.calories}")
        tvCalories?.text = "${mDataBean.calories / 1000} 千卡"
    }

    override fun SpecifyStressFatigueHistoryCallResult(
        startTime: Long,
        endTime: Long,
        mList: ArrayList<PressureBean>?
    ) {
        mRefreshHeader?.setReleaseText("压力刷新")
        TLog.error("获取到的压力数据++" + Gson().toJson(mList))
            if(mList.isNullOrEmpty())
                return
        val name: String = Gson().toJson(mList)

        mViewModel.setPressure(DateUtil.getLongTime(startTime).toString(), DateUtil.getLongTime(endTime).toString(),name)
        if (endTime - startTime >= HeartListBean.day) {
            mPressureListDao.insert(
                PressureListBean(
                    DateUtil.getLongTime(startTime)
                    , DateUtil.getLongTime(endTime),
                    name,
                    true,
                    DateUtil.getDateTime(startTime)
                )
            )
        } else {
            mPressureListDao.insert(
                PressureListBean(
                    DateUtil.getLongTime(startTime)
                     , DateUtil.getLongTime(endTime),
                    name,
                    false,
                    DateUtil.getDateTime(startTime)
                )
            )
        }
        var countNum = 0
        var i = 0
        mList?.forEach {
            if (it.stress > 0) {
                countNum = it.stress
                i++
            }
        }
        if (i == 0)
            return
        mAddList.forEachIndexed { index, addCardDTO ->
            if (addCardDTO.type == 3) {
                mAddList[index].time = XingLianApplication.TIME_START + endTime
                mAddList[index].dayContent = "$countNum"
                when (countNum) {
                    in 1 until 30 -> {
                        mAddList[index].dayContentString = "放松"
                    }
                    in 30 until 60 -> {
                        mAddList[index].dayContentString = "正常"
                    }
                    in 60 until 80 -> {
                        mAddList[index].dayContentString = "中等"
                    }
                    in 80 until 100 -> {
                        mAddList[index].dayContentString = "偏高"
                    }
                }
                mHomeAdapter.notifyItemChanged(index)
                mHomeCardBean.addCard[index] = mAddList[index]
            }
        }

        Hawk.put(HOME_CARD_BEAN, mHomeCardBean)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventReceived(event: SNEvent<Any>) {
        when (event.code) {
            Config.ActiveUpload.DEVICE_REAL_TIME_EXERCISE.toInt() -> {
                var data: DataBean = event.data as DataBean
                val time =
                    (getSelectedCalendar()!!.timeInMillis / 1000) - TimeUtil.getTodayZero(0) / 1000
//                TLog.error("回调")
                if (time in 1..86400) {
                    circleSports?.progress = data.totalSteps.toInt()
                    val forMater = DecimalFormat("#0.00")
                    forMater.roundingMode = RoundingMode.DOWN
                    mDeviceInformationBean=Hawk.get(PERSONAL_INFORMATION,DeviceInformationBean())
                     TLog.error("unitSystem==" + Gson().toJson(mDeviceInformationBean))
                    if (mDeviceInformationBean?.unitSystem == 1.toByte()) {
                        tvKM?.text = "${forMater.format(data.distance.toDouble() / 1000)} 英里"
                    } else
                        tvKM?.text = "${forMater.format(data.distance.toDouble() / 1000)} 公里"
                    //  TLog.error("calories==${data.calories}")
                    tvCalories?.text = "${data.calories / 1000} 千卡"
                }
            }

            com.example.xingliansdk.Config.eventBus.HOME_CARD,
            com.example.xingliansdk.Config.eventBus.BLOOD_PRESSURE_RECORD,
            com.example.xingliansdk.Config.eventBus.MAP_MOVEMENT_SATISFY,
            com.example.xingliansdk.Config.eventBus.DEVICE_DELETE_DEVICE
            -> {
                TLog.error(" 被删除"+event.code)
                mHomeCardBean = Hawk.get(HOME_CARD_BEAN, HomeCardBean())
                TLog.error(" 被删除"+Gson().toJson(mHomeCardBean))
                if (mHomeCardBean==null||mHomeCardBean.addCard==null){
                    setHomeCard()
                  //  mHomeAdapter.notifyDataSetChanged()
                }
                else {
                    if(mHomeCardBean==null||mHomeCardBean.addCard==null) {
                        return
                    }
                    mAddList = mHomeCardBean.addCard
                    mHomeAdapter.data = mAddList
                }
              //  TLog.error("mHomeCardBean.addCard+="+Gson().toJson(mHomeCardBean.addCard))
                mHomeAdapter.notifyDataSetChanged()
            }
            com.example.xingliansdk.Config.eventBus.SPORTS_GOAL_EXERCISE_STEPS -> {
                val step: String = event.data.toString()
                tvGoal.text = "${step}步"
                TLog.error(" mDeviceInformationBean.exerciseSteps+="+ mDeviceInformationBean.exerciseSteps)
                circleSports.maxProgress = step.toInt()
            }
            com.example.xingliansdk.Config.eventBus.HOME_HISTORICAL_BIG_DATA -> {
                TLog.error("首页更新数据++=")
                mSwipeRefreshLayout.finishRefresh()
                mSwipeRefreshLayout.autoRefresh()
                // homeBleWrite()
            }

        }
    }

    override fun SpecifyTemperatureHistoryCallResult(
        startTime: Long,
        endTime: Long,
        mList: ArrayList<Int>?
    ) {
        mRefreshHeader?.setReleaseText("体温刷新")
        TLog.error("time 体温++${Gson().toJson(mList)}")
        if (mList.isNullOrEmpty())
        {
            TLog.error("空的 直接不走下一步了")
            return
        }
        val name: String = Gson().toJson(mList)
        TLog.error("非空 time 体温++${name}")
        mTempListDao.insert(
            TempListBean(
                startTime+XingLianApplication.TIME_START,
                endTime+XingLianApplication.TIME_START,
                name,
                DateUtil.getDateTime(startTime)
            )
        )
        mViewModel.setTemperature((startTime+XingLianApplication.TIME_START).toString()
            ,(endTime+XingLianApplication.TIME_START).toString()
            ,mList.toIntArray())
        var tempLast = 0

        var iFZero = 0    //30个平均为0排除
        var num = 0
        mList?.forEachIndexed {
                index, i->
            num+=i
            if (i == 0)
                iFZero++

            if ((index + 1) % 30 == 0) {//6个数组平分一组
                var size =  //当为0时特殊处理
                    if ((30 - iFZero) <= 0)
                        1
                    else
                        (30 - iFZero)
                var heart = num / size
                if (heart>0)
                    tempLast=heart
                num = 0
                iFZero = 0
            }
        }
        if (tempLast == 0)
            return
        mAddList.forEachIndexed { index, addCardDTO ->
            if (addCardDTO.type == 6) {
                mAddList[index].time = XingLianApplication.TIME_START + endTime
                mAddList[index].dayContent = "${tempLast.toDouble()/10}"
                mAddList[index].dayContentString = "℃"
                mHomeAdapter.notifyItemChanged(index)
                mHomeCardBean.addCard[index] = mAddList[index]
            }
        }
        Hawk.put(HOME_CARD_BEAN, mHomeCardBean)
    }

}

