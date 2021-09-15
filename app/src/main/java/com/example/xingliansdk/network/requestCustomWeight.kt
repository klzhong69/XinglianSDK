package com.example.xingliansdk.network

import android.Manifest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.xingliansdk.Config
import com.example.xingliansdk.R
import com.example.xingliansdk.XingLianApplication
import com.example.xingliansdk.network.api.login.LoginBean
import com.example.xingliansdk.utils.HelpUtil
import com.example.xingliansdk.utils.JumpUtil
import com.example.xingliansdk.utils.ShowToast
import com.google.gson.Gson
import com.orhanobut.hawk.Hawk
import com.qweather.sdk.view.HeContext.context
import com.shon.bluetooth.BLEManager
import com.shon.connector.utils.TLog
import kotlinx.coroutines.launch


fun <T> ViewModel.requestCustomWeight(
    block: suspend () -> BaseResult<T>,
    success: (T) -> Unit,
    error: (code: Int, message: String?) -> Unit
) {
    if(!HelpUtil.netWorkCheck(XingLianApplication.getXingLianApplication()))
    {
        TLog.error("无网络拦截")
        error.invoke(-2, "网络出问题了，快去检查一下吧～")
        return
    }
    viewModelScope.launch {
        kotlin.runCatching {
            block()
        }.onSuccess {
            TLog.error("it=="+ Gson().toJson(it))
            when (it.code) {
                200 -> {
                    success.invoke(it.data)
                }
                2001 -> {
                    ShowToast.showToastLong(context.getString(R.string.cood_2001))
                    Hawk.put(Config.database.USER_INFO,LoginBean())
                    BLEManager.getInstance().disconnectDevice(Hawk.get("address"))
                    BLEManager.getInstance().dataDispatcher.clearAll()
                    JumpUtil.startLoginActivity(XingLianApplication.getXingLianApplication())
                }
                0->
                {
                    TLog.error("网络异常")
                    ShowToast.showToastLong("网络异常")
                }
                else -> error.invoke(it.code,it.msg)
            }

        }.onFailure {
            TLog.error("异常数据")
            error.invoke(-1, it.message)
        }
    }

}