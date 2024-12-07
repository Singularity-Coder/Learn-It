package com.singularitycoder.learnit.helpers

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

fun Int.milliSeconds(): Long = this.toLong()

// Get Epoch Time
val currentTimeMillis: Long
    get() = System.currentTimeMillis()

val thirtySecondsTimeMillis: Long
    get() = TimeUnit.SECONDS.toMillis(30)

val fiveMinTimeMillis: Long
    get() = TimeUnit.MINUTES.toMillis(5)

val tenMinTimeMillis: Long
    get() = TimeUnit.MINUTES.toMillis(10)

val fifteenMinTimeMillis: Long
    get() = TimeUnit.MINUTES.toMillis(15)

val thirtyMinTimeMillis: Long
    get() = TimeUnit.MINUTES.toMillis(30)

val oneHourTimeMillis: Long
    get() = TimeUnit.HOURS.toMillis(1)

val threeHourTimeMillis: Long
    get() = TimeUnit.HOURS.toMillis(3)

val sixHourTimeMillis: Long
    get() = TimeUnit.HOURS.toMillis(6)

val twelveHourTimeMillis: Long
    get() = TimeUnit.HOURS.toMillis(12)

val oneDayTimeMillis: Long
    get() = TimeUnit.DAYS.toMillis(1)

val sevenDayTimeMillis: Long
    get() = TimeUnit.DAYS.toMillis(7)

val sixDayTimeMillis: Long
    get() = TimeUnit.DAYS.toMillis(6)

val nineDayTimeMillis: Long
    get() = TimeUnit.DAYS.toMillis(9)

val sixteenDayTimeMillis: Long
    get() = TimeUnit.DAYS.toMillis(16)

val nineteenDayTimeMillis: Long
    get() = TimeUnit.DAYS.toMillis(19)

val thirtyFiveDayTimeMillis: Long
    get() = TimeUnit.DAYS.toMillis(35)

fun Long.toDateTime(type: String = "dd-MMM-yyyy @ h:mm a"): String {
    val date = Date(this)
    val dateFormat = SimpleDateFormat(type, Locale.getDefault())
    return dateFormat.format(date)
}
