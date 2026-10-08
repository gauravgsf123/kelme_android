package com.kelme.model.response

data class AppKillStausResponse(
    val status: Boolean,
    val message: String,
    val data: DataResponse,
    val code: Long,
)

data class DataResponse(
    val kill_status: Long,
    val kill_at: String,
    val user_id: Long,
    val trackingId: String,
)
