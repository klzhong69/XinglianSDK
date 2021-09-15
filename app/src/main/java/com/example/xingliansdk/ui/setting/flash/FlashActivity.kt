package com.example.xingliansdk.ui.setting.flash

import android.os.Bundle
import android.view.KeyEvent
import com.example.xingliansdk.R
import com.example.xingliansdk.base.BaseActivity
import com.example.xingliansdk.network.api.UIUpdate.UIUpdateBean
import com.example.xingliansdk.ui.setting.MyDeviceActivity
import com.example.xingliansdk.ui.setting.vewmodel.FlashViewModel
import com.example.xingliansdk.utils.FileUtils
import com.example.xingliansdk.utils.ShowToast
import com.example.xingliansdk.widget.TitleBarLayout
import com.gyf.barlibrary.ImmersionBar
import com.shon.bluetooth.DataDispatcher
import com.shon.connector.BleWrite
import com.shon.connector.utils.HexDump
import com.shon.connector.utils.TLog
import com.shon.net.callback.DownLoadCallback
import kotlinx.android.synthetic.main.activity_flash.*

class FlashActivity : BaseActivity<FlashViewModel>(), FlashWriteAssignInterface,
    BleWrite.FlashErasureAssignInterface, DownLoadCallback {
    lateinit var bean: UIUpdateBean
    private var fileName: String? = null
    var keyData = byteArrayOf()

    override fun layoutId() = R.layout.activity_flash

    override fun initView(savedInstanceState: Bundle?) {
        ImmersionBar.with(this)
            .titleBar(titleBar)
            .init()
        titleBar.setTitleBarListener(object : TitleBarLayout.TitleBarListener {
            override fun onBackClick() {
                finish()
                MyDeviceActivity.FlashBean.UIFlash = true
            }
            override fun onActionImageClick() {
            }

            override fun onActionClick() {
            }
        })
        BleWrite.writeFlashErasureAssignCall {
            var uuid = it
            TLog.error(" uuid.toString()==${uuid.toString()}")
            TLog.error(" uuid.toString()==${mDeviceFirmwareBean.productNumber}")
            mViewModel.findUpdate(mDeviceFirmwareBean.productNumber, uuid.toString())
            // mViewModel.findUpdate(""+8002,""+251658241)
        }
        MyDeviceActivity.FlashBean.UIFlash = false
    }

    override fun createObserver() {
        super.createObserver()
        mViewModel.result.observe(this)
        {
            bean = it
            if (bean.ota.isNullOrEmpty()) {
                ShowToast.showToastLong("已是最新版本")
                return@observe
            }
            startByte = HexDump.toByteArray(it.startPosition.toLong())
            endByte = HexDump.toByteArray(it.endPosition.toLong())

            BleWrite.writeFlashErasureAssignCall(it.startPosition, it.endPosition, this)
        }
        mViewModel.msg.observe(this) {
            ShowToast.showToastLong(it)
        }
    }

    override fun onResultFlash(size: Int, type: Int) {
        var currentProgress=((type.toDouble()/size)*100).toInt()
        tvCurrentProgress.text="当前进度:${currentProgress}   ${type}/${size}"
        proBar.max = size
        proBar.progress = type
        if (size == 1 && type == 1) {
            finish()
            DataDispatcher.callDequeStatus=true
        }

    }

    override fun onResultErasure(key: Int) {
        if (key == 2) {
            if (bean != null && bean.ota.isNotEmpty()) {
                mViewModel.downLoadBin(bean, this)
            }
        } else
            ShowToast.showToastLong("不支持擦写FLASH数据")
    }

    val mList = ArrayList<ByteArray>() //组装的 list  现在装了所有数据暂时没做任何操作
    var startByte = ByteArray(4)  //开始位置
    var endByte = ByteArray(4)      //结束位置

    override fun onDownLoadStart(fileName: String?) {
        this.fileName = fileName
        showWaitDialog("正在下载bin文件...")
    }

    override fun onDownLoading(totalSize: Long, currentSize: Long, progress: Int) {
    }

    var length = 0
    override fun onDownLoadSuccess() {
        hideWaitDialog()
        keyData = FileUtils.inputStream2ByteArray(fileName)
        length = keyData.size
        TLog.error("length++" + length)
        TLog.error("长度===" + FileUtils.inputStream2ByteArray(fileName).size)
        FlashCall().writeFlashCall(startByte, endByte, keyData, this)
    }

    override fun onDownLoadError() {
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
            finish()
            MyDeviceActivity.FlashBean.UIFlash = true
            return true
        }

        return super.onKeyUp(keyCode, event)
    }

}