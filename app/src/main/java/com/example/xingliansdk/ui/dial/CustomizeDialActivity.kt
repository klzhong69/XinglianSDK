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
        TLog.error("本地数据++===" + sDao.getAllCustomizeDialList())
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
        mCustomizePlacementDialList.add(CustomizePlacementBean("上", 0, true))
        mCustomizePlacementDialList.add(CustomizePlacementBean("中", 1, false))
        mCustomizePlacementDialList.add(CustomizePlacementBean("下", 2, false))
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
        TLog.error("跑了")
        mCustomizeDialBean.functionType = position
        if (position == 0)
            imgDialType.visibility = View.GONE
        else
            imgDialType.visibility = View.VISIBLE
        when (position) {
            0 -> {
                var ca = Calendar.getInstance()
                ca.timeInMillis = time
                TLog.error("跑了==" + time)
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
        mCustomizeFunctionDialList.add(CustomizeFunctionBean("日期", 0, true))
        mCustomizeFunctionDialList.add(CustomizeFunctionBean("心率", 1, false))
        mCustomizeFunctionDialList.add(CustomizeFunctionBean("记步", 2, false))
        mCustomizeFunctionDialList.add(CustomizeFunctionBean("睡眠", 3, false))
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
        mCustomizeColorDialList.add(CustomizeColorBean("白色", 0, true))
        mCustomizeColorDialList.add(CustomizeColorBean("黑色", 1, false))
        mCustomizeColorDialList.add(CustomizeColorBean("红色", 2, false))
        mCustomizeColorDialList.add(CustomizeColorBean("红橙色", 3, false))
        mCustomizeColorDialList.add(CustomizeColorBean("橙色", 4, false))
        mCustomizeColorDialList.add(CustomizeColorBean("黄色", 5, false))
        mCustomizeColorDialList.add(CustomizeColorBean("青色", 7, false))
        mCustomizeColorDialList.add(CustomizeColorBean("蓝色", 6, false))
        mCustomizeColorDialList.add(CustomizeColorBean("紫色", 8, false))
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
                    .imageEngine(GlideEngine.createGlideEngine()) // 请参考Demo GlideEngine.java
                    .maxSelectNum(1)
                    .isAndroidQTransform(true)// 是否需要处理Android Q 拷贝至应用沙盒的操作，只针对compress(false); && .isEnableCrop(false);有效,默认处理
                    .selectionMode(PictureConfig.SINGLE)
                    .isPreviewImage(true)//是否可预览图片
                    .isEnableCrop(true)//是否裁剪
                    .withAspectRatio(1, 1)
                    .circleDimmedLayer(true) //圆形裁剪
                    .isCropDragSmoothToCenter(true)// 裁剪框拖动时图片自动跟随居中
                    .circleDimmedLayer(true)// 是否圆形裁剪
                    .showCropFrame(false)// 是否显示裁剪矩形边框
                    .showCropGrid(false)// 是否显示裁剪矩形网格
                    .minimumCompressSize(127)// 小于多少kb的图片不压缩
                    .cropWH(360, 360)
                    .forResult(PictureConfig.CHOOSE_REQUEST)
            }
            R.id.imgRecall -> {
//                PictureSelector.create(this).openCamera(PictureMimeType.ofImage())
//                    .imageEngine(GlideEngine.createGlideEngine())// 外部传入图片加载引擎，必传项
//                    .isUseCustomCamera(true)
//                    .isAndroidQTransform(true)// 是否需要处理Android Q 拷贝至应用沙盒的操作，只针对compress(false); && .isEnableCrop(false);有效,默认处理
//                    .isEnableCrop(true)//是否裁剪
//                    .isCropDragSmoothToCenter(true)// 裁剪框拖动时图片自动跟随居中
//                    .circleDimmedLayer(true)// 是否圆形裁剪
//                    .showCropFrame(false)// 是否显示裁剪矩形边框
//                    .showCropGrid(false)// 是否显示裁剪矩形网格
//                    .minimumCompressSize(100)// 小于多少kb的图片不压缩
//                    .cutOutQuality(90)// 裁剪输出质量 默认100
//                    .forResult(PictureConfig.REQUEST_CAMERA)
                imgDialBackground.setImageResource(R.drawable.round_green)
                //  ImgUtil.loadHead(imgDialBackground, R.drawable.round_green)
            }
            R.id.tvSave -> {
                TLog.error("保存时数据++" + Gson().toJson(mCustomizeDialBean))
                var uiFeature = 65534
                var keyData = byteArrayOf()
                if (mCustomizeDialBean.imgPath.isNullOrEmpty()) {
                    ShowToast.showToastLong("你莫管")
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
                            ShowToast.showToastLong("设备存储空间不够")
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
                                    TLog.error("开始擦写")
                                    FlashCall().writeFlashCall(startByte, startByte, keyData, this)
                                } else
                                    ShowToast.showToastLong("不支持擦写FLASH数据")
                            }

                        }
                        3 -> {
                            ShowToast.showToastLong("设备已经有存储这个表盘")
                            //给后台一个 更改表盘的指令
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
                    // 图片选择结果回调
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
        tvSave.text = "当前进度:${currentProgress}   ${type}/${size}"
        // proBar.max = size
        // proBar.progress = type
        if (size == 1 && type == 1) {
            finish()
            DataDispatcher.callDequeStatus = true
        }
    }
}