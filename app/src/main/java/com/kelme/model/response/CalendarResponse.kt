package com.kelme.model.response

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CalendarResponse(
    val code: Int,
    val data: List<CalendarData>,
    val message: String,
    val status: Boolean
):Parcelable

@Parcelize
data class CalendarData(
    val month: String?,
    val month_data: List<MonthData>?
):Parcelable

@Parcelize
data class MonthData(
    val calendar_event_id: String?,
    val country_id: String?,
    val created_at: String?,
    val eventMonth: String?,
    val event_description: String?,
    val event_end: String?,
    val event_start: String?,
    val event_title: String?,
    val module_id: String?,
    val module_type: String?,
    val status: String?,
    val updated_at: String?
):Parcelable