package com.example.xingliansdk.ui.sleep

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.CalendarView
import androidx.recyclerview.widget.GridLayoutManager
import com.example.xingliansdk.R
import com.example.xingliansdk.XingLianApplication
import com.example.xingliansdk.adapter.SleepAdapter
import com.example.xingliansdk.base.BaseActivity
import com.example.xingliansdk.bean.SleepTypeBean
import com.example.xingliansdk.bean.room.*
import com.example.xingliansdk.bean.NetSleepBean
import com.example.xingliansdk.ui.sleep.viewmodel.SleepViewModel
import com.example.xingliansdk.utils.HelpUtil
import com.example.xingliansdk.utils.JumpUtil
import com.example.xingliansdk.utils.ShowToast
import com.example.xingliansdk.view.DateUtil
import com.example.xingliansdk.view.IF
import com.example.xingliansdk.view.SleepTodayView
import com.example.xingliansdk.view.SleepViewData
import com.example.xingliansdk.widget.TitleBarLayout
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.MPPointF
import com.google.gson.Gson
import com.gyf.barlibrary.ImmersionBar
import com.ly.genjidialog.extensions.convertListenerFun
import com.ly.genjidialog.extensions.newGenjiDialog
import com.shon.connector.BleWrite
import com.shon.connector.Config
import com.shon.connector.bean.SleepBean
import com.shon.connector.bean.TimeBean
import com.shon.connector.utils.TLog
import kotlinx.android.synthetic.main.activity_sleep_details.*
import org.json.JSONArray
import java.util.*
import kotlin.collections.ArrayList


class SleepDetailsActivity : BaseActivity<SleepViewModel>(), View.OnClickListener,
        SleepTodayView.OnTouch,
        BleWrite.HistoryCallInterface,
        BleWrite.SpecifySleepHistoryCallInterface {
    private lateinit var sDao: RoomSleepTimeDao
    private lateinit var mSleepListDao: SleepListDao
    private lateinit var mList: MutableList<SleepListBean>
    override fun layoutId() = R.layout.activity_sleep_details

    var dataSource: MutableList<List<SleepViewData>> =
            ArrayList()
    var mSleepList: MutableList<SleepViewData> = ArrayList()

    //    var position = 0
    private var sleepTimeList: MutableList<RoomSleepTimeBean> = mutableListOf()

    private lateinit var mSleepAdapter: SleepAdapter
    private var sleepTypeList: ArrayList<SleepTypeBean> = arrayListOf()
    override fun initView(savedInstanceState: Bundle?) {
        ImmersionBar.with(this)
                .titleBar(titleBar)
                .init()
        titleBar.setTitleBarListener(object : TitleBarLayout.TitleBarListener {
            override fun onBackClick() {
                finish()
            }

            override fun onActionImageClick() {
                dialog()
            }

            override fun onActionClick() {
            }
        })
        mList = mutableListOf()
        sDao = AppDataBase.instance.getRoomSleepTimeDao()
        mSleepListDao = AppDataBase.instance.getRoomSleepListDao()
        mList = mSleepListDao.getAllRoomTimes()
        sleepTimeList = sDao.getAllRoomTimes()
        TLog.error("???????????????+${Gson().toJson(mList)}")
        img_left.setOnClickListener(this)
        img_right.setOnClickListener(this)
        refreshCurrentSelectedDateData()

    }

    private var mStartTime = 0L
    private var mEndTime = 0L
    private fun setAdapter() {
        rySleep.layoutManager =
                GridLayoutManager(this, 2)
        mSleepAdapter = SleepAdapter(sleepTypeList)
        rySleep.adapter = mSleepAdapter
        mSleepAdapter.setOnItemClickListener { adapter, view, position ->
            TLog.error("=" + Gson().toJson(mSleepAdapter.data[position]))
            JumpUtil.startSleepNightActivity(this, mSleepAdapter.data[position])
        }
    }


    var timeLong = 0L
    private fun setView(bean: SleepListBean) {
        mStartTime = (bean.startTime) * 1000L
        mEndTime = (bean.endTime) * 1000L
        TLog.error("mStartTime++$mStartTime   ,mEndTime++$mEndTime")
        var startTime = DateUtil.getDate(DateUtil.YYYY_MM_DD_HH_MM, mStartTime)
        var endTime = DateUtil.getDate(DateUtil.YYYY_MM_DD, mEndTime)
        TLog.error("startTime++$startTime   ,endTime++$endTime")
        timeLong = (bean.endTime - bean.startTime)
        var time = DateUtil.getTextTime(timeLong)
        tvSleepTime.text = HelpUtil.getSpan(
                time.substring(0, 2),
                time.substring(2, 4),
                time.substring(4, 6),
                time.substring(6, 8),
                R.color.main_text_color,
                12
        )
        tvMaxHeart.text = HelpUtil.getSpan(bean.maximumHeartRate.toString(), "???/??????", 11)
        tvMinHeart.text = HelpUtil.getSpan(bean.minimumHeartRate.toString(), "???/??????", 11)
        tvAvgHeart.text = HelpUtil.getSpan(bean.averageHeartRate.toString(), "???/??????", 11)


    }

    private fun setData(mList: MutableList<Float>) {
        TLog.error("==" + Gson().toJson(mList))
        val mListColor = arrayListOf(
                R.color.color_deep_sleep, R.color.color_light_sleep, R.color.color_eye_movement, R.color.color_awake
        )
        val entries = ArrayList<PieEntry>()
        val typeList = arrayListOf("??????", "??????", "??????", "??????")
        for (i in 0 until mList.size) {
            if (mList[i] > 0) {
                entries.add(
                        PieEntry(
                                mList[i],
                                typeList[i] + DateUtil.getTextTime(mList[i].toLong() * 60L)
                        )
                )
            }
        }
        val dataSet = PieDataSet(entries, "")
        dataSet.setDrawIcons(false)
        dataSet.sliceSpace = 3f
        dataSet.iconsOffset = MPPointF(0f, 40f)
        dataSet.selectionShift = 5f
        val colors = ArrayList<Int>()
        for (i in 0 until mList.size) {
            if (mList[i] > 0)
                colors.add(resources.getColor(mListColor[i]))
        }
      //  pieView()
        dataSet.colors = colors
        //dataSet.setSelectionShift(0f);
        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter())
        data.setValueTextSize(11f)
        data.setValueTextColor(Color.WHITE)
        data.setDrawValues(false)
        pieChart.data = data
        pieChart.invalidate()
        setAdapter()
    }

    var setValueStatus=true  //??????????????????
    override fun createObserver() {
        super.createObserver()
        mViewModel.result.observe(this){
            TLog.error("????????????+"+Gson().toJson(it))
            setValueStatus=true
        //    var sleepBean=Gson().fromJson(Gson().toJson()it,NetSleepBean::class.java)
            var sleepBean: NetSleepBean = it
            if(sleepBean.sleepList.isNullOrEmpty()||sleepBean.sleepList.size<=0)
            {
                TLog.error("????????????")
                date?.let { it1 -> getData(it1) } //???????????????????????????????????????
                return@observe
            }
            setValueStatus=false
            mSleepListDao.insert(
                SleepListBean(
                    sleepBean.startTime,
                    sleepBean.avgHeartRate,
                    sleepBean.maxHeartRate,
                    sleepBean.minHeartRate,
                    sleepBean.apneaSecond,
                    sleepBean.endTime,
                    sleepBean.apneaTime,
                    sleepBean.respiratoryQuality,
                    Gson().toJson(sleepBean.sleepList)  //??????String
                    , DateUtil.getDate(DateUtil.YYYY_MM_DD, (sleepBean.endTime)*1000)
                )
            )
            TLog.error("===="+DateUtil.getDate(DateUtil.YYYY_MM_DD, (sleepBean.endTime )*1000))
            getData(DateUtil.getDate(DateUtil.YYYY_MM_DD, (sleepBean.endTime )*1000))
        }
        mViewModel.msg.observe(this){
            TLog.error("??????????????????+"+it)
            setValueStatus=false
            date?.let { it1 -> getData(it1) }
        }
    }

    private fun getData(date: String) {
        //    showWaitDialog("?????????...")
        TLog.error("date+="+date)
        val mTodayList = mSleepListDao.getList(
                date
        )
        sleepTypeList.clear()
        TLog.error("mTodayList++" + Gson().toJson(mTodayList))
        var sleepListBean: SleepListBean
        mSleepList.clear()
        dataSource.clear()
        sleepTodayView.clearView()
        tvSleepTimeType.text=""
        if (mTodayList.size <= 0) {
            pieChart.visibility = View.GONE  //????????????????????????
            tvFallAsleep.text = ""
            tvWakeUp.text = ""
            sleepView.visibility = View.GONE
            rySleep.visibility = View.GONE
            tvFallAsleep.visibility = View.GONE
            tvWakeUp.visibility = View.GONE
            tvMaxHeart.text = HelpUtil.getSpan("0", "???/??????", 11)
            tvMinHeart.text = HelpUtil.getSpan("0", "???/??????", 11)
            tvAvgHeart.text = HelpUtil.getSpan("0", "???/??????", 11)
            tvDeepSleep.text=HelpUtil.getSpan("0", "%", 11)
            tvLightSleep.text= HelpUtil.getSpan("0", "%", 11)
            tvEyeSleep.text= HelpUtil.getSpan("0", "%", 11)
            tvWideAwake.text=HelpUtil.getSpan("0", "???", 11)
            tvSleepTime.text = "--"

            return
        } else {
            pieChart.visibility = View.GONE
            sleepView.visibility = View.VISIBLE
            rySleep.visibility = View.VISIBLE
            tvFallAsleep.visibility = View.VISIBLE
            tvWakeUp.visibility = View.VISIBLE
            sleepListBean = mTodayList[0]
        }
        val sleepList = sleepListBean.sleepList
        TLog.error("sleepListBean.sleepList++" + sleepListBean.sleepList)
        TLog.error("stepList++" + Gson().toJson(sleepList))
        val stepList = arrayListOf<SleepBean.StepChildBean>()
        TLog.error("stepList++" + Gson().toJson(stepList))
        val array = JSONArray(sleepList)
        for (i in 0 until array.length()) {
            val child = array.getString(i)
            val childBean =
                    Gson().fromJson(child, SleepBean.StepChildBean::class.java)
            if (!stepList.contains(childBean)) {
                stepList.add(childBean)
            }
            mSleepChildList = arrayListOf()
            mSleepChildList.addAll(stepList)
            // hideWaitDialog()
            // TLog.error("???????????????????????? ????????????++${Gson().toJson(sleepTimeList)}")
        }
        setView(sleepListBean)
        TLog.error("sleepTimeList[position].startTime+" + sleepListBean.startTime)
        TLog.error("sleepTimeList[position].endTime+" + sleepListBean.endTime)
        sleepData(sleepListBean)

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.img_left -> {
                XingLianApplication.getSelectedCalendar()?.add(Calendar.DAY_OF_MONTH, -1)
                refreshCurrentSelectedDateData()
            }
            R.id.img_right -> {
                XingLianApplication.getSelectedCalendar()?.add(Calendar.DAY_OF_MONTH, +1)
                refreshCurrentSelectedDateData()
            }
        }
    }


    override fun HistoryCallResult(key: Byte, mList: ArrayList<TimeBean>?) {
        if (mList?.size!! <= 0)
            return
        mList.reverse()
        updateData(mList)
        sleepTimeList = sDao.getAllRoomTimes()
        mList?.get(mList.size - 1)?.let {
            when (key) {
                Config.BigData.DEVICE_SLEEP -> {
                    TLog.error(" ??????==DEVICE_SLEEP+" + Gson().toJson(mList))
                    BleWrite.writeSpecifySleepHistoryCall(
                            it.startTime, it.endTime,
                            this
                    )
                    //  position = sleepTimeList.size - 1
                }
            }
        }
    }

    private var mSleepChildList: MutableList<SleepBean.StepChildBean> = arrayListOf()
    override fun SpecifySleepHistoryCallResult(
            startTime: Long,
            endTime: Long,
            mList: ArrayList<SleepBean>?, bean: SleepBean
    ) {
        TLog.error("??????++" + Gson().toJson(mList))
        this.mList = mList as MutableList<SleepListBean>
        //  hideWaitDialog()

        mSleepListDao.insert(
                SleepListBean(
                        bean.startTime,
                        bean.averageHeartRate,
                        bean.maximumHeartRate,
                        bean.minimumHeartRate,
                        bean.numberOfApnea,
                        bean.endTime,
                        bean.indexOne,
                        bean.indexTwo,
                        bean.lengthOne,
                        bean.lengthTwo,
                        bean.totalApneaTime,
                        bean.respiratoryQuality,
                        Gson().toJson(bean.getmList())  //??????String
                        , DateUtil.getDate(DateUtil.YYYY_MM_DD, (endTime + Config.TIME_START) * 1000L)
                )
        )

    }

    private fun sleepData(
            bean: SleepListBean
    ) {
        val time = (bean.endTime - bean.startTime) / 60
        TLog.error("bean.startTime++"+bean.startTime)
        TLog.error("bean.startTime++"+bean.endTime)
        TLog.error("=="+       DateUtil.getDate(
            DateUtil.MM_AND_DD,
            ((bean.startTime) * 1000L)
        ) )
        tvFallAsleep.text =
                DateUtil.getDate(
                        DateUtil.MM_AND_DD,
                        (bean.startTime) * 1000L
                ) + "\n" +
                        DateUtil.getDate(
                                DateUtil.HH_MM,
                                (bean.startTime) * 1000L
                        ) + "??????"
        tvWakeUp.text =
                DateUtil.getDate(
                        DateUtil.MM_AND_DD,
                        (bean.endTime) * 1000L
                ) + "\n" +
                        DateUtil.getDate(
                                DateUtil.HH_MM,
                                (bean.endTime) * 1000L
                        ) + "??????"
        var time1Start = 0
        val mList: MutableList<Float> = ArrayList()
        var typeOne = 0f
        var typeTwo = 0f
        var typeThree = 0f
        var typeThird = 0f
        var numberWake = 0
        mSleepChildList.forEachIndexed { index, stepChildBean ->
            if (stepChildBean.duration >= 0 && stepChildBean.type != 0) {
                when (stepChildBean.type) {
                    1 -> typeOne += stepChildBean.duration
                    2 -> typeTwo += stepChildBean.duration
                    3 -> typeThree += stepChildBean.duration
                    4 -> {
                        typeThird += stepChildBean.duration
                        numberWake++
                    }
                }
                var time1 = DateUtil.getDate(
                        DateUtil.HH_MM,
                        (bean.startTime + time1Start) * 1000L
                )
                time1Start += stepChildBean.duration * 60
                var time2 = DateUtil.getDate(
                        DateUtil.HH_MM,
                        ( bean.startTime + time1Start) * 1000L
                )
                mSleepList.add(
                        SleepViewData(
                                time1, time2,
                                stepChildBean.duration, stepChildBean.type
                        )
                )
            }
        }
        mList.add(typeOne)
        mList.add(typeTwo)
        mList.add(typeThree)
        mList.add(typeThird)
        TLog.error("time++$time")
        var deepSleep = mList[0].toLong() * 100 / time
        var lightSleep = mList[1].toLong() * 100 / time
        var eyeSleep = mList[2].toLong() * 100 / time
        tvDeepSleep.text=  HelpUtil.getSpan(deepSleep.toString(), "%", 11)
        tvLightSleep.text=HelpUtil.getSpan(lightSleep.toString(), "%", 11)
        tvEyeSleep.text=HelpUtil.getSpan(eyeSleep.toString(), "%", 11)
        tvWideAwake.text=HelpUtil.getSpan(numberWake.toString(), "???", 11)
       // setAdapterDate(mList, time, numberWake, bean)
        if (!mSleepList.isNullOrEmpty() || mSleepList.size > 0)
            dataSource.add(mSleepList)
        if (dataSource.isNotEmpty()) {
            TLog.error("??????????????????++" + Gson().toJson(dataSource))
            sleepTodayView.setDataSource(dataSource, this)
            setData(mList)
        }
    }

    override fun SpecifySleepHistoryCallResult(mList: ArrayList<SleepBean>?) {
    }

    //??????????????????
    private fun updateData(allData: ArrayList<TimeBean>) {
        TLog.error("updateData++" + Gson().toJson(allData))
        val allRoomTimes = sDao.getAllRoomTimes()
        allData.forEach { timeBean ->
            val find = allRoomTimes.find { roomTime ->
                timeBean.startTime == roomTime.startTime
            }
            //find ???????????????????????????
            if (find == null) {
                //????????????????????????????????????
                sDao.insert(
                        RoomSleepTimeBean(
                                timeBean.dataUnitType,
                                timeBean.timeInterval,
                                timeBean.startTime,
                                timeBean.endTime,
                                DateUtil.getDate(
                                        DateUtil.YYYY_MM_DD,
                                        (timeBean.endTime + Config.TIME_START) * 1000L
                                )
                        )
                )

            } else {
                //??????????????????
                TLog.error("??????????????????")
                if (timeBean.endTime != find.endTime) {
                    //???????????????????????????????????????
                    //???????????????
                    find.startTime = timeBean.startTime
                    find.endTime = timeBean.endTime
                    sDao.update(find)
                    TLog.error("timeBean??????" + timeBean.endTime)
                    TLog.error("find++" + find.endTime)
                }
            }
        }
        TLog.error("sDao++${Gson().toJson(sDao.getAllRoomTimes())}")
    }

    private fun refreshCurrentSelectedDateData() {
        if (!IF.isEmpty(lastTodayDate)) {
            val curTodayDate = DateUtil.getCurrentDate(DateUtil.YYYY_MM_DD)
            if (DateUtil.getDateOffset(curTodayDate, lastTodayDate) >= 1) {
                //???????????????!, ???????????????????????????
                XingLianApplication.setSelectedCalendar(DateUtil.getCurrentCalendar())
            }
        }
        //????????????
        setTitleDateData()
    }

    private var lastTodayDate: String? = null
    var date : String? = null
    private fun setTitleDateData() {
        val calendar: Calendar? = XingLianApplication.getSelectedCalendar()
        TLog.error("calendar++${calendar?.timeInMillis}")
        timeDialog = calendar?.timeInMillis
        date = DateUtil.getDate(DateUtil.YYYY_MM_DD, calendar)
        if (DateUtil.equalsToday(date)) {
         //   tvTypeTime.setText(R.string.title_today)
            lastTodayDate = date
            img_right.visibility = View.INVISIBLE

        } else {

            img_right.visibility = View.VISIBLE
        }
        tvTypeTime.text = date
        mViewModel.getSleep((timeDialog!!/1000).toString())
     //   getData(date!!)
    }

    private var timeDialog: Long? = System.currentTimeMillis()//?????????????????????
    private var calendar: Calendar? = null
    var toDay = DateUtil.getCurrentDate()
    private fun dialog() {
        newGenjiDialog {
            layoutId = R.layout.dialog_calendar
            dimAmount = 0.3f
            isFullHorizontal = false
            isFullVerticalOverStatusBar = false
            animStyle = R.style.AlphaEnterExitAnimation
            convertListenerFun { holder, dialog ->
                var calenderView = holder.getView<CalendarView>(R.id.calenderView)
                timeDialog?.let { it1 ->
                    TLog.error("it1==${it1}")
                    calenderView?.setDate(it1)
                }
                calenderView?.setOnDateChangeListener { view, year, month, dayOfMonth ->
                    if (calendar == null) {
                        calendar = Calendar.getInstance()
                    }
                    calendar?.set(year, month, dayOfMonth, 0, 0)//?????????????????????
                    var calendarTime = calendar?.timeInMillis
                    if (calendarTime!! > toDay) {
                        TLog.error("calendarTime+= $calendarTime  DateUtil.getCurrentDate()++$toDay")
                        ShowToast.showToastLong("?????????????????????????????????")
                        return@setOnDateChangeListener
                    }
                    XingLianApplication.getSelectedCalendar()?.timeInMillis = calendarTime!!
                    refreshCurrentSelectedDateData()
                    dialog.dismiss()
                }
            }
        }.showOnWindow(supportFragmentManager)
    }

    override fun handleData(data: SleepViewData?) {
//        TLog.error("data++$data")
        if (data != null) {
            tvSleepTimeType.text = data.beginTime + "-" + data.endTime
            var time = DateUtil.getTextTime(data.duration.toLong() * 60L)
            if (time.substring(0, 2).toInt() > 0) {
                tvSleepTime.text = HelpUtil.getSpan(
                        time.substring(0, 2),
                        time.substring(2, 4),
                        time.substring(4, 6),
                        time.substring(6, 8),
                        R.color.main_text_color,
                        12
                )
            } else {
                tvSleepTime.text = HelpUtil.getSpan(
                        time.substring(4, 6),
                        time.substring(6, 8),
                    12
                )
            }

        } else {
            tvSleepTimeType.text = ""
            tvSleepTime.text = "--"
        }
    }
}