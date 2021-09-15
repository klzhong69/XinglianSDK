package com.example.xingliansdk.ui.sleep.viewmodel
import androidx.lifecycle.MutableLiveData
import com.example.xingliansdk.bean.NetSleepBean
import com.example.xingliansdk.base.viewmodel.BaseViewModel
import com.example.xingliansdk.network.api.sleepView.SleepApi
import com.example.xingliansdk.network.requestCustomBig


class SleepViewModel : BaseViewModel() {
    val msg: MutableLiveData<String> = MutableLiveData()
    val result: MutableLiveData<NetSleepBean> = MutableLiveData()
    fun getSleep(date: String) {
        requestCustomBig({
            SleepApi.sleepApi.getSleep(date)
        }, {
            result.postValue(it)
        }, { code, message ->

            message?.let {
                msg.postValue(it)
            }
        })
    }
}