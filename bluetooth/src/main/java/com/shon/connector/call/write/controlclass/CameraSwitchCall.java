package com.shon.connector.call.write.controlclass;

import com.example.xingliansdk.utils.ShowToast;
import com.shon.connector.BleWrite;
import com.shon.connector.Config;
import com.shon.connector.call.CmdUtil;
import com.shon.bluetooth.core.callback.WriteCallback;

/**
 * 3.3.10
 * 相机开关
 */
public class CameraSwitchCall extends WriteCallback {
    byte keyValue;
    BleWrite.CameraSwitchCallInterface mInterface;
    public CameraSwitchCall(String address) {
        super(address);
    }
    public CameraSwitchCall(String address, byte keyValue, BleWrite.CameraSwitchCallInterface mInterface) {
        super(address);
        this.keyValue=keyValue;
        this.mInterface=mInterface;
    }
    @Override
    public byte[] getSendData() {
        byte payload[] = {0x01, 0x0A, 0x00,keyValue};//由 command(1byte) key(1byte) keyValueLeng(2byte)以及keyValue(长度不定)
        return CmdUtil.getFullPackage(payload);
    }

    @Override
    public boolean process(String address, byte[] result,String uuid) {
        if(!uuid.equalsIgnoreCase(Config.readCharacter))
            return false;

        if (result[8] == 0x07 && result[9] == 0x03) {
            if (result[8] == 0x07 && result[9] == Config.DEVICE_KEY_ACK) {
                switch (result[10]) {
                    case 0x01:

                        break;
                    case 0x02:
                    case 0x03:
                        BleWrite.writeCameraSwitchCall(keyValue,mInterface); //重新发送的操作
                        break;
                    case 0x04:
                        ShowToast.INSTANCE.showToastLong("设备不支持当前协议");
                        break;
                }
            }

        }
        return false;

    }

    @Override
    public void onTimeout() {

    }
}
