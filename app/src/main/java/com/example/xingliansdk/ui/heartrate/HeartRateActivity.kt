package com.example.xingliansdk.ui.heartrate

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.CalendarView
import com.example.xingliansdk.R
import com.example.xingliansdk.XingLianApplication
import com.example.xingliansdk.base.BaseActivity
import com.example.xingliansdk.bean.room.*
import com.example.xingliansdk.network.api.heartView.HeartRateVoBean
import com.example.xingliansdk.ui.heartrate.viewmodel.HeartRateViewModel
import com.example.xingliansdk.utils.*
import com.example.xingliansdk.view.DateUtil
import com.example.xingliansdk.view.IF
import com.example.xingliansdk.widget.TitleBarLayout
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.MPPointF
import com.google.gson.Gson
import com.gyf.barlibrary.ImmersionBar
import com.ly.genjidialog.extensions.convertListenerFun
import com.ly.genjidialog.extensions.newGenjiDialog
import com.shon.connector.BleWrite
import com.shon.connector.Config
import com.shon.connector.bean.TimeBean
import com.shon.connector.utils.TLog
import kotlinx.android.synthetic.main.activity_heart_rate_o.*

import java.util.*
import kotlin.collections.ArrayList


fun Long.formatTime(): String? {
    return DateUtil.getDate(
        DateUtil.YYYY_MM_DD,
        (Config.TIME_START + this) * 1000L
    )
}

class HeartRateActivity : BaseActivity<HeartRateViewModel>(), View.OnClickListener,
    OnChartValueSelectedListener,
    BleWrite.HistoryCallInterface,
    BleWrite.SpecifyHeartRateHistoryCallInterface,
    BleWrite.SpecifyBloodOxygenHistoryCallInterface {
    private lateinit var mList: ArrayList<Int>
    private lateinit var hartsHrr: LineChart
    var type = -1
    var position = 0
    private var heartRateList: MutableList<RoomTimeBean> = mutableListOf()
    private lateinit var sDao: RoomTimeDao

    //心率的
    private lateinit var mHeartListDao: HeartListDao
    override fun layoutId() = R.layout.activity_heart_rate_o
    override fun initView(savedInstanceState: Bundle?) {
        ImmersionBar.with(this)
            .titleBar(titleBar)
            .init()
        mList = arrayListOf()
        type = intent.getIntExtra("HistoryType", 0)
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
        img_left.setOnClickListener(this)
        img_right.setOnClickListener(this)
//        tvTypeTime.setOnClickListener(this)
        sDao = AppDataBase.instance.getRoomTimeDao()
        mHeartListDao = AppDataBase.instance.getHeartDao()
        val allRoomTimes = sDao.getAllRoomTimes()
//        TLog.error("个人信息+" + Gson().toJson(mDeviceInformationBean))
        TLog.error("获取mHeartListDao++${allRoomTimes.size}")
        TLog.error("获取一下时间库++${Gson().toJson(allRoomTimes)}")
        TLog.error("心率数组长度++${mHeartListDao.getAllRoomHeartList().size}")
        TLog.error("心率数组++${Gson().toJson(mHeartListDao.getAllRoomHeartList())}")

        if (allRoomTimes.size > 0) {
            heartRateList = allRoomTimes
            position = heartRateList.size - 1
        }
        hartsHrr = harts_hrr
        chartView()
        pieView()
        setView()
        setTitleDateData()
    }

    private fun setView() {
        var time = if (heartRateList != null && heartRateList.isNotEmpty())
            heartRateList[position].startTime.formatTime()!!
        else
            DateUtil.getDate(DateUtil.YYYY_MM_DD, System.currentTimeMillis())
        tvTypeTime.text = time
    }
    var setValueStatus=true  //显示值的设置
    override fun createObserver() {
        super.createObserver()
        mViewModel.result.observe(this) {
            TLog.error("it=" + Gson().toJson(it))
            setValueStatus=true
            var mList = Gson().fromJson(Gson().toJson(it), HeartRateVoBean::class.java)
            if (mList.heartRateVoList == null || mList.heartRateVoList.size <= 0) {
                date?.let { it1 -> getHeart(it1) }
                return@observe
            }
            setValueStatus=false
            var heartRateVoList=mList.heartRateVoList[0]
            var heartList = Gson().toJson(heartRateVoList.heartRate)
            TLog.error("mlist=" + Gson().toJson(mList))
            mHeartListDao.insert(
                HeartListBean(
                    heartRateVoList.startTimestamp.toLong() ,
                    heartRateVoList.endTimestamp.toLong() ,
                    heartList,
                    false,
                    heartRateVoList.date
                )
            )
            TLog.error("heartRateVoList.max=="+heartRateVoList.max)
            TLog.error("heartRateVoList.min=="+heartRateVoList.avg)
            TLog.error("heartRateVoList.avg=="+heartRateVoList.min)
            TLog.error("heartRateVoList.max=="+heartRateVoList.max.toDouble().toInt())

            setValue(heartRateVoList.max.toDouble().toInt(),heartRateVoList.min.toDouble().toInt(),heartRateVoList.avg.toDouble().toInt())
            getHeart(heartRateVoList.date)

        }
        mViewModel.msg.observe(this) {
            date?.let { it1 ->
                setValueStatus=true//错误的情况都是自己算
                getHeart(it1) }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
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
            R.id.tvTypeTime -> {
            //    dialog()
            }

        }
    }

    private fun chartView() {
        hartsHrr.description.isEnabled = false
        hartsHrr.legend.isEnabled = false  //色块不显示
        hartsHrr.setScaleEnabled(true)//设置比列启动
        hartsHrr.setOnChartValueSelectedListener(this)
        hartsHrr.isScaleYEnabled = false
        hartsHrr.viewPortHandler.setMaximumScaleX(10f)
        var xAxis: XAxis
        val timeMatter: IAxisValueFormatter = HeartAxisValueFormatter(hartsHrr)
        run {
            xAxis = hartsHrr.xAxis
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)//设置网格线
            xAxis.textColor = R.color.red
            xAxis.axisMaximum = 288f
            xAxis.axisMinimum=0f
            xAxis.granularity = 72f // 想 弄成 4 hour
            xAxis.axisLineColor=Color.WHITE
//            xAxis.labelCount=5
            xAxis.setLabelCount(5, true)
            //   xAxis.labelCount = 4
            xAxis.valueFormatter = timeMatter
            xAxis.mLabelHeight =20
            xAxis.mLabelRotatedHeight=20
        }
        var leftAxis: YAxis
        run {
            leftAxis = hartsHrr.axisLeft
            leftAxis.isEnabled = false
         //   leftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)
            leftAxis.axisMinimum = 40f
            leftAxis.axisMaximum = 220f
            leftAxis.setDrawZeroLine(false)
        }
        var rightAxis: YAxis
        run {
            rightAxis = hartsHrr.axisRight
            rightAxis.axisMinimum = 40f
            rightAxis.axisMaximum = 220f
            rightAxis.axisLineColor = Color.WHITE
            rightAxis.zeroLineColor = resources.getColor(R.color.color_view)
            rightAxis.gridColor = resources.getColor(R.color.color_view)
            rightAxis.granularity = 40f
            rightAxis.setDrawZeroLine(false)
        }

//        val mv = XYMarkerView(this, timeMatter)
//        mv.chartView = hartsHrr // For bounds control
//        hartsHrr.marker = mv // Set the marker to the chart
        hartsHrr.extraBottomOffset=20f
        setDataView(mList)
        hartsHrr.invalidate()
    }

    private fun pieView() {
        pieChart.setUsePercentValues(true)//设置百分比
        pieChart.description.isEnabled = false
        pieChart.legend.form = Legend.LegendForm.CIRCLE
        pieChart.dragDecelerationFrictionCoef = 0.95f
        pieChart.setCenterTextColor(Color.BLACK)
        pieChart.setCenterTextSize(16f)
        pieChart.centerText = " 心 率 \n 区 分 "
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.WHITE)
        pieChart.setTransparentCircleColor(Color.WHITE)
        pieChart.setTransparentCircleAlpha(110)
        pieChart.holeRadius = 58f
        pieChart.transparentCircleRadius = 61f
        pieChart.setDrawCenterText(true)
        pieChart.rotationAngle = 0f
        pieChart.isRotationEnabled = false
        pieChart.isHighlightPerTapEnabled = true
        pieChart.setDrawEntryLabels(false)
        pieChart.animateY(1000, Easing.EaseInOutQuad)
        val l: Legend = pieChart.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.CENTER
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        l.orientation = Legend.LegendOrientation.VERTICAL
        l.setDrawInside(false)
        l.yEntrySpace = 20f
        pieChart.setEntryLabelColor(Color.WHITE)
        pieChart.setEntryLabelTextSize(12f)
    }

    private var values =
        ArrayList<Entry>()

    private var pieChartStatus = false
    private fun setDataView(mList: MutableList<Int>) {
        // TLog.error("长度" + mList.size)
        pieChartStatus = false
        values = ArrayList()
        var setList: ArrayList<Int> = arrayListOf()
        var deviceList: ArrayList<Int> = arrayListOf()
        var pieList: ArrayList<Float> = arrayListOf(0F, 0F, 0F, 0F, 0F)
        var num = 0
        var iFZero = 0    //30个平均为0排除
        var maxHeart = 0  //86400中最大的值
        var maxIndex = 0  //最大值属于长度 288的哪一位置
        var minHeart = 999//最小值, 默认一个比较大的值
        var minIndex = 0  //最小值属于长度 288的哪一位置
        var avgHeart = 0L //平均值
        var avgNotZero = 0//平均值不为0有多少次
        mList?.forEachIndexed { index, i ->
            run {

                if (i > maxHeart) {
                    maxHeart = i
                    maxIndex = index
                }
                if (i in 1 until minHeart) {
                    minHeart = i
                    minIndex = index
                }
                num += i
                if (i == 0)
                    iFZero++
                else {
                    avgNotZero++
                    avgHeart += i
                }
                if ((index + 1) % 30 == 0) {//6个数组平分一组
                    var size =  //当为0时特殊处理
                        if ((30 - iFZero) <= 0)
                            1
                        else
                            (30 - iFZero)
                    var heart = num / size
                    setList.add(heart)
                    if (heart > 0) {
                        deviceList.add(heart)
                    }
                    num = 0
                    iFZero = 0
                }
                when (i) {
                    in 80..109 -> {
                        pieChartStatus = true
                        pieList[4] += 10f
                    }
                    in 110..129 -> {
                        pieChartStatus = true
                        pieList[3] += 10f
                    }
                    in 130..159 -> {
                        pieChartStatus = true
                        pieList[2] += 10f
                    }
                    in 160..179 -> {
                        pieChartStatus = true
                        pieList[1] += 10f
                    }
                    in 180..219 -> {
                        pieChartStatus = true
                        pieList[0] += 10f
                    }

                    //                if (i >= targetHeartRate(0.5) && i < targetHeartRate(0.6)) {
                    //                    pieChartStatus = true
                    //                    pieList[4] += 10f
                    //                } else if (i >= targetHeartRate(0.6) && i < targetHeartRate(0.7)) {
                    //                    pieChartStatus = true
                    //                    pieList[3] += 10f
                    //                    TLog.error(" pieList[3]+" + pieList[3])
                    //                } else if (i >= targetHeartRate(0.7) && i < targetHeartRate(0.8)) {
                    //                    pieChartStatus = true
                    //                    pieList[2] += 10f
                    //                } else if (i >= targetHeartRate(0.8) && i < targetHeartRate(0.9)) {
                    //                    pieChartStatus = true
                    //                    pieList[1] += 10f
                    //                } else if (i >= targetHeartRate(0.9) && i < targetHeartRate(1.0)) {
                    //                    pieChartStatus = true
                    //                    pieList[0] += 10f
                    //                }
                }

//
            }
        }
//        TLog.error("setList==" + Gson().toJson(setList))
//        TLog.error("setList==" + setList.size)
//        TLog.error("maxIndex+$maxIndex   maxIndex/30+=${maxIndex / 30}")
        //上面取完了下面再来放最大最小值
        if (mList.size > 0) {
            if (maxIndex / 30 == minIndex / 30) {
                setList[maxIndex / 30] = maxHeart
            } else {
                setList[maxIndex / 30] = maxHeart
                setList[minIndex / 30] = minHeart
            }
        }
        //自己算的情况
        if (minHeart >= 999)
            minHeart = 0
        if (avgNotZero <= 0)
            avgNotZero = 1
        if (setValueStatus) {
            TLog.error("过来")
            setValue(maxHeart, minHeart, (avgHeart / avgNotZero).toInt())
        }

        /**
         * values装取所有数据然后再一次性 画完
         * ios那边告诉的结果是 values装数据的时候分段装 >0的装了直接画 画完了 在小于0的时候
         * 清空数据 然后直接画点不画任何东西 在for循环中画 而不是for循环 添加完了画
         */
        var data = LineData()
        setList.forEachIndexed { index, i ->
//            if (i > 0)
//            TLog.error("setList 非0==$i")
//            if (i <= 0) {
//                if (values.isNotEmpty()) {
//                    val set1 = LineDataSet(values, "")
//                    set1.color = resources.getColor(R.color.color_heart)
//                    set1.setDrawFilled(true)
//                    set1.fillColor = resources.getColor(R.color.color_heart_view)
//                    set1.fillFormatter =
//                        IFillFormatter { _, _ -> hartsHrr.axisLeft.axisMinimum }
//                    set1.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
//                    set1.setDrawCircles(false)//设置画圆点
//                    set1.setDrawValues(false)//设置缩放一定程度以后的展示文字
//                    set1.fillColor = Color.BLUE
//                    data.addDataSet(set1)
//                }
//                values = ArrayList()
//            } else {
//                values.add(Entry(index.toFloat(), i.toFloat()))
//            }

            values.add(Entry(index.toFloat(), i.toFloat()))
        }
        if (values.isNotEmpty()) {
            var set1 = LineDataSet(values, "")
            set1.color = resources.getColor(R.color.color_heart)
            set1.setDrawFilled(true)
            set1.fillColor = resources.getColor(R.color.color_heart_view)
            set1.fillFormatter =
                IFillFormatter { _, _ -> hartsHrr.axisLeft.axisMinimum }
            set1.setDrawCircles(false)//设置画圆点
            set1.setDrawValues(false)//设置缩放一定程度以后的展示文字
            set1.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
            data = LineData(set1)
        }
        hartsHrr.data = data

//        }
        var mDataPieCount = 0F
        if (deviceList.size > 0 && pieChartStatus) {
            TLog.error("deviceList==" + Gson().toJson(deviceList))
            TLog.error("pieChartStatus==" + Gson().toJson(pieChartStatus))
            for (i in 0 until pieList.size) {
                pieList[i] = (pieList[i].toLong() / 60).toFloat()
                mDataPieCount += pieList[i]
            }
//            if(mDataPieCount>0)
//            setDataPieView(pieList)
//            else
//                pieChart.visibility = View.GONE
        } else
            pieChart.visibility = View.GONE

//        }
    }

    private fun setDataPieView(mList: MutableList<Float>) {
        pieChart.visibility = View.GONE
        val entries = ArrayList<PieEntry>()
        val typeList = arrayListOf("无氧极限", "无氧耐力", "有氧耐力", "燃烧脂肪", "热身放松")
        val mListColor = arrayListOf(
            R.color.color_anaerobic_limit,
            R.color.color_anaerobic_endurance,
            R.color.color_aerobic_endurance,
            R.color.color_heart_fat_burning,
            R.color.color_warm_up_and_relax
        )
        TLog.error("数据++" + Gson().toJson(mList))
        for (i in 0 until mList.size) {
            if (mList[i] != 0F)
                entries.add(
                    PieEntry(
                        mList[i],
                        typeList[i] + "    " + (mList[i].toLong()) + "分钟"
                    )
                )
        }
        val dataSet = PieDataSet(entries, " ")
        dataSet.setDrawIcons(false)
        dataSet.sliceSpace = 3f
        dataSet.iconsOffset = MPPointF(0f, 40f)
        dataSet.selectionShift = 5f

        // add a lot of colors
        val colors = ArrayList<Int>()
        for (i in 0 until mList.size) {
            if (mList[i] > 0)
                colors.add(resources.getColor(mListColor[i]))
        }
        dataSet.colors = colors
        //dataSet.setSelectionShift(0f);
        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter())
        data.setValueTextSize(11f)
        data.setValueTextColor(Color.WHITE)
        data.setDrawValues(false)//文字不展示
        pieChart.data = data
        pieChart.invalidate()
    }

    override fun SpecifyHeartRateHistoryCallResult(
        startTime: Long,
        endTime: Long,
        mList: java.util.ArrayList<Int>
    ) {
        hideWaitDialog()
//        var setList: ArrayList<Int> = arrayListOf()
//        var num = 0
//        var iFZero = 0
//        mList?.forEachIndexed { index, i ->
//            run {
//                num += i
//                if (i == 0)
//                    iFZero++
//                if (index % 6 == 0) {//6个数组平分一组
//                    var size =  //当为0时特殊处理
//                        if ((6 - iFZero) <= 0)
//                            1
//                        else
//                            (6 - iFZero)
//                    setList.add(num / size)
//                    num = 0
//                    iFZero = 0
//                }
//            }
//        }
        val name: String = Gson().toJson(mList)
        if (endTime - startTime >= HeartListBean.day) {
            TLog.error("他妈一整天")
            //  sDao.updateTime(startTime, endTime)
            //  heartRateList = sDao.getAllRoomTimes()
            TLog.error("改变++${Gson().toJson(heartRateList)}")
            //   mHeartListDao.insert(HeartListBean(startTime, endTime, name, true))
        } else {
            TLog.error("他妈不到一整天==" + startTime + "endTime +" + endTime)
            //  mHeartListDao.insert(HeartListBean(startTime, endTime, name, false))
        }
        TLog.error("===" + mList.size)
        //   setDataView(mList)
        //   hartsHrr.invalidate()

    }

    override fun SpecifyHeartRateHistoryCallResult(mList: ArrayList<Int>?) {
    }

    override fun HistoryCallResult(key: Byte, mList: ArrayList<TimeBean>?) {
        if (mList?.size!! <= 0)
            return
        TLog.error("mList++${Gson().toJson(mList)}")
        mList.reverse()
        TLog.error("mList++${Gson().toJson(mList)}")
        updateData(mList)
        heartRateList = sDao.getAllRoomTimes()
        TLog.error("mList ++${mList[mList.size - 1].endTime}")
        TLog.error("sdao长度 ++${sDao.getAllRoomTimes().size}")
        TLog.error("sdao++${Gson().toJson(sDao.getAllRoomTimes())}")
        mList?.get(mList.size - 1)?.let {
            when (key) {
                Config.BigData.DEVICE_HEART_RATE -> {
                    TLog.error("DEVICE_SLEEP heartRateList.size+" + heartRateList.size)
                    TLog.error("DEVICE_SLEEP+" + Gson().toJson(mList))
                    position = heartRateList.size - 1
                    setView()
                }
                Config.BigData.DEVICE_BLOOD_OXYGEN -> {
                    position = heartRateList.size - 1
                    setView()
                }
            }
        }
    }

    override fun SpecifyBloodOxygenHistoryCallResult(
        startTime: Long,
        endTime: Long,
        mList: java.util.ArrayList<Int>
    ) {
        hideWaitDialog()
//        var setList: ArrayList<Int> = arrayListOf()
//        var num = 0
//        var iFZero = 0
//        mList?.forEachIndexed { index, i ->
//            run {
//                num += i
//                if (i == 0)
//                    iFZero++
//                if (index % 6 == 0) {//6个数组平分一组
//                    var size =  //当为0时特殊处理
//                        if ((6 - iFZero) <= 0)
//                            1
//                        else
//                            (6 - iFZero)
//                    setList.add(num / size)
//                    num = 0
//                    iFZero = 0
//                }
//            }
//        }
        val name: String = Gson().toJson(mList)
        if (endTime - startTime >= BloodOxygenListBean.day) {
            TLog.error("他妈一整天")
            sDao.updateBloodOxygen(startTime, endTime)
            heartRateList = sDao.getAllRoomTimes()
            TLog.error("改变++${Gson().toJson(heartRateList)}")
            // mBloodOxygenListDao.insert(BloodOxygenListBean(startTime, endTime, name, true))
        } else {
            TLog.error("他妈不到一整天")
            //   mBloodOxygenListDao.insert(BloodOxygenListBean(startTime, endTime, name, false))
        }
        TLog.error("===" + mList.size)
        setDataView(mList)
        hartsHrr.invalidate()

    }

    override fun SpecifyBloodOxygenHistoryCallResult(
        mList: ArrayList<Int>?
    ) {

    }

    override fun onNothingSelected() {
        TLog.error("onNothingSelected")
    }

    override fun onValueSelected(e: Entry, h: Highlight) {
//        TLog.error("onValueSelected++" + e.x + ",  y==" + e.y + ",  Highlight+=" + h.x)
        tvType.text = String.format(
            "%s",
            DateUtil.getDate(
                DateUtil.HH_MM,
                (TimeUtil.getTodayZero(0) + (e.x.toLong() * 5 * 60000L))
            )
        )
        tvHeart.text = HelpUtil.getSpan(h.y.toLong().toString(), "次/分钟")

    }


    private fun updateData(allData: ArrayList<TimeBean>) {
        TLog.error("updateData++" + Gson().toJson(allData))
        val allRoomTimes = sDao.getAllRoomTimes()
        allData.forEach { timeBean ->
            val find = allRoomTimes.find { roomTime ->
                timeBean.startTime == roomTime.startTime
            }
            //find 就是数据库中的数据
            if (find == null) {
                //不在数据库，插入新的数据

                sDao.insert(
                    RoomTimeBean(
                        timeBean.dataUnitType,
                        timeBean.timeInterval,
                        timeBean.startTime,
                        timeBean.endTime,
                        true,
                        false
                    )
                )
                TLog.error(
                    "不存在的+${
                        Gson().toJson(
                            sDao.getRoomTime(
                                timeBean.startTime,
                                timeBean.endTime
                            )
                        )
                    }"
                )
            } else {
                //这里已经存在
                TLog.error("这里已经存在")
                if (timeBean.endTime != find.endTime) {
                    //结束时间不相同，更新数据库
                    //赋值新数据
                    find.startTime = timeBean.startTime
                    find.endTime = timeBean.endTime
                    sDao.update(find)
                    TLog.error("timeBean修改" + timeBean.endTime)
                    TLog.error("find++" + find.endTime)
//                    BleWrite.writeSpecifyHeartRateHistoryCall(
//                        timeBean.startTime,
//                        timeBean.endTime,
//                        this
//                    )
                }
            }
        }
        TLog.error("sDao++${Gson().toJson(sDao.getAllRoomTimes())}")
    }

    /**
     * 设置标题日期相关数据
     */
    private var timeDialog: Long? = System.currentTimeMillis()//默认为当天时间
    private var lastTodayDate: String? = null
    var date: String? = null
    private fun setTitleDateData() {
        val calendar: Calendar? = XingLianApplication.getSelectedCalendar()
//        TLog.error("calendar++${calendar?.timeInMillis}")
        timeDialog = calendar?.timeInMillis
         date = DateUtil.getDate(DateUtil.YYYY_MM_DD, calendar)
        tvType.text = "00:00"
        tvHeart.text = "--"
        if (DateUtil.equalsToday(date)) {
         //   tvTypeTime.setText(R.string.title_today)
            lastTodayDate = date
            img_right.visibility = View.INVISIBLE
        } else {

            img_right.visibility = View.VISIBLE
//            tlTitle.setTitle(date)
        }
        tvTypeTime.text = date
//        DateUtil.getDayZero(timeDialog)
        TLog.error("timeDialog++" + DateUtil.getDayZero(timeDialog!!))
        TLog.error("timeDialog++" + (DateUtil.getDayZero(timeDialog!!) / 1000 - XingLianApplication.TIME_START))
        mViewModel.getHeartRate(
            (DateUtil.getDayZero(timeDialog!!) / 1000).toString(),
            (DateUtil.getDayEnd(timeDialog!!) / 1000).toString()
        )
        //  getHeart(date)
    }

    private fun getHeart(date: String) {
        TLog.error("datae++${Gson().toJson(date)}")
        TLog.error("list心率某天++${Gson().toJson(mHeartListDao.getSomedayHeartList(date).size)}")
        TLog.error("bean心率某天++${Gson().toJson(mHeartListDao.getSomedayHeart(date))}")
        val mHeartList = mHeartListDao.getSomedayHeart(date)
        if (mHeartList == null || mHeartList.heart.isNullOrEmpty() || mHeartList.heart == "[]") {
            var nullList = arrayListOf<Int>()
            for (i in 0 until 8640)
                nullList.add(0)
            setDataView(nullList)
            hartsHrr.invalidate()
            return
        }

        hideWaitDialog()
        val heartRateList =
            Gson().fromJson(mHeartList.heart, ArrayList::class.java)
        setDataView(heartRateList as ArrayList<Int>)
        hartsHrr.invalidate()

    }

    private fun targetHeartRate(hrm: Double): Int {
//    TLog.error("targetHeartRate==="+  (((220 - mDeviceInformationBean.age - 60) * hrm + 60).toInt()))
        return (((220 - mDeviceInformationBean.age - 60) * hrm + 60).toInt())
    }

    private fun refreshCurrentSelectedDateData() {
        if (!IF.isEmpty(lastTodayDate)) {
            val curTodayDate = DateUtil.getCurrentDate(DateUtil.YYYY_MM_DD)
            if (DateUtil.getDateOffset(curTodayDate, lastTodayDate) >= 1) {
                //检测到跨天!, 刷新界面为最新时间
                XingLianApplication.setSelectedCalendar(DateUtil.getCurrentCalendar())
            }
        }
        //刷新标题
        setTitleDateData()
    }

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
                    calendar?.set(year, month, dayOfMonth,0,0)//不设置时分不行
                    var calendarTime = calendar?.timeInMillis
                    if (calendarTime!! > toDay) {
                        TLog.error("calendarTime+= $calendarTime  DateUtil.getCurrentDate()++$toDay")
                        ShowToast.showToastLong("不可选择大于今天的日期")
                        return@setOnDateChangeListener
                    }
                    XingLianApplication.getSelectedCalendar()?.timeInMillis = calendarTime!!

                    refreshCurrentSelectedDateData()
                    dialog.dismiss()
                }
            }
        }.showOnWindow(supportFragmentManager)
    }

    private fun setValue( maxHeart:Int,minHeart:Int,avgHeart:Int)
    {
        tvMaxNum.text = HelpUtil.getSpan(maxHeart.toString(), "次/分钟", 11)

        tvMinNum.text = HelpUtil.getSpan(minHeart.toString(), "次/分钟", 11)

        tvAvgNum.text = HelpUtil.getSpan(avgHeart.toString(), "次/分钟", 11)
    }
}