package com.elsharif.geminichatbot

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elsharif.geminichatbot.data.Chat
import com.elsharif.geminichatbot.data.ChatData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel:ViewModel() {

    private val _chatState = MutableStateFlow(ChatState())
    val chatState = _chatState.asStateFlow()

    fun onEvent(event: ChatEvent){
        when(event){
            is ChatEvent.SendPrompt -> {
                if(event.prompt!!.isNotEmpty()){
                    addPrompt(event.prompt,event.bitmap)
                    if(event.bitmap !=null){
                        getResponseWithImage(prompt = event.prompt,bitmap = event.bitmap)

                    }else {
                        getResponse(prompt = event.prompt)
                    }
                }
                else {
                    addPrompt(event.prompt,event.bitmap)
                    if(event.bitmap !=null) {
                        getResponseImage(bitmap = event.bitmap)

                    }

                }


            }
            is ChatEvent.UpdatePrompt -> {

                _chatState.update {
                    it.copy(
                        prompt = event.newPrompt
                    )
                }

            }
        }
    }

    private fun addPrompt(prompt: String,bitmap: Bitmap?){
        _chatState.update {
            it.copy(
            chatList = it.chatList.toMutableList().apply {
                add(0,Chat(prompt,bitmap,true))
            },
                prompt="",
                bitmap = null
            )
        }
    }
    private fun getResponse(prompt: String) {

        viewModelScope.launch {

            val chat = ChatData.getResponse(prompt)
            _chatState.update {
                it.copy(
                    chatList = it.chatList.toMutableList().apply {
                        add(0, chat)
                    })
            }
        }
    }
    private fun getResponseWithImage(prompt: String,bitmap: Bitmap) {

        viewModelScope.launch {

            val chat = ChatData.getResponseWithImage(prompt,bitmap)
            _chatState.update {
                it.copy(
                    chatList = it.chatList.toMutableList().apply {
                        add(0, chat)
                    })
            }
        }
    }
    private fun getResponseImage(bitmap: Bitmap) {

        viewModelScope.launch {

            val chat = ChatData.getResponseImage(bitmap)
            _chatState.update {
                it.copy(
                    chatList = it.chatList.toMutableList().apply {
                        add(0, chat)
                    })
            }
        }
    }


}