package com.example.xingliansdk

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
interface HttpService {

    @GET("https://gitee.com/frankm/oatzip/blob/master/NFR52_OTA.zip")
      fun otaFile(): Call<ResponseBody>
}