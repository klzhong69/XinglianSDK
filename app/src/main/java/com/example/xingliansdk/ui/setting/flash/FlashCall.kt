package com.example.xingliansdk.ui.setting.flash

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import com.example.xingliansdk.utils.ShowToast
import com.shon.bluetooth.core.call.WriteCall
import com.shon.bluetooth.core.callback.WriteCallback
import com.shon.bluetooth.core.gatt.CharacteristicGattCallback
import com.shon.bluetooth.util.BleLog
import com.shon.bluetooth.util.ByteUtil
import com.shon.connector.BleWrite
import com.shon.connector.Config
import com.shon.connector.utils.HexDump
import com.shon.connector.utils.TLog
import java.util.*


class FlashCall {
    var length = 0 //总长度
    fun writeFlashCall(startKey: ByteArray, endKey: ByteArray, count: ByteArray,mInterface: FlashWriteAssignInterface) {
        val write = WriteCall(BleWrite.address)
        write.isPriority = true
        write.setTimeout(30 * 60 * 1000)
        write.setServiceUUid(Config.serviceUUID)
        write.setCharacteristicUUID(Config.WriteCharacterBig)
        length = count.size + 17
        write.enqueue(FlashCallback(write, startKey, endKey, count, length,mInterface))
    }
}
interface FlashWriteAssignInterface {
    fun onResultFlash(size: Int, type: Int)
}
public class FlashCallback(
    val call: WriteCall,
    startKey: ByteArray,
    endKey: ByteArray,
    count: ByteArray,
    length: Int,
    mInterface: FlashWriteAssignInterface
) : WriteCallback(BleWrite.address) {
    var length=length
    var mList: MutableList<List<ByteArray>> = mutableListOf()
    var mInterface=mInterface
    private var mIndex = 0
    var arraySize: Int = count.size / 4096
    init {
      //  var arraySize: Int = count.size / 4096
        val list: MutableList<ByteArray> = mutableListOf()
        if (count.size % 4096 > 0) {
            arraySize += 1
        }
        for (i in 0 until arraySize) {
            val srcStart = i * 4096
            var array = ByteArray(4096)
            if (count.size - srcStart < 4096) {
                array = ByteArray(count.size - srcStart)
                System.arraycopy(count, srcStart, array, 0, count.size - srcStart)
            } else
                System.arraycopy(count, srcStart, array, 0, array.size)
            list.add(array)
        }
        list.forEachIndexed { index, childrArry ->
            val ll: MutableList<ByteArray> = mutableListOf()
            var arraySize2: Int = childrArry.size / 243
            if (count.size % 243 > 0) {
                arraySize2 += 1
            }
            for (i in 0 until arraySize2) {
                var array = ByteArray(243)
                if (i == 0 && index == 0) { //只有第一位的第一个需要
                    array = ByteArray(218)
                    System.arraycopy(childrArry, 0, array, 0, array.size)
                    array = ByteUtil.hexStringToByte(keyValue(startKey, endKey, array))
                } else if (i == (arraySize2 - 1)) {
                    var srcStart = i * 243
                    if(index==0)
                        srcStart -= 25
                    var num = childrArry.size - (srcStart)
                    array = ByteArray(num)
                    System.arraycopy(childrArry, srcStart, array, 0, array.size)
                } else {
                    val srcStart = i * 243
                    System.arraycopy(childrArry, srcStart, array, 0, array.size)
                }
                val arrayXOR = HexDump.byteMerger(array, HexDump.byteXOR(array))
                ll.add(arrayXOR)
            }
            mList.add(ll)
        }
    }

    //返回监听处理
    override fun process(address: String?, result: ByteArray, uuid: String?): Boolean {
        //处理 4096 返回的数据
        if (result[8] == Config.Flash.KEY && result[9] == Config.Flash.DEVICE_FLASH_OK_UPDATE) {
            when (result[10]) {
                1.toByte() -> {
                    sendFinsh=true
                    mInterface.onResultFlash(-1,-1)
                    ShowToast.showToastLong("更新失败")
                }
                2.toByte() -> {
                    sendFinsh=true
                    mInterface.onResultFlash(1,1)
                    ShowToast.showToastLong("更新成功")
                }
                3.toByte() -> {
                    mIndex = 0
                    sendFlash(0)

                  //  mInterface.onResultFlash(1,1)
                    TLog.error("第 1 个 4K 数据块异常（含 APP 端发擦写和实际写入的数据地址不一致），APP 需要重走流程")

                }
                4.toByte() -> {
                    sendFlash(0)
                    TLog.error("非第 1 个 4K 数据块异常，需要重新发送当前 4K 数据块")
                }
                5.toByte() -> {
                    mIndex++
                    sendFlash(0)
                    mInterface.onResultFlash(arraySize,mIndex)
                    TLog.error("4K 数据块正常，发送下一个包")
                }
                6.toByte() -> {
                    mIndex = 0
                    sendFlash(0)
                    TLog.error("4K错误,全部重发")
                }
            }
            return true
        }

        return false//返回了很多 无用的我也不知道咋办只能false
    }

    fun sendFlash(index: Int) {
        if (mIndex >= mList.size) {
            TLog.error("mIndex $mIndex  mList.size${mList.size}")
            sendFinsh = true
            return
        }
        Thread {
            var index = index
            while (index < mList[mIndex].size) {
                if (CharacteristicGattCallback.flashB) {
                    CharacteristicGattCallback.flashB = false
                    sendData(mList[mIndex][index])
                    index++
                }
            }
        }.start()
    }

    private var sendFinsh = false
    override fun isFinish(): Boolean {
        TLog.error("最终返回 isFinish==$sendFinsh")
        return sendFinsh
    }

    override fun onTimeout() {
    }

    override fun getSendData(): ByteArray {
        android.os.Handler().postDelayed({
            sendFlash(1)
        }, 100)
        return mList[0][0]
    }

    fun sendData(bytes: ByteArray) {

        val gatt: BluetoothGatt? = call.bluetoothGatt()
        if (gatt == null) {
            BleLog.e("WriteCall gatt is null")
            return
        }
        val characteristic: BluetoothGattCharacteristic? = gattCharacteristic(gatt)
        if (characteristic == null) {
            BleLog.e("WriteCall characteristic is null")
            return
        }

        val setValue = characteristic.setValue(bytes)
        if (setValue) {
            gatt.writeCharacteristic(characteristic)
        }
    }

    private fun gattCharacteristic(gatt: BluetoothGatt): BluetoothGattCharacteristic? {
        val service = gatt.getService(UUID.fromString(Config.serviceUUID)) ?: return null
        return service.getCharacteristic(UUID.fromString(Config.WriteCharacterBig))
    }

    private fun keyValue(
        startKey: ByteArray,
        endKey: ByteArray,
        sendData: ByteArray
    ): String? {
       var length= ByteUtil.getHexString(HexDump.toByteArray(length))
        return "880000" +length + "000805010009" +  //索引,长度
                ByteUtil.getHexString(startKey) +  //起始位
                ByteUtil.getHexString(endKey) +  //结束位
                "0202FFFF" +  //含crc效验包,索引2,俩个字节的长度
                ByteUtil.getHexString(sendData) //+
    }


}