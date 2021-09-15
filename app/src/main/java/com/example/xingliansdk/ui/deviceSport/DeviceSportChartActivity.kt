package com.example.xingliansdk.ui.deviceSport

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.CalendarView
import androidx.annotation.RequiresApi
import com.example.xingliansdk.R
import com.example.xingliansdk.XingLianApplication
import com.example.xingliansdk.base.BaseActivity
import com.example.xingliansdk.bean.WeekBean
import com.example.xingliansdk.bean.YearBean
import com.example.xingliansdk.bean.room.AppDataBase
import com.example.xingliansdk.bean.room.MotionListBean
import com.example.xingliansdk.bean.room.MotionListDao
import com.example.xingliansdk.custom.MyMarkerView
import com.example.xingliansdk.network.api.dailyActiveBean.DailyActiveModel
import com.example.xingliansdk.network.api.dailyActiveBean.DailyActiveVoBean
import com.example.xingliansdk.utils.DeviceSportAxisValueFormatter
import com.example.xingliansdk.utils.HelpUtil
import com.example.xingliansdk.utils.ShowToast
import com.shon.connector.utils.TLog
import com.example.xingliansdk.view.CustomBarChart
import com.example.xingliansdk.view.DateUtil
import com.example.xingliansdk.view.DateUtil.minusTime
import com.example.xingliansdk.viewmodel.MainViewModel
import com.example.xingliansdk.widget.TitleBarLayout
import com.flyco.tablayout.listener.OnTabSelectListener
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.gyf.barlibrary.ImmersionBar
import com.ly.genjidialog.extensions.convertListenerFun
import com.ly.genjidialog.extensions.newGenjiDialog
import com.shon.connector.Config
import kotlinx.android.synthetic.main.activity_device_sport_chart.*
import java.util.*
import kotlin.collections.ArrayList

class DeviceSportChartActivity : BaseActivity<DailyActiveModel>(), View.OnClickListener,
    OnChartValueSelectedListener {
    override fun layoutId() = R.layout.activity_device_sport_chart
    private val mTitles  = arrayOf("日", "周", "月", "年")
    private lateinit var chart: BarChart
    private var position = 0
    lateinit var motionListDao: MotionListDao
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
        tabDate.setTabData(mTitles)
        motionListDao = AppDataBase.instance.getMotionListDao()
        TLog.error("数据+" + Gson().toJson(motionListDao.getAllRoomMotionList()))
        onClickListener()
        setTitleDateData()
        chartInitView()
    }


    override fun createObserver() {
        super.createObserver()
        mViewModel.msg.observe(this)
        {

        }
        mViewModel.result.observe(this) { it ->
            TLog.error("查询到的==" + Gson().toJson(it))
            var data = Gson().fromJson(Gson().toJson(it), DailyActiveVoBean::class.java)
            if (data == null || data.list == null || data.list.size <= 0) {
                return@observe
            }

            data.list.forEach { list ->
                var stepList: MutableList<Int> = mutableListOf()
                list.data.forEach { data ->
                    stepList.add(data.steps)
                }
                motionListDao.insert(
                    MotionListBean(
                        list.startTimestamp,
                        list.endTimestamp,
                        Gson().toJson(stepList),
                        list.totalSteps,
                        true,
                        list.date
                    )
                )

            }
            chartInitView()
        }
    }

    var yearStatus = false
    var mothStatus = false
    var weekStatus = false
    var dayStatus = false
    var yearCalendar: Calendar = Calendar.getInstance()
    var monthCalendar: Calendar = Calendar.getInstance()
    var weekCalendar: Calendar = Calendar.getInstance()
    var dayCalendar: Calendar = Calendar.getInstance()

    private fun onClickListener() {
        img_left.setOnClickListener(this)
        img_right.setOnClickListener(this)
        tabDate.setOnTabSelectListener(object :OnTabSelectListener{
            override fun onTabSelect(position: Int) {
                this@DeviceSportChartActivity.position  = position
                chart.highlightValue(null)
                if ( position == 0) {
                    titleBar.showActionImage(true)
                } else {
                    titleBar.showActionImage(false)
                }
                when (position) {
                    0 -> {
                        if (!dayStatus) {
                            dayStatus = true
                        }
                        setTitleDateData()
                    }
                    1 -> {
                        if (!weekStatus) {
                            weekStatus = true
                            weekCalendar = DateUtil.getWeekFirstDate(Calendar.getInstance())

                        }
                        setTitleDateData()
                    }
                    2 -> {
                        if (!mothStatus) {
                            mothStatus = true
                            monthCalendar = Calendar.getInstance()
                            monthCalendar?.set(Calendar.DAY_OF_MONTH, 1) //把日期设置为当月第一天
                        }
                        setTitleDateData()
                    }
                    3 -> {
                        if (!yearStatus) {
                            yearStatus = true
                            yearCalendar = Calendar.getInstance()
                            yearCalendar?.set(Calendar.MONTH, 0)
                            yearCalendar?.set(Calendar.DAY_OF_MONTH, 1) //把日期设置为当月第一天
                            yearCalendar.add(Calendar.YEAR, 0)
                        }
                        setTitleDateData()
                    }
                }
                chartInitView()
            }

            override fun onTabReselect(position: Int) {
            }
        })
    }

    var mv: MyMarkerView? = null
    private fun chartInitView() {
        chart = chart1
        chart.description.isEnabled = false
        chart.setMaxVisibleValueCount(60)
        chart.setPinchZoom(false)
        chart.setDrawBarShadow(false)
        chart.setDrawGridBackground(false)
        chart.setOnChartValueSelectedListener(this)
//        chart.defaultFocusHighlightEnabled=true
        chart.viewPortHandler.setMinMaxScaleX(1.0f,3.0f)
        chart.viewPortHandler.setMinMaxScaleY(1.0f,3.0f)
        chart.isScaleYEnabled = false
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        lateinit var timeMatter: IAxisValueFormatter
        when (position) {
            0 -> {
                tvHours.visibility = View.VISIBLE
                TLog.error("value==" + position)
                timeMatter = DeviceSportAxisValueFormatter(chart, position)
                xAxis.granularity = 12f
                xAxis.labelCount = 5
            }
            1 -> {
                timeMatter =
                    DeviceSportAxisValueFormatter(chart, position, weekCalendar.timeInMillis)
                //  xAxis.axisMaximum = 6f
                xAxis.granularity = 1f
                xAxis.labelCount = 7
                tvHours.visibility = View.GONE
            }
            2 -> {
                timeMatter =
                    DeviceSportAxisValueFormatter(chart, position, monthCalendar.timeInMillis)
                // xAxis.axisMaximum = 30f
                xAxis.granularity = 5f
                xAxis.labelCount = 6
                tvHours.visibility = View.GONE
            }
            3 -> {
                timeMatter = DeviceSportAxisValueFormatter(chart, position)
                // xAxis.axisMaximum = 12f
                xAxis.granularity = 1f
                xAxis.labelCount = 12
                tvHours.visibility = View.GONE
            }
        }
        //隐藏左边Y轴
        xAxis.setDrawGridLines(false)
        if (timeMatter != null)
            xAxis.valueFormatter = timeMatter
        val mRAxis = chart.axisRight
        mRAxis.isEnabled = false//隐藏右边Y轴
        val mLAxis = chart.axisLeft
        mLAxis.isEnabled = false//隐藏左边Y轴
        mLAxis.setDrawGridLines(false)
        mv = MyMarkerView(this, R.layout.custom_marker_view)
        mv?.chartView = chart
        chart.marker = mv
        chart.axisLeft.setDrawGridLines(false)
        chart.animateY(1000)
        chart.legend.isEnabled = false
        setData()
    }

    private fun setData() {
        val values = ArrayList<BarEntry>()
        var totalStep = 0F
        if (position == 0) {
            view_2.visibility = View.GONE
            llRight.visibility = View.GONE
        } else {
            view_2.visibility = View.VISIBLE
            llRight.visibility = View.VISIBLE
        }
        var mListSize = 0
        when (position) {
            0 -> {
                totalStep = 0F
                val list: MotionListBean =
                    motionListDao.getDayStep(DateUtil.getDate(DateUtil.YYYY_MM_DD, dayCalendar))
                TLog.error("运动大数据==" + Gson().toJson(list))
                if (list != null && !list.stepList.isNullOrEmpty()) {
                    var stepList =
                        Gson().fromJson<ArrayList<Int>>(list.stepList, ArrayList::class.java)
                    stepList.forEachIndexed { index, any ->
                        values.add(BarEntry(index.toFloat(), any.toString().toFloat()))
                        totalStep += any
                    }
                }
                while (values.size < 48)
                    values.add(BarEntry(values.size.toFloat(), 0F))
                mListSize = 1
            }
            1 -> {
                var weekList: List<MotionListBean> = motionListDao.getTimeStepList(
                    minusTime(weekCalendar.timeInMillis),
                    (minusTime(weekCalendar.timeInMillis) + 86400 * 6)
                )
                weekList.forEach {
                    if(it.totalSteps>0)
                        mListSize++
                }

                TLog.error("长度++" + weekList.size)
                totalStep = 0F
                val weekBean: ArrayList<WeekBean> = ArrayList()
                for (i in 0 until 7) {
                    weekBean.add(
                        WeekBean(
                            minusTime(weekCalendar.timeInMillis) + 86400 * i,
                            0
                        )
                    )
                    values.add(BarEntry(i.toFloat(), 0F))
                }
                for (motionListBean in weekList) {
                    var find = weekBean.find {
                        it.time >= motionListBean.startTime
                                &&
                                it.time <= motionListBean.endTime
                    }
                    if (find != null) {
                        find.totalStep = motionListBean.totalSteps
                        val indexOf: Int = weekBean.indexOf(find)
                        totalStep += find.totalStep
                        values[indexOf] = BarEntry(indexOf.toFloat(), find.totalStep.toFloat())
                    }

                }
            }
            2 -> {
                var monthList: List<MotionListBean> = motionListDao.getList(
                    DateUtil.getDate(DateUtil.YYYY_MM, monthCalendar)
                )
            //    mListSize = monthList.size
                monthList.forEach {
                    if(it.totalSteps>0)
                        mListSize++
                }
                totalStep = 0F
                val weekBean: ArrayList<WeekBean> = ArrayList()
                val day = monthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                for (i in 0 until day) {
                    weekBean.add(
                        WeekBean(
                            minusTime(monthCalendar.timeInMillis) + 86400 * i * 1000L,
                            0,
                            DateUtil.getDate(
                                DateUtil.YYYY_MM_DD,
                                monthCalendar.timeInMillis + 86400 * i * 1000L
                            )
                        )
                    )
                    values.add(BarEntry(i.toFloat(), 0F))
                }
                for (motionListBean in monthList) {
                    var find = weekBean.find {
                        it.date == motionListBean.dateTime
                    }
                    if (find != null) {
                        find.totalStep = motionListBean.totalSteps
                        val indexOf: Int = weekBean.indexOf(find)
                        totalStep += find.totalStep
                        values[indexOf] = BarEntry(indexOf.toFloat(), find.totalStep.toFloat())
                    }
                }
            }
            3 -> {
                totalStep = 0F
                var yearList: List<MotionListBean> = motionListDao.getList(
                    DateUtil.getDate(DateUtil.YYYY, yearCalendar)
                )
               // mListSize = yearList.size
                yearList.forEach {
                    if(it.totalSteps>0)
                        mListSize++
                }
                TLog.error("yearList+=" + Gson().toJson(yearList))
                val monthList: ArrayList<YearBean> = ArrayList()
                for (i in 0 until 12) {
                    val yearBean = YearBean(yearCalendar.get(Calendar.YEAR), i, 0)
                    TLog.error("yearBean++"+Gson().toJson(yearBean))
                    monthList.add(yearBean)
                    values.add(BarEntry(i.toFloat(), 0F))
                }

                for (motionListBean in yearList) {
                    val calendar =
                        DateUtil.convertLongToCalendar((motionListBean.startTime) * 1000)
                    TLog.error("calendar++"+calendar.toString())
                    var find = monthList.find { yearBean ->
                        TLog.error("yearBean.month+" + yearBean.month)
                        TLog.error("calendarYear.get(Calendar.MONTH)+" + calendar.get(Calendar.MONTH))
                        yearBean.year == calendar.get(Calendar.YEAR) && yearBean.month == calendar.get(
                            Calendar.MONTH
                        )
                    }
                    if (find != null) {
                        val indexOf = monthList.indexOf(find)
                        find.totalStep += motionListBean.totalSteps
                        totalStep += motionListBean.totalSteps
                        TLog.error("数据总和步数" + totalStep)
                        values[indexOf] = BarEntry(indexOf.toFloat(), find.totalStep.toFloat())
                    }
                }
                TLog.error("values" + Gson().toJson(values))
            }
        }
        tvTotalStep.text = HelpUtil.getSpan(totalStep.toLong().toString(), "步", 11)
        if (mListSize <= 0)
            mListSize = 1
        tvStep.text = HelpUtil.getSpan(
            (totalStep / mListSize).toLong().toString(), "步", 11
        )
        val set1: BarDataSet
//        if (chart.data != null &&
//            chart.data.dataSetCount > 0
//        ) {
//            set1 = chart.data.getDataSetByIndex(0) as BarDataSet
//            set1.values = values
//            chart.data.notifyDataChanged()
//            chart.notifyDataSetChanged()
//        } else {
        set1 = BarDataSet(values, "")
        set1.color = resources.getColor(R.color.color_marker)
        set1.setDrawValues(false)
        set1.highLightColor = resources.getColor(R.color.color_marker)
        val dataSets = ArrayList<IBarDataSet>()
        dataSets.add(set1)
        val data =
            BarData(dataSets)
        if (position == 1 || position == 3) {
            data.barWidth = 0.3f
        }
        chart.data = data
        chart.setFitBars(true)
//        }
        chart.invalidate()
    }

    var calendarType: Calendar? = null
    private fun setTitleDateData() {
        // var calendar: Calendar? = null
        when (position) {
            0 -> {
                calendarType = dayCalendar
            }
            1 -> {
                calendarType = weekCalendar
            }
            2 -> {
                calendarType = monthCalendar
            }
            3 -> {
                calendarType = yearCalendar
            }
        }
        TLog.error("calendar++${calendarType?.timeInMillis}")
        //   timeDialog = calendar?.timeInMillis
        mViewModel.getDailyActive(
            position.toString(), DateUtil.getDate(
                DateUtil.YYYY_MM_DD,
                calendarType
            )
        )
        var date = DateUtil.getDate(DateUtil.YYYY_MM_DD_AND, calendarType)
        when (position) {
            1 -> {
                date += "-" + calendarType?.timeInMillis?.plus(86400 * 6 * 1000L)?.let {
                    DateUtil.getDate(
                        DateUtil.YYYY_MM_DD_AND,
                        it
                    )
                }
            }
            2 -> {
                date += "-" + calendarType?.timeInMillis?.plus(
                    86400 * (calendarType?.getActualMaximum(
                        Calendar.DAY_OF_MONTH
                    )!! - 1) * 1000L
                )?.let {
                    DateUtil.getDate(
                        DateUtil.YYYY_MM_DD_AND,
                        it
                    )
                }
            }
            3 -> {
                date += "-" + DateUtil.getDate(
                    DateUtil.YYYY_MM_DD_AND,
                    DateUtil.getYearLastDate(calendarType)
                )
            }
        }

        when (position) {
            0 -> {
                if (DateUtil.equalsToday(calendarType))
                    img_right.visibility = View.INVISIBLE
                else
                    img_right.visibility = View.VISIBLE
            }
            1 -> {
                // if (DateUtil.equalsToday(calendarType))
                if (DateUtil.getWeekLastDate(calendarType).timeInMillis >= timeDialog)
                    img_right.visibility = View.INVISIBLE
                else
                    img_right.visibility = View.VISIBLE
            }
            2 -> {
                if (calendarType?.get(Calendar.MONTH)!! >= DateUtil.getCurrentCalendar()
                        .get(Calendar.MONTH)
                )
                    img_right.visibility = View.INVISIBLE
                else
                    img_right.visibility = View.VISIBLE
            }
            3 -> {

                if (calendarType?.get(Calendar.YEAR)!! >= DateUtil.getCurrentCalendar()
                        .get(Calendar.YEAR)
                )
                    img_right.visibility = View.INVISIBLE
                else
                    img_right.visibility = View.VISIBLE
                TLog.error("展示 date+$date")
            }
        }
        tvTypeTime.text = date
    }

    private fun refreshCurrentSelectedDateData() {
//        if (!IF.isEmpty(lastTodayDate)) {
//            val curTodayDate = DateUtil.getCurrentDate(DateUtil.YYYY_MM_DD)
//            if (DateUtil.getDateOffset(curTodayDate, lastTodayDate) >= 1) {
//                //检测到跨天!, 刷新界面为最新时间
//                XingLianApplication.setSelectedCalendar(DateUtil.getCurrentCalendar())
//            }
//        }
        //刷新标题
        setTitleDateData()
        chartInitView()
        // setData()
    }

    /**
     * 设置标题日期相关数据
     */
    private var timeDialog: Long = System.currentTimeMillis()//默认为当天时间
    private var calendar: Calendar? = null
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
                    calendar?.set(year, month, dayOfMonth, 0, 0)
                    var calendarTime = calendar?.timeInMillis
                    TLog.error("calendarTime==" + calendarTime)
                    TLog.error("timeDialog==" + timeDialog)
                    if (calendarTime!! > timeDialog!!) {
                        ShowToast.showToastLong("不可选择大于今天的日期")
                        return@setOnDateChangeListener
                    }
                    dayCalendar?.timeInMillis = calendarTime!!
                    refreshCurrentSelectedDateData()
                    dialog.dismiss()
                }
            }
        }.showOnWindow(supportFragmentManager)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.img_left -> {
                when (position) {
                    0 ->
                        dayCalendar.add(Calendar.DAY_OF_MONTH, -1)
                    1 -> {
                        weekCalendar.add(Calendar.DAY_OF_MONTH, -7)
                    }
                    2 -> {
                        monthCalendar.add(Calendar.MONTH, -1)
                    }
                    3 -> {
                        yearCalendar.add(Calendar.YEAR, -1)
                    }
                }
                refreshCurrentSelectedDateData()
            }
            R.id.img_right -> {
                when (position) {
                    0 ->
                        dayCalendar.add(Calendar.DAY_OF_MONTH, 1)
                    1 -> {
                        //XingLianApplication.getSelectedCalendar()?.add(Calendar.DAY_OF_MONTH, +7)
                        weekCalendar.add(Calendar.DAY_OF_MONTH, +7)
                    }
                    2 -> {
                        monthCalendar.add(Calendar.MONTH, 1)

                    }
                    3 -> {
                        yearCalendar.add(Calendar.YEAR, 1)
                    }
                }

                refreshCurrentSelectedDateData()
            }
        }
    }

    override fun onValueSelected(e: Entry, h: Highlight?) {
        TLog.error("点击" + e.y.toInt())
        if(e.y.toInt()<=0) {
            chart.highlightValue(null)
        }
        else {
            chart.highlightValue(h)
        }
    }

    override fun onNothingSelected() {
    }
}

