package com.example.tool

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Date

class Util {
    @SuppressLint("SimpleDateFormat")
     fun formatTimestampToDate(timestamp: Long, pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat(pattern)
        return formatter.format(date)
    }
}