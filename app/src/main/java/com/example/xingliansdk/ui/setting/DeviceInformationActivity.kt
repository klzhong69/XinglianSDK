package com.example.xingliansdk.ui.setting

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.listener.OnTimeSelectListener
import com.bigkoo.pickerview.view.OptionsPickerView
import com.bigkoo.pickerview.view.TimePickerView
import com.example.xingliansdk.Config
import com.example.xingliansdk.R
import com.example.xingliansdk.base.BaseActivity
import com.example.xingliansdk.base.TestViewApi
import com.example.xingliansdk.bean.CardBean
import com.example.xingliansdk.eventbus.SNEventBus
import com.example.xingliansdk.ui.login.viewMode.UserViewModel
import com.example.xingliansdk.utils.*
import com.example.xingliansdk.view.DateUtil
import com.example.xingliansdk.view.GlideLoader
import com.example.xingliansdk.widget.TitleBarLayout
import com.google.gson.Gson
import com.gyf.barlibrary.ImmersionBar
import com.lcw.library.imagepicker.ImagePicker
import com.orhanobut.hawk.Hawk
import com.shon.connector.BleWrite
import com.shon.connector.bean.DeviceInformationBean
import com.shon.connector.utils.TLog
import kotlinx.android.synthetic.main.activity_device_information.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.shaohui.advancedluban.Luban
import me.shaohui.advancedluban.OnCompressListener
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.util.*
import kotlin.collections.HashMap

open class DeviceInformationActivity : BaseActivity<UserViewModel>(), View.OnClickListener,
    BleWrite.DeviceInformationCallInterface {

    override fun layoutId() = R.layout.activity_device_information
    private var pvCustomOptions: OptionsPickerView<*>? = null
    private var cardItem: ArrayList<CardBean> = ArrayList()
    private val REQUEST_SELECT_IMAGES_CODE = 0x01
    private var mImagePaths: ArrayList<String>? = null//???????????????????????????????????? ????????????????????????????????????????????????????????????,???????????????????????????
    private var register = false
    private var weightStatus = false
    private var time = System.currentTimeMillis()
    override fun initView(savedInstanceState: Bundle?) {
        ImmersionBar.with(this)
            .titleBar(titleBar)
            .init()
        register = intent.getBooleanExtra("register", false)
        if (!register) {
            tvNext.text = "????????????"
            tvContent.visibility = View.GONE
            tvSignOut.visibility = View.GONE
            tvLogout.visibility = View.GONE
            tvNext.visibility = View.GONE
            tvUserId.visibility = View.VISIBLE
            TLog.error("???????????????++$register")
        } else {
            tvNext.text = "?????????"
            titleBar.setActionText("")
            tvContent.visibility = View.VISIBLE
            tvSignOut.visibility = View.GONE
            tvLogout.visibility = View.GONE
            tvNext.visibility = View.VISIBLE
            tvUserId.visibility = View.GONE
        }
        titleBar.setTitleBarListener(object : TitleBarLayout.TitleBarListener {
            override fun onBackClick() {
                finish()
            }

            override fun onActionImageClick() {
                TLog.error("onActionImageClick")
            }

            override fun onActionClick() {
                if (TextUtils.equals(edtName.text.trim().toString(), "")) {
                    ShowToast.showToastLong("???????????????")
                    return
                }
                setHeadImg()
                if (!imgCheck)
                    setUserInfo()//????????????????????????????????? ??????????????????
            }
        })
        getInformation()
        setOnClick()
        initTimePicker()
        if (!register) {
            TLog.error("????????????????????????]]")
            mainViewModel.userInfo()
        }


    }

    private fun setUserInfo() {
        mDeviceInformationBean.name = edtName.text.toString()
        Hawk.put(Config.database.PERSONAL_INFORMATION, mDeviceInformationBean)
        TLog.error("?????????+${Gson().toJson(mDeviceInformationBean)}")
        var value = HashMap<String, String>()
        if (!mDeviceInformationBean.name.isNullOrEmpty())
            value["nickname"] = mDeviceInformationBean.name
        value["height"] = mDeviceInformationBean.height.toString()
        value["weight"] = mDeviceInformationBean.weight.toString()
        value["age"] = mDeviceInformationBean.age.toString()
        value["sex"] = mDeviceInformationBean.sex.toString()
        value["birthDate"] =
            DateUtil.getDate(DateUtil.YYYY_MM_DD, mDeviceInformationBean.birth)
        value["createTime"] = (time / 1000).toString()
        updateHomeWeight()
        mViewModel.setUserInfo(value!!)

        BleWrite.writeDeviceInformationCall(
            mDeviceInformationBean,
            this@DeviceInformationActivity,
            true
        )
    }

    private fun getInformation() {
        if (mDeviceInformationBean.sex == 1) {
            setting_sex.setContentText(getString(R.string.man))
        } else {
            setting_sex.setContentText(getString(R.string.woman))
        }
        setting_age.setContentText(mDeviceInformationBean.age.toString())
        //      TLog.error("????????????+" + mDeviceInformationBean.birth)
        setting_age.setContentText(
            DateUtil.getDate(
                DateUtil.YYYY_MM_DD,
                mDeviceInformationBean.birth
            )

        )
        setting_height.setContentText(mDeviceInformationBean.height.toString() + "cm")
        setting_weight.setContentText(mDeviceInformationBean.weight.toInt().toString() + "kg")
        if (!register)
            edtName.setText(mDeviceInformationBean?.name)

        tvUserId.text = userInfo.user.userId
        if (userInfo.user.headPortrait.isNullOrEmpty()) {
            var img = Hawk.get<String>(Config.database.IMG_HEAD)
            if (Hawk.get<String>(Config.database.IMG_HEAD).isNullOrEmpty()) {
                if (mDeviceInformationBean.sex == 1)
                    ImgUtil.loadHead(imgHead, R.mipmap.icon_head_man)
                else
                    ImgUtil.loadHead(imgHead, R.mipmap.icon_head_woman)
                return
            }
            if (FileUtil.isFileExists(img)) {  //??????????????????
                ImgUtil.loadHead(imgHead, img)
            }
        } else {
            TLog.error("????????????" + userInfo.user.headPortrait)
            ImgUtil.loadImage(this, userInfo.user.headPortrait)
            ImgUtil.loadHead(imgHead, userInfo.user.headPortrait)
        }

    }

    override fun createObserver() {
        super.createObserver()
        mViewModel.result.observe(this) {
            TLog.error("it==${Gson().toJson(it)}")
            userInfo.user = it.user
            userInfo.userConfig = it.userConfig
            Hawk.put(Config.database.USER_INFO, userInfo)
            SNEventBus.sendEvent(Config.eventBus.EVENT_BUS_IMG_HEAD)
            mainViewModel.userInfo.postValue(it)
            HelpUtil.hideSoftInputView(this)
            finish()
        }
        mViewModel.resultImg.observe(this) {
            TLog.error("????????????===" + Gson().toJson(it))
//            ImgUtil.loadImage(this, mImagePaths?.get(0).toString())
            setUserInfo()
            ThreadUtils.submit {
                //    mainViewModel.userInfo()

                SNEventBus.sendEvent(Config.eventBus.EVENT_BUS_IMG_HEAD)
            }
        }
        mainViewModel.result.observe(this) {
            TLog.error("??????????????????" + Gson().toJson(it))

            userInfo.user = it.user
            userInfo.userConfig = it.userConfig
            userInfo.permission = it.permission
            Hawk.put(Config.database.USER_INFO, userInfo)
            mDeviceInformationBean = DeviceInformationBean(
                it.user.sex.toInt(),
                it.user.age.toInt(),
                it.user.height.toInt(),
                it.user.weight.toDouble().toInt(),
                mDeviceInformationBean.language.toInt(),
                it.userConfig.timeFormat,
                1,
                it.userConfig.distanceUnit,
                mDeviceInformationBean.wearHands.toInt(),
                it.userConfig.temperatureUnit,
                it.userConfig.movingTarget.toLong(),
                DateUtil.convertStringToLong(DateUtil.YYYY_MM_DD, it.user.birthDate),
                it.user.nickname
            )

            Hawk.put(Config.database.SLEEP_GOAL, it.userConfig.sleepTarget.toLong())
            Hawk.put(Config.database.PERSONAL_INFORMATION, mDeviceInformationBean)
            getInformation()
        }
    }

    private fun setOnClick() {
        setting_sex.setOnClickListener(this)
        setting_age.setOnClickListener(this)
        setting_height.setOnClickListener(this)
        setting_weight.setOnClickListener(this)
        imgHead.setOnClickListener(this)
        tvNext.setOnClickListener(this)
        tvSignOut.setOnClickListener(this)
        tvLogout.setOnClickListener(this)
    }

    private fun initCustomOptionPicker(id: Int) { //??????????????????????????????????????????
        /**
         * @description
         *
         * ???????????????
         * ?????????????????????id??? optionspicker ?????? timepicker ??????????????????????????????????????????????????????????????????
         * ???????????????demo ????????????????????????layout?????????
         */
        if (pvCustomOptions?.isShowing == true)
            pvCustomOptions?.dismiss()
        pvCustomOptions = OptionsPickerBuilder(
            this
        ) { options1, option2, options3, v -> //?????????????????????????????????????????????
            when (id) {
                R.id.setting_sex -> {
                    mDeviceInformationBean.sex = cardItem[options1].id + 1
                    setting_sex.setContentText(cardItem[options1].pickerViewText)
                    TLog.error("===" + options1)
                    if (mDeviceInformationBean.sex == 1)
                        ImgUtil.loadHead(imgHead, R.mipmap.icon_head_man)
                    else
                        ImgUtil.loadHead(imgHead, R.mipmap.icon_head_woman)
                }
                R.id.setting_age -> {
                    mDeviceInformationBean.age = cardItem[options1].pickerViewText.toInt()

                    setting_age.setContentText(cardItem[options1].pickerViewText)
                }
                R.id.setting_height -> {
                    mDeviceInformationBean.height = cardItem[options1].pickerViewText.toInt()
                    setting_height.setContentText(cardItem[options1].pickerViewText + "cm")
                }
                R.id.setting_weight -> {
                    weightStatus = true
                    mDeviceInformationBean.setWeight(cardItem[options1].pickerViewText.toInt())
                    setting_weight.setContentText(cardItem[options1].pickerViewText + "kg")
                }

            }
        }
            .setLayoutRes(
                R.layout.pickerview_custom_options
            ) { v ->
                val tvSubmit =
                    v.findViewById<View>(R.id.tv_finish) as TextView
                val ivCancel =
                    v.findViewById<View>(R.id.iv_cancel) as ImageView
                tvSubmit.setOnClickListener {
                    pvCustomOptions?.returnData()
                    pvCustomOptions?.dismiss()
                }
                ivCancel.setOnClickListener { pvCustomOptions?.dismiss() }
            }
            .isDialog(true)
            .setCyclic(cardItem.size > 2, false, false)
            .isRestoreItem(true)
            .setSelectOptions(cardItem.size / 2)
            .setOutSideCancelable(false)
            .build<Any>()
        (pvCustomOptions as OptionsPickerView<Any>?)?.setPicker(cardItem as List<Any>?) //????????????

    }

    private fun getCardData(id: Int) {
        cardItem = ArrayList()
        when (id) {
            R.id.setting_sex -> {
                cardItem.add(CardBean(0, getString(R.string.man)))
                cardItem.add(CardBean(1, getString(R.string.woman)))
            }
            R.id.setting_age -> {
                for (i in 0..120) {
                    cardItem.add(CardBean(i, i.toString()))
                }
            }
            R.id.setting_height -> {
                for (i in 60..210) {
                    cardItem.add(CardBean(i, i.toString()))
                }
            }
            R.id.setting_weight -> {
                for (i in 30..150) {
                    cardItem.add(CardBean(i, i.toString()))
                }
            }


        }
    }

    override fun onClick(v: View) {
        if (v.id == R.id.setting_age || v.id == R.id.imgHead || v.id == R.id.tvNext || v.id == R.id.tvSignOut || v.id == R.id.tvLogout) {
            when (v.id) {
                R.id.setting_age -> {
                    TLog.error("m==${Gson().toJson(mDeviceInformationBean)}")
                    pvTime?.show()
                }
                R.id.imgHead -> {
                    ImagePicker.getInstance()
                        .setTitle(resources.getString(R.string.app_name)) //????????????
                        .showCamera(true) //??????????????????????????????
                        .showImage(true) //????????????????????????
                        .showVideo(false) //????????????????????????
                        //.setMaxCount(1) //??????????????????????????????(?????????1?????????)
                        .setSingleType(true) //????????????????????????????????????
                        // .setImagePaths(mImagePaths) //????????????????????????
                        .setImageLoader(GlideLoader()) //??????????????????????????????
                        .start(
                            this@DeviceInformationActivity,
                            REQUEST_SELECT_IMAGES_CODE
                        )
                }
                R.id.tvNext -> {
                    if (register) {
                        if (TextUtils.equals(edtName.text.trim().toString(), "")) {
                            ShowToast.showToastLong("???????????????")
                            return
                        }
                        setHeadImg()
                        mDeviceInformationBean.name = edtName.text.toString()
                        Hawk.put(Config.database.PERSONAL_INFORMATION, mDeviceInformationBean)
                        TLog.error("?????????+${Gson().toJson(mDeviceInformationBean)}")
                        JumpUtil.startGoalActivity(this)
                    } else {
                        mViewModel.outLogin(this)
                    }
                }
                R.id.tvSignOut -> {
                    AllGenJIDialog.signOutDialog(supportFragmentManager, mViewModel, userInfo, this)
                }
                R.id.tvLogout -> {
                    JumpUtil.startLogOutActivity(this)
                }
            }

        } else {
            getCardData(v.id)
            if (pvCustomOptions != null) {
                if (pvCustomOptions?.isShowing == true) {
                    pvCustomOptions?.dismiss()
                }
            }
            initCustomOptionPicker(v.id)
            pvCustomOptions?.show()
        }

    }

    override fun DeviceInformationCallResult() {
        //ShowToast.showToastLong(getString(R.string.set_successfully))
//        setting_movement.getContentText()?.toInt()?.let {
//            mainViewModel.setText(it)
//            Hawk.put("step", it)
//        }
        finish()
    }

    private var pvTime: TimePickerView? = null
    private fun initTimePicker() { //Dialog ???????????????????????????
        pvTime = TimePickerBuilder(this,
            OnTimeSelectListener { date, v ->
                if (date.time > time) {
                    ShowToast.showToastLong("??????????????????????????????")
                    return@OnTimeSelectListener
                }
                mDeviceInformationBean.age = DateUtil.getAge(date)
                mDeviceInformationBean.birth = DateUtil.convertDateToLong(date)
                setting_age.setContentText(DateUtil.getDate(DateUtil.YYYY_MM_DD, date))
            })
            .setType(booleanArrayOf(true, true, true, false, false, false))
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

    fun setHeadImg() {
        if (!imgCheck)
            return
        TLog.error("????????????")
        mImagePaths?.get(0)?.let {
            ImgUtil.loadHead(imgHead, it)
        }
        var file = File(mImagePaths?.get(0))//??????????????????
        Luban.compress(this, file)
            .setMaxSize(500)
            .putGear(Luban.THIRD_GEAR)
            .launch(object : OnCompressListener {
                override fun onStart() {
                }

                override fun onSuccess(file: File) {
                    TLog.error("??????")
                    ThreadUtils.submit {
                        val requestBody =
                            RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)
                        val part =
                            MultipartBody.Part.createFormData("file", file.name, requestBody)
                        mViewModel.setImg(part)
                    }
                }

                override fun onError(e: Throwable?) {
                    TLog.error("??????????????????++" + e.toString())
                }
            })
    }

    var imgCheck = false
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_SELECT_IMAGES_CODE) {
                mImagePaths = data?.getStringArrayListExtra(ImagePicker.EXTRA_SELECT_IMAGES)
           //     Hawk.put(Config.database.IMG_HEAD, mImagePaths?.get(0))
                imgCheck = true
                ImgUtil.loadHead(imgHead, mImagePaths?.get(0).toString())
                //  SNEventBus.sendEvent(Config.eventBus.EVENT_BUS_IMG_HEAD)

            }
        }
    }

    private fun updateHomeWeight() {
        if (weightStatus) {
            if (mHomeCardBean.addCard != null && mHomeCardBean.addCard.size > 0) {
                var cardList = mHomeCardBean.addCard
                cardList.forEachIndexed { index, addCardDTO ->
                    if (addCardDTO.type == 7) {
                        mHomeCardBean.addCard[index].time =
                            System.currentTimeMillis() / 1000
                        mHomeCardBean.addCard[index].dayContent =
                            mDeviceInformationBean.weight.toString()
                        Hawk.put(Config.database.HOME_CARD_BEAN, mHomeCardBean)
                        SNEventBus.sendEvent(Config.eventBus.BLOOD_PRESSURE_RECORD)
                    }
                }
            }
        }
    }
}