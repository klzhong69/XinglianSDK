package com.example.xingliansdk.ui.login

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.annotation.Nullable
import androidx.core.widget.addTextChangedListener
import com.example.phoneareacodelibrary.AreaCodeModel
import com.example.phoneareacodelibrary.PhoneAreaCodeActivity
import com.example.phoneareacodelibrary.SelectPhoneCode
import com.example.xingliansdk.Config
import com.example.xingliansdk.R
import com.example.xingliansdk.XingLianApplication
import com.example.xingliansdk.base.BaseActivity
import com.example.xingliansdk.blecontent.BleConnection
import com.example.xingliansdk.service.AppService
import com.example.xingliansdk.ui.login.viewMode.LoginViewModel
import com.example.xingliansdk.utils.*
import com.google.gson.Gson
import com.orhanobut.hawk.Hawk
import com.shon.bluetooth.BLEManager
import com.shon.connector.utils.TLog
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : BaseActivity<LoginViewModel>(), View.OnClickListener {

    private var countDownTimer: MyCountDownTimer? = null
    override fun layoutId() = R.layout.activity_login
    var areaCode = "86"
    var imgPasswordStatus = false
    override fun initView(savedInstanceState: Bundle?) {
        tv_login.setOnClickListener(this)
        tvPhoneCode.setOnClickListener(this)
        tv_getcode.setOnClickListener(this)
        tvPassword.setOnClickListener(this)
        tvForgotPassword.setOnClickListener(this)
        imgPassword.setOnClickListener(this)
        tv_user_service_protocol.setOnClickListener(this)
        tv_privacy_policy.setOnClickListener(this)
        checkbox.setOnClickListener {
            checkbox.isSelected = !checkbox.isSelected
        }
        Permissions()
        //此处直接停止 服务 进去以后再开启
        stopService(Intent(this, AppService::class.java))
        countDownTimer = MyCountDownTimer(60000, 1000)
        if (userInfo.user != null) {
            edt_mobile.setText(userInfo.user.phone)
        }
        edt_mobile.addTextChangedListener {
            setSureBtnColor()
        }
        edt_code.addTextChangedListener {
            setSureBtnColor()
        }
        edtPassword.addTextChangedListener {
            setSureBtnColor()
        }
    }

    private fun setSureBtnColor() {

        if (edt_mobile.text?.trim()?.length!! >= 1 &&
            (edt_code.text!!.trim().length >= 4 && edt_code.visibility == View.VISIBLE)
            || (edtPassword.text!!.trim().length >= 3 && edtPassword.visibility == View.VISIBLE)
        ) {
            tv_login.setTextColor(resources.getColor(R.color.white))
            tv_login.setBackgroundResource(R.drawable.bg_login_password)
        } else {
            tv_login.setTextColor(resources.getColor(R.color.color_login_code))
            tv_login.setBackgroundResource(R.drawable.bg_login_password_gray)
        }

    }

    override fun createObserver() {
        super.createObserver()
        mViewModel.result.observe(this) {
            TLog.error("本地的 userInfo++" + Gson().toJson(userInfo))
            TLog.error("网络的++" + Gson().toJson(it))
            if(userInfo == null || userInfo.user == null)
            {
                BleConnection.Unbind=true
                BleConnection.iFonConnectError=true
                RoomUtils.roomDeleteAll()
                BLEManager.getInstance().dataDispatcher.clearAll()
                RoomUtils.roomDeleteAll()
                Hawk.put("name", "")
                Hawk.put("address","")
                TLog.error("if ==isNullOrEmpty")
            }
           else if ( userInfo.user.phone != it.user.phone) {
                TLog.error("进入")
                RoomUtils.roomDeleteAll()
                BLEManager.getInstance().dataDispatcher.clearAll()
                RoomUtils.roomDeleteAll()
                if (it.user.mac.isNullOrEmpty()) {
                    BleConnection.Unbind=true
                    Hawk.put("address", "")
                    Hawk.put("name", "")
                    TLog.error("else if ==isNullOrEmpty")
                }
                else{
                    TLog.error("else if ==NotEmpty")
                    BleConnection.iFonConnectError = true
                    Hawk.put("address", "" + it.user.mac)
                    Hawk.put("name","StarLink GT1")
                }

            }
            else {
                    if (it.user.mac.isNullOrEmpty()) {
                        TLog.error("else ==isNullOrEmpty")
                        BleConnection.Unbind=true
                        Hawk.put("address", "")
                        Hawk.put("name", "")
                    }
                else{
                        TLog.error("else ==isnotEmpty")
                        BleConnection.iFonConnectError = true
                        Hawk.put("address", "" + it.user.mac)
                        Hawk.put("name","StarLink GT1")
                }
            }
            Hawk.put(Config.database.USER_INFO, it)
            hideWaitDialog()
            TLog.error("hai====" + Gson().toJson(it))
            when {
                it.register -> {
                    JumpUtil.startPasswordActivity(
                        this,
                        edt_mobile.text.toString(),
                        edt_code.text.toString(),
                        areaCode,
                        1
                    )
                    //  JumpUtil.startDeviceInformationActivity(this,true)
                }
                it.user.sex.equals("0") -> {
                    JumpUtil.startDeviceInformationActivity(this, true)
                }
                else -> {
                    // TLog.error("=="+Hawk.get<LoginBean>(Config.database.USER_INFO).token)
                    JumpUtil.startMainHomeActivity(this)
                    finish()
                }
            }
        }
        mViewModel.msg.observe(this) {
            hideWaitDialog()
            // ShowToast.showToastLong("登录失败")
        }
        mViewModel.areaCodeResult.observe(this)
        {
            hideWaitDialog()
            TLog.error("验证码++" + Gson().toJson(it))
        }

    }

    var password = ""
    var md5Password = ""
    override fun onClick(v: View) {
        when (v.id) {
            R.id.tv_login -> {
                HelpUtil.hideSoftInputView(this)
                TLog.error("111111==" + edt_mobile.text.toString())
                var value = HashMap<String, String>()
                value["phone"] = edt_mobile.text.toString()
                value["areaCode"] = areaCode
                if (edt_code.visibility == View.VISIBLE)
                    value["verifyCode"] = edt_code.text.toString()
                if (edtPassword.visibility == View.VISIBLE)
                    value["password"] = edtPassword.text.toString()
                if (edt_mobile.text.toString().isNullOrEmpty()) {
                    ShowToast.showToastLong("请输入手机号")
                    return
                } else if (edtPassword.text.toString().isNullOrEmpty() &&
                    tvPassword.text.toString() == "免密登录"
                ) {
                    ShowToast.showToastLong("请输入密码")
                    return
                } else if (edt_code.text.toString().isNullOrEmpty() &&
                    tvPassword.text.toString() == "密码登录"
                ) {
                    ShowToast.showToastLong("请输入验证码")
                    return
                }
                if (!checkbox.isSelected) {
                    ShowToast.showToastLong("请勾选用户级隐私协议")
                    return
                }
                HelpUtil.hideSoftInputView(this)
                showWaitDialog("登陆中...")
                mViewModel.loginRegistered(value)
            }
            R.id.tvPhoneCode -> {
                SelectPhoneCode.with(this)
                    .setTitle("区号选择")
                    .setStickHeaderColor("#41B1FD")//粘性头部背景颜色
                    .setTitleBgColor("#ffffff")//界面头部标题背景颜色
                    .setTitleTextColor("#454545")//标题文字颜色
                    .select()
            }
            R.id.tv_getcode -> {
                TLog.error("+++" + edt_mobile.text.toString())
                TLog.error("+++$areaCode")
                if (edt_mobile.text.toString().isNullOrEmpty()) {
                    ShowToast.showToastLong("请输入手机号")
                    return
                }
                countDownTimer?.start()
                edt_code.isFocusable = true
                edt_code.isFocusableInTouchMode = true
                edt_code.requestFocus()
                password = edt_mobile.text.toString() + areaCode + 10861
                md5Password = MD5Util.md5(password)
                TLog.error("md5Password+=" + md5Password)
                tv_getcode.setTextColor(resources.getColor(R.color.color_login_code))
                tv_getcode.setBackgroundResource(R.drawable.login_code_btn_false)
                mViewModel.getVerifyCode(edt_mobile.text.toString(), areaCode, md5Password)
            }
            R.id.tvPassword -> {
                if (tvPassword.text.toString() == "密码登录") {
                    tvTitle.text = "密码登录"
                    tvPassword.text = "免密登录"
                    edtPassword.visibility = View.VISIBLE
                    imgPassword.visibility = View.VISIBLE
                    tv_getcode.visibility = View.INVISIBLE
                    edt_code.visibility = View.INVISIBLE
                    tvForgotPassword.visibility = View.VISIBLE
                    tv_regist.visibility = View.INVISIBLE
                } else {
                    tvTitle.text = "手机号登录/注册"
                    tvPassword.text = "密码登录"
                    edtPassword.visibility = View.INVISIBLE
                    imgPassword.visibility = View.INVISIBLE
                    tv_getcode.visibility = View.VISIBLE
                    edt_code.visibility = View.VISIBLE
                    tvForgotPassword.visibility = View.INVISIBLE
                    tv_regist.visibility = View.VISIBLE
                }
            }
            R.id.tvForgotPassword -> {
                JumpUtil.startForgetPasswordActivity(this)
            }
            R.id.imgPassword -> {
                if (imgPasswordStatus) {
                    edtPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                    imgPassword.setImageResource(R.mipmap.icon_non)
                } else {
                    edtPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                    imgPassword.setImageResource(R.mipmap.icon_see)
                }
                imgPasswordStatus = !imgPasswordStatus
            }
            R.id.tv_user_service_protocol -> {
                TLog.error("点击")
                JumpUtil.startWeb(this, XingLianApplication.baseUrl + "/agreement/user")
            }
            R.id.tv_privacy_policy -> {
                JumpUtil.startWeb(this, XingLianApplication.baseUrl + "/agreement/privacy")
            }

        }

    }


    inner class MyCountDownTimer(
        millisInFuture: Long,
        countDownInterval: Long
    ) : CountDownTimer(millisInFuture, countDownInterval) {
        //计时过程
        override fun onTick(l: Long) { //防止计时过程中重复点击
            tv_getcode.isClickable = false
            tv_getcode.text = (l / 1000).toString() + "秒"
        }

        //计时完毕的方法
        override fun onFinish() { //重新给Button设置文字
            tv_getcode.text = "重新获取"
            //设置可点击
            tv_getcode.setTextColor(resources.getColor(R.color.color_main_green))
            tv_getcode.setBackgroundResource(R.drawable.login_code_btn)
            tv_getcode.isClickable = true
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == PhoneAreaCodeActivity.resultCode) {
            if (data != null) {
                val model: AreaCodeModel =
                    data.getSerializableExtra(PhoneAreaCodeActivity.DATAKEY) as AreaCodeModel
                tvPhoneCode.text = "+" + model.tel
                areaCode = model.tel
            }
        }
    }

}