package com.example.xingliansdk.ui.setting.takeMedicine

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.listener.OnTimeSelectListener
import com.bigkoo.pickerview.view.TimePickerView
import com.example.xingliansdk.Config
import com.example.xingliansdk.R
import com.example.xingliansdk.adapter.TimesPerDayAdapter
import com.example.xingliansdk.base.BaseActivity
import com.example.xingliansdk.eventbus.SNEvent
import com.example.xingliansdk.eventbus.SNEventBus
import com.example.xingliansdk.utils.HelpUtil
import com.example.xingliansdk.utils.JumpUtil
import com.example.xingliansdk.utils.ShowToast
import com.shon.connector.utils.TLog
import com.example.xingliansdk.view.DateUtil
import com.example.xingliansdk.viewmodel.MainViewModel
import com.example.xingliansdk.widget.TitleBarLayout
import com.google.gson.Gson
import com.gyf.barlibrary.ImmersionBar
import com.ly.genjidialog.extensions.convertListenerFun
import com.ly.genjidialog.extensions.newGenjiDialog
import com.ly.genjidialog.other.DialogGravity
import com.orhanobut.hawk.Hawk
import com.shon.connector.BleWrite
import com.shon.connector.bean.RemindTakeMedicineBean
import kotlinx.android.synthetic.main.activity_take_medicine.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class TakeMedicineActivity : BaseActivity<MainViewModel>(), View.OnClickListener {
    private lateinit var mTimesPerDayAdapter: TimesPerDayAdapter
    var mBean: RemindTakeMedicineBean = RemindTakeMedicineBean()
    lateinit var mRemindTakeMedicineList: MutableList<RemindTakeMedicineBean>
    var position = -1
    override fun layoutId() = R.layout.activity_take_medicine
    override fun initView(savedInstanceState: Bundle?) {
        ImmersionBar.with(this)
            .titleBar(titleBar)
            .init()
        SNEventBus.register(this)
        settingNumber.setOnClickListener(this)
        settingRepeat.setOnClickListener(this)
        settingStartTime.setOnClickListener(this)
        settingEndTime.setOnClickListener(this)
        titleBar.setTitleBarListener(object : TitleBarLayout.TitleBarListener {
            override fun onBackClick() {
                finish()
                HelpUtil.hideSoftInputView(this@TakeMedicineActivity)
            }

            override fun onActionImageClick() {
            }

            override fun onActionClick() {
                if (mRemindTakeMedicineList.size > 5) {
                    ShowToast.showToastLong("????????????????????????????????????")
                    return
                }
                if (mBean.startTime > mBean.endTime && mBean.endTime > 0) {
                    ShowToast.showToastLong("????????????????????????????????????")
                    return
                }
                if (mBean.startTime <= 1000)
                    mBean.startTime = System.currentTimeMillis()
                TLog.error("mTimeList.size++" + mRemindTakeMedicineList.size + " , num==")
                mBean.switch = 2
                mBean.unicodeTitle = edtTitle.text.toString()

                if (mRemindTakeMedicineList.size > 0 && position >= 0) {
                    mRemindTakeMedicineList[position] = mBean
                } else {
                    mBean.number = mRemindTakeMedicineList.size
                    mRemindTakeMedicineList.add(mBean)
                }
                Hawk.put(Config.database.REMIND_TAKE_MEDICINE, mRemindTakeMedicineList)
                TLog.error("m==" + Gson().toJson(mBean))
                TLog.error("m==" + Gson().toJson(mRemindTakeMedicineList))
                BleWrite.writeRemindTakeMedicineCall(mBean, true)
                finish()
            }
        })
        mRemindTakeMedicineList =
            if (Hawk.get<ArrayList<RemindTakeMedicineBean>>(Config.database.REMIND_TAKE_MEDICINE)
                    .isNullOrEmpty()
            )
                ArrayList()
            else
                Hawk.get<ArrayList<RemindTakeMedicineBean>>(Config.database.REMIND_TAKE_MEDICINE)
        position = intent.getIntExtra("update", -1)

        if (position >= 0) {
            mBean = mRemindTakeMedicineList[position]
            settingNumber.setContentText("${mBean.getGroupList().size}???")
            TLog.error("==??????+" + Gson().toJson(mBean.getGroupList()))
            setAdapter(mBean.getGroupList())
            if (mBean.ReminderPeriod == 0)
                settingRepeat.setContentText("??????")
            else
                settingRepeat.setContentText("${mBean.ReminderPeriod}???")

            if (mBean.startTime <= 100)
                settingStartTime.setContentText(
                    DateUtil.getDate(
                        DateUtil.YYYY_MM_DD_HH_MM,
                        System.currentTimeMillis()
                    )
                )
            else
                settingStartTime.setContentText(
                    DateUtil.getDate(
                        DateUtil.YYYY_MM_DD_HH_MM,
                        mBean.startTime
                    )
                )
            if (mBean.endTime <= 100)
                settingEndTime.setContentText("??????")
            else
                settingEndTime.setContentText(
                    DateUtil.getDate(
                        DateUtil.YYYY_MM_DD_HH_MM,
                        mBean.endTime
                    )
                )
            edtTitle.setText(mBean.unicodeTitle)
        } else {
            settingStartTime.setContentText(
                DateUtil.getDate(
                    DateUtil.YYYY_MM_DD,
                    System.currentTimeMillis()
                )
            )
            var mReminderGroup: MutableList<RemindTakeMedicineBean.ReminderGroup> = arrayListOf()
            mReminderGroup.add(mBean.ReminderGroup(0, 0))
            setAdapter(mReminderGroup)
        }
        initDatePicker()
    }

    var startEndType = false
    override fun onClick(v: View) {
        when (v.id) {
            R.id.settingNumber -> {
                dialog()
            }
            R.id.settingRepeat -> {
                TLog.error("mBean.ReminderPeriod+" + mBean.ReminderPeriod)
                JumpUtil.startTakeMedicineRepeatActivity(this, mBean.ReminderPeriod)
            }
            R.id.settingStartTime -> {
                TLog.error("dianji")
                startEndType = true
                dateTime?.let {
                    if (it.isShowing)
                        it.dismiss()
                    dateTime?.show()
                }
            }
            R.id.settingEndTime -> {
                startEndType = false
                dateTime?.let {
                    if (it.isShowing)
                        it.dismiss()
                    dateTime?.show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SNEventBus.unregister(this)
    }

    var mList: MutableList<RemindTakeMedicineBean.ReminderGroup> = arrayListOf()
    fun setAdapter(mList: MutableList<RemindTakeMedicineBean.ReminderGroup>) {
        this.mList = mList
        mBean.setGroupList(mList)
        ryIndex.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )

        mTimesPerDayAdapter = TimesPerDayAdapter(mList)
        ryIndex.adapter = mTimesPerDayAdapter
        ryIndex.isNestedScrollingEnabled = false
        mTimesPerDayAdapter.setOnItemClickListener { adapter, view, position ->
            TLog.error("????????????+$position")
            initTimePicker(position)
            pvTime?.show()
        }


    }

    private var pvTime: TimePickerView? = null
    private fun initTimePicker(pos: Int) { //Dialog ???????????????????????????
        pvTime = TimePickerBuilder(this,
            OnTimeSelectListener { date, v ->

                mList[pos].groupHH = DateUtil.getHour(date)
                mList[pos].groupMM = DateUtil.getMinute(date)
                mBean.setGroupList(mList)
                mTimesPerDayAdapter.notifyItemChanged(pos)
//                TLog.error("???+++"+mTimeBean.min+"???++"+mTimeBean.hours)
            })
            .setType(booleanArrayOf(false, false, false, true, true, false))
            .isDialog(true) //????????????false ??????????????????DecorView ????????????????????????
            .setItemVisibleCount(5) //?????????????????????????????????1???????????????6???????????????????????????7???
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
            dialogWindow.setWindowAnimations(com.bigkoo.pickerview.R.style.picker_view_slide_anim) //??????????????????
            dialogWindow.setGravity(Gravity.BOTTOM) //??????Bottom,????????????
            dialogWindow.setDimAmount(0.3f)
        }
    }

    private var dateTime: TimePickerView? = null
    private fun initDatePicker() { //Dialog ???????????????????????????
        dateTime = TimePickerBuilder(this,
            OnTimeSelectListener { date, v ->
                if (startEndType) {
                    mBean.startTime = DateUtil.convertDateToLong(date)
                    settingStartTime.setContentText(
                        DateUtil.getDate(
                            DateUtil.YYYY_MM_DD_HH_MM,
                            date
                        )
                    )
                } else {
                    mBean.endTime = DateUtil.convertDateToLong(date)
                    settingEndTime.setContentText(DateUtil.getDate(DateUtil.YYYY_MM_DD, date))
                }
                TLog.error("+++" + mBean.startTime + "  ???++" + DateUtil.convertDateToLong(date) + " endTime===" + mBean.endTime)
            })
            .setType(booleanArrayOf(true, true, true, false, false, false))
            .isDialog(true) //????????????false ??????????????????DecorView ????????????????????????
            .setItemVisibleCount(5) //?????????????????????????????????1???????????????6???????????????????????????7???
            .setLineSpacingMultiplier(2.0f)
            .isAlphaGradient(true)
            .isCyclic(true)
            .build()
        val mDialog: Dialog = dateTime?.dialog!!
        val params =
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM
            )
        params.leftMargin = 0
        params.rightMargin = 0
        dateTime?.let { it.dialogContainerLayout.layoutParams = params }
        val dialogWindow = mDialog.window
        if (dialogWindow != null) {
            dialogWindow.setWindowAnimations(com.bigkoo.pickerview.R.style.picker_view_slide_anim) //??????????????????
            dialogWindow.setGravity(Gravity.BOTTOM) //??????Bottom,????????????
            dialogWindow.setDimAmount(0.3f)
        }
    }

    private fun dialog() {
        newGenjiDialog {
            layoutId = R.layout.dialog_times_per_day
            dimAmount = 0.3f
            isFullHorizontal = true
            isFullVerticalOverStatusBar = false
            gravity = DialogGravity.CENTER_BOTTOM
            animStyle = R.style.BottomTransAlphaADAnimation
            var mList: MutableList<RemindTakeMedicineBean.ReminderGroup> = arrayListOf()
            convertListenerFun { holder, dialog ->
                var tvDele = holder.getView<TextView>(R.id.tvDele)
                var tvOne = holder.getView<TextView>(R.id.tvOne)
                var tvTwo = holder.getView<TextView>(R.id.tvTwo)
                var tvThree = holder.getView<TextView>(R.id.tvThree)
                var tvFour = holder.getView<TextView>(R.id.tvFour)

                tvOne?.setOnClickListener {
                    mList.add(mBean.ReminderGroup(0, 0))
                    settingNumber.setContentText("1???")

                    setAdapter(mList)
                    dialog.dismiss()
                }
                tvTwo?.setOnClickListener {
                    mList.add(mBean.ReminderGroup(0, 0))
                    mList.add(mBean.ReminderGroup(0, 0))
                    settingNumber.setContentText("2???")
                    setAdapter(mList)
                    dialog.dismiss()
                }
                tvThree?.setOnClickListener {
                    mList.add(mBean.ReminderGroup(0, 0))
                    mList.add(mBean.ReminderGroup(0, 0))
                    mList.add(mBean.ReminderGroup(0, 0))
                    settingNumber.setContentText("3???")
                    setAdapter(mList)
                    dialog.dismiss()
                }
                tvFour?.setOnClickListener {
                    mList.add(mBean.ReminderGroup(0, 0))
                    mList.add(mBean.ReminderGroup(0, 0))
                    mList.add(mBean.ReminderGroup(0, 0))
                    mList.add(mBean.ReminderGroup(0, 0))

                    settingNumber.setContentText("4???")
                    setAdapter(mList)
                    dialog.dismiss()
                }
                tvDele?.setOnClickListener {
                    dialog.dismiss()
                }
            }
        }.showOnWindow(supportFragmentManager)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventResult(event: SNEvent<*>) {
        when (event.code) {
            Config.eventBus.REMIND_TAKE_MEDICINE_REMINDER_PERIOD -> {
                val type: Int = event.data.toString().toInt()
                mBean.reminderPeriod = type
                if (type <= 0) {
                    settingRepeat.setContentText("??????")
                } else
                    settingRepeat.setContentText("${type}???")
            }
        }
    }
}