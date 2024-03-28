package io.github.toyota32k.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
object PackageUtil { fun getPackageInfo(context:Context):PackageInfo? {
        return try {
            val name = context.packageName
            val pm = context.packageManager
            return pm.getPackageInfo(name, PackageManager.GET_META_DATA)
        } catch (e: Throwable) {
            UtLog.libLogger.stackTrace(e)
            null
        }

    }

    fun getVersion(context: Context):String? {
        return try {
            // バージョン番号の文字列を返す
            getPackageInfo(context)?.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            UtLog.libLogger.stackTrace(e)
            null
        }
    }

//    fun appName(context: Context, @StringRes appNameId: Int ):String {
//        return context.resources.getString(appNameId)
//    }
}