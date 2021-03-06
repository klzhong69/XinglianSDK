package com.example.xingliansdk.ui.dial

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.example.xingliansdk.R
import com.example.xingliansdk.adapter.CustomizeColorDialAdapter
import com.example.xingliansdk.adapter.CustomizeFunctionDialAdapter
import com.example.xingliansdk.adapter.CustomizePlacementDialAdapter
import com.example.xingliansdk.base.BaseActivity
import com.example.xingliansdk.base.viewmodel.BaseViewModel
import com.example.xingliansdk.bean.dialBean.CustomizeColorBean
import com.example.xingliansdk.bean.dialBean.CustomizeFunctionBean
import com.example.xingliansdk.bean.dialBean.CustomizePlacementBean
import com.example.xingliansdk.bean.room.AppDataBase
import com.example.xingliansdk.bean.room.CustomizeDialBean
import com.example.xingliansdk.bean.room.CustomizeDialDao
import com.example.xingliansdk.pictureselector.GlideEngine
import com.example.xingliansdk.ui.setting.flash.FlashCall
import com.example.xingliansdk.ui.setting.flash.FlashWriteAssignInterface
import com.example.xingliansdk.utils.FileUtils
import com.example.xingliansdk.utils.HelpUtil
import com.example.xingliansdk.utils.ImgUtil
import com.example.xingliansdk.utils.ShowToast
import com.example.xingliansdk.view.DateUtil
import com.google.gson.Gson
import com.gyf.barlibrary.ImmersionBar
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.shon.bluetooth.DataDispatcher
import com.shon.connector.BleWrite
import com.shon.connector.bean.DialCustomBean
import com.shon.connector.utils.TLog
import kotlinx.android.synthetic.main.activity_customize_dial.*
import java.util.*
import kotlin.collections.ArrayList

class CustomizeDialActivity : BaseActivity<BaseViewModel>(), View.OnClickListener,
    FlashWriteAssignInterface {
    private lateinit var mCustomizeColorDialAdapter: CustomizeColorDialAdapter
    private var mCustomizeColorDialList = arrayListOf<CustomizeColorBean>()
    private lateinit var mCustomizeFunctionDialAdapter: CustomizeFunctionDialAdapter
    private var mCustomizeFunctionDialList = arrayListOf<CustomizeFunctionBean>()
    private lateinit var mCustomizePlacementDialAdapter: CustomizePlacementDialAdapter
    lateinit var sDao: CustomizeDialDao
    var mCustomizePlacementDialList = arrayListOf<CustomizePlacementBean>()
    var time = System.currentTimeMillis()
    var mCustomizeDialBean: CustomizeDialBean = CustomizeDialBean()
    private var colorList = arrayListOf(
        R.color.white,
        R.color.black,
        R.color.red,
        R.color.color_dial_color_red_orange,
        R.color.color_dial_color_orange,
        R.color.color_dial_color_yellow,
        R.color.color_main_green,
        R.color.color_text_blue,
        R.color.color_dial_color_purple
    )

    override fun layoutId() = R.layout.activity_customize_dial
    override fun initView(savedInstanceState: Bundle?) {
        ImmersionBar.with(this)
            .titleBar(titleBar)
            .init()
        tvTime.text = HelpUtil.getSpan(DateUtil.getDate(DateUtil.HH_MM, time), "AM", 8)
        sDao = AppDataBase.instance.getCustomizeDialDao()
        mCustomizeDialBean = CustomizeDialBean()
        mCustomizeDialBean.date = tvTime.text.toString()
        mCustomizeDialBean.startTime = time / 1000
        TLog.error("????????????++===" + sDao.getAllCustomizeDialList())
        imgPhoto.setOnClickListener(this)
        imgRecall.setOnClickListener(this)
        tvSave.setOnClickListener(this)
        setColorAdapter()
        setFunctionAdapter()
        setPlacementAdapter()
        setImgPicture()

    }

    private fun setImgPicture() {

    }

    private fun setPlacementAdapter() {
        mCustomizePlacementDialList = ArrayList()
        mCustomizePlacementDialList.add(CustomizePlacementBean("???", 0, true))
        mCustomizePlacementDialList.add(CustomizePlacementBean("???", 1, false))
        mCustomizePlacementDialList.add(CustomizePlacementBean("???", 2, false))
        ryPlacementDial.layoutManager = GridLayoutManager(this, 5)
        mCustomizePlacementDialAdapter = CustomizePlacementDialAdapter(mCustomizePlacementDialList)
        ryPlacementDial.adapter = mCustomizePlacementDialAdapter
        setPlacementInit(0)
        mCustomizePlacementDialAdapter.setOnItemClickListener { adapter, view, position ->
            mCustomizePlacementDialList.forEach {
                it.setSelected(false)
            }
            mCustomizePlacementDialList[position].setSelected(true)
            mCustomizePlacementDialAdapter.notifyDataSetChanged()
            setPlacementInit(position)
        }
    }

    private fun setPlacementInit(position: Int) {
        mCustomizeDialBean.locationType = position
        when (position) {
            0 -> {
                llDial.gravity = Gravity.TOP
            }
            1 -> {
                llDial.gravity = Gravity.CENTER
            }
            2 -> {
                llDial.gravity = Gravity.BOTTOM
            }
        }
    }

    private fun setFunctionInit(position: Int) {
        TLog.error("??????")
        mCustomizeDialBean.functionType = position
        if (position == 0)
            imgDialType.visibility = View.GONE
        else
            imgDialType.visibility = View.VISIBLE
        when (position) {
            0 -> {
                var ca = Calendar.getInstance()
                ca.timeInMillis = time
                TLog.error("??????==" + time)
                tvDate.text =
                    DateUtil.getDate(DateUtil.MM_AND_DD, time) + " " + DateUtil.getWeekStringEN(ca)
                mCustomizeDialBean.function =
                    DateUtil.getDate(DateUtil.MM_AND_DD, time) + " " + DateUtil.getWeekStringEN(ca)

            }
            1 -> {
                imgDialType.setImageResource(R.mipmap.icon_dial_heart)
                tvDate.text = "123"
                mCustomizeDialBean.function = "123"
            }
            2 -> {
                imgDialType.setImageResource(R.mipmap.icon_dial_step)
                tvDate.text = "12345"
                mCustomizeDialBean.function = "12345"
            }
            3 -> {
                imgDialType.setImageResource(R.mipmap.icon_dial_sleep)
                tvDate.text = "7h18m"
                mCustomizeDialBean.function = "7h18m"
            }
        }
    }

    private fun setColorInit(position: Int) {
        mCustomizeDialBean.color = position
        tvTime.setTextColor(resources.getColor(colorList[position]))
        tvDate.setTextColor(resources.getColor(colorList[position]))
        imgDialType.setColorFilter(resources.getColor(colorList[position]))
    }

    private fun setFunctionAdapter() {
        mCustomizeFunctionDialList = ArrayList()
        mCustomizeFunctionDialList.add(CustomizeFunctionBean("??????", 0, true))
        mCustomizeFunctionDialList.add(CustomizeFunctionBean("??????", 1, false))
        mCustomizeFunctionDialList.add(CustomizeFunctionBean("??????", 2, false))
        mCustomizeFunctionDialList.add(CustomizeFunctionBean("??????", 3, false))
        ryFunctionDial.layoutManager = GridLayoutManager(this, 5)
        mCustomizeFunctionDialAdapter = CustomizeFunctionDialAdapter(mCustomizeFunctionDialList)
        ryFunctionDial.adapter = mCustomizeFunctionDialAdapter
        setFunctionInit(0)
        mCustomizeFunctionDialAdapter.setOnItemClickListener { adapter, view, position ->
            mCustomizeFunctionDialList.forEach {
                it.setSelected(false)
            }
            mCustomizeFunctionDialList[position].setSelected(true)
            mCustomizeFunctionDialAdapter.notifyDataSetChanged()
            setFunctionInit(position)
        }
    }

    private fun setColorAdapter() {
        mCustomizeColorDialList = ArrayList()
        mCustomizeColorDialList.add(CustomizeColorBean("??????", 0, true))
        mCustomizeColorDialList.add(CustomizeColorBean("??????", 1, false))
        mCustomizeColorDialList.add(CustomizeColorBean("??????", 2, false))
        mCustomizeColorDialList.add(CustomizeColorBean("?????????", 3, false))
        mCustomizeColorDialList.add(CustomizeColorBean("??????", 4, false))
        mCustomizeColorDialList.add(CustomizeColorBean("??????", 5, false))
        mCustomizeColorDialList.add(CustomizeColorBean("??????", 7, false))
        mCustomizeColorDialList.add(CustomizeColorBean("??????", 6, false))
        mCustomizeColorDialList.add(CustomizeColorBean("??????", 8, false))
        ryTextColorDial.layoutManager = GridLayoutManager(this, 6)
        mCustomizeColorDialAdapter = CustomizeColorDialAdapter(mCustomizeColorDialList)
        ryTextColorDial.adapter = mCustomizeColorDialAdapter
        //  setColorInit(0)
        mCustomizeColorDialAdapter.setOnItemClickListener { adapter, view, position ->
            mCustomizeColorDialList.forEach {
                it.isColorCheck = false
            }
            mCustomizeColorDialList[position].isColorCheck = true
            mCustomizeColorDialAdapter.notifyDataSetChanged()
            setColorInit(position)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.imgPhoto -> {
                PictureSelector.create(this)
                    .openGallery(PictureMimeType.ofImage())
                    .imageEngine(GlideEngine.createGlideEngine()) // ?????????Demo GlideEngine.java
                    .maxSelectNum(1)
                    .isAndroidQTransform(true)// ??????????????????Android Q ??????????????????????????????????????????compress(false); && .isEnableCrop(false);??????,????????????
                    .selectionMode(PictureConfig.SINGLE)
                    .isPreviewImage(true)//?????????????????????
                    .isEnableCrop(true)//????????????
                    .withAspectRatio(1, 1)
                    .circleDimmedLayer(true) //????????????
                    .isCropDragSmoothToCenter(true)// ??????????????????????????????????????????
                    .circleDimmedLayer(true)// ??????????????????
                    .showCropFrame(false)// ??????????????????????????????
                    .showCropGrid(false)// ??????????????????????????????
                    .minimumCompressSize(127)// ????????????kb??????????????????
                    .cropWH(360, 360)
                    .forResult(PictureConfig.CHOOSE_REQUEST)
            }
            R.id.imgRecall -> {
//                PictureSelector.create(this).openCamera(PictureMimeType.ofImage())
//                    .imageEngine(GlideEngine.createGlideEngine())// ??????????????????????????????????????????
//                    .isUseCustomCamera(true)
//                    .isAndroidQTransform(true)// ??????????????????Android Q ??????????????????????????????????????????compress(false); && .isEnableCrop(false);??????,????????????
//                    .isEnableCrop(true)//????????????
//                    .isCropDragSmoothToCenter(true)// ??????????????????????????????????????????
//                    .circleDimmedLayer(true)// ??????????????????
//                    .showCropFrame(false)// ??????????????????????????????
//                    .showCropGrid(false)// ??????????????????????????????
//                    .minimumCompressSize(100)// ????????????kb??????????????????
//                    .cutOutQuality(90)// ?????????????????? ??????100
//                    .forResult(PictureConfig.REQUEST_CAMERA)
                imgDialBackground.setImageResource(R.drawable.round_green)
                //  ImgUtil.loadHead(imgDialBackground, R.drawable.round_green)
            }
            R.id.tvSave -> {
                TLog.error("???????????????++" + Gson().toJson(mCustomizeDialBean))
                var uiFeature = 65534
                var keyData = byteArrayOf()
                if (mCustomizeDialBean.imgPath.isNullOrEmpty()) {
                    ShowToast.showToastLong("?????????")
                    uiFeature = 65533

                    //    return
                } else {
                    uiFeature = 65534
                    keyData = FileUtils.inputStream2ByteArray(mCustomizeDialBean.imgPath)
                }
                // sDao.insert(mCustomizeDialBean)
                TLog.error("==" + keyData.size)

                BleWrite.writeDialWriteAssignCall(
                    DialCustomBean(
                        1,
                        uiFeature,
                        keyData.size,
                        mCustomizeDialBean.color,
                        mCustomizeDialBean.functionType,
                        mCustomizeDialBean.locationType
                    )
                ) {
                    when (it) {
                        1 -> {
                            ShowToast.showToastLong("????????????????????????")
                        }
                        2 -> {
                            val startByte = byteArrayOf(
                                0x00, 0xff.toByte(), 0xff.toByte(),
                                0xff.toByte()
                            )
                            TLog.error("mCustomizeDialBean.imgPath+=" + mCustomizeDialBean.imgPath)
                            var keyData =
                                FileUtils.inputStream2ByteArray(mCustomizeDialBean.imgPath)
                            TLog.error("length++" + keyData.size)
                            BleWrite.writeFlashErasureAssignCall(
                                16777215, 16777215
                            ) { key ->
                                if (key == 2) {
                                    TLog.error("????????????")
                                    FlashCall().writeFlashCall(startByte, startByte, keyData, this)
                                } else
                                    ShowToast.showToastLong("???????????????FLASH??????")
                            }

                        }
                        3 -> {
                            ShowToast.showToastLong("?????????????????????????????????")
                            //??????????????? ?????????????????????
                        }
                    }

                }


            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                PictureConfig.CHOOSE_REQUEST -> {
                    // ????????????????????????
                    val selectList = PictureSelector.obtainMultipleResult(data)
                    TLog.error("selectList+=" + selectList[0].cutPath.toString())
                    ImgUtil.loadHead(imgDialBackground, selectList[0].cutPath.toString())
                    mCustomizeDialBean.imgPath = selectList[0].cutPath.toString()
                }

            }
        }

    }

    override fun onResultFlash(size: Int, type: Int) {
        var currentProgress = ((type.toDouble() / size) * 100).toInt()
        tvSave.text = "????????????:${currentProgress}   ${type}/${size}"
        // proBar.max = size
        // proBar.progress = type
        if (size == 1 && type == 1) {
            finish()
            DataDispatcher.callDequeStatus = true
        }
    }
}