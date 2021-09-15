package com.example.xingliansdk.ui.setting.alarmClock

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.FrameLayout
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.listener.OnTimeSelectListener
import com.bigkoo.pickerview.view.TimePickerView
import com.example.xingliansdk.Config.database.TIME_LIST
import com.example.xingliansdk.R
import com.example.xingliansdk.base.BaseActivity
import com.example.xingliansdk.base.viewmodel.BaseViewModel
import com.example.xingliansdk.utils.ShowToast
import com.shon.connector.utils.TLog
import com.example.xingliansdk.view.DateUtil
import com.example.xingliansdk.widget.TitleBarLayout
import com.google.gson.Gson
import com.gyf.barlibrary.ImmersionBar
import com.ly.genjidialog.extensions.convertListenerFun
import com.ly.genjidialog.extensions.newGenjiDialog
import com.ly.genjidialog.other.DialogGravity
import com.orhanobut.hawk.Hawk
import com.shon.connector.BleWrite
import com.shon.connector.bean.TimeBean
import kotlinx.android.synthetic.main.activity_alarm_clock.*

class AlarmClockActivity : BaseActivity<BaseViewModel>(), View.OnClickListener {
    var mTimeBean: TimeBean = TimeBean()
    lateinit var mTimeList: ArrayList<TimeBean>
    var position = -1
    var time=DateUtil.getTodayZero()  //默认早晨8:00
    override fun layoutId(): Int = R.layout.activity_alarm_clock
    override fun initView(savedInstanceState: Bundle?) {
        ImmersionBar.with(this)
            .titleBar(titleBar)
            .init()
        TLog.error("Time++"+time)
        titleBar.setTitleBarListener(object : TitleBarLayout.TitleBarListener {
            override fun onBackClick() {
                finish()
            }

            override fun onActionImageClick() {

            }

            override fun onActionClick() {

                if (mTimeList.size > 5) {
                    ShowToast.showToastLong("闹钟最多只可添加五条")
                    return
                }
                TLog.error("mTimeList.size++" + mTimeList.size + " , num==" + num)
                mTimeBean.switch = 2
                mTimeBean.unicode = edt_remarks.text.toString()
                mTimeBean.setSpecifiedTimeDescription(Setting_alarm_clock.getContentText())
                mTimeBean.unicodeType = TimeBean.ALARM_FEATURES_UNICODE
                mTimeBean.startTime=time
                if (time<System.currentTimeMillis()) {
                    mTimeBean.endTime = time + 86400000
                }
                else
                {
                    mTimeBean.endTime = time
                }
                TLog.error(" mTimeBean.endTime=="+ mTimeBean.endTime)
                mTimeBean.specifiedTime = num
                if (mTimeList.size > 0 && position >= 0) {
                    mTimeList[position] = mTimeBean
                } else {
                    mTimeBean.number = mTimeList.size
                    mTimeList.add(mTimeBean)
                }
                Hawk.put(TIME_LIST, mTimeList)
                TLog.error("m==" + Gson().toJson(mTimeBean))
                TLog.error("m==" + Gson().toJson(mTimeList))
                mTimeList.forEach {
                    if(it.startTime<System.currentTimeMillis())
                        it.switch=1
                    TLog.error("===最终结果"+Gson().toJson(it))
                    BleWrite.writeAlarmClockScheduleCall(it,false)
                }
               // BleWrite.writeAlarmClockScheduleCall(mTimeBean,true)
                finish()
            }

        }
        )
        llTime.setOnClickListener(this)
        Setting_alarm_clock.setOnClickListener(this)
        mTimeList = if (Hawk.get<ArrayList<TimeBean>>(TIME_LIST).isNullOrEmpty())
            ArrayList()
        else
            Hawk.get<ArrayList<TimeBean>>(TIME_LIST)
        var type = intent.getIntExtra("type", 0)
        position = intent.getIntExtra("update", -1)
        TLog.error("post+$position")
        num = if(mTimeList.size<=0 || position<0)
            128
        else
            mTimeList[position].specifiedTime
        if (position >= 0) {
            mTimeBean = mTimeList[position]
            var hours=mTimeList[position].hours
            var min=mTimeList[position].min
            var mText=""
            mText = if(hours<9)
                "0$hours"
            else
                hours.toString()
            mText += if(min<9)
                ":0$min"
            else
                ":$min"
            tvTime.text= mText
            edt_remarks.setText(mTimeList[position].unicode)
            Setting_alarm_clock.setContentText(mTimeBean.getSpecifiedTimeDescription())
        }
        else {
            mTimeBean.characteristic = type//特征
            mTimeBean.hours=8
            mTimeBean.min=0
        }
        initTimePicker()
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.llTime -> {
                pvTime?.show()
            }
            R.id.Setting_alarm_clock->
            {
                dialog()
            }

        }
    }
    var num = 128//默认只执行一次
    var stringBuilder = StringBuilder()
    private fun dialog() {
        newGenjiDialog {
            layoutId = R.layout.dialog_week
            dimAmount = 0.3f
            isFullHorizontal = true
            isFullVerticalOverStatusBar = false
            gravity = DialogGravity.CENTER_BOTTOM
            animStyle = R.style.BottomTransAlphaADAnimation
            convertListenerFun { holder, dialog ->
                val radMonday = holder.getView<CheckBox>(R.id.rad_monday)
                val radTuesday = holder.getView<CheckBox>(R.id.rad_tuesday)
                val radWednesday = holder.getView<CheckBox>(R.id.rad_wednesday)
                val radThursday = holder.getView<CheckBox>(R.id.rad_thursday)
                val radFriday = holder.getView<CheckBox>(R.id.rad_friday)
                val radSaturday = holder.getView<CheckBox>(R.id.rad_saturday)
                val radSunday = holder.getView<CheckBox>(R.id.rad_sunday)
                val btnOk = holder.getView<Button>(R.id.btn_ok)
                val btnCancel = holder.getView<Button>(R.id.btn_cancel)
                if((num shr 1 and 1)==1)
                {
                    radMonday?.isChecked=true
                }
                if((num shr 2 and 1)==1)
                {
                    radTuesday?.isChecked=true
                }
                if((num shr 3 and 1)==1)
                {
                    radWednesday?.isChecked=true
                }
                if((num shr 4 and 1)==1)
                {
                    radThursday?.isChecked=true
                }
                if((num shr 5 and 1)==1)
                {
                    radFriday?.isChecked=true
                }
                if((num shr 6 and 1)==1)
                {
                    radSaturday?.isChecked=true
                }
                if((num  and 1)==1)
                {
                    radSunday?.isChecked=true
                }

                btnOk?.setOnClickListener {
                    num = 0
                    stringBuilder = StringBuilder()
                    if (radMonday?.isChecked == true) {
                        num += (1 shl 1)
                        stringBuilder.append("周一  ")
                    }
                    if (radTuesday?.isChecked == true) {
                        stringBuilder.append("周二  ")
                        num += (1 shl 2)
                    }
                    if (radWednesday?.isChecked == true) {
                        stringBuilder.append("周三  ")
                        num += (1 shl 3)
                    }
                    if (radThursday?.isChecked == true) {
                        stringBuilder.append("周四  ")
                        num += (1 shl 4)
                    }
                    if (radFriday?.isChecked == true) {
                        stringBuilder.append("周五  ")
                        num += (1 shl 5)
                    }
                    if (radSaturday?.isChecked == true) {
                        stringBuilder.append("周六  ")
                        num += (1 shl 6)
                    }
                    if (radSunday?.isChecked == true) {
                        stringBuilder.append("周日  ")
                        num += 1
                    }
                    mTimeBean.specifiedTime = num
                    TLog.error("num==$num")
                    Setting_alarm_clock.setContentText(stringBuilder.toString())
                    dialog.dismiss()
                }
                btnCancel?.setOnClickListener {
                    dialog.dismiss()
                }
            }
        }.showOnWindow(supportFragmentManager)
    }
    private var pvTime: TimePickerView? = null
    private fun initTimePicker() { //Dialog 模式下，在底部弹出
        pvTime = TimePickerBuilder(this,
            OnTimeSelectListener { date, v ->
                tvTime.text=DateUtil.getDate(DateUtil.HH_MM,date)
                mTimeBean.min=DateUtil.getMinute(date)
                mTimeBean.hours=DateUtil.getHour(date)
                time=date.time
                TLog.error("date.time=="+date.time)
                TLog.error("分+++"+mTimeBean.min+"时++"+mTimeBean.hours)
            })
            .setType(booleanArrayOf(false, false, false, true, true, false))
            .isDialog(true) //默认设置false ，内部实现将DecorView 作为它的父控件。
            .setItemVisibleCount(5) //若设置偶数，实际值会加1（比如设置6，则最大可见条目为7）
            .setLineSpacingMultiplier(2.0f)
            .isAlphaGradient(true)
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