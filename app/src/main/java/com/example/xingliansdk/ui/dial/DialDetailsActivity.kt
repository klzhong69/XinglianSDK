package com.example.xingliansdk.ui.dial

import android.os.Bundle
import com.example.xingliansdk.R
import com.example.xingliansdk.base.BaseActivity
import com.example.xingliansdk.network.api.dialView.RecommendDialBean
import com.example.xingliansdk.ui.setting.flash.FlashCall
import com.example.xingliansdk.ui.setting.flash.FlashWriteAssignInterface
import com.example.xingliansdk.ui.setting.vewmodel.MyDeviceViewModel
import com.example.xingliansdk.utils.*
import com.google.gson.Gson
import com.gyf.barlibrary.ImmersionBar
import com.shon.bluetooth.DataDispatcher
import com.shon.connector.BleWrite
import com.shon.connector.bean.DialCustomBean
import com.shon.connector.utils.TLog
import com.shon.net.DownLoadRequest
import com.shon.net.callback.DownLoadCallback
import kotlinx.android.synthetic.main.activity_dial_details.*
import java.io.File

class DialDetailsActivity : BaseActivity<MyDeviceViewModel>(), DownLoadCallback ,
    FlashWriteAssignInterface{
    var mTypeList: String? = null
    override fun layoutId() = R.layout.activity_dial_details
    var bean:RecommendDialBean.ListDTO.TypeListDTO?=null
    override fun initView(savedInstanceState: Bundle?) {
        ImmersionBar.with(this)
            .titleBar(titleBar)
            .init()
        mTypeList = intent.getStringExtra("TypeList")
          bean  = Gson().fromJson(mTypeList, RecommendDialBean.ListDTO.TypeListDTO::class.java)
        TLog.error("bean=="+Gson().toJson(bean))
        ImgUtil.loadMeImgDialCircle(imgDial, bean?.image!!)
        if (!bean?.isCharge!!)
            tvType.text = "免费"
        else
            tvType.text = "¥ ${bean?.price}"
        tvSize.text = AppUtils.getFormatSize(bean?.binSize!!.toDouble())
        tvNumber.text = "${NumUtils.formatBigNum(bean?.downloads!!)}人安装"
        circleProgressView.setOnClickListener {
            TLog.error("点击下载")
            val file = File(ExcelUtil.dialPath)
            if (!file.exists()) {
                file.mkdirs()
            }
            DownLoadRequest(bean?.ota).startDownLoad(
                "${ExcelUtil.dialPath}/${bean?.fileName}",
                this
            )
        }


    }
    private var fileName: String? = null
    override fun onDownLoadStart(fileName: String?) {
        this.fileName=fileName
    }

    override fun onDownLoading(totalSize: Long, currentSize: Long, progress: Int) {
        TLog.error("totalSize==$totalSize  currentSize==$currentSize   progress==$progress")
        circleProgressView.setProgress(progress.toFloat())
    }

    override fun onDownLoadSuccess() {
        circleProgressView.setText("完成")
        BleWrite.writeDialWriteAssignCall(
            bean?.let {it->
                DialCustomBean(
                    2,
                    it.dialId,
                    it.binSize,
                    it.name
                )
            }

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
                    var keyData =
                        FileUtils.inputStream2ByteArray(fileName)
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

    override fun onDownLoadError() {
        circleProgressView.setText("下载失败")
    }

    override fun onResultFlash(size: Int, type: Int) {

        var currentProgress = ((type.toDouble() / size) * 100).toInt()
        circleProgressView.setProgress(currentProgress.toFloat())
        // proBar.max = size
        // proBar.progress = type
        if (size == 1 && type == 1) {
            finish()
            DataDispatcher.callDequeStatus = true
        }

    }
}