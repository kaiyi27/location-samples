package com.android.example.sleepsamplekotlin

import android.content.Context
import android.net.Uri
import java.io.FileOutputStream
import kotlinx.coroutines.*

fun Context.writeTextToUri(
    uri: Uri,
    textContent: String,
    progressListener: FileWriterProgressListener? = null
) {
    progressListener?.onWritingStarted()

    CoroutineScope(Dispatchers.IO).launch {
        try {

            contentResolver.openFileDescriptor(uri, "w")?.use { pfd ->
                FileOutputStream(pfd.fileDescriptor).use { out ->
                    textContent.byteInputStream().copyTo(out)
                }
            }
            withContext(Dispatchers.Main) {
                progressListener?.onWritingCompleted()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                progressListener?.onErrorWriting("Error writing file: ${e.message}")
            }
        }
    }
}
