package com.example.xingliansdk.ui.heartrate.viewmodel

import androidx.lifecycle.MutableLiveData
import com.example.xingliansdk.base.viewmodel.BaseViewModel
import com.example.xingliansdk.network.api.heartView.HeartRateApi
import com.example.xingliansdk.network.requestCustomBig


open class HeartRateViewModel : BaseViewModel() {
    val result: MutableLiveData<Any> = MutableLiveData()
    val msg: MutableLiveData<String> = MutableLiveData()
    fun getHeartRate(startTime: String, endTime: String) {
        requestCustomBig({
            HeartRateApi.heartRateApi.getHeartRate(startTime, endTime)
        }, {
            result.postValue(it)
        }, { code, message ->

            message?.let {
                msg.postValue(it)
            }
        })
    }
}