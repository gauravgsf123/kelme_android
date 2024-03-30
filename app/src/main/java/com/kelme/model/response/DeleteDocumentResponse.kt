package com.kelme.model.response

data class DeleteDocumentResponse(
    val code: Int,
    val message: String,
    val status: Boolean
)

class DeleteDocumentData()