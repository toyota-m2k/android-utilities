package io.github.toyota32k.utils

import java.util.Locale
import kotlin.time.Duration

class TimeSpan (ms:Long) {
    val milliseconds: Long = ms

    val seconds: Long
        get() = (milliseconds / 1000) % 60

    val minutes: Long
        get() = (milliseconds / 1000 / 60) % 60

    val hours: Long
        get() = (milliseconds / 1000 / 60 / 60)

    fun formatH(format:String="%02d:%02d'%02d\"") : String {
        return String.format(Locale.US, format, hours, minutes, seconds)
    }
    fun formatM(format:String="%02d'%02d\"") : String {
        return String.format(Locale.US, format, minutes, seconds)
    }
    fun formatMm(format:String="%02d'%02d\"%03d") : String {
        return String.format(Locale.US, format, minutes, seconds, milliseconds)
    }
    fun formatS(format:String="%02d\"%02d") : String {
        return String.format(Locale.US, format, seconds, milliseconds/10)
    }
    fun formatSm(format:String="%02d\"%03d") : String {
        return String.format(Locale.US, format, seconds, milliseconds)
    }
    fun formatAuto() : String {
        return when {
            hours>0 -> formatH()
            minutes>0 -> formatM()
            else-> formatS()
        }
    }


    companion object {
        @JvmStatic
        fun formatH(ms:Long) : String {
            return TimeSpan(ms).formatH()
        }
        @JvmStatic
        fun formatM(ms:Long) : String {
            return TimeSpan(ms).formatM()
        }
        @JvmStatic
        fun formatS(ms:Long) : String {
            return TimeSpan(ms).formatS()
        }
        @JvmStatic
        fun formatAuto(ms:Long):String {
            return TimeSpan(ms).formatAuto()
        }
    }
}