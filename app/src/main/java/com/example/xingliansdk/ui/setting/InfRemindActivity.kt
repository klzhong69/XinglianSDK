package com.example.xingliansdk.ui.setting

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.xingliansdk.Config
import com.example.xingliansdk.R
import com.example.xingliansdk.adapter.OtherSwitchAdapter
import com.example.xingliansdk.base.BaseActivity
import com.example.xingliansdk.bean.RemindConfig
import com.shon.connector.utils.TLog
import com.example.xingliansdk.viewmodel.MainViewModel
import com.github.iielse.switchbutton.SwitchView
import com.google.gson.Gson
import com.gyf.barlibrary.ImmersionBar
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.activity_inf_remind.*
import kotlinx.android.synthetic.main.activity_inf_remind.titleBar


class InfRemindActivity : BaseActivity<MainViewModel>() {

    lateinit var mOtherSwitchAdapter: OtherSwitchAdapter
      var mList: ArrayList<RemindConfig.Apps> = ArrayList()

    var remindConfig = RemindConfig()

    override fun layoutId()=R.layout.activity_inf_remind
    override fun initView(savedInstanceState: Bundle?) {
        ImmersionBar.with(this)
            .titleBar(titleBar)
            .init()
//        app.appIconFile
//        TLog.error("app="+Gson().toJson(app))

        TLog.error("mList=="+mList)
        //填充推送app列表
        val nnList= Hawk.get<ArrayList<RemindConfig.Apps> >("RemindList",
            remindConfig.getRemindAppPushList() as ArrayList<RemindConfig.Apps>?
        )
        TLog.error("nnList+=${nnList.size}")
        TLog.error("nnList+=${nnList}")
        TLog.error("nnList+=${Gson().toJson(nnList)}")
        onState()
        if(nnList!=null&& nnList.isNotEmpty())
        {
            for (i in nnList.indices) {
                TLog.error("走进来了")
                mList.add(nnList[i])
            }
        }
//        else {
//            for (i in remindAppPushList.indices) {
//                val apps = remindAppPushList[i]
//                mList.add(remindAppPushList[i])
//            }
//            Hawk.put("RemindList", mList)
//        }
        TLog.error("mList+="+Gson().toJson(mList))
        TLog.error("存储以后取++"+Hawk.get<ArrayList<RemindConfig.Apps> >("RemindList"))
        setAdapter()

    }
    private  fun onState()
    {
       var call= Hawk.get(Config.database.INCOMING_CALL,2)
        SwitchALL.isOpened=call==2
        var sms= Hawk.get(Config.database.SMS,2)
        SwitchSMS.isOpened=sms==2
        var other=Hawk.get(Config.database.OTHER,2)
        SwitchOther.isOpened=other==2
        if (SwitchOther.isOpened)
            ryRemind.visibility=View.VISIBLE
        else
            ryRemind.visibility=View.GONE
        TLog.error("call =$call")
        SwitchALL.setOnStateChangedListener(object :SwitchView.OnStateChangedListener{
            override fun toggleToOn(view: SwitchView?) {
                Hawk.put(Config.database.INCOMING_CALL,2)
                SwitchALL.isOpened=true
            }

            override fun toggleToOff(view: SwitchView?) {
                Hawk.put(Config.database.INCOMING_CALL,1)
                SwitchALL.isOpened=false
            }
        })
        SwitchSMS.setOnStateChangedListener(object :SwitchView.OnStateChangedListener{
            override fun toggleToOn(view: SwitchView?) {
                Hawk.put(Config.database.SMS,2)
                SwitchSMS.isOpened=true
            }

            override fun toggleToOff(view: SwitchView?) {
                Hawk.put(Config.database.SMS,1)
                SwitchSMS.isOpened=false
            }
        })
        SwitchOther.setOnStateChangedListener(object :SwitchView.OnStateChangedListener{
            override fun toggleToOn(view: SwitchView?) {
                Hawk.put(Config.database.OTHER,2)
                SwitchOther.isOpened=true
                ryRemind.visibility=View.VISIBLE
            }

            override fun toggleToOff(view: SwitchView?) {
                Hawk.put(Config.database.OTHER,1)
                SwitchOther.isOpened=false
                ryRemind.visibility=View.GONE
            }
        })
    }
    private  fun setAdapter()
    {

        ryRemind.layoutManager = LinearLayoutManager(
            this@InfRemindActivity,
            LinearLayoutManager.VERTICAL,
            false
        )

        mOtherSwitchAdapter = OtherSwitchAdapter(mList)
        ryRemind.adapter = mOtherSwitchAdapter
        mOtherSwitchAdapter.addChildClickViewIds(R.id.Switch)
        mOtherSwitchAdapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.Switch -> {
                    mList[position].isOn = !mList[position].isOn
                    Hawk.put("RemindList", mList)
                    TLog.error("=${Gson().toJson(mList[position])}")
                }
            }
        }
    }
}