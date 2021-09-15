package com.example.xingliansdk.network.api.login

import android.os.Build
import android.util.Config
import com.example.xingliansdk.BuildConfig
import com.example.xingliansdk.base.AppApi
import com.example.xingliansdk.network.BaseResult
import com.example.xingliansdk.network.api.weather.bean.WeatherBean
import com.google.gson.Gson
import com.shon.connector.bean.DataBean
import com.shon.connector.utils.TLog
import com.shon.net.BaseApi
import okhttp3.MultipartBody
import retrofit2.http.Query

class LoginApi private constructor():AppApi<LoginInterface>(){
    companion object{
        val loginApi:LoginApi by lazy { LoginApi() }
    }

    suspend fun getLogin(value:HashMap<String,String> ): BaseResult<LoginBean> {
        val data= apiInterface?.login(value)
        return data!!
    }

    suspend fun getVerifyCode(phone:String,areaCode:String,password:String,type:String): BaseResult<LoginBean> {
        val data= apiInterface?.getVersionCode(phone,areaCode,password,type)
     //   TLog.error("data++"+data)
        return data!!
    }

    suspend fun setUserInfo(value:HashMap<String,String> ):BaseResult<LoginBean>{
        val data=apiInterface?.userUpdate(value)
        TLog.error("data++"+Gson().toJson(data))
        return data!!
    }
    suspend fun outLogin():BaseResult<Any>
    {
        val data=apiInterface?.outLogin()
        return data!!
    }
    suspend fun headImg(file: MultipartBody.Part):BaseResult<Any>
    {
        val data=apiInterface?.upLoadSingleFile(file)
        return  data!!
    }
    suspend fun register(phone: String,password: String,areaCode: String,verifyCode: String):BaseResult<LoginBean>
    {
        return  apiInterface?.register(phone,password,areaCode,verifyCode)!!
    }
    suspend fun updatePassword(value:HashMap<String,String>):BaseResult<Any>
    {
        return apiInterface?.updatePassword(value)!!
    }
    suspend fun updatePhone(value:HashMap<String,String>):BaseResult<Any>
    {
        return apiInterface?.updatePhone(value)!!
    }
    suspend fun saveAppeal(value:HashMap<String,String>):BaseResult<Any>
    {
        return apiInterface?.saveAppeal(value)!!
    }
    suspend fun userDelete(verifyCode: String):BaseResult<Any>
    {
        return  apiInterface?.userDelete(verifyCode)!!
    }
    suspend fun checkVerifyCode(value :HashMap<String,String>):BaseResult<Any>
    {
        return apiInterface?.checkVerifyCode(value)!!
    }
    suspend fun checkPassword(value :HashMap<String,String>):BaseResult<Any>
    {
        return apiInterface?.checkPassword(value)!!
    }
}