package com.example.xingliansdk

import android.R
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.multidex.MultiDex
import com.example.xingliansdk.base.BaseApp
import com.example.xingliansdk.bean.room.AppDataBase
import com.example.xingliansdk.blecontent.BleConnection
import com.example.xingliansdk.livedata.LiveDataBus
import com.example.xingliansdk.service.AppService
import com.example.xingliansdk.utils.AppActivityManager
import com.example.xingliansdk.utils.DynamicTimeFormat
import com.example.xingliansdk.utils.HelpUtil
import com.example.xingliansdk.utils.ShowToast
import com.example.xingliansdk.view.DateUtil
import com.orhanobut.hawk.Hawk
import com.orhanobut.hawk.NoEncryption
import com.qweather.sdk.bean.weather.WeatherDailyBean
import com.qweather.sdk.view.HeConfig
import com.qweather.sdk.view.QWeather
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.shon.bluetooth.BLEManager
import com.shon.bluetooth.LiveDataUtils
import com.shon.connector.BleWrite
import com.shon.connector.utils.TLog
import com.tencent.bugly.crashreport.CrashReport
import me.jessyan.autosize.AutoSizeConfig
import me.jessyan.autosize.unit.Subunits
import org.litepal.LitePal

import java.lang.ref.WeakReference
import java.util.*

class XingLianApplication : BaseApp() {
    companion object {
        lateinit var mXingLianApplication: XingLianApplication
        private var mSelectedCalendar: Calendar? = null
        const val serviceUUIDxiaoxie = "1f400001-aab4-14a3-f1ba-f61f35cddbaa"
        const val testuuid = "00001800-0000-1000-8000-00805f9b34fb"

        //pro板子的
        const val serviceUUIDXINLU = "0000180d-0000-1000-8000-00805f9b34fb"
        const val readCharacterXINLIN = "00002a37-0000-1000-8000-00805f9b34fb"
        const val mRedXINLIN = "00002a38-0000-1000-8000-00805f9b34fb"

        //手表的
        const val serviceUUID1 = "8F400001-CFB4-14A3-F1BA-F61F35CDDBAF"
        const val mWriteCharactertest = "8F400002-CFB4-14A3-F1BA-F61F35CDDBAF"
        const val readCharactertest = "8F400003-CFB4-14A3-F1BA-F61F35CDDBAF"

        /**
         * SDK正式板子服务特征
         */
        const val serviceUUID = "1F40EAF8-AAB4-14A3-F1BA-F61F35CDDBAA"
        const val mWriteCharacter = "1F400001-AAB4-14A3-F1BA-F61F35CDDBAA"
        const val readCharacter = "1F400002-AAB4-14A3-F1BA-F61F35CDDBAA"
        const val WriteCharacterBig = "1F400003-AAB4-14A3-F1BA-F61F35CDDBAA"
        const val readCharacterBig = "1F400004-AAB4-14A3-F1BA-F61F35CDDBAA"

        //5.0板子
        const val serviceUUID5 = "6E400001-B5A3-F393-E0A9-E50E24DCCA9E"
        const val writeCharacter5 = "6E400002-B5A3-F393-E0A9-E50E24DCCA9E"
        const val readCharacter5 = "6E400003-B5A3-F393-E0A9-E50E24DCCA9E"
        const val appId = "c6e1f7ad2c"
        const val appKey = "b3f98779-a899-466b-b2bd-c3e2690c406e"
        var baseUrl = BuildConfig.baseUrl
        const val TIME_START = 946656000
        private var context: WeakReference<Context>? = null
        var ifStartedOrStopped = true
        fun getSelectedCalendar(): Calendar? {
            return mSelectedCalendar
        }

        fun setSelectedCalendar(newCalendar: Calendar?) {
            mSelectedCalendar = newCalendar
        }

        fun getXingLianApplication(): XingLianApplication {
            return mXingLianApplication
        }

        lateinit var sin: AppDataBase

        init {
            SmartRefreshLayout.setDefaultRefreshInitializer { context, layout ->
                layout.setEnableAutoLoadMore(true)//滑动到底部触发加载更多
                layout.setEnableOverScrollDrag(false)//苹果越界启用
                layout.setEnableOverScrollBounce(true)
                //  layout.setEnableHeaderTranslationContent(false)//头部head内容 如果不加header 可以使用false 来使用原生的
                layout.setEnableLoadMoreWhenContentNotFull(false)  //不在一个页面时不加载 more
                layout.setEnableScrollContentWhenRefreshed(true)
//                layout.setDisableContentWhenLoading(true)
//                layout.setDisableContentWhenRefresh(true)
//                layout.setDisableContentWhenLoading(true)
                layout.setPrimaryColorsId(R.color.transparent, R.color.darker_gray)
                layout.layout.tag = "close egg"
            }
            SmartRefreshLayout.setDefaultRefreshHeaderCreator { context: Context?, layout: RefreshLayout? ->//刷新样式 这里 仿照 ios 模式
                ClassicsHeader(
                    context
                ).setTimeFormat(DynamicTimeFormat("更新于 %s"))
            }
        }


    }

    override fun onCreate() {
        super.onCreate()
        mXingLianApplication = this
        context = WeakReference(applicationContext)
        initAppData();

    }


    private fun initAppData(){
        BLEManager.init(this)
        MultiDex.install(this)
        AutoSizeConfig.getInstance().unitsManager.setSupportDP(true)
            .setSupportSP(true).supportSubunits = Subunits.PT
        ShowToast.init(this)
        mSelectedCalendar = DateUtil.getCurrentCalendar()
        //数据库
        Hawk.init(applicationContext)
            .setEncryption(NoEncryption())
            .build()
        registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
        //bugly错误信息
        CrashReport.initCrashReport(applicationContext, "d242b786b4", true)
       // HeConfig.init("HE2105082053391380", "9f28301da43746ac8bf3f223d0930b4f")

       // HeConfig.switchToDevService()
        AppActivityManager.getInstance().init(this)

        baseUrl = if (HelpUtil.isApkInDebug(mXingLianApplication)) {
            TLog.error("测试版?")
            BuildConfig.baseUrlDev
//                 BuildConfig.baseUrl
        } else {
//            BuildConfig.baseUrlDev
            BuildConfig.baseUrl
        }

        //litePal数据库
        LitePal.initialize(this)
    }


    //弱引用
    fun getContext(): Context? {
        return context!!.get()
    }

    private var mCount = 0
    private var lastWriteTime: Long = 0
    fun uploadModeCall() {
//        if (!BleConnection.iFonConnectError) {
//            TLog.error("mCount+=$mCount")
        //点击时间
        val clickTime = System.currentTimeMillis()
        if(lastWriteTime<clickTime-10000) {
            lastWriteTime=clickTime
            var type = if (mCount > 0)
                1 else 0
          //  BleWrite.writeSportsUploadModeCall(type)
        }
//        }
    }

    private val activityLifecycleCallbacks: ActivityLifecycleCallbacks =
        object : ActivityLifecycleCallbacks {
            override fun onActivityPaused(activity: Activity) {
            }


            override fun onActivityDestroyed(activity: Activity) {
//                    TLog.error("onActivityDestroyed+=$activity")
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            }

            override fun onActivityStarted(activity: Activity) {
//                TLog.error("onActivityStarted+=$activity")
                mCount++
                uploadModeCall()
            }

            override fun onActivityStopped(activity: Activity) {
//                TLog.error("onActivityStopped+=$activity")
                mCount--
                uploadModeCall()
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            }

            override fun onActivityResumed(activity: Activity) {
            }
        }

    override fun onTerminate() {
        super.onTerminate()
        TLog.error("终止时")
        val intent = Intent(getContext(), AppService::class.java)
        stopService(intent)
        unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks)
    }
}