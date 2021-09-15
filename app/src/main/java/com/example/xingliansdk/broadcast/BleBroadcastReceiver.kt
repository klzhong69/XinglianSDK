package com.example.xingliansdk.broadcast

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.KeyEvent
import androidx.annotation.RequiresApi
import com.example.xingliansdk.Config.eventBus.DEVICE_ELECTRICITY
import com.example.xingliansdk.eventbus.SNEventBus
import com.example.xingliansdk.utils.*
import com.google.gson.Gson
import com.shon.bluetooth.core.call.Listener
import com.shon.bluetooth.core.call.NotifyCall
import com.shon.bluetooth.util.ByteUtil
import com.shon.connector.Config
import com.shon.connector.bean.DataBean
import com.shon.connector.call.notify.XLNotifyCall
import com.shon.connector.utils.HexDump
import com.shon.connector.utils.TLog
import com.shon.connector.utils.TLog.Companion.error

class BleBroadcastReceiver : BroadcastReceiver(), XLNotifyCall.NotifyCallInterface {
    lateinit var mDataBean: DataBean
    lateinit var address: String

    //    private lateinit var message: MessageInfo
    lateinit var mContext: Context
    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        mContext = context
        mDataBean = DataBean()
        address = intent.getStringExtra("address").toString()
        if (address?.isNullOrEmpty()) {
            return
        }
        bleNotifyCall(address)
    }

    private fun bleNotifyCall(address: String?) {
        NotifyCall(address)
            .setCharacteristicUUID(Config.readCharacter)
            .setServiceUUid(Config.serviceUUID)
            .enqueue(XLNotifyCall(address, this))
    }

    @SuppressLint("NewApi")
    fun bleListener(address: String?) {
        Listener(address)
            .enqueue { _, result, uuid ->
                error("拿到的数据++" + ByteUtil.getHexString(result))
                if (result[0] == Config.PRODUCT_CODE && result[8] == Config.ActiveUpload.COMMAND
                ) {
                    when (result[9]) {
                        Config.ActiveUpload.DEVICE_REAL_TIME_EXERCISE -> //运动
                        {
                            mDataBean = DataBean()
                            mDataBean.totalSteps = HexDump.byte2intHigh(result, 10, 4).toLong()
                            mDataBean.distance = HexDump.byte2intHigh(result, 14, 4).toLong()
                            mDataBean.calories = HexDump.byte2intHigh(result, 18, 4).toLong()
                            //   mDeviceMotionInterface.DeviceMotionResult(mDataBean)
                        }
                        Config.ActiveUpload.DEVICE_REAL_TIME_OTHER -> {
                            mDataBean = DataBean()
                            mDataBean.heartHealth = result[10].toInt()
                            mDataBean.bloodOxygen = result[11].toInt()
                            mDataBean.systolicBloodPressure = result[12].toInt()
                            mDataBean.diastolicBloodPressure = result[13].toInt()
                            mDataBean.temperature = HexDump.byte2intHigh(result, 14, 2).toString()
                            mDataBean.humidity = HexDump.byte2intHigh(result, 16, 2).toString()
                            //   mDeviceMotionInterface.DeviceMotionResult(mDataBean)
                        }
                        Config.ActiveUpload.DEVICE_FIND_PHONE -> //寻找手机
                        {
                            //00默认  01发起
                        }
                        Config.ActiveUpload.DEVICE_CHANGE_PHONE_CALL_STATUS -> //设备端主动发起改变手机来电状态
                        {
                            if (result[10] == 2.toByte()) {//来电静音
                                ContactsUtil.modifyingVolume(mContext, true)
                            }
                            if (result[10] == 1.toByte()) {//挂断
                                ContactsUtil.rejectCall(mContext)
                            }
                            if (result[10] == 0.toByte()) {//默认
                                //  ContactsUtil.modifyingVolume(mContext,true)
                            }
                            //00默认  01发起
                        }
                        Config.ActiveUpload.DEVICE_CAMERA_CONFIRMATION -> //设备端主动发起相机确认信号
                        {
                            //00默认  01发起
                        }
                        Config.ActiveUpload.DEVICE_MUSIC_CONTROL_KEY -> //音乐控制
                        {
                            TLog.error("音乐控制")
                            if (result[10] == 1.toByte()) {
                                MusicControlUtil.sendKeyEvents(
                                    mContext,
                                    KeyEvent.KEYCODE_MEDIA_NEXT
                                )//下一首
                            }
                            if (result[10] == 2.toByte()) {
                                MusicControlUtil.sendKeyEvents(
                                    mContext,
                                    KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                                )//播放/暂停
                            }
                            if (result[10] == 3.toByte()) {
                                MusicControlUtil.sendKeyEvents(
                                    mContext,
                                    KeyEvent.KEYCODE_MEDIA_PREVIOUS
                                )//上一首
                            }
                            if (result[10] == 4.toByte()) {
                                VolumeControlUtil.setVolumeUp(mContext)
                            }
                            if (result[10] == 5.toByte()) {
                                VolumeControlUtil.setVolumeDown(mContext)
                            }


                            //00默认  01发起
                        }
                        Config.ActiveUpload.DEVICE_WARNING_SIGNAL_KEY -> //设备端主动发起告警信号
                        {
                            //00默认  01发起
                        }
                        Config.ActiveUpload.DEVICE_POWER_CHANGE_KEY -> //电量变化
                        {
                            TLog.error("电量变化")
                            val  electricity=result[10]
                            if (result[11] > 0x0F) {
                             val   mDisplayBattery = (result[11].toInt() shl 4)
                                val   mCurrentBattery = (result[11].toInt() shr 4)
                                TLog.error("实时监听的电量++"+mCurrentBattery)
                                TLog.error("实时监听的电量++"+mDisplayBattery)
                                SNEventBus.sendEvent(DEVICE_ELECTRICITY, electricity)
                            }


                        }

                    }

                }

                true
            }
    }
//
//    interface MessageInfo {
//        fun getMessage(data: DataBean)
//    }
//
//    fun setMessage(message: MessageInfo) {
//        this.message = message
//    }

    override fun NotifyCallResult(key: Byte, mData: DataBean?) {
        when (key) {
            Config.ActiveUpload.DEVICE_REAL_TIME_EXERCISE -> //运动
            {
                mDataBean = DataBean()
                if (mData != null) {
                    mDataBean.totalSteps = mData.totalSteps
                    mDataBean.distance = mData.distance
                    mDataBean.calories = mData.calories
                }
                SNEventBus.sendEvent(
                    Config.ActiveUpload.DEVICE_REAL_TIME_EXERCISE.toInt(),
                    mDataBean
                )
//            TLog.error("你好实时运动"+mDataBean.totalSteps )
            }
            Config.ActiveUpload.DEVICE_REAL_TIME_OTHER -> {
                mDataBean = DataBean()
                if (mData != null) {
                    mDataBean.heartRate = mData.heartRate
                    mDataBean.bloodOxygen = mData.bloodOxygen
                    mDataBean.systolicBloodPressure = mData.systolicBloodPressure
                    mDataBean.diastolicBloodPressure = mData.diastolicBloodPressure
                    mDataBean.temperature = mData.temperature
                    mDataBean.humidity = mData.humidity
                    TLog.error("===${Gson().toJson(mDataBean)}")
                    SNEventBus.sendEvent(
                        Config.ActiveUpload.DEVICE_REAL_TIME_OTHER.toInt(),
                        mDataBean
                    )
                    //   message.getMessage(mDataBean)
                }

            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun NotifyCallResult(key: Byte, type: Int) {
        TLog.error(" NotifyCallResult ")
        when (key) {
            Config.ActiveUpload.DEVICE_FIND_PHONE -> //寻找手机
            {
                BandCallPhoneNotifyUtil.startNotification(mContext)
            }
            Config.ActiveUpload.DEVICE_CHANGE_PHONE_CALL_STATUS -> //设备端主动发起改变手机来电状态
            {
                if (type == 2) {//来电静音
                    ContactsUtil.modifyingVolume(mContext, true)
                }
                if (type == 1) {//挂断
                    ContactsUtil.rejectCall(mContext)
                }
                if (type == 0) {//默认
                    //  ContactsUtil.modifyingVolume(mContext,true)
                }
            }
            Config.ActiveUpload.DEVICE_CAMERA_CONFIRMATION -> //设备端主动发起相机确认信号
            {
                //00默认  01发起
            }
            Config.ActiveUpload.DEVICE_MUSIC_CONTROL_KEY -> //音乐控制
            {
                TLog.error("音乐 NotifyCallResult type+$type")
                if (type == 1) {
                    MusicControlUtil.sendKeyEvents(mContext, KeyEvent.KEYCODE_MEDIA_NEXT)//下一首
                }
                if (type == 2) {
                    MusicControlUtil.sendKeyEvents(
                        mContext,
                        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                    )//播放/暂停
                }
                if (type == 3) {
                    MusicControlUtil.sendKeyEvents(mContext, KeyEvent.KEYCODE_MEDIA_PREVIOUS)//上一首
                }
                if (type == 4) {
                    VolumeControlUtil.setVolumeUp(mContext)
                }
                if (type == 5) {
                    VolumeControlUtil.setVolumeDown(mContext)
                }


                //00默认  01发起
            }
            Config.ActiveUpload.DEVICE_POWER_CHANGE_KEY -> //设备端主动上传设备电量变化信息
            {
                TLog.error("电量变化2222"+type)
                SNEventBus.sendEvent(DEVICE_ELECTRICITY, type)
//                val  electricity=result[10]
//                if (result[11] > 0x0F) {
//                    val   mDisplayBattery = (result[11].toInt() shl 4)
//                    val   mCurrentBattery = (result[11].toInt() shr 4)
//                    TLog.error("实时监听的电量++"+mCurrentBattery)
//                    TLog.error("实时监听的电量++"+mDisplayBattery)
//                    SNEventBus.sendEvent(DEVICE_ELECTRICITY, electricity)
//                }


              //  SNEventBus.sendEvent(DEVICE_ELECTRICITY, electricity)
            }
        }
    }
}