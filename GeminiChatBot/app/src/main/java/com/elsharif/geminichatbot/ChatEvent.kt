package com.elsharif.geminichatbot

import android.graphics.Bitmap
import com.elsharif.geminichatbot.data.Chat

sealed class ChatEvent {
    data class UpdatePrompt(val newPrompt: String):ChatEvent()
    data class SendPrompt(
        val prompt:String?,
        val bitmap: Bitmap?
    ):ChatEvent()
}