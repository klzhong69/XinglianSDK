package com.example.xingliansdk.ui.setting.takeMedicine

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import com.example.xingliansdk.Config.eventBus.REMIND_TAKE_MEDICINE_REMINDER_PERIOD
import com.example.xingliansdk.R
import com.example.xingliansdk.base.BaseActivity
import com.example.xingliansdk.eventbus.SNEventBus
import com.example.xingliansdk.utils.HelpUtil
import com.example.xingliansdk.utils.ShowToast
import com.shon.connector.utils.TLog
import com.example.xingliansdk.viewmodel.MainViewModel
import com.example.xingliansdk.widget.TitleBarLayout
import com.gyf.barlibrary.ImmersionBar
import kotlinx.android.synthetic.main.activity_take_medicine_repeat.*

class TakeMedicineRepeatActivity : BaseActivity<MainViewModel>(), View.OnClickListener {

    override fun layoutId() = R.layout.activity_take_medicine_repeat
    var type=0
    override fun initView(savedInstanceState: Bundle?) {
        ImmersionBar.with(this)
            .titleBar(titleBar)
            .init()
        radRepeat.setOnClickListener(this)
        radInterval.setOnClickListener(this)
        llRepeat.setOnClickListener(this)
        tvOne.setOnClickListener(this)
        tvTwo.setOnClickListener(this)
        tvThree.setOnClickListener(this)
        llRepeat.visibility=View.GONE
        type=intent.getIntExtra("ReminderPeriod",0)
        if(type>0) {
            radInterval.isChecked = true
            llRepeat.visibility = View.VISIBLE
            when (type) {
                1 -> {
                    getType(R.id.tvOne)
                }
                2 -> {
                    getType(R.id.tvTwo)
                }
                3 -> {
                    getType(R.id.tvThree)
                }
                else->{
                    edtCustom.setText(type.toString())

                }
            }
        }
        edtCustom.addTextChangedListener {
            if(it==null||it.isEmpty()){
                edtCustom.setText("1")
                it?.let { it1 -> edtCustom.setSelection(edtCustom.text.length)
                TLog.error("it??"+it1.length)
                }//将光标移至文字末尾
                ShowToast.showToastLong("周期天数间隔不能小于1天")
                return@addTextChangedListener
            }
            val day=it.toString().toInt()
            if (day>255)
            {
                ShowToast.showToastLong("周期天数不大于255天")
                edtCustom.setText("1")
                return@addTextChangedListener
            }
            type=day
            getType(edtCustom.id)
        }
        titleBar.setTitleBarListener(object :TitleBarLayout.TitleBarListener{
            override fun onBackClick() {
               finish()
            }

            override fun onActionImageClick() {

            }

            override fun onActionClick() {
                TLog.error("===$type")
                SNEventBus.sendEvent(REMIND_TAKE_MEDICINE_REMINDER_PERIOD,type)
                finish()
            }

        })
    }

    override fun onClick(v: View) {
        HelpUtil.hideSoftInputView(this)
        when (v.id) {
            R.id.radRepeat -> {
                type=0
                llRepeat.visibility = View.GONE
            }
            R.id.radInterval -> {
                getType(R.id.tvOne)
                type=1
                llRepeat.visibility = View.VISIBLE
            }
            R.id.llRepeat -> {
            }
            R.id.tvOne,R.id.tvTwo,R.id.tvThree -> {
                when (v.id) {
                    R.id.tvOne -> type=1
                    R.id.tvTwo -> type=2
                    R.id.tvThree -> type=3
                }
                getType(v.id)
            }
        }

    }

    private fun getType(type: Int) {
        tvOne.setBackgroundResource(
            if (type == R.id.tvOne)
                R.drawable.device_repeat_true_green
            else
                R.drawable.device_repeat_false_gray
        )
        tvOne.setTextColor(
            if (type == R.id.tvOne)
                resources.getColor(R.color.white)
            else
                resources.getColor(R.color.sub_text_color)
        )
        tvTwo.setBackgroundResource(
            if (type == R.id.tvTwo)
                R.drawable.device_repeat_true_green
            else
                R.drawable.device_repeat_false_gray
        )
        tvTwo.setTextColor(
            if (type == R.id.tvTwo)
                resources.getColor(R.color.white)
            else
                resources.getColor(R.color.sub_text_color)
        )
        tvThree.setBackgroundResource(
            if (type == R.id.tvThree)
                R.drawable.device_repeat_true_green
            else
                R.drawable.device_repeat_false_gray
        )
        tvThree.setTextColor(
            if (type == R.id.tvThree)
                resources.getColor(R.color.white)
            else
                resources.getColor(R.color.sub_text_color)
        )
    }


}