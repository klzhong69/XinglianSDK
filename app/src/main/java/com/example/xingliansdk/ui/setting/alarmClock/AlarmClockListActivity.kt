package com.example.xingliansdk.ui.setting.alarmClock
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.xingliansdk.Config.database.TIME_LIST
import com.example.xingliansdk.R
import com.example.xingliansdk.adapter.AlarmClockAdapter
import com.example.xingliansdk.base.BaseActivity
import com.example.xingliansdk.base.viewmodel.BaseViewModel
import com.example.xingliansdk.utils.JumpUtil
import com.example.xingliansdk.utils.ShowToast
import com.example.xingliansdk.view.DateUtil
import com.shon.connector.utils.TLog
import com.example.xingliansdk.widget.TitleBarLayout
import com.google.gson.Gson
import com.gyf.barlibrary.ImmersionBar
import com.orhanobut.hawk.Hawk
import com.shon.connector.BleWrite
import com.shon.connector.bean.TimeBean
import kotlinx.android.synthetic.main.activity_alarm_clock_list.*



class AlarmClockListActivity : BaseActivity<BaseViewModel>(), View.OnClickListener {
    private lateinit var mAlarmClockAdapter: AlarmClockAdapter
    var type = 1//默认闹钟
    private lateinit var mTimeList: ArrayList<TimeBean>
    override fun layoutId() = R.layout.activity_alarm_clock_list

    override fun initView(savedInstanceState: Bundle?) {
        ImmersionBar.with(this)
            .titleBar(titleBar)
            .init()
        tvSettingAlarmClock.setOnClickListener(this)
        titleBar.setTitleBarListener(object :TitleBarLayout.TitleBarListener{
            override fun onBackClick() {
                finish()
            }

            override fun onActionImageClick() {
                if (mTimeList.size >= 5) {
                    ShowToast.showToastLong("最多只可以添加五条,请选择删除或修改")
                    return
                }
                JumpUtil.startAlarmClockActivity(this@AlarmClockListActivity, type)
            }

            override fun onActionClick() {

            }
        })

    }

    fun setAdapter() {
        mTimeList=Hawk.get(TIME_LIST, ArrayList())
        mTimeList.forEachIndexed { index, timeBean ->
                var hours=DateUtil.getHour(System.currentTimeMillis())
            var min=DateUtil.getMinute(System.currentTimeMillis())
                if ((timeBean.hours<hours||(timeBean.hours<=hours&&timeBean.min<=min))
                    &&timeBean.specifiedTime==128
                    &&timeBean.endTime<System.currentTimeMillis())
                {
                    mTimeList[index].mSwitch=1
                }

        }
        TLog.error("m==" + Gson().toJson(mTimeList))
        recyclerview.layoutManager = LinearLayoutManager(
            this@AlarmClockListActivity,
            LinearLayoutManager.VERTICAL,
            false
        )
        if(mTimeList.size>0) {
            llNoAlarmClock.visibility = View.GONE
            recyclerview.visibility = View.VISIBLE
        }
        else {
            llNoAlarmClock.visibility = View.VISIBLE
            recyclerview.visibility = View.GONE
        }
        mAlarmClockAdapter = AlarmClockAdapter(mTimeList)
        recyclerview.adapter = mAlarmClockAdapter
        mAlarmClockAdapter.addChildClickViewIds(R.id.Switch)
        mAlarmClockAdapter.setOnItemChildClickListener { adapter, view, position ->
            when (view.id) {
                R.id.Switch -> {
                    if (mTimeList[position].switch == 2)
                        mTimeList[position].switch = 1
                    else
                        mTimeList[position].switch = 2
                    Hawk.put(TIME_LIST, mTimeList)
                    for (i in 0 until  mTimeList.size)
                    BleWrite.writeAlarmClockScheduleCall(mTimeList[i],false)
                }
            }
        }
        mAlarmClockAdapter.setOnDelListener(object :AlarmClockAdapter.onSwipeListener{
            override fun onDel(pos: Int) {
                if (pos >= 0 && pos < mTimeList.size) {

                    TLog.error("mlist=+${Gson().toJson(mTimeList)}")
//                    mTimeList[pos].switch=1
//                    BleWrite.writeAlarmClockScheduleCall(mTimeList[pos])
                    mTimeList.removeAt(pos)
                    mAlarmClockAdapter.notifyItemRemoved(pos)
                    for (i in 0 until mTimeList.size) {
                        TLog.error("删除的position+=$i")
                        mTimeList[i].number=i
                        BleWrite.writeAlarmClockScheduleCall(mTimeList[i],true)
                    }
                    if(mTimeList.size<=0)
                    {
                        TLog.error("删除===")
                        var mTimeBean= TimeBean()
                        mTimeBean.number=0
                        mTimeBean.switch=0
                        mTimeBean.characteristic=TimeBean.ALARM_FEATURES.toInt()
                        BleWrite.writeAlarmClockScheduleCall(mTimeBean,true)
                    }
                    TLog.error("数据流++${Gson().toJson(mTimeList)}")
                    Hawk.put(TIME_LIST,mTimeList)
                }
            }
            override fun onClick(pos: Int) {
                JumpUtil.startAlarmClockActivity(this@AlarmClockListActivity,type,pos)
            }

        })
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tvSettingAlarmClock -> {

                JumpUtil.startAlarmClockActivity(this@AlarmClockListActivity, type)
            }

        }
    }

    override fun onResume() {
        super.onResume()
        setAdapter()
    }


}