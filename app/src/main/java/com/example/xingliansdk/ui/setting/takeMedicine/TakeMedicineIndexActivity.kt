package com.example.xingliansdk.ui.setting.takeMedicine

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.xingliansdk.Config
import com.example.xingliansdk.R
import com.example.xingliansdk.adapter.TakeMedicineAdapter
import com.example.xingliansdk.base.BaseActivity
import com.example.xingliansdk.utils.JumpUtil
import com.example.xingliansdk.utils.ShowToast
import com.shon.connector.utils.TLog
import com.example.xingliansdk.viewmodel.MainViewModel
import com.example.xingliansdk.widget.TitleBarLayout
import com.google.gson.Gson
import com.gyf.barlibrary.ImmersionBar
import com.orhanobut.hawk.Hawk
import com.shon.connector.BleWrite
import com.shon.connector.bean.RemindTakeMedicineBean
import com.shon.connector.bean.TimeBean
import kotlinx.android.synthetic.main.activity_take_medicine_index.*

class TakeMedicineIndexActivity : BaseActivity<MainViewModel>(),View.OnClickListener {
    lateinit var mList: ArrayList<RemindTakeMedicineBean>
    private lateinit var mTakeMedicineAdapter: TakeMedicineAdapter
    override fun layoutId() = R.layout.activity_take_medicine_index
    var position = -1
    override fun initView(savedInstanceState: Bundle?) {
        ImmersionBar.with(this)
            .titleBar(titleBar)
            .init()
        tvAdd.setOnClickListener(this)
        titleBar.setTitleBarListener(object : TitleBarLayout.TitleBarListener {
            override fun onBackClick() {
                finish()
            }

            override fun onActionImageClick() {
                if (mList.size >= 5) {
                    ShowToast.showToastLong("最多只可以添加五条,请选择删除或修改")
                    return
                }
                JumpUtil.startTakeMedicineActivity(this@TakeMedicineIndexActivity)
            }
            override fun onActionClick() {
            }
        }
        )
    }

    override fun onResume() {
        super.onResume()
        setAdapter()
    }
    fun setAdapter() {
        mList = if (Hawk.get<ArrayList<RemindTakeMedicineBean>>(Config.database.REMIND_TAKE_MEDICINE).isNullOrEmpty())
            ArrayList()
        else
            Hawk.get<ArrayList<RemindTakeMedicineBean>>(Config.database.REMIND_TAKE_MEDICINE)
        TLog.error("m==" + Gson().toJson(mList))
        recyclerview.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        if(mList.size>0) {
            llNoTakeMedicine.visibility = View.GONE
            recyclerview.visibility = View.VISIBLE
        }
        else {
            llNoTakeMedicine.visibility = View.VISIBLE
            recyclerview.visibility = View.GONE
        }
        mTakeMedicineAdapter = TakeMedicineAdapter(mList)
        recyclerview.adapter = mTakeMedicineAdapter

        mTakeMedicineAdapter.setOnDelListener(object : TakeMedicineAdapter.onSwipeListener{
            override fun onDel(pos: Int) {
                if (pos >= 0 && pos < mList.size) {

                    TLog.error("mlist=+${Gson().toJson(mList)}")
//                    mList[pos].switch=1
//                    BleWrite.writeRemindTakeMedicineCall(mList[pos])
                    mList.removeAt(pos)
                    mTakeMedicineAdapter.notifyItemRemoved(pos)
                    for (i in 0 until mList.size) {
                        TLog.error("删除的position+=$i")
                        mList[i].number=i
                        BleWrite.writeRemindTakeMedicineCall(mList[i],true)
                    }
                    if(mList.size<=0)
                    {
                        TLog.error("删除===")
                        var mTimeBean= RemindTakeMedicineBean()
                        mTimeBean.number=0
                        mTimeBean.switch=0
                        BleWrite.writeRemindTakeMedicineCall(mTimeBean,true)
                    }
                    TLog.error("数据流++${Gson().toJson(mList)}")
                    Hawk.put(Config.database.REMIND_TAKE_MEDICINE,mList)
                }
            }
            override fun onClick(pos: Int) {
                JumpUtil.startTakeMedicineActivity(this@TakeMedicineIndexActivity,pos)
            }

        })
    }

    override fun onClick(v: View) {
         when(v.id){
             R.id.tvAdd->
             JumpUtil.startTakeMedicineActivity(this)
         }
    }

//    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
//        return super.dispatchTouchEvent(ev)
//    }
}