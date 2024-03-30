package com.kelme.fragment.chat

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelme.R
import com.kelme.model.ChatListModel
import com.kelme.model.ContactModel
import com.kelme.model.ContactUserDetailsModel
import com.kelme.model.request.*
import com.kelme.model.response.*
import com.kelme.repo.ChatRepository
import com.kelme.utils.Resource
import com.kelme.utils.Utils
import kotlinx.coroutines.launch
import retrofit2.Response

/**
 * Created by Amit Gupta on 16-05-2021.
 */
class ChatViewModal(private val app: Application) :
    ViewModel() {

    private val repository = ChatRepository()
    val contactList = MutableLiveData<Resource<List<ContactModel>>>()
    val chatList = MutableLiveData<Resource<List<ChatListModel>>>()
    val logout = MutableLiveData<Resource<String>>()

    val getTokenAgoraResponse = MutableLiveData<Resource<GetTokenAgoraResponse.Data>>()
    val getVoipTokenResponse = MutableLiveData<Resource<String>>()
    val otherUserRequestCallResponse = MutableLiveData<Resource<OtherUserRequestCallResponse.Data>>()
    val senderJoinCalSuccessfullyResponse = MutableLiveData<Resource<SenderJoinCalSuccessfullyResponse.Data>>()
    val otherUserJoinCallSuccessfullyResponse = MutableLiveData<Resource<OtherUserJoinCallSuccessfullyResponse.Data>>()
    val otherUserjoinRejectCallResponse = MutableLiveData<Resource<OtherUserJoinCallSuccessfullyResponse.Data>>()
    //val callEndResponse = MutableLiveData<Resource<OtherUserJoinCallSuccessfullyResponse>>()

    val senderCanNotJoinCallResponse = MutableLiveData<Resource<SenderCanNotJoinCallResponse.Data>>()
    val callEndResponse = MutableLiveData<Resource<String>>()

//    fun contactList() = viewModelScope.launch {
//        if (Utils.hasInternetConnection(app.applicationContext)) {
//            contactList.postValue(Resource.Loading())
//            val response = repository.contactList()
//            contactList.postValue(handleContactListResponse(response))
//        } else {
//            contactList.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
//        }
//    }

    private fun handleContactListResponse(response: Response<ContactListResponse>?): Resource<List<ContactUserDetailsModel>>? {
        if (response?.isSuccessful!!) {
            response.body()?.let { res ->
                return if (res.status) {
                    Resource.Success(res.message, res.data)
                } else {
                    if (res.code == 240) {
                        Resource.Error(res.code.toString())
                    } else {
                        Resource.Error(res.message)
                    }
                }
            }
        }
        return Resource.Error(app.resources.getString(R.string.something_went_wrong))
    }

    fun logout() = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            logout.postValue(Resource.Loading())
            val response = repository.logout()
            logout.postValue(handleLogoutResponse(response))
        } else {
            logout.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleLogoutResponse(response: Response<CommonResponse>?): Resource<String>? {
        if (response?.isSuccessful!!) {
            response.body()?.let { res ->
                return if (res.status) {
                    Resource.Success(res.message, res.message)
                } else {
                    Resource.Error(res.message)
                }
            }
        }
        return Resource.Error(app.resources.getString(R.string.something_went_wrong))
    }

    fun getTokenAgora(request: GetTokenAgoraRequest) = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            getTokenAgoraResponse.postValue(Resource.Loading())
            val response = repository.getTokenAgora(request)
            getTokenAgoraResponse.postValue(handleGetTokenAgoraResponse(response))
        } else {
            getTokenAgoraResponse.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleGetTokenAgoraResponse(response: Response<GetTokenAgoraResponse>?): Resource<GetTokenAgoraResponse.Data>? {
        if (response?.isSuccessful!!) {
            response.body()?.let { res ->
                return if (res.status) {
                    Resource.Success(res.message, res.data)
                } else {
                    Resource.Error(res.message)
                }
            }
        }
        return Resource.Error(app.resources.getString(R.string.something_went_wrong))
    }

    fun getVoipToken(request: GetVoipTokenRequest) = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            getVoipTokenResponse.postValue(Resource.Loading())
            val response = repository.getVoipToken(request)
            getVoipTokenResponse.postValue(handleGetVoipTokenResponse(response))
        } else {
            getVoipTokenResponse.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleGetVoipTokenResponse(response: Response<GetVoipTokenResponse>?): Resource<String>? {
        if (response?.isSuccessful!!) {
            response.body()?.let { res ->
                return if (res.status) {
                    Resource.Success(res.message, res.message)
                } else {
                    Resource.Error(res.message)
                }
            }
        }
        return Resource.Error(app.resources.getString(R.string.something_went_wrong))
    }

    fun otherUserRequestCall(request: OtherUserRequestCallRequest) = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            otherUserRequestCallResponse.postValue(Resource.Loading())
            val response = repository.otherUserRequestCall(request)
            otherUserRequestCallResponse.postValue(handleOtherUserRequestCallResponse(response))
        } else {
            otherUserRequestCallResponse.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleOtherUserRequestCallResponse(response: Response<OtherUserRequestCallResponse>?): Resource<OtherUserRequestCallResponse.Data>? {
        if (response?.isSuccessful!!) {
            response.body()?.let { res ->
                return if (res.status) {
                    Resource.Success(res.message, res.data)
                } else {
                    Resource.Error(res.message)
                }
            }
        }
        return Resource.Error(app.resources.getString(R.string.something_went_wrong))
    }

    fun senderJoinCalSuccessfully(request: SenderJoinCallSuccessfullyRequest) = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            senderJoinCalSuccessfullyResponse.postValue(Resource.Loading())
            val response = repository.senderJoinCalSuccessfully(request)
            senderJoinCalSuccessfullyResponse.postValue(handleSenderJoinCalSuccessfullyResponse(response))
        } else {
            senderJoinCalSuccessfullyResponse.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleSenderJoinCalSuccessfullyResponse(response: Response<SenderJoinCalSuccessfullyResponse>?): Resource<SenderJoinCalSuccessfullyResponse.Data>? {
        if (response?.isSuccessful!!) {
            response.body()?.let { res ->
                return if (res.status) {
                    Resource.Success(res.message, res.data)
                } else {
                    Resource.Error(res.message)
                }
            }
        }
        return Resource.Error(app.resources.getString(R.string.something_went_wrong))
    }

    fun otherUserJoinCallSuccessfully(request: OtherUserJoinCallSuccessfullyRequest) = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            otherUserJoinCallSuccessfullyResponse.postValue(Resource.Loading())
            val response = repository.otherUserJoinCallSuccessfully(request)
            otherUserJoinCallSuccessfullyResponse.postValue(handleOtherUserJoinCallSuccessfullyResponse(response))
        } else {
            otherUserJoinCallSuccessfullyResponse.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleOtherUserJoinCallSuccessfullyResponse(response: Response<OtherUserJoinCallSuccessfullyResponse>?): Resource<OtherUserJoinCallSuccessfullyResponse.Data>? {
        if (response?.isSuccessful!!) {
            response.body()?.let { res ->
                return if (res.status) {
                    Resource.Success(res.message,res.data)
                } else {
                    Resource.Error(res.message)
                }
            }
        }
        return Resource.Error(app.resources.getString(R.string.something_went_wrong))
    }

    fun senderCanNotJoinCall(request: SenderCanNotJoinCallRequest) = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            senderCanNotJoinCallResponse.postValue(Resource.Loading())
            val response = repository.senderCanNotJoinCall(request)
            senderCanNotJoinCallResponse.postValue(handleSenderCanNotJoinCallResponse(response))
        } else {
            senderCanNotJoinCallResponse.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleSenderCanNotJoinCallResponse(response: Response<SenderCanNotJoinCallResponse>?): Resource<SenderCanNotJoinCallResponse.Data>? {
        if (response?.isSuccessful!!) {
            response.body()?.let { res ->
                return if (res.status) {
                    Resource.Success(res.message, res.data)
                } else {
                    Resource.Error(res.message)
                }
            }
        }
        return Resource.Error(app.resources.getString(R.string.something_went_wrong))
    }

    fun otherUserjoinRejectCall(request: OtherUserJoinRejectCallRequest) = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            otherUserjoinRejectCallResponse.postValue(Resource.Loading())
            val response = repository.otherUserjoinRejectCall(request)
            otherUserjoinRejectCallResponse.postValue(handleOtherUserjoinRejectCallResponse(response))
        } else {
            otherUserjoinRejectCallResponse.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleOtherUserjoinRejectCallResponse(response: Response<OtherUserJoinCallSuccessfullyResponse>?): Resource<OtherUserJoinCallSuccessfullyResponse.Data>? {
        if (response?.isSuccessful!!) {
            response.body()?.let { res ->
                return if (res.status) {
                    Resource.Success(res.message, res.data)
                } else {
                    Resource.Error(res.message)
                }
            }
        }
        return Resource.Error(app.resources.getString(R.string.something_went_wrong))
    }

    fun callEnd(request: CallEndRequest) = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            callEndResponse.postValue(Resource.Loading())
            val response = repository.callEnd(request)
            callEndResponse.postValue(handleCallEndResponse(response))
        } else {
            callEndResponse.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleCallEndResponse(response: Response<CallEndResponse>?): Resource<String>? {
        if (response?.isSuccessful!!) {
            response.body()?.let { res ->
                return if (res.status) {
                    Resource.Success(res.message, res.message)
                } else {
                    Resource.Error(res.message)
                }
            }
        }
        return Resource.Error(app.resources.getString(R.string.something_went_wrong))
    }


//    fun chatList(request: ChatListRequest) = viewModelScope.launch {
//        if (Utils.hasInternetConnection(app.applicationContext)) {
//            chatList.postValue(Resource.Loading())
//            val response = repository.chatList(request)
//          //  chatList.postValue(handleChatListResponse(response))
//        } else {
//            chatList.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
//        }
//    }

//    private fun handleChatListResponse(response: Response<ChatListResponse>?): Resource<List<ChatListFragment.User>>? {
//        if (response?.isSuccessful!!) {
//            response.body()?.let { res ->
//                return if (res.status) {
//                    Resource.Success(res.message, res.data)
//                } else {
//                    if (res.code == 240) {
//                        Resource.Error(res.code.toString())
//                    } else {
//                        Resource.Error(res.message)
//                    }
//                }
//            }
//        }
//        return Resource.Error(app.resources.getString(R.string.something_went_wrong))
//    }


}