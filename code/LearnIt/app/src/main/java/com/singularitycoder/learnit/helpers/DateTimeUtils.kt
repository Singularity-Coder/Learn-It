package com.singularitycoder.learnit.helpers

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

fun Int.milliSeconds(): Long = this.toLong()

// Get Epoch Time
val currentTimeMillis: Long
    get() = System.currentTimeMillis()

val twelveHourTimeMillis: Long
    get() = TimeUnit.HOURS.toMillis(12)

val oneDayTimeMillis: Long
    get() = TimeUnit.DAYS.toMillis(1)

val sixDayTimeMillis: Long
    get() = TimeUnit.DAYS.toMillis(6)

val nineDayTimeMillis: Long
    get() = TimeUnit.DAYS.toMillis(9)

val nineteenDayTimeMillis: Long
    get() = TimeUnit.DAYS.toMillis(19)

fun Long.toDateTime(type: String = "dd-MMM-yyyy @ h:mm a"): String {
    val date = Date(this)
    val dateFormat = SimpleDateFormat(type, Locale.getDefault())
    return dateFormat.format(date)
}
