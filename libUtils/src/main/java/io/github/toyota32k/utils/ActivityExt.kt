@file:Suppress("unused")

package io.github.toyota32k.utils

import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity

fun FragmentActivity.isStatusBarVisible(): Boolean {
    val controller = WindowCompat.getInsetsController(window, window.decorView)
    return controller.systemBarsBehavior != WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
}

fun FragmentActivity.fitSystemWindows(fit:Boolean) {
    WindowCompat.setDecorFitsSystemWindows(window, fit)
}

fun FragmentActivity.hideStatusBar() {
//    WindowCompat.setDecorFitsSystemWindows(window, false)
    WindowInsetsControllerCompat(window, window.decorView.rootView).let { controller ->
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}

fun FragmentActivity.showStatusBar() {
//    WindowCompat.setDecorFitsSystemWindows(window, true)
    WindowInsetsControllerCompat(window, window.decorView.rootView).let { controller ->
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
        controller.show(WindowInsetsCompat.Type.systemBars())
    }
}

fun FragmentActivity.showStatusBar(flag:Boolean) {
    if(flag) {
        showStatusBar()
    } else {
        hideStatusBar()
    }
}

fun AppCompatActivity.isActionBarVisible(): Boolean {
    return supportActionBar?.isShowing ?: false
}

fun AppCompatActivity.hideActionBar() {
    supportActionBar?.hide()
}

fun AppCompatActivity.showActionBar() {
    supportActionBar?.show()
}

fun AppCompatActivity.showActionBar(flag:Boolean) {
    if(flag) {
        supportActionBar?.show()
    } else {
        supportActionBar?.hide()
    }
}

enum class ActivityOrientation(val value:Int) {
    AUTO(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED),
    LANDSCAPE(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE),
    PORTRAIT(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT),
}

fun FragmentActivity.setOrientation(orientation:ActivityOrientation) {
    requestedOrientation = orientation.value
}

data class ActivityOptions(
    private val showActionBar:Boolean? = null,              // nullなら現状の値を変更しない
    private val showStatusBar:Boolean? = null,
    private val requestedOrientation:ActivityOrientation? = null
) {
    fun apply(activity: FragmentActivity) {
        showActionBar?.also { show ->
            if (activity is AppCompatActivity) {
                activity.showActionBar(show)
            }
        }
        showStatusBar?.also { show ->
            activity.showStatusBar(show)
        }
        requestedOrientation?.also { orientation ->
            activity.setOrientation(orientation)
        }
    }

    companion object {
        fun actionBar(showActionBar: Boolean, orientation: ActivityOrientation?=null):ActivityOptions
                = ActivityOptions(showActionBar, null, orientation)
        fun statusBar(showStatusBar: Boolean, orientation: ActivityOrientation?=null):ActivityOptions
                = ActivityOptions(null, showStatusBar, orientation)
        fun actionAndStatusBar(showActionBar: Boolean, showStatusBar: Boolean, orientation: ActivityOrientation?=null):ActivityOptions
                = ActivityOptions(showActionBar, showStatusBar, orientation)
    }
}

