package com.moon.lib_safe

import android.app.ActivityManager
import android.content.Context
import android.os.Process
import android.util.Log


/**
 * @Des：
 * @author: moon
 * @date: 4/24/23
 */
class ProcessUtil {


    companion object {

        private fun getProcessName(cxt: Context): String? {
            val pid = Process.myPid()
            Log.v("statis" , "pid = $pid")
            val am: ActivityManager =
                cxt.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val runningApps: List<ActivityManager.RunningAppProcessInfo> = am.getRunningAppProcesses()
                ?: return null
            for (procInfo in runningApps) {
                if (procInfo.pid === pid) {
                    return procInfo.processName
                }
            }
            return null
        }

        fun isMainProcess(context: Context?): Boolean {
            try {
                if (null != context) {
                    return context.packageName == getProcessName(context)
                }
            } catch (e: Exception) {
                return false
            }
            return true
        }


    }
}