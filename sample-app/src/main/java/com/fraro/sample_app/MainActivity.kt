package com.fraro.sample_app

import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fraro.sample_app.ui.screens.SampleScreen
import com.fraro.sample_app.ui.theme.ComposablerealtimeanimationsTheme
import com.fraro.sample_app.ui.theme.NightPurple
import com.fraro.sample_app.ui.theme.NightRed
import com.fraro.sample_app.ui.theme.Purple40
import kotlinx.serialization.Serializable
import org.intellij.lang.annotations.Language

class MainActivity : ComponentActivity() {

    @Language("AGSL")
    private val SHADER = """
        uniform float2 resolution;
        layout(color) uniform half4 color;
        layout(color) uniform half4 color2;
        
        half4 main(in float2 fragCoord) {
            float2 uv = fragCoord/resolution.xy;
            
            float mixValue = distance(uv, vec2(1,1));
            return mix(color, color2, mixValue);
        }
    """.trimIndent()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ComposablerealtimeanimationsTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawWithCache {
                            val shader = RuntimeShader(SHADER)
                            val shaderBrush = ShaderBrush(shader)
                            shader.setFloatUniform("resolution", size.width, size.height)
                            onDrawBehind {

                                shader.setColorUniform(
                                    "color2",
                                    Color.valueOf(
                                        Purple40.red,
                                        Purple40.green,
                                        Purple40.blue,
                                        1f
                                    )
                                )

                                shader.setColorUniform(
                                    "color",
                                    Color.valueOf(
                                        NightRed.red,
                                        NightRed.green,
                                        NightRed.blue,
                                        1f
                                    )
                                )
                                drawRect(shaderBrush)
                            }
                        }
                ) {
                    SampleScreen()
                }
            }
        }
    }
}

