package com.example.xingliansdk

import android.graphics.Color
import android.os.Bundle
import com.example.xingliansdk.base.BaseActivity
import com.example.xingliansdk.ui.bloodOxygen.viewmodel.BloodOxygenViewModel
import com.example.xingliansdk.utils.HelpUtil
import com.example.xingliansdk.utils.TimeUtil
import com.example.xingliansdk.view.DateUtil
import com.example.xingliansdk.widget.TitleBarLayout
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.gson.Gson
import com.gyf.barlibrary.ImmersionBar
import com.shon.connector.utils.TLog
import kotlinx.android.synthetic.main.activity_heart_rate_o.*
class HeartRateTestActivity : BaseActivity<BloodOxygenViewModel>(),
    OnChartValueSelectedListener {
    private lateinit var mList: ArrayList<Int>
    private lateinit var hartsHrr: LineChart
    override fun layoutId() = R.layout.activity_heart_rate_o
    override fun initView(savedInstanceState: Bundle?) {
        ImmersionBar.with(this)
            .titleBar(titleBar)
            .init()
        mList = arrayListOf()
        titleBar.setTitleBarListener(object : TitleBarLayout.TitleBarListener {
            override fun onBackClick() {
                finish()
            }

            override fun onActionImageClick() {
            }

            override fun onActionClick() {
            }
        })
        hartsHrr = harts_hrr
        chartView()
        getHeart()
    }


    private fun chartView() {
        hartsHrr.description.isEnabled = false
        hartsHrr.legend.isEnabled = false  //色块不显示
        hartsHrr.setScaleEnabled(true)//设置比列启动
        hartsHrr.setOnChartValueSelectedListener(this)
        var xAxis: XAxis
        run {
            xAxis = hartsHrr.xAxis
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)//设置网格线
            xAxis.textColor = R.color.red
            xAxis.axisMaximum = 288f
            xAxis.granularity = 72f // 想 弄成 4 hour
            xAxis.setLabelCount(5,true)
//            xAxis.labelCount = 5
        }
        var leftAxis: YAxis
        run {
            leftAxis = hartsHrr.axisLeft
            leftAxis.isEnabled = false
            leftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)
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
            rightAxis.setDrawZeroLine(false)
        }
//        setDataView(mList)
   //     setData(1440,50f)
        hartsHrr.invalidate()
    }

    private var values =
        ArrayList<Entry>()

    private fun setDataView(mList: MutableList<Int>) {
        TLog.error("长度" + mList.size)
        TLog.error("最新值=" + Gson().toJson(mList))
        values = ArrayList()

        var setList: ArrayList<Int> = arrayListOf()
        var deviceList: ArrayList<Int> = arrayListOf()
        var num = 0
        var iFZero = 0
        var maxHeart = 0
        var minHeart = 999//默认一个比较大的值
        var avgHeart = 0L
        mList?.forEachIndexed { index, i ->
            run {
                if (i > maxHeart)
                    maxHeart = i
                if (i in 1 until minHeart) {
                    minHeart = i
                }

                num += i
                if (i == 0)
                    iFZero++
                if (index % 6 == 0) {//6个数组平分一组
                    var size =  //当为0时特殊处理
                        if ((6 - iFZero) <= 0)
                            1
                        else
                            (6 - iFZero)
                    var heart = num / size
                    setList.add(heart)
                    if (heart > 0) {
                        deviceList.add(heart)
                    }
                    avgHeart += heart
                    num = 0
                    iFZero = 0
                }
            }
        }
        /**
         * values装取所有数据然后再一次性 画完
         * ios那边告诉的结果是 values装数据的时候分段装 >0的装了直接画 画完了 在小于0的时候
         * 清空数据 然后直接画点不画任何东西 在for循环中画 而不是for循环 添加完了画
         */
        for (i in 0..9) {
            if (i <= 5) {
                continue
            }
            print("$i ")
        }
        setList.forEachIndexed { index, i ->
            values.add(Entry(index.toFloat(), i.toFloat()))
        }
        var set1: LineDataSet = LineDataSet(values, "")
        set1.color = resources.getColor(R.color.color_heart)
        set1.setDrawFilled(true)
        set1.fillColor = resources.getColor(R.color.color_heart_view)

        set1.setDrawCircles(false)//设置画圆点
        set1.setDrawValues(false)//设置缩放一定程度以后的展示文字

        //阴影错乱 无法展示正确的阴影
        set1.mode = LineDataSet.Mode.HORIZONTAL_BEZIER_Y_ZERO//圆弧
        //缩放一定比例时出现了只显示阴影不显示线形图问题
        //set1.mode = LineDataSet.Mode.Y_ZERO//不是圆弧
        val data = LineData(set1)
        hartsHrr.data = data
    }

    override fun onNothingSelected() {
    }

    override fun onValueSelected(e: Entry, h: Highlight) {
//        TLog.error("onValueSelected++" + e.x + ",  y==" + e.y + ",  Highlight+=" + h.x)
        tvType
        tvType.text = String.format(
            "%s",
            DateUtil.getDate(
                DateUtil.HH_MM,
                (TimeUtil.getTodayZero(0) + (e.x.toLong() * 60000L))
            )
        )
        tvHeart.text = HelpUtil.getSpan(h.y.toLong().toString(), "次/分钟")

    }


    private fun getHeart() {
        var nullList = arrayListOf<Int>()
//        for (i in 0 until 8640) {
//            when (i) {
//                in 0..100 -> nullList.add(0)
//                in 101..200 -> nullList.add((Math.random() * 10 + 60).toInt())
//                in 201..300 -> nullList.add(0)
//                in 301..400 -> nullList.add((Math.random() * 20 + 70).toInt())
//                in 401..1000 -> nullList.add(0)
//                in 1001..2000 -> nullList.add((Math.random() * 30 + 70).toInt())
//                in 2001..3000 -> nullList.add(0)
//                in 3001..4000 -> nullList.add((Math.random() * 40 + 70).toInt())
//                in 4001..5000 -> nullList.add(0)
//                in 5001..6000 -> nullList.add((Math.random() * 50 + 70).toInt())
//                in 6001..7000 -> nullList.add(0)
//                in 7001..8640 -> nullList.add((Math.random() * 100 + 40).toInt())
//            }
//        }
      //  setDataView(nullList)
        setData(288,10F)
        hartsHrr.invalidate()
    }
    private fun setData(count: Int, range: Float) {
        var values: ArrayList<Entry?> = ArrayList()
        val data = LineData()
        TLog.error("setData==")
        for (i in 0 until count) {
            TLog.error(" for  setData==$i")
            var test = (Math.random() * range+ (90)).toFloat()
            //                values.add(new Entry(i, 0));
            when (i) {
                in 11..39 ->{
                    val x = "${(i / 5)}:${(i % 5)}"
                    test=0f
                }

                in 101..199 ->{
                    val x = "${(i / 5)}:${(i % 5)}"
                    test=0f
                }
                in 201..210 -> test=0f
                in 251..288-> test=110f
            }
            TLog.error("test==$test  count+=$i")

            if (test <= 0) {
                TLog.error("<0="+count)
//                values.add(new Entry(i, 0));
                if (values.isNotEmpty()) {
                    TLog.error("values.isNotEmpty")
                    val set1 = LineDataSet(values, Math.random().toString())
                    set1.color = resources.getColor(R.color.color_heart)

                    set1.fillColor = resources.getColor(R.color.color_heart_view)
                    set1.fillFormatter =
                        IFillFormatter { _, _ -> hartsHrr.axisLeft.axisMinimum }
                    set1.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
                    set1.setDrawFilled(true)
                    set1.setDrawCircles(false)//设置画圆点
                    set1.setDrawValues(false)//设置缩放一定程度以后的展示文字
                    set1.fillColor = Color.BLUE
                    data.addDataSet(set1)
                }
                values = ArrayList()
            } else {
                values.add(Entry(i.toFloat(), test))
            }
        }
        if (values.isNotEmpty()) {
            val set1 = LineDataSet(values, Math.random().toString())
            set1.color = resources.getColor(R.color.color_heart)
            set1.fillColor = resources.getColor(R.color.color_heart_view)
            set1.fillFormatter =
                IFillFormatter { _, _ -> hartsHrr.axisLeft.axisMinimum }
            set1.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
            set1.setDrawFilled(true)
            set1.setDrawCircles(false)//设置画圆点
            set1.setDrawValues(false)//设置缩放一定程度以后的展示文字
            set1.fillColor = Color.BLUE
            data.addDataSet(set1)
        }

        data.setDrawValues(false)

        hartsHrr.data = data
    }
}