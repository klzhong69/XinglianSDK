package com.example.xingliansdk.ui.setting.schedule

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.xingliansdk.Config
import com.example.xingliansdk.Config.database.SCHEDULE_LIST
import com.example.xingliansdk.R
import com.example.xingliansdk.adapter.AlarmClockAdapter
import com.example.xingliansdk.adapter.ScheduleAdapter
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


class ScheduleListActivity : BaseActivity<BaseViewModel>(), View.OnClickListener {
    lateinit var mScheduleAdapter: ScheduleAdapter
    lateinit var mScheduleList: ArrayList<TimeBean>
    override fun layoutId() = R.layout.activity_alarm_clock_list
    override fun initView(savedInstanceState: Bundle?) {
        ImmersionBar.with(this)
            .titleBar(titleBar)
            .init()
        tvSettingAlarmClock.setOnClickListener(this)
        tv_title.text="当前无日程"
        titleBar.setTitleText("日程设置")
        titleBar.setTitleBarListener(object : TitleBarLayout.TitleBarListener{
            override fun onBackClick() {
                finish()
            }

            override fun onActionImageClick() {
                if (mScheduleList.size >= 5) {
                    ShowToast.showToastLong("最多只可以添加五条,请选择删除或修改")
                    return
                }
                JumpUtil.startScheduleActivity(this@ScheduleListActivity)
            }

            override fun onActionClick() {

            }
        })
    }

    fun setAdapter() {
        mScheduleList = Hawk.get(SCHEDULE_LIST,ArrayList())
        mScheduleList.forEachIndexed { index, timeBean ->

            if ( timeBean.endTime<System.currentTimeMillis())
            {
                mScheduleList[index].mSwitch=1
            }

        }

        recyclerview.layoutManager = LinearLayoutManager(
            this@ScheduleListActivity,
            LinearLayoutManager.VERTICAL,
            false
        )
        if(mScheduleList.size>0) {
            llNoAlarmClock.visibility = View.GONE
            recyclerview.visibility = View.VISIBLE
        }
        else {
            llNoAlarmClock.visibility = View.VISIBLE
            recyclerview.visibility = View.GONE
        }
        mScheduleAdapter = ScheduleAdapter(mScheduleList)
        recyclerview.adapter = mScheduleAdapter
        mScheduleAdapter.addChildClickViewIds(R.id.Switch)
        mScheduleAdapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.Switch -> {
                    if (mScheduleList[position].switch == 2)
                        mScheduleList[position].switch = 1
                    else
                        mScheduleList[position].switch = 2
                    Hawk.put(SCHEDULE_LIST, mScheduleList)
                    mScheduleAdapter.notifyItemChanged(position)
                    for (i in 0 until  mScheduleList.size)
                    BleWrite.writeAlarmClockScheduleCall(mScheduleList[position],true)
                }
            }
        }

        mScheduleAdapter.setOnDelListener(object : AlarmClockAdapter.onSwipeListener{
            override fun onDel(pos: Int) {
                if (pos >= 0 && pos < mScheduleList.size) {

                    TLog.error("mlist=+${Gson().toJson(mScheduleList)}")
//                    mScheduleList[pos].switch=1
//                    BleWrite.writeAlarmClockScheduleCall(mScheduleList[pos])
                    mScheduleList.removeAt(pos)
                    mScheduleAdapter.notifyItemRemoved(pos)
                    for (i in 0 until mScheduleList.size) {
                        TLog.error("删除的position+=$i")
                        mScheduleList[i].number=i
                        BleWrite.writeAlarmClockScheduleCall(mScheduleList[i],true)
                    }
                    if(mScheduleList.size<=0)
                    {
                        TLog.error("删除===")
                        var mTimeBean= TimeBean()
                        mTimeBean.number=0
                        mTimeBean.switch=0
                        mTimeBean.characteristic= TimeBean.SCHEDULE_FEATURES
                        BleWrite.writeAlarmClockScheduleCall(mTimeBean,true)
                    }
                    TLog.error("数据流++${Gson().toJson(mScheduleList)}")
                    Hawk.put(Config.database.SCHEDULE_LIST,mScheduleList)
                }
            }
            override fun onClick(pos: Int) {
                JumpUtil.startScheduleActivity(this@ScheduleListActivity,pos)
            }

        })


    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tvSettingAlarmClock -> {

                JumpUtil.startScheduleActivity(this@ScheduleListActivity)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setAdapter()
    }


}