package com.example.xingliansdk.dfu

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.ViewModelProvider
import com.example.otalib.DFUViewModel
import com.example.otalib.service.DfuService
import com.example.xingliansdk.Config
import com.example.xingliansdk.R
import com.example.xingliansdk.base.BaseActivity
import com.example.xingliansdk.blecontent.BleConnection
import com.example.xingliansdk.eventbus.SNEvent
import com.example.xingliansdk.eventbus.SNEventBus
import com.example.xingliansdk.network.api.otaUpdate.OTAUpdateBean
import com.example.xingliansdk.ui.setting.vewmodel.MyDeviceViewModel
import com.example.xingliansdk.utils.ShowToast
import com.google.gson.Gson
import com.gyf.barlibrary.ImmersionBar
import com.orhanobut.hawk.Hawk
import com.shon.bluetooth.BLEManager
import com.shon.bluetooth.util.ByteUtil
import com.shon.connector.BleWrite
import com.shon.connector.utils.TLog
import com.shon.net.callback.DownLoadCallback
import kotlinx.android.synthetic.main.activity_update_zip.*
import kotlinx.android.synthetic.main.activity_update_zip.titleBar
import no.nordicsemi.android.dfu.DfuProgressListener
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class DFUActivity : BaseActivity<MyDeviceViewModel>(), DfuProgressListener, DownLoadCallback,
    View.OnClickListener, BleWrite.BleInterface {
    private lateinit var dfuViewModel: DFUViewModel
    lateinit var address: String
    lateinit var name: String
    private var fileName: String? = null
    lateinit var productNumber: String
    private var otaBean: OTAUpdateBean? = null
    var status = false
    var version = 0
    override fun createObserver() {
        mViewModel.result.observe(this) {
            TLog.error("IT==" + Gson().toJson(it))
            otaBean = it
            if (otaBean?.ota.isNullOrEmpty())
            {
                tvBegan.visibility=View.GONE
                tvUpdateCode.text = "??????????????????"
            }
            else {
                //  tvUpdateCode.text = otaBean?.version
                this.version = otaBean?.versionCode!!
                var code = otaBean?.versionCode?.toString(16)
                var codeName = ByteUtil.hexStringToByte(code)
                if (codeName.size >= 3) {
                    name =
                        "V " + codeName[0].toString() + "." + codeName[1].toString() + "." + codeName[2].toString()
                }
                tvUpdateCode.text = "????????????: " + name
                if (BleConnection.startOTAActivity) {
                  //  showWaitDialog("??????ota????????????")
                    otaBean?.let { mViewModel.downLoadZIP(it, this) }
                }
            }
        }
        mViewModel.msg.observe(this) {
            TLog.error("?????????")
            ShowToast.showToastLong(it)
            proBar.visibility=View.GONE
            tvBegan.visibility = View.GONE
        }
    }

    //    var mWindow: Window ?=null
    override fun layoutId() = R.layout.activity_update_zip
    override fun initView(savedInstanceState: Bundle?) {
        //  mWindow = window
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        SNEventBus.register(this)
        dfuViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(application)

            .create(DFUViewModel::class.java)
        ImmersionBar.with(this)
            .titleBar(titleBar)
            .init()
        tvNowVersionName.text = mDeviceFirmwareBean.versionName
        address = intent.getStringExtra("address").toString()
        name = intent.getStringExtra("name").toString()
        productNumber = intent.getStringExtra("productNumber").toString()
        status = intent.getBooleanExtra("writeOTAUpdate", false)
        version = intent.getIntExtra("version", 0)
        tvBegan.setOnClickListener(this)
        fileName = Hawk.get<String>("OTAFile")
        Hawk.put("dfuAddress", address)
        TLog.error(
            "address=" + address
                    + "    name+=" + name
                    + "    productNumber+=" + productNumber
                    + "    version+=" + version
        )
        TLog.error("fileName==" + fileName)
        if (status) {
            tvBegan.visibility = View.VISIBLE
            airUpgradeTv.visibility = View.GONE
            proBar.visibility = View.GONE
        } else {
            tvBegan.visibility = View.GONE
            airUpgradeTv.visibility = View.VISIBLE
            proBar.visibility = View.VISIBLE
        }

        //version = 1  //????????????
        TLog.error(
            "?????? ???" + productNumber
                    + "===+" + version
        )
        mViewModel.findUpdate(productNumber, version)  //????????????
//        }
        dfuViewModel.attachView(this, this)
    }

    override fun onDownLoadStart(fileName: String?) {
        this.fileName = fileName!!
        ShowToast.showToastLong("?????????????????????,????????????????????????..")
        showWaitDialog("??????ota????????????")
    }

    override fun onDownLoading(totalSize: Long, currentSize: Long, progress: Int) {

    }

    override fun onDownLoadSuccess() {
        TLog.error("????????????")
        hideWaitDialog()
        if (status) //????????????????????????????????????
        {
            TLog.error("status==" + status)
            BleWrite.writeOTAUpdateCall(this)
            Handler().postDelayed({ // ?????????????????????????????????????????????????????????????????????????????????
                TLog.error("5???")
                updateMac()
            }, 5000)

        } else {
            dfuViewModel.startDFU(address, name, "$fileName", this)
        }


    }

    override fun onDownLoadError() {
        hideWaitDialog()
        ShowToast.showToastLong("????????????")
    }

    private fun updateMac() {
        var address1 = Hawk.get<String>("address")
        BLEManager.getInstance().disconnectDevice(address1)
        val s = ByteUtil.byteToHex((address1.substring(address1.length - 2).toInt(16) + 1).toByte())
        address1 = address1.substring(0, address1.length - 2) + s
        TLog.error("???????????????++$address1")
        address = address1//???????????????????????? address?????????????????????mac?????? ?????????address=address1 ??????
        Hawk.put("dfuAddress", address1)
        Hawk.put(Config.database.DEVICE_OTA, true)
        Hawk.put("OTAFile", fileName)
        BleConnection.initStart(/*it,*/true)
    }

    override fun onResult() {
        TLog.error("?????????")
        //    updateMac()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tvBegan -> {
                tvBegan.visibility = View.GONE
                airUpgradeTv.visibility = View.VISIBLE
                proBar.visibility = View.VISIBLE
                otaBean?.let { mViewModel.downLoadZIP(it, this) }

            }
        }
    }

    //**********?????????  DfuProgressListener  ??? ?????? *********************/
    //********** ????????????????????????????????????????????????????????????????????????   *************/
    override fun onDeviceConnecting(deviceAddress: String?) {
        TLog.error("onDeviceConnecting")
        proBar.isIndeterminate = true
        airUpgradeTv.setText(R.string.dfu_status_connecting)
    }

    override fun onDeviceConnected(deviceAddress: String?) {
        TLog.error("onDeviceConnected")
    }

    override fun onDfuProcessStarting(deviceAddress: String?) {
        TLog.error("onDfuProcessStarting")
    }

    override fun onDfuProcessStarted(deviceAddress: String?) {
        TLog.error("onDfuProcessStarted")
        proBar.isIndeterminate = true
        airUpgradeTv.setText(R.string.dfu_status_starting)
    }

    override fun onEnablingDfuMode(deviceAddress: String?) {
        //??????DFU ??????????????????????????????????????????????????????
        proBar.isIndeterminate = true
        airUpgradeTv.setText(R.string.dfu_status_switching_to_dfu)
    }

    override fun onProgressChanged(
        deviceAddress: String?,
        percent: Int,
        speed: Float,
        avgSpeed: Float,
        currentPart: Int,
        partsTotal: Int
    ) {
        //????????????
//        TLog.error(
//            "deviceAddress+=${deviceAddress}"
//                    + "\n?????????percent+=${percent}"
//                    + "\n??????speed++${speed}"
//                    + "\n????????????++${avgSpeed}"
//                    + "\ncurrentPart+=${currentPart}"
//                    + "\npartsTotal+=${partsTotal}"
//        )
        proBar.progress = percent
        proBar.isIndeterminate = false
        airUpgradeTv.text = "$percent%"
    }

    override fun onFirmwareValidating(deviceAddress: String?) {
        TLog.error("onFirmwareValidating")
        //????????????
    }

    override fun onDeviceDisconnecting(deviceAddress: String?) {
        TLog.error("onDeviceDisconnecting")
    }

    override fun onDeviceDisconnected(deviceAddress: String?) {
        TLog.error("onDeviceDisconnected")
        proBar.isIndeterminate = true
        airUpgradeTv.setText(R.string.dfu_status_disconnecting)
        Handler().postDelayed({
            TLog.error("5???")
            updateMac()
        }, 3000)
    }

    override fun onDfuCompleted(deviceAddress: String?) {
        TLog.error("onDfuCompleted")
        airUpgradeTv.setText(R.string.dfu_status_completed)
        proBar.progress = 100
        proBar.isIndeterminate = false
        //??????????????????????????????????????????????????????????????????
        Handler().postDelayed({ // ?????????????????????????????????????????????????????????????????????????????????

            BLEManager.getInstance().disconnectDevice(deviceAddress)
            val manager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(DfuService.NOTIFICATION_ID)
            TLog.error("==" + Hawk.get("address"))
            TLog.error("dfuAddress==" + Hawk.get("dfuAddress"))
            // BLEManager.getInstance().disconnectDevice(Hawk.get("dfuAddress"))
            // BLEManager.getInstance().disconnectDevice(Hawk.get("address"))
            Hawk.put("dfuAddress", "")
//            Hawk.put("address", "")
//            Hawk.put("name", "")
            mDeviceFirmwareBean.version=version
            mDeviceFirmwareBean.versionName=name
            Hawk.put("DeviceFirmwareBean",
                mDeviceFirmwareBean
            )
            BleConnection.iFOta = false
            Hawk.put("ota", false)
            BleConnection.Unbind = true
         //   BleConnection.initStart()
            SNEventBus.sendEvent(Config.eventBus.DEVICE_DELETE_DEVICE)

        }, 200)

        //  JumpUtil.restartApp(this)
         BleConnection.initStart(false) //?????????
        // JumpUtil.startBleConnectActivity(this)
        finish()
    }

    override fun onDfuAborted(deviceAddress: String?) {
        TLog.error("onDfuAborted ???????????? ??????????????????????????????????????????????????????")
        //???????????? ??????????????????????????????????????????????????????
    }

    override fun onError(deviceAddress: String?, error: Int, errorType: Int, message: String?) {
        ShowToast.showToastLong("????????????")
        // Hawk.put("ota", true)
        // BleConnection.initStart(true)
        // finish()
        //????????????   ???????????????????????????????????????????????????
        TLog.error(
            "deviceAddress+=${deviceAddress}"
                    + "\nerror+=${error}"
                    + "\nerrorType++${errorType}"
                    + "\nmessage++${message}"
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventReceived(event: SNEvent<Any>) {
        when (event.code) {
            Config.eventBus.DEVICE_OTA_UPDATE -> {
                TLog.error(
                    "????????????==??????  mac+" + address
                            + "\n  name==" + name
                            + "\n fileName===" + fileName
                )
                dfuViewModel.startDFU(address, name, "$fileName", this)
            }
        }

    }
}