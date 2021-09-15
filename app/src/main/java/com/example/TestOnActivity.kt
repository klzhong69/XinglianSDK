package com.example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.xingliansdk.Config
import com.example.xingliansdk.R
import com.example.xingliansdk.network.api.login.LoginBean
import com.google.gson.Gson
import com.orhanobut.hawk.Hawk

import kotlinx.android.synthetic.main.activity_test_on.*

class TestOnActivity : AppCompatActivity() {

    private  val tags  = "TestOnActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_on)

        getUserInfoBtn.setOnClickListener{
            var userInfo = Hawk.get<LoginBean>(Config.database.USER_INFO)

            //var u_id = userInfo.user.userId

            Log.e(tags, "---------userId="+Gson().toJson(userInfo))
        }
    }



}