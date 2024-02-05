package com.example.bench

import com.elvishew.xlog.printer.file.naming.FileNameGenerator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CustomFileNameGenerator(val filenameDefault: String) : FileNameGenerator {

    override fun generateFileName(logLevel: Int, timestamp: Long): String {
        val formattedTimestamp =
            SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(timestamp))
        return "custom_log_$formattedTimestamp.txt"
        return filenameDefault
    }

    override fun isFileNameChangeable(): Boolean {
        return true
    }
}