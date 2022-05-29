package com.codeboy.mediafacer

import java.lang.Exception
import java.text.DecimalFormat

object MediaDataUtils {

    fun convertDuration(duration: Long): String? {
        var out: String? = null
        var hours: Long = 0
        hours = try {
            duration / 3600000
        } catch (e: Exception) {
            e.printStackTrace()
            return out
        }
        val remainingMinutes = (duration - hours * 3600000) / 60000
        var minutes = remainingMinutes.toString()
        if (minutes == "0") {
            minutes = "00"
        }
        val remaining_seconds = duration - hours * 3600000 - remainingMinutes * 60000
        var seconds = remaining_seconds.toString()
        seconds = if (seconds.length < 2) {
            "00"
        } else {
            seconds.substring(0, 2)
        }
        out = if (hours > 0) {
            "$hours:$minutes:$seconds"
        } else {
            "$minutes:$seconds"
        }
        return out
    }

    fun milliSecondsToTimer(milliseconds: Long): String {
        var finalTimerString = ""
        var secondsString = ""
        // Convert total duration into time
        val hours = (milliseconds / (1000 * 60 * 60)).toInt()
        val minutes = (milliseconds % (1000 * 60 * 60)).toInt() / (1000 * 60)
        val seconds = (milliseconds % (1000 * 60 * 60) % (1000 * 60) / 1000).toInt()
        // Add hours if there
        if (hours > 0) {
            finalTimerString = "$hours:"
        }

        // Prepending 0 to seconds if it is one digit
        secondsString = if (seconds < 10) {
            "0$seconds"
        } else {
            "" + seconds
        }
        finalTimerString = "$finalTimerString$minutes:$secondsString"

        // return timer string
        return finalTimerString
    }

    fun size(size: Int): String {
        var hrSize = ""
        val m = size / 1024.0
        val dec = DecimalFormat("0.00")
        hrSize = if (m > 1) {
            dec.format(m) + " Mo"
        } else {
            dec.format(size.toLong()) + " Ko"
        }
        return hrSize
    }

    //returns the size of a media file
    fun convertBytes(fileSize: Long): String {
        // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
        val fileSizeInKB = fileSize / 1024
        // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
        //long fileSizeInMB = fileSizeInKB / 1024;
        val finalSize = fileSizeInKB.toString()
        val lastSize = Integer.valueOf(finalSize)
        return size(lastSize)
    }
}