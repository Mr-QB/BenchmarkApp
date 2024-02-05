package com.example.benchmarkapp

import com.elvishew.xlog.XLog
import com.elvishew.xlog.printer.file.writer.Writer
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.time.Instant
import java.util.Timer
import java.util.TimerTask

class CustomWriter : Writer() {
    private var logFileName: String? = null
    private var logFile: File? = null
    private var bufferedWriter: BufferedWriter? = null
    override fun open(file: File): Boolean {
        logFileName = file.name
        logFile = file

        var isNewFile = false

        if (!logFile!!.exists()) {
            try {
                val parent = logFile!!.parentFile
                if (!parent.exists()) {
                    parent.mkdirs()
                }
                logFile!!.createNewFile()
                isNewFile = true
            } catch (e: Exception) {
                e.printStackTrace()
                close()
                return false
            }
        }

        try {
            bufferedWriter = BufferedWriter(FileWriter(logFile, true), 5 * 1024)
            if (isNewFile) {
                onNewFileCreated(logFile!!)
            }
            startFlushTimer()
        } catch (e: Exception) {
            e.printStackTrace()
            close()
            return false
        }
        return true
    }

    override fun isOpened(): Boolean {
        return bufferedWriter != null && logFile!!.exists()
    }

    override fun getOpenedFile(): File? {
        return logFile
    }

    override fun getOpenedFileName(): String? {
        return logFileName
    }

    fun onNewFileCreated(file: File) {
    }

    private fun startFlushTimer() {
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                flushBuffer()
                XLog.tag("checkThread").d("Thread writing log to file: ${Thread.currentThread().name}")
            }
        }, 0, 2000)
    }

    private fun flushBuffer() {
        try {
            bufferedWriter?.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun appendLog(log: String) {
        var nLog = log + "--${System.currentTimeMillis()}"
        try {
            bufferedWriter?.write(nLog)
            bufferedWriter?.newLine()
        } catch (e: Exception) {
        }
    }

    override fun close(): Boolean {
        bufferedWriter?.close()
        bufferedWriter = null
        logFileName = null
        logFile = null
        return true
    }
}