/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.example.sleepsamplekotlin.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.location.SleepClassifyEvent
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Entity class (table version of the class) for [SleepClassifyEvent] which represents a sleep
 * classification event including the classification timestamp, the sleep confidence, and the
 * supporting data such as device motion and ambient light level. Classification events are
 * reported regularly.
 */
@Entity(tableName = "sleep_classify_events_table")
data class SleepClassifyEventEntity(
    @PrimaryKey
    @ColumnInfo(name = "time_stamp_seconds")
    val timestampSeconds: Long,

    @ColumnInfo(name = "sgt_time")
    val sgtTime: String,

    @ColumnInfo(name = "confidence")
    val confidence: Int,

    @ColumnInfo(name = "motion")
    val motion: Int,

    @ColumnInfo(name = "light")
    val light: Int
) {
    companion object {
        fun epochToSGT(epochMillis: Long): String {
            return Instant.ofEpochMilli(epochMillis)
                .atZone(ZoneId.of("Asia/Singapore"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        }

        fun from(sleepClassifyEvent: SleepClassifyEvent): SleepClassifyEventEntity {
            return SleepClassifyEventEntity(
                timestampSeconds = sleepClassifyEvent.timestampMillis,
                sgtTime = epochToSGT(sleepClassifyEvent.timestampMillis),
                confidence = sleepClassifyEvent.confidence,
                motion = sleepClassifyEvent.motion,
                light = sleepClassifyEvent.light
            )
        }
    }

    override fun toString(): String {
        return "SleepClassifyEventEntity(timestamp=$sgtTime, light=$light, motion=$motion, confidence=$confidence)"
    }
}
