package com.android.example.sleepsamplekotlin

interface FileWriterProgressListener {
    fun onWritingStarted()
    fun onWritingCompleted()

    fun onErrorWriting(message:String)
}