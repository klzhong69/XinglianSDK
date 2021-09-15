package com.example.xingliansdk.service.work

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.example.xingliansdk.Config.database.*
import com.example.xingliansdk.Config.eventBus.*
import com.example.xingliansdk.R
import com.example.xingliansdk.XingLianApplication
import com.example.xingliansdk.bean.DeviceFirmwareBean
import com.example.xingliansdk.bean.DevicePropertiesBean
import com.example.xingliansdk.bean.room.*
import com.example.xingliansdk.blecontent.BleConnection
import com.example.xingliansdk.blecontent.BleConnection.iFonConnectError
import com.example.xingliansdk.blesend.BleSend
import com.example.xingliansdk.eventbus.SNEvent
import com.example.xingliansdk.eventbus.SNEventBus
import com.example.xingliansdk.network.api.homeView.HomeViewApi
import com.example.xingliansdk.network.api.login.LoginBean
import com.example.xingliansdk.service.core.IWork
import com.example.xingliansdk.ui.setting.MyDeviceActivity
import com.example.xingliansdk.utils.*
import com.example.xingliansdk.utils.CountTimer.OnCountTimerListener
import com.example.xingliansdk.view.DateUtil
import com.google.gson.Gson
import com.orhanobut.hawk.Hawk
import com.shon.connector.BleWrite
import com.shon.connector.BleWrite.*
import com.shon.connector.Config
import com.shon.connector.bean.*
import com.shon.connector.utils.TLog
import com.shon.connector.utils.TLog.Companion.error
import kotlinx.android.synthetic.main.activity_my_device.*
import kotlinx.android.synthetic.main.item_switch.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import kotlin.collections.HashMap

/**
 * 功能：主服务
 * 常驻，负责处理app中各种耗时杂事
 * 功能:位置定位服务,事件接收器
 */
class BleWork : IWork, OnCountTimerListener,
    HistoryCallInterface,
    SpecifyDailyActivitiesHistoryCallInterface,
    SpecifySleepHistoryCallInterface,
    SpecifyHeartRateHistoryCallInterface,
    SpecifyBloodOxygenHistoryCallInterface,
    SpecifyStressFatigueHistoryCallInterface, //血压
    SpecifyTemperatureHistoryCallInterface,
    FirmwareInformationInterface,//3.2.1获取设备固件信息
    DevicePropertiesInterface,//3.2.3 APP端获取设备属性信息
    DeviceMotionInterface,//3.2.7 APP端获取设备实时运动数据
    DoNotDisturbModeSwitchCallInterface,//设备勿扰模式开关
    UUIDBindInterface,//是否绑定
    TimeCallInterface//首次设置时间
{
    //多久刷新一次大数据
    var time = 1000L
    var oneHour = 30 * 1000L
    private var countTimer = CountTimer(oneHour, this)
    private var minutes: Long = 0
    private lateinit var context: Context
    var bigDataHistoryStatus = false
    var userInfo: LoginBean = Hawk.get(USER_INFO, LoginBean())
    override fun init(context: Context) {
        super.init(context)
        this.context = context
        getRoom()
        countTimer.start()
        minutes = 0
        SNEventBus.register(this)
    }


    /**
     * 一分钟回调一次
     *
     * @param millisecond
     */
    override fun onCountTimerChanged(millisecond: Long) {
//        error("一分钟调用一次?$millisecond")
        var address = Hawk.get<String>("address")
        if (address.isNullOrEmpty())
            return
//        error("是否连接++$iFonConnectError")
        if (iFonConnectError) {
            error("后台 没链接的时候重连")
            BleConnection.initStart(Hawk.get(DEVICE_OTA, false), 5000)
            //    BleConnection.connectDevice(address)
        } else {
            if (bigDataHistoryStatus) //必须首页回调过
            {
                TLog.error("HomeFragment 首页 发送过后 的回调 拿去过往数据")
                if (MyDeviceActivity.FlashBean.UIFlash) {
                    getBigDataHistory()
                }
            }
        }
        minutes++
    }

    //最先发出去的数据
    private fun getInstruction() {
        BleSend.sendDateTime(this)
        BleWrite.writeForGetFirmwareInformation(this, false)
        BleWrite.writeForGetDeviceProperties(this, false)
        var mDeviceInformation = Hawk.get(
            PERSONAL_INFORMATION,
            DeviceInformationBean(2, 18, 160, 50, 0, 0, 1, 0, 0, 0, 10000)
        )
        BleWrite.writeDeviceInformationCall(mDeviceInformation, false)
        BleWrite.writeForGetDeviceMotion(this, false)
        BleWrite.writeUUIDBind(HelpUtil.getAndroidId(context), this@BleWork)
        error("HomeFragment getInstruction")
    }

    private fun controlInstructions() {
        // notDisturb()  //勿扰模式被阉割
        Hawk.put(TURN_ON_SCREEN, Hawk.get(TURN_ON_SCREEN, 2))
        writeTurnOnScreenCall(Hawk.get(TURN_ON_SCREEN, 2))
        drinkWater()
        sedentary()
        // var heartData = Hawk.get(HEART_RATE_ALARM, HeartRateAlarmBean(0, 0))
        // writeHeartRateAlarmSwitchCall(heartData.getmSwitch(), heartData.heartNum)
    }

    private fun sedentary() {
        val sedentaryTimeBean = TimeBean()
        sedentaryTimeBean.switch = 2
        sedentaryTimeBean.specifiedTime = 127
        sedentaryTimeBean.openHour = 8
        sedentaryTimeBean.openMin = 0
        sedentaryTimeBean.closeHour = 22
        sedentaryTimeBean.closeMin = 0
        sedentaryTimeBean.reminderInterval = 60
        var sedentary = Hawk.get<TimeBean>(REMINDER_SEDENTARY)
        if (sedentary == null) {
            Hawk.put(REMINDER_SEDENTARY, sedentaryTimeBean)
        } else
            sedentary = Hawk.get(REMINDER_SEDENTARY, sedentaryTimeBean)
        writeReminderSedentaryCall(Hawk.get(REMINDER_SEDENTARY, sedentary))
    }

    private fun notDisturb() {
        val notDisturbTimeBean = TimeBean()
        notDisturbTimeBean.switch = 2
        notDisturbTimeBean.openHour = 22
        notDisturbTimeBean.openMin = 0
        notDisturbTimeBean.closeHour = 7
        notDisturbTimeBean.closeMin = 0
        var notDisturb = Hawk.get<TimeBean>(DO_NOT_DISTURB_MODE_SWITCH)
        if (notDisturb == null) {
            Hawk.put(DO_NOT_DISTURB_MODE_SWITCH, notDisturbTimeBean)
        } else
            notDisturb = Hawk.get(DO_NOT_DISTURB_MODE_SWITCH, notDisturbTimeBean)
        writeDoNotDisturbModeSwitchCall(Hawk.get(DO_NOT_DISTURB_MODE_SWITCH, notDisturb), this)
    }

    private fun drinkWater() {
        val drinkWaterTimeBean = TimeBean()
        drinkWaterTimeBean.switch = 2
        drinkWaterTimeBean.specifiedTime = 127
        drinkWaterTimeBean.openHour = 8
        drinkWaterTimeBean.openMin = 0
        drinkWaterTimeBean.closeHour = 22
        drinkWaterTimeBean.closeMin = 0
        drinkWaterTimeBean.reminderInterval = 60
        var drinkWater = Hawk.get<TimeBean>(REMINDER_DRINK_WATER)
        if (drinkWater == null) {
            Hawk.put(REMINDER_DRINK_WATER, drinkWaterTimeBean)
        } else
            drinkWater = Hawk.get(REMINDER_DRINK_WATER, drinkWaterTimeBean)
        writeReminderDrinkWaterCall(Hawk.get(REMINDER_DRINK_WATER, drinkWater))
    }

    private fun settingInstructions() {
        controlInstructions()

        BleWrite.writeHeartRateAlarmSwitchCall(
            userInfo.userConfig.heartRateAlarm.toDouble().toInt(),
            userInfo.userConfig.heartRateThreshold
        )
        var mTimeList = Hawk.get<ArrayList<TimeBean>>(TIME_LIST)
        //闹钟
        if (!mTimeList.isNullOrEmpty()) {
            TLog.error("mTimeList==${Gson().toJson(mTimeList)}")
            if (mTimeList.size > 0)
                for (i in 0 until mTimeList.size)
                    writeAlarmClockScheduleCall(mTimeList[i], false)
        } else {
            var timeBean = TimeBean()
            timeBean.characteristic = 1
            timeBean.number = 0
            timeBean.mSwitch = 0
            timeBean.specifiedTime = 0
            timeBean.hours = 0
            timeBean.min = 0
            writeAlarmClockScheduleCall(timeBean, false)
        }
        //日程
        var scheduleList = Hawk.get<ArrayList<TimeBean>>(SCHEDULE_LIST)
        if (!scheduleList.isNullOrEmpty()) {
            if (scheduleList.size > 0)
                for (i in 0 until scheduleList.size)
                    writeAlarmClockScheduleCall(scheduleList[i], false)
        } else {
            var timeBean = TimeBean()
            timeBean.characteristic = 2
            timeBean.number = 0
            timeBean.mSwitch = 0
            timeBean.year = 0
            timeBean.month = 0
            timeBean.day = 0
            timeBean.hours = 0
            timeBean.min = 0
            writeAlarmClockScheduleCall(timeBean, false)
        }
        writeSettingUID()
    }

    private fun getBigDataHistory() {
        TLog.error("getBigDataHistory 被访问")
        Handler(Looper.getMainLooper())
            .postDelayed({
                if (!iFonConnectError) {
                    BleWrite.writeBigDataHistoryCall(
                        Config.BigData.APP_DAILY_ACTIVITIES,
                        this@BleWork
                    )
                    BleWrite.writeBigDataHistoryCall(
                        Config.BigData.APP_SLEEP,
                        this@BleWork
                    )
                    BleWrite.writeBigDataHistoryCall(
                        Config.BigData.APP_HEART_RATE,
                        this@BleWork
                    )
                    BleWrite.writeBigDataHistoryCall(
                        Config.BigData.APP_BLOOD_OXYGEN,
                        this@BleWork
                    )
                    writeBigDataHistoryCall(
                        Config.BigData.APP_STRESS_FATIGUE,
                        this@BleWork
                    )
                    writeBigDataHistoryCall(
                        Config.BigData.APP_TEMPERATURE,
                        this@BleWork
                    )
                }
            }, 500)
    }


    override fun destroy() {
        super.destroy()
        countTimer.stop()
        SNEventBus.unregister(this)
    }

    override fun onReceivedMessage(event: SNEvent<*>) {
        when (event.code) {
            Config.ActiveUpload.DEVICE_REAL_TIME_EXERCISE.toInt() -> {
                val mDataBean = event.data as DataBean
                //                TLog.Companion.error("=="+ new Gson().toJson(mDataBean));
                AppDataNotifyUtil.updateNotificationTitle(
                    context,
                    context.resources.getString(R.string.app_name),
                    "当前步数:" + mDataBean.totalSteps.toString()
                )
            }
            DEVICE_CONNECT_NOTIFY -> {
                error("链接成功返回指令")
                countTimer.stop()
                countTimer = CountTimer(oneHour * 30, this)
                countTimer.start()
                getInstruction()
            }

        }
    }

    //  var timeList=ArrayList<TimeBean>()
    override fun HistoryCallResult(
        key: Byte,
        mList: ArrayList<TimeBean>
    ) {
        if (mList.size <= 0) return
        mList.reverse()
//        error(
//            " mList++${key.toInt()} " +
//                    "数据==${Gson().toJson(mList)}"
//        )
        timeList = mList
        mList?.let { time ->
            when (key) {
                Config.BigData.DEVICE_DAILY_ACTIVITIES -> {
//                        error("访问时间++${time.startTime}")
//                        TLog.error(
//                            "${Gson().toJson(
//                                AppDataBase.instance.getMotionListDao()
//                                    .getRoomTime(time.endTime, time.startTime)
//                            )}"
//                        )
                    //数据库修改时间
                    RoomUtils.updateMovementTime(mList, this)
                }
                Config.BigData.DEVICE_SLEEP -> {
                    RoomUtils.updateSleepTime(mList, this)
                    error("DEVICE_SLEEP==")
                }
                Config.BigData.DEVICE_BLOOD_OXYGEN -> {
                    error("血氧  DEVICE_BLOOD_OXYGEN=" + Gson().toJson(mList))
                    RoomUtils.updateBloodOxygenDate(mList, this)

                }
                Config.BigData.DEVICE_HEART_RATE -> {
                    error("Heart_Rate==${Gson().toJson(mList)}")

//                    if(mList.size>1)
//                        mList[mList.size-2].endTime= mList[mList.size-2].endTime-7200

                    error("Heart_Rate==${Gson().toJson(mList)}")
                      RoomUtils.updateHeartRateData(mList, this)
                }
                Config.BigData.DEVICE_STRESS_FATIGUE -> {
                    TLog.error("压力值")
                    RoomUtils.updatePressure(mList, this)
                }
                Config.BigData.DEVICE_TEMPERATURE -> {
                    TLog.error("体温" + Gson().toJson(mList))
                    RoomUtils.updateTemp(mList, this)
                }
                else -> {
                }
            }
        }
    }

    private lateinit var sDao: RoomMotionTimeDao
    private lateinit var timeList: MutableList<TimeBean>
    private lateinit var mMotionListDao: MotionListDao
    private lateinit var mSleepListDao: SleepListDao

    //心率的
    private lateinit var mHeartListDao: HeartListDao

    //血氧
    private lateinit var mBloodOxygenListDao: BloodOxygenListDao

    //压力
    private lateinit var mPressureListDao: PressureListDao
    private lateinit var mTempListDao: TempListDao
    private fun getRoom() {
        //运动
        sDao = AppDataBase.instance.getRoomMotionTimeDao()
        mMotionListDao = AppDataBase.instance.getMotionListDao()
        mSleepListDao = AppDataBase.instance.getRoomSleepListDao()
        mHeartListDao = AppDataBase.instance.getHeartDao()
        mBloodOxygenListDao = AppDataBase.instance.getBloodOxygenDao()
        mPressureListDao = AppDataBase.instance.getPressureListDao()
        mTempListDao = AppDataBase.instance.getRoomTempListDao()
        //    timeList= sDao.getAllRoomTimes()

    }

    override fun SpecifyDailyActivitiesHistoryCallResult(
        startTime: Long,
        endTime: Long,
        mList: ArrayList<DailyActiveBean>
    ) {
        //   error("你好结束时间++$startTime,  结束时间++$endTime")
        // error("指定日常数据++${Gson().toJson(mList)}")
        var stepList: MutableList<Int> = mutableListOf()
        var step = 0L
        var distanceTotal = 0L
        var caloriesTotal = 0L
        mList?.forEach {
            stepList.add(it.steps)
            step += it.steps
            distanceTotal += it.distance
            caloriesTotal += it.calorie
        }
        error("30分钟一组的步数++${Gson().toJson(stepList)}")
        val stepStr: String = Gson().toJson(stepList)
        if (endTime - startTime >= MotionListBean.day) {
            sDao.updateTime(startTime, endTime)
            mMotionListDao.insert(
                MotionListBean(
                    DateUtil.getLongTime(startTime),
                    DateUtil.getLongTime(endTime),
                    stepStr,
                    step,
                    true,
                    DateUtil.getDate(DateUtil.YYYY_MM_DD, (startTime + Config.TIME_START) * 1000L)
                )
            )
        } else {
            mMotionListDao.insert(
                MotionListBean(
                    DateUtil.getLongTime(startTime),
                        DateUtil.getLongTime(endTime),
                    stepStr,
                    step,
                    false,
                    DateUtil.getDate(DateUtil.YYYY_MM_DD, (startTime + Config.TIME_START) * 1000L)
                )
            )
        }
        TLog.error("运动大数据++${DateUtil.getLongTime(startTime)}++"+Gson().toJson(mList))
        getDaily( DateUtil.getLongTime(startTime),
            DateUtil.getLongTime(endTime),Gson().toJson(mList))
    }

    override fun SpecifyDailyActivitiesHistoryCallResult(mList: ArrayList<DailyActiveBean>) {}
    override fun SpecifySleepHistoryCallResult(
        startTime: Long,
        endTime: Long,
        mList: ArrayList<SleepBean>,
        bean: SleepBean
    ) {
        TLog.error("SpecifySleepHistoryCallResult")
        bean.getmList()
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
                Gson().toJson(bean.getmList())  //转成String
                , DateUtil.getDate(DateUtil.YYYY_MM_DD, (endTime + Config.TIME_START) * 1000L)
            )
        )
        getSleep(bean)
    }

    override fun SpecifySleepHistoryCallResult(mList: ArrayList<SleepBean>) {

    }

    override fun SpecifyHeartRateHistoryCallResult(
        startTime: Long,
        endTime: Long,
        mList: ArrayList<Int>
    ) {
        val name: String = Gson().toJson(mList)
        TLog.error("RetrofitLog Heart_Rate 心率大数据 ==name+"+name)
        TLog.error("RetrofitLog Heart_Rate 心率大数据 ==startTime+"+(XingLianApplication.TIME_START + startTime)+"====endTime++"+(XingLianApplication.TIME_START + endTime))

        getHeart(
            (XingLianApplication.TIME_START + startTime),
            (XingLianApplication.TIME_START + endTime),
            mList
        )

        if (endTime - startTime >= HeartListBean.day) {
            sDao.updateTime(startTime, endTime)
            TLog.error(" HeartRate if==" + Gson().toJson(name))
            mHeartListDao.insert(
                HeartListBean(
                    XingLianApplication.TIME_START +startTime,
                    XingLianApplication.TIME_START +endTime,
                    name,
                    true,
                    DateUtil.getDateTime(startTime)
                )
            )
        } else {
            TLog.error(" HeartRate else==" + Gson().toJson(name))
            mHeartListDao.insert(
                HeartListBean(
                    XingLianApplication.TIME_START +startTime,
                    XingLianApplication.TIME_START +endTime,
                    name,
                    false,
                    DateUtil.getDateTime(startTime)
                )
            )
        }
    }

    override fun SpecifyHeartRateHistoryCallResult(mList: ArrayList<Int>) {}
    override fun SpecifyBloodOxygenHistoryCallResult(
        startTime: Long,
        endTime: Long,
        mList: ArrayList<Int>
    ) {
        //   TLog.error("血氧++" + startTime)
        //    TLog.error("血氧++" + endTime)
        //    TLog.error("血氧++" + (endTime - startTime))
        val name: String = Gson().toJson(mList)
        TLog.error("血氧++" + name)
        getBloodOxygen(startTime+ XingLianApplication.TIME_START,endTime+ XingLianApplication.TIME_START,mList)
        if (endTime - startTime >= BloodOxygenListBean.day) {
            mBloodOxygenListDao.insert(
                BloodOxygenListBean(
                    startTime+ XingLianApplication.TIME_START, endTime+ XingLianApplication.TIME_START, name, true, DateUtil.getDateTime(
                        startTime
                    )
                )
            )
        } else {
            mBloodOxygenListDao.insert(
                BloodOxygenListBean(
                    startTime+ XingLianApplication.TIME_START, endTime+ XingLianApplication.TIME_START, name, false, DateUtil.getDateTime(
                        startTime
                    )
                )
            )
        }
    }

    override fun SpecifyBloodOxygenHistoryCallResult(mList: ArrayList<Int>) {}

    override fun SpecifyStressFatigueHistoryCallResult(
        startTime: Long,
        endTime: Long,
        mList: ArrayList<PressureBean>
    ) {
        val name: String = Gson().toJson(mList)
        getPressure(startTime,endTime,mList)
        if (endTime - startTime >= HeartListBean.day) {
            mPressureListDao.insert(
                PressureListBean(
                    DateUtil.getLongTime(startTime), DateUtil.getLongTime(endTime), name, true, DateUtil.getDateTime(
                        startTime
                    )
                )
            )
        } else {
            mPressureListDao.insert(
                PressureListBean(
                    DateUtil.getLongTime(startTime), DateUtil.getLongTime(endTime), name, false, DateUtil.getDateTime(
                        startTime
                    )
                )
            )
        }
    }
    override fun SpecifyTemperatureHistoryCallResult(
        startTime: Long,
        endTime: Long,
        mList: ArrayList<Int>
    ) {
        TLog.error("体温 时间+" + (XingLianApplication.TIME_START + endTime))
        TLog.error("体温 时间+" + (XingLianApplication.TIME_START + startTime))
        TLog.error("体温 历史记录温度数据+${Gson().toJson(mList)}")
        val name: String = Gson().toJson(mList)

        getTemp(XingLianApplication.TIME_START + startTime,
            XingLianApplication.TIME_START + endTime,mList)
        mTempListDao.insert(
            TempListBean(
                XingLianApplication.TIME_START + startTime,
                XingLianApplication.TIME_START + endTime,
                name,
                DateUtil.getDateTime(startTime)
            )
        )
    }
    override fun onResult(
        productNumber: String?,
        versionName: String?,
        version: Int,
        nowMaC: String?,
        mac: String?
    ) {
//        TLog.error("productNumber+=$productNumber")
//        TLog.error("productNumber+=$version")
        //       TLog.error("productNumber+=$versionName")
       var value= HashMap<String,Any>()
        value["mac"]=nowMaC!!
        value["productNumber"]=productNumber!!
        value["firmwareVersion"]=version.toString()
        value["appVersion"]=HelpUtil.getVersionCode(context).toString()
        value["osType"]="1"
        value["appVersionName"]=HelpUtil.getVersionName(context)!!
        if(HelpUtil.netWorkCheck(context))
        GlobalScope.launch(Dispatchers.IO)
        {
            kotlin.runCatching {
                HomeViewApi.mHomeViewApi.saveUserEquip(value)
            }.onSuccess {
                TLog.error("正常")
            }.onFailure {
                TLog.error("异常")
            }
        }
        Hawk.put("address",nowMaC)
        Hawk.put(
            "DeviceFirmwareBean", DeviceFirmwareBean(
                productNumber,
                versionName,
                version,
                nowMaC,
                mac
            )
        )
        SNEventBus.sendEvent(
            DEVICE_FIRMWARE,
            DeviceFirmwareBean(productNumber, versionName, version, nowMaC, mac)
        )

    }

    override fun DeviceMotionResult(mDataBean: DataBean?) {
//        TLog.error("获取到的实时运动+" + Gson().toJson(mDataBean))
        SNEventBus.sendEvent(Config.ActiveUpload.DEVICE_REAL_TIME_EXERCISE.toInt(), mDataBean)
    }

    override fun DevicePropertiesResult(
        electricity: Int,
        mCurrentBattery: Int,
        mDisplayBattery: Int,
        type: Int
    ) {
        TLog.error("DevicePropertiesResult==$electricity")
        Hawk.put(
            DEVICE_ATTRIBUTE_INFORMATION,
            DevicePropertiesBean(electricity, mCurrentBattery, mDisplayBattery, type)
        )
        SNEventBus.sendEvent(DEVICE_ELECTRICITY, electricity)

    }

    override fun UUIDBindResult(key: Int) {
        TLog.error("UUIDBindResult==$key")
        if (key == 2) {
            Hawk.put(BIND_UUID, true)
        } else {

            settingInstructions()
            TLog.error("时间 UUIDBindResult settingInstructions++" + System.currentTimeMillis())
            Hawk.put(BIND_UUID, false)
        }
        TLog.error("时间 UUIDBindResult++" + System.currentTimeMillis())
//        TLog.error("HomeFragment  首页更新大数据")
        SNEventBus.sendEvent(HOME_HISTORICAL_BIG_DATA)
    }

    override fun DoNotDisturbModeSwitchCallResult() {

    }

    override fun TimeCall() {
        TLog.error("天气 设置时间成功")
        SNEventBus.sendEvent(DEVICE_CONNECT_WEATHER_SERVICE)//天气
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventReceived(event: SNEvent<Any>) {
        when (event.code) {
            HOME_HISTORICAL_BIG_DATA_WEEK -> {
                bigDataHistoryStatus = event.data as Boolean
                TLog.error("HomeFragment 回调完成了")
                getBigDataHistory()
            }
        }
    }



    fun getHeart(startTime: Long, endTime: Long, heartList: ArrayList<Int>) {
        if(HelpUtil.netWorkCheck(context))
            GlobalScope.launch(Dispatchers.IO)
            {
                kotlin.runCatching {
                    HomeViewApi.mHomeViewApi.setHeartRate(
                        startTime.toString(),
                        endTime.toString(),
                        heartList.toIntArray())
                }.onSuccess {
                    TLog.error("Heart_Rate 正常"+endTime)
                }.onFailure {
                    TLog.error("Heart_Rate 异常"+endTime)
                }
            }

    }
    private fun getSleep(bean: SleepBean) {
        if(HelpUtil.netWorkCheck(context))
            GlobalScope.launch(Dispatchers.IO)
            {
                kotlin.runCatching {
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
                    HomeViewApi.mHomeViewApi.saveSleep(value)
                }.onSuccess {
                 //   TLog.error("正常")
                }.onFailure {
                    TLog.error("异常")
                }
            }

    }
    fun getBloodOxygen(startTime: Long, endTime: Long, list: ArrayList<Int>) {
        TLog.error("开始网络请求===${startTime}   endtime==${endTime}")
        if(HelpUtil.netWorkCheck(context))
            GlobalScope.launch(Dispatchers.IO)
            {
                kotlin.runCatching {
                    HomeViewApi.mHomeViewApi.saveBloodOxygen(
                        startTime.toString(),
                        endTime.toString(),
                        list.toIntArray()
                    )
                }.onSuccess {
                  //  TLog.error("正常")
                }.onFailure {
                    TLog.error("异常")
                }
            }

    }
    fun getTemp(startTime: Long, endTime: Long, list: ArrayList<Int>) {
        TLog.error("开始网络请求===${startTime}   endtime==${endTime}")
        if(HelpUtil.netWorkCheck(context))
            GlobalScope.launch(Dispatchers.IO)
            {
                kotlin.runCatching {
                    HomeViewApi.mHomeViewApi.setTemperature(
                        startTime.toString(),
                        endTime.toString(),
                        list.toIntArray())
                }.onSuccess {
//                    TLog.error("正常")
                }.onFailure {
                    TLog.error("异常")
                }
            }
    }
    private fun getPressure(startTime: Long, endTime: Long, list: ArrayList<PressureBean>) {
        if(HelpUtil.netWorkCheck(context))
            GlobalScope.launch(Dispatchers.IO)
            {
                kotlin.runCatching {
                    HomeViewApi.mHomeViewApi.setPressure(
                        startTime.toString(),
                        endTime.toString(),
                        Gson().toJson(list))
                }.onSuccess {
                    TLog.error("正常")
                }.onFailure {
                    TLog.error("异常")
                }
            }
    }
    private fun getDaily(startTime: Long, endTime: Long, list: String){
        if(HelpUtil.netWorkCheck(context))
            GlobalScope.launch(Dispatchers.IO)
            {
                kotlin.runCatching {
                    HomeViewApi.mHomeViewApi.saveDailyActive(startTime,endTime,list)
                }.onSuccess {
                    TLog.error("正常")
                }.onFailure {
                    TLog.error("异常")
                }
            }
    }

}