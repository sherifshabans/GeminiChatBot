package com.elsharif.geminichatbot

import android.graphics.Bitmap
import com.elsharif.geminichatbot.data.Chat

data class ChatState(
    val chatList :MutableList<Chat> = mutableListOf(),
    val prompt :String = "",
    val bitmap: Bitmap?=null
)
