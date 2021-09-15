package com.example.xingliansdk.broadcast;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.xingliansdk.blecontent.BleConnection;
import com.orhanobut.hawk.Hawk;
import com.shon.connector.utils.TLog;

import static com.example.xingliansdk.Config.database.DEVICE_OTA;

public class BluetoothMonitorReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
//                        case BluetoothAdapter.STATE_TURNING_ON:
                        case BluetoothAdapter.STATE_ON:
                            TLog.Companion.error("广播重连机制");
                            BleConnection.INSTANCE.initStart(Hawk.get(DEVICE_OTA, false),60000);
                            break;
//                        case BluetoothAdapter.STATE_TURNING_OFF:
                        case BluetoothAdapter.STATE_OFF:
                            TLog.Companion.error("====蓝牙已经关闭");

                            Toast.makeText(context, "蓝牙已经关闭", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    break;
//                case BluetoothDevice.ACTION_ACL_CONNECTED:
//                    Toast.makeText(context, "蓝牙设备已连接", Toast.LENGTH_SHORT).show();
//                    break;
//                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
//                    Toast.makeText(context, "蓝牙设备已断开", Toast.LENGTH_SHORT).show();
//                    break;
            }

        }
    }

}
