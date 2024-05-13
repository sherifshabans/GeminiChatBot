package com.elsharif.geminichatbot


import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.elsharif.geminichatbot.ui.theme.GeminiChatBotTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.automirrored.rounded.Send

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer


class MainActivity : ComponentActivity() {

    private val uriState = MutableStateFlow("")

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let {
                uriState.update { uri.toString() }
            }
        }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeminiChatBotTheme {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Scaffold(
                        topBar =
                        {
                        Box(modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary)
                            .height(55.dp)
                            .padding(horizontal = 16.dp),

                        ){

                            Text(
                                modifier = Modifier.align(Alignment.CenterStart),
                                text  = stringResource(R.string.app_name),
                               fontSize = 19.sp,
                               color = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        }
                    ) {

                        ChatScreen(paddingValues = it)

                    }
                }
            }
        }
    }

    @Composable
    fun ChatScreen(paddingValues: PaddingValues) {

        val chatViewModel=viewModel<ChatViewModel>()
        val chatState=chatViewModel.chatState.collectAsState().value

        val bitmap=getBitmap()


        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
            verticalArrangement = Arrangement.Bottom

        ){
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                reverseLayout = true
            ) {

                itemsIndexed(chatState.chatList){ index,chat->
                    if(chat.isFromUser){

                            UserChatItem(chat.prompt, chat.bitmap)

                        }else{
                        ModelChatItem(chat.prompt)
                    }

                }

            }


            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, start = 4.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ){

                Column {
                    bitmap?.let {
                        AnimatedVisibility(visible = bitmap != null) {

                            Image(
                                modifier = Modifier
                                    .size(40.dp)
                                    .padding(bottom = 2.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                contentDescription = "image",
                                contentScale = ContentScale.Crop,
                                bitmap = it.asImageBitmap()

                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Rounded.AddPhotoAlternate,
                        contentDescription = "Add Photo",
                        modifier = Modifier
                            .size(40.dp)
                            .clickable {
                                imagePicker.launch(
                                    PickVisualMediaRequest
                                        .Builder()
                                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        .build()
                                )

                            },
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                    Spacer(modifier =Modifier.width(8.dp))

                    TextField(
                        modifier = Modifier.weight(1f) ,
                        value = chatState.prompt,
                        onValueChange ={
                            chatViewModel.onEvent(ChatEvent.UpdatePrompt(it))
                        } ,
                        placeholder = {
                            Text(text = "Type a prompt")
                        }
                        )
                    Spacer(modifier =Modifier.width(8.dp))

                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription ="Send prompt",
                        modifier = Modifier
                            .size(40.dp)
                            .clickable {
                                chatViewModel.onEvent(
                                    ChatEvent.SendPrompt(
                                        chatState.prompt,
                                        bitmap
                                    )
                                )
                                uriState.update { "" }
                            },
                        tint = MaterialTheme.colorScheme.primary
                    )



            }

        }

    }



    @Composable
    fun UserChatItem(prompt:String,bitmap: Bitmap?) {

        Column (
            modifier = Modifier.padding(start = 100.dp, bottom = 16.dp)
        ){
            AnimatedVisibility(visible = bitmap != null) {

                bitmap?.let {
                    Image(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .padding(bottom = 2.dp)
                            .clip(RoundedCornerShape(12.dp)),

                        contentDescription = "image",
                        contentScale = ContentScale.Crop,
                        bitmap = it.asImageBitmap()

                    )
                }
            }
           if(!prompt.isEmpty())
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(16.dp),
                text = prompt,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onPrimary
            )

        }
    }
    @Composable
    fun UserChatItemImage(bitmap: Bitmap?) {

        Column (
            modifier = Modifier.padding(start = 100.dp, bottom = 16.dp)
        ){
            bitmap?.let {
                Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .padding(bottom = 2.dp)
                        .clip(RoundedCornerShape(12.dp)),

                    contentDescription = "image",
                    contentScale = ContentScale.Crop,
                    bitmap = it.asImageBitmap()

                )
            }



        }
    }

    @Composable
    fun ModelChatItem(response:String) {

        Column (
            modifier = Modifier.padding(end = 100.dp, bottom = 22.dp)

        ){

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.tertiary)
                .padding(16.dp),
            text = "✨ : " +response,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onTertiary
        )


        }
    }
    @Composable

    private  fun getBitmap():Bitmap?{
        val uri=uriState.collectAsState().value

        val imageState :AsyncImagePainter.State=rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(uri)
                .size(Size.ORIGINAL)
                .build()
        ).state
        return if(imageState is AsyncImagePainter.State.Success){
            imageState.result.drawable.toBitmap()
        }else{
            null
        }

    }

}
