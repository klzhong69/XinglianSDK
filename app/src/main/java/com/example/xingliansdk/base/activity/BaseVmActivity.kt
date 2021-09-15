package com.example.xingliansdk.base.activity

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.xingliansdk.Config
import com.example.xingliansdk.R
import com.example.xingliansdk.XingLianApplication
import com.example.xingliansdk.base.viewmodel.BaseViewModel
import com.example.xingliansdk.bean.DeviceFirmwareBean
import com.example.xingliansdk.bean.HomeCardBean
import com.example.xingliansdk.ext.getAppViewModel
import com.example.xingliansdk.ext.getVmClazz
import com.example.xingliansdk.network.api.login.LoginBean
import com.example.xingliansdk.network.manager.NetState
import com.example.xingliansdk.network.manager.NetworkStateManager
import com.example.xingliansdk.utils.HelpUtil
import com.example.xingliansdk.utils.ShowToast
import com.example.xingliansdk.view.DateUtil
import com.example.xingliansdk.viewmodel.MainViewModel
import com.example.xingliansdk.widget.LoadingDialogUtils
import com.example.xingliansdk.widget.LoginDialog
import com.gyf.barlibrary.ImmersionBar
import com.orhanobut.hawk.Hawk
import com.sembozdemir.permissionskt.askPermissions
import com.shon.connector.bean.DeviceInformationBean
import com.shon.connector.utils.TLog
import java.util.*

/**
 * 描述　: ViewModelActivity基类，把ViewModel注入进来了
 */
abstract class BaseVmActivity<VM : BaseViewModel> : AppCompatActivity() {

    /**
     * 是否需要使用DataBinding 供子类BaseVmDbActivity修改，用户请慎动
     */
    private var isUserDb = false

    /**
     * 数据存储个人数据
     */
    var mDeviceInformationBean: DeviceInformationBean = DeviceInformationBean()

    //固件信息
    var mDeviceFirmwareBean = DeviceFirmwareBean()

    /**
     * 后台返回的个人信息数据
     */
    var userInfo = LoginBean()

    /**
     * 首页展示效果存储
     */
    var mHomeCardBean: HomeCardBean = HomeCardBean()
    lateinit var mViewModel: VM
    lateinit var baseDialog: Dialog

    //Application全局的ViewModel，用于发送全局通知操作
    val mainViewModel: MainViewModel by lazy { getAppViewModel<MainViewModel>() }
//    protected var tfLight: Typeface? = null

    abstract fun layoutId(): Int

    abstract fun initView(savedInstanceState: Bundle?)

    abstract fun showLoading(message: String = "请求网络中...")

    abstract fun dismissLoading()

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        //初始化沉浸式
        if (isImmersionBarEnabled()) {
            initImmersionBar()
        }
        HelpUtil.hideSoftInputView(this)
        XingLianApplication.setSelectedCalendar(DateUtil.getCurrentCalendar())//更新为最新时间
//        TLog.error("走了==XingLianApplication")
        var year = Calendar.getInstance()
        year.roll(Calendar.YEAR, -18)
        year.timeInMillis
        var birth: Long = year.timeInMillis
        userInfo = Hawk.get(Config.database.USER_INFO, LoginBean())
        mDeviceInformationBean = Hawk.get(
            Config.database.PERSONAL_INFORMATION,
            DeviceInformationBean(2, 0, 160, 50, 0, 0, 0, 0, 0, 0, 10000, birth)
        )
//        TLog.error("走了==mDeviceInformationBean")
        mDeviceFirmwareBean = Hawk.get("DeviceFirmwareBean", DeviceFirmwareBean())
        mHomeCardBean = Hawk.get(Config.database.HOME_CARD_BEAN, HomeCardBean())
//        tfLight = Typeface.createFromAsset(assets, "OpenSans-Light.ttf")
//        ImmersionBar.with(this).init()
//        TLog.error("走了==")
        if (!isUserDb) {
            setContentView(layoutId())
        } else {
            initDataBind()
        }
        init(savedInstanceState)
    }

    private fun init(savedInstanceState: Bundle?) {
        mViewModel = createViewModel()
        registerUiChange()
        initView(savedInstanceState)
        createObserver()
//        TLog.error("=====来了网络监听")
        NetworkStateManager.instance.mNetworkStateCallback.observe(this, Observer {
            TLog.error("来了网络监听")
            onNetworkStateChanged(it)
        })
    }

    /**
     * 网络变化监听 子类重写
     */
    open fun onNetworkStateChanged(netState: NetState) {}

    /**
     * 创建viewModel
     */
    private fun createViewModel(): VM {
        return ViewModelProvider(this).get(getVmClazz(this))
    }

    /**
     * 创建LiveData数据观察者
     */
    abstract fun createObserver()

    /**
     * 注册UI 事件
     */
    private fun registerUiChange() {
        //显示弹窗
        mViewModel.loadingChange.showDialog.observe(this, Observer {
            showLoading(it)
        })
        //关闭弹窗
        mViewModel.loadingChange.dismissDialog.observe(this, Observer {
            dismissLoading()
        })
    }

    /**
     * 将非该Activity绑定的ViewModel添加 loading回调 防止出现请求时不显示 loading 弹窗bug
     * @param viewModels Array<out BaseViewModel>
     */
    protected fun addLoadingObserve(vararg viewModels: BaseViewModel) {
        viewModels.forEach { viewModel ->
            //显示弹窗
            viewModel.loadingChange.showDialog.observe(this, Observer {
                showLoading(it)
            })
            //关闭弹窗
            viewModel.loadingChange.dismissDialog.observe(this, Observer {
                dismissLoading()
            })
        }
    }

    fun userDataBinding(isUserDb: Boolean) {
        this.isUserDb = isUserDb
    }

    /**
     * 供子类BaseVmDbActivity 初始化Databinding操作
     */
    open fun initDataBind() {}
    fun Permissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            askPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.MEDIA_CONTENT_CONTROL,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.BLUETOOTH_PRIVILEGED,
                Manifest.permission.SEND_SMS, //短信权限
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.BROADCAST_SMS,
                Manifest.permission.GET_ACCOUNTS,
                Manifest.permission.READ_CONTACTS,//获取通讯录权限
                Manifest.permission.ANSWER_PHONE_CALLS, //9.0来电号码和挂电话需要正式申请权限
                Manifest.permission.READ_PHONE_STATE, //读取手机状态
                Manifest.permission.READ_CALL_LOG,  //通讯记录
                Manifest.permission.ACCESS_FINE_LOCATION, //定位权限
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            {
                onGranted { }
                onDenied { permissions ->
                    TLog.error("拒绝")
                    permissions.forEach {
                        when (it) {

                            Manifest.permission.ACCESS_FINE_LOCATION -> ShowToast.showToastLong("定位权限未开启,请打开设置开启权限")
                        }
                    }
                }
                onShowRationale { _ ->
                    TLog.error("拒绝onShowRationale")
                    showPermissionSettingsDialog()
                }
                onNeverAskAgain { permissions ->
                    permissions.forEach {
                        when (it) {

                            Manifest.permission.CAMERA ->{
                                ShowToast.showToastLong("GPS功能,已被禁止,请打开设置开启权限")
                                TLog.error("11111")
                            }//jump2PermissionSettings()
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION-> {ShowToast.showToastLong("GPS功能,已被禁止,请打开设置开启权限")
                                TLog.error("22222")
                            //jump2PermissionSettings()
                             }

                        }
                    }
                }
            }
        }
    }

    /**
     *
     */
    open fun showPermissionSettingsDialog() {
        val builder = LoginDialog.Builder(this)
        builder.setMessage(R.string.lack_of_necessary_permissions)
        builder.setGravity(LoginDialog.Builder.MESSAGE_CENTER_GRAVITY)
        builder.setNegativeButton(R.string.text_cancel)
        builder.setPositiveButton(R.string.go_to_set)
        builder.setOnDialogClickListener(object : LoginDialog.OnDialogClickListener {
            override fun onConfirmClick(dialog: Dialog?, which: Int) {
                dialog?.dismiss()
                jump2PermissionSettings()
            }

            override fun onCancelClick(dialog: Dialog?, which: Int) {
                dialog?.dismiss()
            }

        })
        val mDialog = builder.create()
        mDialog.setCancelable(false)
        mDialog.show()
    }

    /**
     * 跳转到应用程序信息详情页面
     */
    open fun jump2PermissionSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    }

    open fun getFooterView(
        type: Int,
        mRecyclerView: RecyclerView
    ): View? {
        return layoutInflater.inflate(R.layout.no_more, mRecyclerView, false)
    }

    protected fun showWaitDialog(msg: String) {
//        if (dialog.isShowing)
//            return
        baseDialog = LoadingDialogUtils.createLoadingDialog(this, msg)
    }

    protected fun showWaitDialog() {
//        if (dialog.isShowing)
//            return
        baseDialog = LoadingDialogUtils.createLoadingDialog(this, "扫描蓝牙中...")
    }

    protected fun hideWaitDialog() {
        if (::baseDialog.isInitialized) {
            LoadingDialogUtils.closeDialog(baseDialog)
        }


    }

    /**
     * 是否可以使用沉浸式
     * Is immersion bar enabled boolean.
     *
     * @return the boolean
     */
    private fun isImmersionBarEnabled(): Boolean {
        return true
    }

    protected open fun initImmersionBar() {
        //在BaseActivity里初始化
        ImmersionBar.with(this).navigationBarColor(R.color.white)
            .statusBarDarkFont(true, 0.3f)
            .fitsSystemWindows(false)
            .init()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isImmersionBarEnabled()) {
            ImmersionBar.with(this).destroy()
        }
    }

    /**
     * 界面已销毁
     * @return
     */
    open fun isFinished(): Boolean {
        return isDestroyed || isFinishing
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            if (checkDoubleClick()) {
                return true
            }
        }
        return super.dispatchTouchEvent(ev)
    }


    /** 判断是否是快速点击  */
    private var lastClickTime: Long = 0
    fun checkDoubleClick(): Boolean {
        //点击时间
        val clickTime = SystemClock.uptimeMillis()
        //如果当前点击间隔小于500毫秒
        if (lastClickTime >= clickTime - 100) {
            return true
        }
        //记录上次点击时间
        lastClickTime = clickTime
        return false
    }
}