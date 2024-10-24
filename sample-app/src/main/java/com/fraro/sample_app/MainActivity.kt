package com.fraro.sample_app

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fraro.composable_realtime_animations.R
import com.fraro.composable_realtime_animations.ui.screens.RealtimeAnimationCanvas
import com.fraro.sample_app.ui.theme.ComposablerealtimeanimationsTheme
import com.fraro.sample_app.ui.viewmodels.MainViewModel
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import kotlin.random.Random

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()


    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposablerealtimeanimationsTheme {
                var samplingValue by rememberSaveable { mutableStateOf("10") }
                val imageMap = remember {
                    loadImagesToMap(this@MainActivity)
                }
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RealtimeAnimationCanvas(
                        animationFlow = mainViewModel.particleFlow(),
                        samplingInterval = samplingValue.toIntOrNull() ?: 10,
                        iconsBitmap = imageMap
                    )
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            modifier = Modifier.padding(20.dp),
                            onClick = {
                                this@MainActivity.startActivity(Intent(
                                    this@MainActivity, OssLicensesMenuActivity::class.java))
                            },
                        ) {
                            Text(text = "Licenses")
                        }
                    }
                    Column(
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 20.dp)
                    ) {
                        TextField(
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            value = samplingValue,
                            onValueChange = {
                                samplingValue = it
                            },
                            label = { Text("Insert a sampling interval in m_sec") }
                        )
                    }
                }
            }
        }
    }
}


fun loadImagesToMap(context: Context): Map<Long, ImageBitmap> {
    val imageMap = mutableMapOf<Long, ImageBitmap>()

    // Loop through the image files from "10.png" to "55.png"
    for (i in 1..100) {
        var fileName: String
        if (i in 10 ..  55) {
            fileName = "emoji$i"
        }
        else {
            fileName = "emoji${Random.nextInt(10,55)}"
        }

        // Get the resource identifier (assuming these images are in the res/drawable directory)
        val resourceId = context.resources.getIdentifier(fileName, "drawable", context.packageName)

        if (resourceId != 0) {
            val bitmap = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(context.resources, resourceId),
                50,
                50,
                true
            )
            imageMap[i.toLong()] = bitmap.asImageBitmap()
        }
    }
    return imageMap
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun Preview() {
    ComposablerealtimeanimationsTheme {
        Row(
            verticalAlignment = Alignment.Top
        ) {
            Button(
                onClick = {},
            ) {
                Text(text = "Licenses")
            }
        }
        Column(
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(30.dp),
        ) {
            var text by rememberSaveable { mutableStateOf("10") }
            TextField(
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                value = text,
                onValueChange = {
                    text = it
                },
                label = { Text("Insert a sampling rate") }
            )
        }
    }
}