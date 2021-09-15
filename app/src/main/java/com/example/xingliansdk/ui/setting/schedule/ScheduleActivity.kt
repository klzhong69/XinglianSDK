package com.example.xingliansdk.ui.setting.schedule

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.listener.OnTimeSelectListener
import com.bigkoo.pickerview.view.TimePickerView
import com.example.xingliansdk.Config.database.SCHEDULE_LIST
import com.example.xingliansdk.R
import com.example.xingliansdk.base.BaseActivity
import com.example.xingliansdk.base.viewmodel.BaseViewModel
import com.example.xingliansdk.utils.ShowToast
import com.shon.connector.utils.TLog
import com.example.xingliansdk.view.DateUtil
import com.example.xingliansdk.widget.TitleBarLayout
import com.google.gson.Gson
import com.gyf.barlibrary.ImmersionBar
import com.orhanobut.hawk.Hawk
import com.shon.connector.BleWrite
import com.shon.connector.bean.TimeBean
import kotlinx.android.synthetic.main.activity_schedule.*
import java.util.*
import kotlin.collections.ArrayList

class ScheduleActivity : BaseActivity<BaseViewModel>(), View.OnClickListener {
    var mTimeBean: TimeBean = TimeBean()
    lateinit var mTimeList: ArrayList<TimeBean>
    var position = -1
    var time=System.currentTimeMillis()
    var  dateDay=Calendar.getInstance()
    override fun layoutId(): Int = R.layout.activity_schedule
    override fun initView(savedInstanceState: Bundle?) {
        ImmersionBar.with(this)
            .titleBar(titleBar)
            .init()
        titleBar.setTitleText("添加日程")
        Setting_alarm_clock.setOnClickListener(this)
        SettingDateTime.setOnClickListener(this)
        titleBar.setTitleBarListener(object : TitleBarLayout.TitleBarListener {
            override fun onBackClick() {
                finish()
            }

            override fun onActionImageClick() {
            }

            override fun onActionClick() {
                TLog.error("m==" + edt_remarks.text.toString())
                if (mTimeList.size > 5) {
                    ShowToast.showToastLong("日程最多只可添加五条")
                    return
                }
                TLog.error("mTimeList.size++" + mTimeList.size)
                mTimeBean.switch = 2
                mTimeBean.unicode = edt_remarks.text.toString()
                mTimeBean.unicodeType = TimeBean.SCHEDULE_FEATURES_UNICODE
                mTimeBean.startTime=System.currentTimeMillis()
                mTimeBean.endTime=time
                if (mTimeList.size > 0 && position >= 0) {
                    mTimeList[position] = mTimeBean
                } else {
                    mTimeBean.number = mTimeList.size
                    mTimeList.add(mTimeBean)
                }

                Hawk.put(SCHEDULE_LIST, mTimeList)
                TLog.error("m==" + Gson().toJson(mTimeBean))
                TLog.error("m==" + Gson().toJson(mTimeList))
                BleWrite.writeAlarmClockScheduleCall(mTimeBean,true)
                finish()
            }


        })
        mTimeList = if (Hawk.get<ArrayList<TimeBean>>(SCHEDULE_LIST).isNullOrEmpty())
            ArrayList()
        else
            Hawk.get<ArrayList<TimeBean>>(SCHEDULE_LIST)
        position = intent.getIntExtra("update", -1)
        if (position >= 0) {
            mTimeBean = mTimeList[position]
            var year = mTimeList[position].year
            var month = mTimeList[position].month
            var day = mTimeList[position].day
            var hours = mTimeList[position].hours
            var min = mTimeList[position].min
            var mText = "${month}月${day}日"
            var hh=""
            hh += if (hours < 9)
                "0$hours"
            else
                hours.toString()
            hh += if (min < 9)
                ":0$min"
            else
                ":$min"
//            tvTime.text= mText
            SettingDateTime.setContentText(mText)
            TLog.error("mText+=" + mText)
            Setting_alarm_clock.setContentText(hh)
            edt_remarks.setText(mTimeList[position].unicode)
        } else {
            mTimeBean.characteristic = 2//特征
            SettingDateTime.setContentText(
                DateUtil.getDate(
                    DateUtil.MM_AND_DD_STRING,
                    System.currentTimeMillis()
                )
            )
            Setting_alarm_clock.setContentText(
                DateUtil.getDate(
                    DateUtil.HH_MM,
                    System.currentTimeMillis()
                )
            )
            // tvTime.text= DateUtil.getDate(DateUtil.YYYY_MM_DD_HH_MM,System.currentTimeMillis())
            mTimeBean.year = DateUtil.getYear(System.currentTimeMillis())
            mTimeBean.month = DateUtil.getMonth(System.currentTimeMillis())
            mTimeBean.day = DateUtil.getDay(System.currentTimeMillis())
            mTimeBean.min = DateUtil.getMinute(System.currentTimeMillis())
            mTimeBean.hours = DateUtil.getHour(System.currentTimeMillis())
        }
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.Setting_alarm_clock -> {
                initTimePicker(false)
                pvTime?.show()
            }
            R.id.SettingDateTime -> {
                initTimePicker(true)
                pvTime?.show()
            }
        }
    }

    private var pvTime: TimePickerView? = null
    private fun initTimePicker(yearOrHours: Boolean) { //Dialog 模式下，在底部弹出
        var hoursStatus = !yearOrHours
        pvTime = TimePickerBuilder(this
        ) { date, _ ->
            if (yearOrHours) {
                SettingDateTime.setContentText(
                    DateUtil.getDate(
                        DateUtil.MM_AND_DD_STRING,
                        date
                    )
                )
                mTimeBean.year = DateUtil.getYear(date)
                mTimeBean.month = DateUtil.getMonth(date)
                mTimeBean.day = DateUtil.getDay(date)
                dateDay.time=date
            } else {
                Setting_alarm_clock.setContentText(DateUtil.getDate(DateUtil.HH_MM, date))
                mTimeBean.min = DateUtil.getMinute(date)
                mTimeBean.hours = DateUtil.getHour(date)
            }
            time = date.time
            TLog.error("最终日期+" + date.time)
        }
            .setType(
                booleanArrayOf(
                    yearOrHours,
                    yearOrHours,
                    yearOrHours,
                    hoursStatus,
                    hoursStatus,
                    false
                )
            )
            .isDialog(true) //默认设置false ，内部实现将DecorView 作为它的父控件。
            .setItemVisibleCount(5) //若设置偶数，实际值会加1（比如设置6，则最大可见条目为7）
            .setLineSpacingMultiplier(2.0f)
            .isAlphaGradient(true)
            .setDate(dateDay)
            .isCyclic(true)
            .build()
        val mDialog: Dialog = pvTime?.dialog!!
        val params =
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM
            )
        params.leftMargin = 0
        params.rightMargin = 0
        pvTime?.let { it.dialogContainerLayout.layoutParams = params }
        val dialogWindow = mDialog.window
        if (dialogWindow != null) {
            dialogWindow.setWindowAnimations(com.bigkoo.pickerview.R.style.picker_view_slide_anim) //修改动画样式
            dialogWindow.setGravity(Gravity.BOTTOM) //改成Bottom,底部显示
            dialogWindow.setDimAmount(0.3f)
        }
    }
}