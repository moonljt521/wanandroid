package com.sum.tea

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDex
import com.moon.lib_safe.ProcessUtil
import com.moon.lib_safe.StabilityOptimize
import com.moon.lib_safe.statistics.StartDuringHelper
import com.sum.framework.jank.JankMonitor
import com.sum.framework.log.LogUtil
import com.sum.framework.manager.ActivityManager
import com.sum.framework.manager.AppFrontBack
import com.sum.framework.manager.AppFrontBackListener
import com.sum.framework.toast.TipsToast
import com.sum.stater.dispatcher.TaskDispatcher
import com.sum.tea.task.*

/**
 * @author mingyan.su
 * @date   2023/2/9 23:19
 * @desc   应用类
 */
class SumApplication : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        if(ProcessUtil.isMainProcess(this)){
            StartDuringHelper.logStart()
        }
        MultiDex.install(base)
    }

    override fun getApplicationContext(): Context {
        return super.getApplicationContext()
    }

    override fun onCreate() {
        super.onCreate()

        if (!ProcessUtil.isMainProcess(this)){
            return
        }

        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver())

        // todo jank 帧率监测 + 打印堆栈
        JankMonitor.init(this , dumpJankStacktrace = BuildConfig.DEBUG)

        //注册APP前后台切换监听
        appFrontBackRegister()
        // App启动立即注册监听
        registerActivityLifecycle()
        TipsToast.init(this)

        //1.启动器：TaskDispatcher初始化
        TaskDispatcher.init(this)
        //2.创建dispatcher实例
        val dispatcher: TaskDispatcher = TaskDispatcher.createInstance()

        //3.添加任务并且启动任务
        dispatcher.addTask(InitSumHelperTask(this))
                .addTask(InitMmkvTask())
                .addTask(InitAppManagerTask())
                .addTask(InitRefreshLayoutTask())
                .addTask(InitArouterTask())
                .start()

        //等待，需要等待的方法执行完才可以往下执行
        dispatcher.await()

        StabilityOptimize.setUpJavaAirBag(this , configList = listOf())

        StartDuringHelper.logColdStart()
    }

    /**
     * 注册APP前后台切换监听
     */
    private fun appFrontBackRegister() {
        AppFrontBack.register(this, object : AppFrontBackListener {
            override fun onBack(activity: Activity?) {
                LogUtil.d("onBack")
            }

            override fun onFront(activity: Activity?) {
                LogUtil.d("onFront")
            }
        })
    }

    /**
     * 注册Activity生命周期监听
     */
    private fun registerActivityLifecycle() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityPaused(activity: Activity) {
            }

            override fun onActivityStarted(activity: Activity) {
            }

            override fun onActivityDestroyed(activity: Activity) {
                ActivityManager.pop(activity)
            }

            override fun onActivitySaveInstanceState(activity: Activity, p1: Bundle) {
            }

            override fun onActivityStopped(activity: Activity) {
            }

            override fun onActivityCreated(activity: Activity, p1: Bundle?) {
                ActivityManager.push(activity)
            }

            override fun onActivityResumed(activity: Activity) {
            }
        })
    }
}

class AppLifecycleObserver : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackground(){
        Log.v("application","====AppLifecycleObserver=====onAppBackgound==")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForeground(){
        Log.v("application","====AppLifecycleObserver=====onAppForeground==")
    }
}

