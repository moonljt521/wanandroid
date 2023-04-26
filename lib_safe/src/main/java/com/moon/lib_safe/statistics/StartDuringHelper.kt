package com.moon.lib_safe.statistics

import com.sum.safe.BuildConfig
import timber.log.Timber

/**
 *
 * 【启动耗时的统计和问题】
 * 1  较为准确的方案，在 application#attachBaseContext 方法中开始计时，然后在第一个（MainActivity）Activity方法
 *   第一帧绘制结束的方法（onWindowFocusChanged）里结束计时， 不过有个小问题，如果是过渡页（splash）有未执行这个方法，页面就被finish的
 *   风险 ，所以视情况而定，是在splash还是Main
 *
 * 2 1里的方案虽然较为准确，但是有误报漏报的风险：
 *          * 应用接收到广播，拉起了主进程，之后再点击桌面图标进入
            * 主进程通过ContentProvider被拉起，之后再点击桌面图标进入
            * 通过点击push进到了一个落地页，按返回键退出后，再点击桌面图标进入
 *
 * 3 冷热启动的区别就是是否有application的创建 ，如果甄别呢，在application#onCreate结束时记一个t1， 在Launch#Activity#onCreate里记t2，
 *     如果t2- t1 < 200ms 则为冷启动
 *
 *
 * 4 存在的问题：
 *      假如使用app_start_up这类并行库 ，某个启动任务的耗时有112ms，如果通过打日志的方式，我们得到的结论就是这个任务耗时过长
 *      ，但是从systrace上，我们看到CPU Duration只有18ms，真正占用了很多时间的，是多次锁的竞用。
        因此这个任务优化的重点应该是解决锁竞用的问题，如果用打日志的方式，只能看到表面现象，很容易把优化方向带偏了。
        如果是过度并行，导致很多任务在Runnable的状态等待CPU时间片，这种情况通过日志也会得出错误的信息，线下分析还是建议用systrace（新版本改用
        as 的profiler 工具）。
 *
 * @Des：
 * @author: moon
 * @date: 4/24/23
 */
object StartDuringHelper {

    init {
        if (BuildConfig.DEBUG){
            Timber.plant(Timber.DebugTree())
        }
    }

    var t : Long = 0

    var coldBootStart: Long = 0

    fun logStart(){
        t = System.currentTimeMillis()
    }

    fun logEnd() {
        if (t <= 0) return

        val end = System.currentTimeMillis()
        val during = end-t
        val isValidate = during / (1000 * 60) < 5
        Timber.i("冷启动耗时： ${during} , 是否有效 ${isValidate}")
        t = 0
    }


    fun logColdStart() {
        coldBootStart = System.currentTimeMillis()
    }

    fun logColdEnd() {
        val t = System.currentTimeMillis()
        val isCold = (t - coldBootStart) < 200
        Timber.i("是否是冷启动： $isCold")
    }

    /**
     * 在release 模式中启动 systrace
     */
    fun enableReleaseTrace() {
        try {
            Class.forName("android.os.Trace").getDeclaredMethod("setAppTracingAllowed", Boolean::class.java).invoke(null, true)
        } catch (tr: Throwable) {
            Timber.i("${tr.message}")
        }
    }


}