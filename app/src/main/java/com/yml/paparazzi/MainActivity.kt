package com.yml.paparazzi

import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media
import android.system.Os
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.yml.paparazzi.imagelist.Image
import com.yml.paparazzi.imagelist.ImageListDestination
import com.yml.paparazzi.ui.theme.PaparazziTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PaparazziTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var imageList by remember {
                        mutableStateOf(
                            listOf(
                                Image.emptyImage, Image.emptyImage, Image.emptyImage
                            )
                        )
                    }
                    ImageListDestination(imageList = imageList)

                    LaunchedEffect(key1 = Unit, block = {
                        readPhotosFromExternalStorage().let {
                            imageList = it
                        }
                    })
                }
            }

        }
    }

    private suspend fun readPhotosFromExternalStorage(): List<Image> {
        return withContext(Dispatchers.Main) {
            val externalContentUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else Media.EXTERNAL_CONTENT_URI

            Log.d("VOLUME_URI", externalContentUri.toString())
            val projection = arrayOf(
                Media._ID,
                Media.HEIGHT,
                Media.WIDTH,
                Media.DISPLAY_NAME
            )

            val result = mutableListOf<Image>()

            contentResolver.query(
                externalContentUri,
                projection,
                null,
                null,
                "${Media.DISPLAY_NAME} ASC"
            )?.use { cursor ->
                Log.d("CURSOR", cursor.count.toString())
                val columnIndex = cursor.getColumnIndex(Media.DISPLAY_NAME)
                val height = cursor.getColumnIndex(Media.HEIGHT)
                val width = cursor.getColumnIndex(Media.WIDTH)
                val id = cursor.getColumnIndex(Media._ID)
                while (cursor.moveToNext()) {
                    val image = Image(
                        name = cursor.getString(columnIndex),
                        height = cursor.getInt(height),
                        width = cursor.getInt(width),
                        fileUri = ContentUris.withAppendedId(
                            Media.EXTERNAL_CONTENT_URI,
                            cursor.getLong(id)
                        ).toString()
                    )
                    Log.d("CURSOR_INFO", "${image.height} / ${image.width}")
                    result.add(image)
                }

            }
            return@withContext result
        }
    }
}