package com.kelme.repo

import com.kelme.model.request.*
import com.kelme.network.RetrofitInstance

/**
 * Created by Amit Gupta on 08-07-2021.
 */
class ChatRepository {

    suspend fun contactList(request: ContactListRequest) =
        RetrofitInstance.apiService?.contactList(request)

    suspend fun chatList(request: ChatListRequest) =
        RetrofitInstance.apiService?.chatList(request)

    suspend fun logout() =
        RetrofitInstance.apiService?.logout()

    suspend fun getTokenAgora(request: GetTokenAgoraRequest) = RetrofitInstance.apiService?.getTokenAgora(request)
    suspend fun getVoipToken(request: GetVoipTokenRequest) = RetrofitInstance.apiService?.getVoipToken(request)
    suspend fun otherUserRequestCall(request: OtherUserRequestCallRequest) = RetrofitInstance.apiService?.otherUserRequestCall(request)
    suspend fun senderJoinCalSuccessfully(request: SenderJoinCallSuccessfullyRequest) = RetrofitInstance.apiService?.senderJoinCalSuccessfully(request)
    suspend fun otherUserJoinCallSuccessfully(request: OtherUserJoinCallSuccessfullyRequest) = RetrofitInstance.apiService?.otherUserJoinCallSuccessfully(request)
    suspend fun senderCanNotJoinCall(request: SenderCanNotJoinCallRequest) = RetrofitInstance.apiService?.senderCanNotJoinCall(request)
    suspend fun otherUserjoinRejectCall(request: OtherUserJoinRejectCallRequest) = RetrofitInstance.apiService?.otherUserjoinRejectCall(request)
    suspend fun callEnd(request: CallEndRequest) = RetrofitInstance.apiService?.callEnd(request)
}