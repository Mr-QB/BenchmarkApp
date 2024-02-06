package com.example.bench

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import com.elvishew.xlog.printer.file.FilePrinter
import com.elvishew.xlog.printer.file.backup.FileSizeBackupStrategy2
import com.elvishew.xlog.printer.file.clean.FileLastModifiedCleanStrategy
import com.elvishew.xlog.printer.file.naming.DateFileNameGenerator
import com.example.benchmarkapp.CustomWriter
import java.io.BufferedReader
import java.io.FileReader
import java.io.InputStreamReader


class BenchApp {
    companion object {
        lateinit var fileContentPath: String
        lateinit var contentBuffer: String
        lateinit var customLogPrinter: FilePrinter
    }


    @Composable
    public fun mainApp(logFolderPath: String) {
        customLogPrinter = FilePrinter.Builder(logFolderPath)
            .writer(CustomWriter())
            .fileNameGenerator(DateFileNameGenerator())
            .backupStrategy(FileSizeBackupStrategy2(5* 1024 * 1024, 100))
            .cleanStrategy(FileLastModifiedCleanStrategy(7 * 24 * 60 * 60 * 1000))
            .build()

        XLog.init(LogLevel.ALL)

        Column(modifier = Modifier.padding(8.dp)) {
            bntFindContent()

            btnBench()
        }
    }

    @Composable
    private fun btnBench() {

        Button(onClick = {
            if (fileContentPath != null) {
                startPushMsgToQueueLogThread(customLogPrinter)
//                writeTextFileToLog(customLogPrinter, fileContentPath)
            }
        }) {
            Text(text = "Bench...")
        }
    }
    private fun startPushMsgToQueueLogThread(filePrinter: FilePrinter){
        val pushQueueThread = Thread{
            pushMsgToQueueLog(filePrinter)
        }
        pushQueueThread.start()
    }

    private fun pushMsgToQueueLog(filePrinter: FilePrinter) {
        var lineCount = 0
        var dataSize = 0
        var msg = ""

        while (true) {
            if (contentBuffer != null) {
                contentBuffer.lineSequence().forEach { line ->
                    lineCount++
                    msg =
                        "Msg line:$lineCount ++ Sum size of msg log: $dataSize ++ Line conntent: $line"
                    dataSize += msg.length
                    XLog.tag("writelog").printers(filePrinter).d(msg)
                    XLog.tag("checkThread").d("Thread put log to queue: ${Thread.currentThread().name}")
                }
            }
        }
    }


    @Composable
    private fun bntFindContent() {

        var contentPath by remember { mutableStateOf<String?>("/mnt/shared/Pictures/Hoang Kim Dong - Da Nhan.txt") }

        val activity = LocalContext.current as? ComponentActivity

        val filePickerLauncher = remember(activity) {
            activity?.activityResultRegistry?.register(
                "file_picker",
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == ComponentActivity.RESULT_OK) {
                    result.data?.data?.let { uri ->

                        val contentResolver = activity.contentResolver
                        val documentFile = DocumentFile.fromSingleUri(activity, uri)
                        if (documentFile != null && documentFile.exists()) {
                            contentBuffer = readFileContentFromUri(contentResolver, uri)
                            contentPath = documentFile.uri.path
                            XLog.tag("BenchLog").d("Done load text file....")
                        } else {
                            XLog.tag("BenchLog").e("DocumentFile does not exist.")
                        }
                    }
                }
            }
        }
        Row(
            modifier = Modifier.padding(8.dp)
        ) {
            Button(onClick = { openFilePicker(filePickerLauncher) }) {
                Text(text = "Finding File...")
            }
            Text(text = contentPath.toString())
        }
        fileContentPath = contentPath.toString()
    }

    private fun openFilePicker(filePickerLauncher: ActivityResultLauncher<Intent?>?) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
            .setType("*/*")

        filePickerLauncher?.launch(intent)
    }

    private fun readFileContentFromUri(contentResolver: ContentResolver, uri: Uri): String {
        val inputStream = contentResolver.openInputStream(uri)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()

        var line: String?
        while (reader.readLine().also { line = it } != null) {
//            while (line != null && line != "") {
                while (line != null) {
                stringBuilder.append(line).append('\n')
                line = reader.readLine()
            }
        }
        return stringBuilder.toString()
    }

}
