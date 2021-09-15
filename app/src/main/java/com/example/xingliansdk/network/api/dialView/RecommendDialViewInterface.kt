package com.example.xingliansdk.network.api.dialView

import com.example.xingliansdk.network.BaseResult
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import java.util.HashMap

interface RecommendDialViewInterface {
    @FormUrlEncoded
    @POST("dial/find_dial")
    suspend fun findDialImg(
        @FieldMap value: HashMap<String, String>
    ): BaseResult<RecommendDialBean>
}