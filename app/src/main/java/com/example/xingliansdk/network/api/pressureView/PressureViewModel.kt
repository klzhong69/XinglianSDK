package com.example.xingliansdk.network.api.pressureView

import androidx.lifecycle.MutableLiveData
import com.example.xingliansdk.base.viewmodel.BaseViewModel
import com.example.xingliansdk.network.requestCustomBig

open class PressureViewModel : BaseViewModel() {
    val result: MutableLiveData<PressureVoBean> = MutableLiveData()
    val msg: MutableLiveData<String> = MutableLiveData()
    fun getPressure(startTime: String, endTime: String) {
        requestCustomBig({
            PressureApi.pressureApi.getPressure(startTime, endTime)
        }, {
            result.postValue(it)
        }, { code, message ->

            message?.let {
                msg.postValue(it)
            }
        })
    }
}