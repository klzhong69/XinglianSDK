package com.shon.connector.call.notify;

import com.shon.bluetooth.util.ByteUtil;
import com.shon.connector.utils.TLog;
import com.shon.bluetooth.core.call.Listener;
import com.shon.bluetooth.core.callback.ICallback;
import com.shon.bluetooth.core.callback.NotifyCallback;
import com.shon.connector.Config;
import com.shon.connector.bean.DataBean;
import com.shon.connector.utils.HexDump;

/**
 * 3.5
 */
public class XLNotifyCall extends NotifyCallback {
    private DataBean mDataBean;
    private String address;
    NotifyCallInterface mInterface;
   public XLNotifyCall(String address,NotifyCallInterface mInterface)
    {
        this.address=address;
        this.mInterface=mInterface;

    }
    public interface NotifyCallInterface {
        void NotifyCallResult(byte key, DataBean mDataBean);
        void NotifyCallResult(byte key, int type);
    }

    @Override
    public boolean getTargetSate() {
        return true;
    }

    @Override
    public void onTimeout() {
        TLog.Companion.error("超时");
    }

    @Override
    public void onChangeResult(boolean result) {
        super.onChangeResult(result);
        new Listener(address).enqueue(new ICallback() {
            @Override
            public boolean process(String address, byte[] result,String uuid) {
//                TLog.Companion.error("uuid=="+uuid);
                if(!Config.readCharacter.equalsIgnoreCase(uuid))
                    return false;
//               TLog.Companion.error("广播值++"+ ByteUtil.getHexString(result));
                if (result[0] == Config.PRODUCT_CODE && result[8] == Config.ActiveUpload.COMMAND
                ) {
                    switch (result[9])
                    {
                      case   Config.ActiveUpload.DEVICE_REAL_TIME_EXERCISE: //运动
                        {
                            mDataBean= new DataBean();
                            mDataBean.setTotalSteps(HexDump.byte2intHigh(result, 10, 4));
                            mDataBean.setDistance(HexDump.byte2intHigh(result, 14, 4));
                            mDataBean.setCalories(HexDump.byte2intHigh(result, 18, 4));
                            mInterface.NotifyCallResult(result[9],mDataBean);
                            break;
                        }
                        case  Config.ActiveUpload.DEVICE_REAL_TIME_OTHER:
                        {

                            mDataBean=new DataBean();
                            mDataBean.setHeartRate(result[10]);
                            mDataBean.setBloodOxygen(result[11]);
                            mDataBean.setSystolicBloodPressure(result[12]);
                            mDataBean.setDiastolicBloodPressure(result[13]);
                            mDataBean.setTemperature(String.valueOf(HexDump.byte2intHigh(result, 14, 2)));
                            mDataBean.setHumidity(String.valueOf(HexDump.byte2intHigh(result, 16, 2)));
                            mInterface.NotifyCallResult(result[9],mDataBean);
                          TLog.Companion.error("其他心率之类的的++"+result[10]);
                            break;
                        }
                        case   Config.ActiveUpload.DEVICE_FIND_PHONE: //寻找手机
//                        {
//                            mInterface.NotifyCallResult(result[9],result[10]);
//                            //00默认  01发起
//                        }
                        case   Config.ActiveUpload.DEVICE_CHANGE_PHONE_CALL_STATUS://设备端主动发起改变手机来电状态
                        case   Config.ActiveUpload.DEVICE_CAMERA_CONFIRMATION: //设备端主动发起相机确认信号
                        case  Config.ActiveUpload.DEVICE_MUSIC_CONTROL_KEY: //音乐控制
                        case   Config.ActiveUpload.DEVICE_WARNING_SIGNAL_KEY: //设备端主动发起告警信号
                        {
                            TLog.Companion.error("音乐++"+result[9]);
                            mInterface.NotifyCallResult(result[9], result[10]);
                            break;
                        }
                        case  Config.ActiveUpload.DEVICE_POWER_CHANGE_KEY : //设备端主动上传设备电量变化信息
                        {
                            TLog.Companion.error("111电量变化2222");
                            mInterface.NotifyCallResult(result[9], result[10]);
                       //     int   electricity=result[10];
//                            if (result[11] > 0x0F) {
//                                int   mDisplayBattery = (result[11]<<4);
//                                int   mCurrentBattery = (result[11]>>4);
//                                TLog.Companion.error("实时监听的电量++"+mCurrentBattery);
//                                TLog.Companion.error("实时监听的电量++"+mDisplayBattery);
//
//                            }

                            //                SNEventBus.sendEvent(DEVICE_ELECTRICITY, electricity)
                            break;
                        }


                    }
                    return true;
                }
                return true;
            }
        });
    }

//    @Override
//    public boolean process(String address, byte[] result) {
//        TLog.Companion.error("步数=="+ ByteUtil.getHexString(result));
//      //  return super.process(address, result);
//        return false;
//    }
}
