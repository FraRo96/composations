package com.fraro.sample_app.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.PointF
import android.graphics.RuntimeShader
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.plus
import androidx.core.graphics.times
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.pill
import androidx.graphics.shapes.pillStar
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fraro.composable_realtime_animations.data.models.Animation
import com.fraro.composable_realtime_animations.data.models.AnimationType
import com.fraro.composable_realtime_animations.data.models.State
import com.fraro.composable_realtime_animations.data.models.State.Start
import com.fraro.composable_realtime_animations.data.models.StateHolder
import com.fraro.composable_realtime_animations.data.models.VisualDescriptor
import com.fraro.composable_realtime_animations.ui.screens.RealtimeBox
import com.fraro.sample_app.ui.theme.Brown
import com.fraro.sample_app.ui.theme.DarkBrown
import com.fraro.sample_app.ui.theme.DarkGreen
import com.fraro.sample_app.ui.theme.LightBlue
import com.fraro.sample_app.ui.theme.LightPink
import com.fraro.sample_app.ui.viewmodels.SampleViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.intellij.lang.annotations.Language
import com.fraro.sample_app.ui.theme.NightPurple
import com.fraro.sample_app.ui.theme.NightRed
import com.fraro.sample_app.ui.theme.Pink80
import com.fraro.sample_app.ui.theme.PinkOrange
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Language("AGSL")
private val SHADER_TREE = """
            uniform float2 resolution;
            layout(color) uniform half4 brownColor;
            layout(color) uniform half4 darkerBrownColor;
           
            vec3 hash33(vec3 p){ 
                
                float n = sin(dot(p, vec3(7, 157, 113)));    
                return fract(vec3(2097152.011, 262144.984, 32768.115)*n); 
            }
            float voronoi(vec3 p){

            	vec3 b, r, g = floor(p);
            	p = fract(p); // "p -= g;" works on some GPUs, but not all, for some annoying reason.
            	
            	float d = 0.5; 

                for(float j = -1.; j <= 1.; j++) {
            	    for(float i = -1.; i <= 1.; i++) {
                		
            		    b = vec3(i, j, -1.);
            		    r = b - p + hash33(g+b);
            		    d = min(d, dot(r,r));
                		
            		    b.z = 0.0;
            		    r = b - p + hash33(g+b);
            		    d = min(d, dot(r,r));
                		
            		    b.z = 1.;
            		    r = b - p + hash33(g+b);
            		    d = min(d, dot(r,r));
                			
            	    }
            	}
            	
            	return d; // Range: [0, 1]
            }

            float noiseLayers(in vec3 p) {
                vec3 t = vec3(0., 0., 0.);

                const int iter = 5; // Just five layers is enough.
                float tot = 0.08, sum = 0., amp = 0.5; // Total, sum, amplitude.

                for (int i = 0; i < iter; i++) {
                    tot += voronoi(p + t) * amp; // Add the layer to the total.
                    p *= 2.0; // Position multiplied by two.
                    t *= 1.5; // Time multiplied by less than two.
                    sum += amp; // Sum of amplitudes.
                    amp *= 0.5; // Decrease successive layer amplitude, as normal.
                }
                
                return tot/sum; // Range: [0, 1].
            }
            //=================================================================


            vec4 main(in vec2 fragcoord)
            {
                vec2 uv = (vec2(fragcoord.x/resolution.x, fragcoord.y/resolution.y));
                vec2 uv2 = (vec2(fragcoord.x/resolution.x, fragcoord.y/resolution.y));
                vec2 uv3 = (vec2(fragcoord.x/resolution.x, fragcoord.y/resolution.y));
                vec2 uv4 = (vec2(fragcoord.x/resolution.x, fragcoord.y/resolution.y));
                
                uv *= vec2(55., 95.); 
            	vec3 rd = normalize(vec3(uv.x, uv.y, 3.1415926535898/8.));
            	float c = voronoi(rd*120.);
                vec3 col =  vec3(1.0, 1.0, 1.0) - (c * vec3(2.0,2.0,2.0));
            
                uv2 *= vec2(16., 32.);
            	vec3 rd2 = normalize(vec3(uv2.x, uv2.y, 3.1415926535898/8.));
            	float c2 = voronoi(rd2*30.);
                vec3 col2 =  vec3(1.0, 1.0, 1.0) - (c2 * vec3(2.0,2.0,2.0));
                
                uv4 *= vec2(16., 32.);
                vec3 rd4 = normalize(vec3(uv4.x, uv4.y, 3.1415926535898/8.));
                float c4 = voronoi(rd4*30.);
                vec3 col4 =  (c4 * vec3(2.0,2.0,2.0));
                
                uv3 *= vec2(3., 15.);
                vec3 rd3 = normalize(vec3(uv3.x, uv3.y, 3.1415926535898/8.));
                float c3 = voronoi(rd3*130.8);
                vec3 col3 =  vec3(1.0, 1.0, 1.0) - (c3 * vec3(2.0,2.0,2.0));
                vec3 max = vec3(1.0,1.0,1.0);
            	vec4 finalColor = vec4((2*col2 - col3 + (col * (col2-col3)) + 2*col4) * darkerBrownColor.xyz, 1.);
            
                vec4 black = vec4(0.0,0.0,0.0,1.0);
                float2 uvMap = fragcoord/resolution.xy;
            
                float mixValue = distance(uvMap, vec2(0.1,0.39));
                //return mix(finalColor, black, mixValue);
                return finalColor;
            }
    """.trimIndent()

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SampleScreen() {

    val context = LocalContext.current
    val localConfig = LocalConfiguration.current
    val density = LocalDensity.current
    val lifecycleOwner = context as ViewModelStoreOwner
    val viewModel: SampleViewModel = ViewModelProvider(lifecycleOwner)[SampleViewModel::class.java]

    val screenHeight = with (density) { localConfig.screenHeightDp.dp.toPx() }
    val screenHeightDp = localConfig.screenHeightDp.dp
    val screenWidth = with (density) { localConfig.screenWidthDp.dp.toPx() }
    val screenWidthDp = localConfig.screenWidthDp.dp

    var isFlyButtonEnabled by remember { mutableStateOf(true) }

    val points = floatArrayOf(    radialToCartesian(1f, 270f.toRadians()).x,
        radialToCartesian(1f, 270f.toRadians()).y,
        radialToCartesian(1f, 30f.toRadians()).x,
        radialToCartesian(1f, 30f.toRadians()).y,
        radialToCartesian(0.1f, 90f.toRadians()).x,
        radialToCartesian(0.1f, 90f.toRadians()).y,
        radialToCartesian(1f, 150f.toRadians()).x,
        radialToCartesian(1f, 150f.toRadians()).y)

    val birdP1 = RoundedPolygon(points, CornerRounding(0.05f, 0.0f), centerX = 0f, centerY = 0f)
    val birdP2 = RoundedPolygon.star(
        numVerticesPerRadius = 4,
        innerRadius = 0.6f,
        rounding = CornerRounding(0.1f),
        innerRounding = CornerRounding(0.2f)
    )

    val treeTrunkP1 = RoundedPolygon.pill()
    val treeTrunkP2 = RoundedPolygon.star(
        numVerticesPerRadius = 5,
        innerRadius = 0.01f,
        //rounding = CornerRounding(0.05f),
        //innerRounding = CornerRounding(0.05f)
    )

    val dodecagonPoly = remember {
        RoundedPolygon(
            12,
            rounding = CornerRounding(0.2f)
        )
    }

    val trianglePoly = remember {
        RoundedPolygon.star(
            3,
            innerRadius = 0.4f,
            rounding = CornerRounding(0.2f)
        )
    }

    val hexagonStarPoly = remember {
        RoundedPolygon.star(
            numVerticesPerRadius = 7,
            innerRadius = 0.4f,
            //rounding = CornerRounding(0.5f),
            innerRounding = CornerRounding(0.5f)
        )
    }

    val dodecagonStarPoly = remember {
        RoundedPolygon.star(
            numVerticesPerRadius = 12,
            rounding = CornerRounding(0.2f)
        )
    }

    val octagonPoly = remember {
        RoundedPolygon(
            8,
            rounding = CornerRounding(0.2f)
        )
    }

    val pillStarMultipleVertsPoly = remember {
        RoundedPolygon.pillStar(
            rounding = CornerRounding(0f),
            numVerticesPerRadius = 200
        )
    }

    val pillStar = remember {
        RoundedPolygon.pillStar(
            rounding = CornerRounding(0.2f),
            numVerticesPerRadius = 10
        )
    }

    val pillStarMultipleVertsPoly2 = remember {
        RoundedPolygon.pillStar(
            rounding = CornerRounding(0f),
            numVerticesPerRadius = 80
        )
    }

    val circularMorph = remember {
        Morph(dodecagonPoly, dodecagonStarPoly)
    }

    val groundGrassMorph = remember {
        Morph(octagonPoly, pillStarMultipleVertsPoly)
    }

    val cloudMorph = remember {
        Morph(octagonPoly, pillStar)
    }

    val engineMorph = remember {
        Morph(hexagonStarPoly, trianglePoly)
    }

    val birdMorph = remember {
        Morph(birdP1, birdP2)
    }

    val treeTrunkMorph = remember {
        Morph(treeTrunkP1, treeTrunkP2)
    }

    val infiniteTransition = rememberInfiniteTransition("infinite nature movement")

    val natureProgress = infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "animatedMorphProgress2"
    )

    val groundGrassProgress = infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "animatedMorphProgress2"
    )

    val animatedProgressB1 = infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "animatedMorphProgress3"
    )


    val animatedProgressB2 = infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "animatedMorphProgress3"
    )



    Box(modifier = Modifier.fillMaxSize()) {
        for (i in 1..40) {
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = Random.nextInt(0, screenWidthDp.toPx().toInt()),
                            y = Random.nextInt(0, (screenHeightDp.toPx().toInt() / 3))
                        )
                    }
                    .clip(
                        CustomRotatingMorphShape(
                            circularMorph,
                            1f,
                            0f
                        )
                    )
                    .padding(0.dp)
                    .background(Color.Yellow)
                    .size(7.dp / Random.nextInt(1, 3))
            ) {
            }
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .offset(150.dp, 0.dp)
                .size(width = 120.dp, height = 100.dp)
                //.background(Color.Yellow.copy(0.3f))
                .drawWithCache {
                    onDrawBehind {
                        val path = CustomRotatingMorphShape(
                            groundGrassMorph,
                            natureProgress.value,
                            0f
                        ).getPath()

                        scale(
                            scale = size.height,
                            pivot = Offset(
                                size.width / 2,
                                size.height / 2
                            )
                        ) {
                            translate(
                                left = size.width / 2,
                                top = size.height / 2
                            ) {
                                /*drawRect(
                                    size = size / 2f,
                                    topLeft = Offset(0f,0f),
                                    color = Color.Red
                                )*/
                                drawPath(
                                    path = path,
                                    color = Color.Cyan.copy(0.1f)
                                )
                            }
                        }
                    }
                }
        ) {}

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .offset(0.dp, 0.dp)
                .size(width = 150.dp, height = 130.dp)
                //.background(Color.Yellow.copy(0.3f))
                .drawWithCache {
                    onDrawBehind {
                        val path = CustomRotatingMorphShape(
                            groundGrassMorph,
                            natureProgress.value,
                            0f
                        ).getPath()

                        scale(
                            scale = size.height,
                            pivot = Offset(
                                size.width / 2,
                                size.height / 2
                            )
                        ) {
                            translate(
                                left = size.width / 2,
                                top = size.height / 2
                            ) {
                                /*drawRect(
                                    size = size / 2f,
                                    topLeft = Offset(0f,0f),
                                    color = Color.Red
                                )*/
                                drawPath(
                                    path = path,
                                    color = Color.Cyan.copy(0.1f)
                                )
                            }
                        }
                    }
                }
        ) {}

        if (screenHeight > screenWidth) {
            RealtimeBox(
                animationFlow = MutableStateFlow<StateHolder<*, *>?>(null),
                initialOffset = Offset(screenWidth / 1.55f, screenHeight / 2.7f)
            ) {
                Box(
                    Modifier
                        .size(screenWidthDp / 2.5f)
                        .drawWithCache {
                            val shader = RuntimeShader(SHADER_TREE)
                            val shaderBrush = ShaderBrush(shader)
                            shader.setFloatUniform("resolution", size.width, size.height)
                            shader.setColorUniform(
                                "brownColor",
                                android.graphics.Color.valueOf(
                                    Brown.red,
                                    Brown.green,
                                    Brown.blue,
                                    1f
                                )
                            )

                            shader.setColorUniform(
                                "darkerBrownColor",
                                android.graphics.Color.valueOf(
                                    DarkBrown.red,
                                    DarkBrown.green,
                                    DarkBrown.blue,
                                    1f
                                )
                            )
                            onDrawBehind {
                                val path = CustomRotatingMorphShape(
                                    treeTrunkMorph,
                                    0.5f,
                                    0f
                                ).getPath()

                                val pivot = Offset(
                                    size.width / 2,
                                    size.height / 2
                                )
                                rotate(degrees = 180f, pivot = pivot) {
                                    scale(
                                        scale = size.height,
                                        pivot = pivot
                                    ) {
                                        translate(
                                            left = size.width / 2,
                                            top = size.height / 2
                                        ) {
                                            //rotate(degrees = 45f, pivot = pivot) {
                                            drawPath(
                                                path = path,
                                                brush = shaderBrush,

                                                )
                                            //}
                                        }
                                    }
                                }
                            }
                        }
                        //.offset(1500.dp, 70.dp)
                        //.background(Color.Red)
                        .padding(0.dp)
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .offset(screenWidthDp / 1.5f, screenHeightDp / 3.7f)
                    .size(width = 100.dp, height = 80.dp)
                    //.background(Color.Yellow.copy(0.3f))
                    .drawWithCache {
                        onDrawBehind {
                            val path = CustomRotatingMorphShape(
                                cloudMorph,
                                natureProgress.value,
                                0f
                            ).getPath()

                            scale(
                                scale = size.height,
                                pivot = Offset(
                                    size.width / 2,
                                    size.height / 2
                                )
                            ) {
                                translate(
                                    left = size.width / 2,
                                    top = size.height / 2
                                ) {
                                    /*drawRect(
                                        size = size / 2f,
                                        topLeft = Offset(0f,0f),
                                        color = Color.Red
                                    )*/
                                    drawPath(
                                        path = path,
                                        color = DarkGreen.copy(0.85f)
                                    )
                                }
                            }
                        }
                    }
            ) {}

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .offset(screenWidthDp / 3.5f, screenHeightDp / 2.55f)
                    .size(width = 100.dp, height = 80.dp)
                    //.background(Color.Yellow.copy(0.3f))
                    .drawWithCache {
                        onDrawBehind {
                            val path = CustomRotatingMorphShape(
                                cloudMorph,
                                natureProgress.value,
                                0f
                            ).getPath()

                            rotate(
                                degrees = 65f,
                                pivot = Offset(
                                    size.width / 2,
                                    size.height / 2
                                )
                            ) {
                                scale(
                                    scale = size.height,
                                    pivot = Offset(
                                        size.width / 2,
                                        size.height / 2
                                    )
                                ) {
                                    translate(
                                        left = size.width / 2,
                                        top = size.height / 2
                                    ) {
                                        /*drawRect(
                                            size = size / 2f,
                                            topLeft = Offset(0f,0f),
                                            color = Color.Red
                                        )*/
                                        drawPath(
                                            path = path,
                                            color = DarkGreen.copy(0.85f)
                                        )
                                    }
                                }
                            }
                        }
                    }
            ) {}

        }

        val initialOffsetBig = remember { Offset(screenWidth / 1.55f, screenHeight / 1.75f) }
        val initialRotationBig = remember { -45f }

        RealtimeBox(
            animationFlow = viewModel.animationEmitter.getTransformedFlow2(),
            initialOffset = initialOffsetBig,
            initialRotation = initialRotationBig,
            isStartedCallback = {
                viewModel.animationTimer.startTimer();
            },
            isStoppedCallback = {
                isFlyButtonEnabled = true
                viewModel.animationTimer.pauseTimer()
            }
        ) {

            Box(
                Modifier
                    .size(40.dp)
                    .drawWithCache {
                        onDrawBehind {
                            val path = CustomRotatingMorphShape(
                                birdMorph,
                                animatedProgressB1.value,
                                0f
                            ).getPath()

                            scale(
                                scale = size.height,
                                pivot = Offset(
                                    size.width / 2,
                                    size.height / 2
                                )
                            ) {
                                translate(
                                    left = size.width / 2,
                                    top = size.height / 2
                                ) {
                                    drawPath(
                                        path = path,
                                        color = Color.Black.copy(0.8f)
                                    )
                                }
                            }
                        }
                    }
                    //.offset(1500.dp, 70.dp)
                    //.background(Color.Red)
                    .padding(0.dp)
            )
            /*Image(
               painter = painterResource(R.drawable.car),
               contentDescription = "bird",
               modifier = Modifier.size(100.dp)
            )*/
            //}
        }

        val initialOffsetSmall = remember { Offset(screenWidth / 1.25f, screenHeight / 1.85f) }
        val initialRotationSmall = remember { 0f }

        RealtimeBox(
            animationFlow = viewModel.animationEmitter.getTransformedFlow(),
            initialOffset = initialOffsetSmall,
            initialRotation = initialRotationSmall,
            isStartedCallback = {
                viewModel.animationTimer.startTimer()
            },
            isStoppedCallback = {
                isFlyButtonEnabled = true
                viewModel.animationTimer.pauseTimer()
            }
        ) {

            Box(
                Modifier
                    .size(30.dp)
                    .drawWithCache {
                        onDrawBehind {
                            val path = CustomRotatingMorphShape(
                                birdMorph,
                                animatedProgressB2.value,
                                0f
                            ).getPath()

                            scale(
                                scale = size.height,
                                pivot = Offset(
                                    size.width / 2,
                                    size.height / 2
                                )
                            ) {
                                translate(
                                    left = size.width / 2,
                                    top = size.height / 2
                                ) {
                                    drawPath(
                                        path = path,
                                        color = Color.Black.copy(0.8f)
                                    )
                                }
                            }
                        }
                    }
                    //.offset(1500.dp, 70.dp)
                    //.background(Color.Red)
                    .padding(0.dp)
            )
            /*Image(
               painter = painterResource(R.drawable.car),
               contentDescription = "bird",
               modifier = Modifier.size(100.dp)
            )*/
            //}
        }

        if (screenWidth < screenHeight) {
            Column(modifier = Modifier.align(Alignment.BottomCenter)) {
                Box(
                    Modifier
                        .size(width = screenWidthDp, height = 100.dp)
                        .offset(0.dp, screenWidthDp / 4)
                        .drawWithCache {
                            onDrawBehind {
                                val path = CustomRotatingMorphShape(
                                    groundGrassMorph,
                                    groundGrassProgress.value,
                                    0f
                                ).getPath()

                                scale(
                                    scale = size.width,
                                    pivot = Offset(
                                        size.width / 2,
                                        size.height / 2
                                    )
                                ) {
                                    translate(
                                        left = size.width / 1.9995f,
                                        top = size.height / 1.993f
                                    ) {
                                        /*drawRect(
                                            size = size / 2f,
                                            topLeft = Offset(0f,0f),
                                            color = Color.Red
                                        )*/
                                        drawPath(
                                            path = path,
                                            color = Color.Green.copy(0.3f)
                                        )
                                    }
                                }
                            }
                        }
                        //.background(Color.Green)
                        .padding(0.dp)
                ) {
                    SideEffect { println("Internal box") }
                }
            }
        }

        val leftColor = remember { NightPurple }
        val rightColor = remember { NightRed }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.End
        ) {
            OutlinedButton(
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                border = BorderStroke( width = 1.dp, color = PinkOrange ),
                modifier = Modifier
                    .padding(top = 20.dp, end = 20.dp)
                    .height(ButtonDefaults.MinHeight),
                onClick = {
                    context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
                }
            ) {
                Text(
                    text = "Licenses",
                    color = Pink80,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        AnimatorComponent(
            initialOffsetSmall,
            initialRotationSmall,
            initialOffsetBig,
            initialRotationBig,
            screenHeight,
            screenWidth,
            viewModel,
            isFlyButtonEnabled,
            { isFlyButtonEnabled = false }
        )
    }
}

@Composable
fun AnimatorComponent(
    initialOffsetSmallBat: Offset,
    initialRotationSmallBat: Float,
    initialOffsetBigBat: Offset,
    initialRotationBigBat: Float,
    screenHeight: Float,
    screenWidth: Float,
    viewModel: SampleViewModel,
    isFlyButtonEnabled: Boolean,
    flyButtonCallback: () -> Unit
) {
    var identifier by remember { mutableLongStateOf(0L) }
    var identifier2 by remember { mutableLongStateOf(1L) }

    val traj: MutableList<StateHolder<*, *>> = remember { mutableListOf() }
    val traj2: MutableList<StateHolder<*, *>> = remember { mutableListOf() }

    val offsetAnimatable = remember { Animatable(
        initialValue = initialOffsetSmallBat,
        typeConverter = Offset.VectorConverter) }

    val rotationAnimatable = remember { Animatable(
        initialValue = initialRotationSmallBat,
        typeConverter = Float.VectorConverter) }

    val startRotation = remember {
        StateHolder<Float, AnimationVector1D>(
            id = identifier,
            state = Start(
                visualDescriptor = VisualDescriptor(
                    currentValue = initialRotationSmallBat,
                    animationType = AnimationType.ROTATION,
                    animationSpec = tween(
                        durationMillis = 1000,
                        easing = LinearEasing
                    ),
                    animatable = rotationAnimatable,
                    isAnimated = true,
                    durationMillis = 1000
                )
            ),
            animationType = AnimationType.ROTATION
        )
    }

    var startOffset = remember {
        StateHolder<Offset, AnimationVector2D>(
            id = identifier,
            state = Start(
                visualDescriptor = VisualDescriptor(
                    currentValue = initialOffsetSmallBat,
                    animationType = AnimationType.OFFSET,
                    animationSpec = tween(
                        durationMillis = 1000,
                        easing = LinearEasing
                    ),
                    animatable = offsetAnimatable,
                    isAnimated = true,
                    durationMillis = 1000
                )
            ),
            animationType = AnimationType.OFFSET,
            wrappedStateHolders = listOf(
                startRotation
            )
        )
    }

    val offsetAnimatable2 = remember { Animatable(
        initialValue = initialOffsetBigBat,
        typeConverter = Offset.VectorConverter) }

    val rotationAnimatable2 = remember { Animatable(
        initialValue = initialRotationBigBat,
        typeConverter = Float.VectorConverter) }

    val startRotation2 = remember {
        StateHolder<Float, AnimationVector1D>(
            id = identifier,
            state = Start(
                visualDescriptor = VisualDescriptor(
                    currentValue = initialRotationBigBat,
                    animationType = AnimationType.ROTATION,
                    animationSpec = tween(
                        durationMillis = 1000,
                        easing = LinearEasing
                    ),
                    animatable = rotationAnimatable2,
                    isAnimated = true,
                    durationMillis = 1000
                )
            ),
            animationType = AnimationType.ROTATION
        )
    }

    var startOffset2 = remember {
        StateHolder<Offset, AnimationVector2D>(
            id = identifier,
            state = Start(
                visualDescriptor = VisualDescriptor(
                    currentValue = initialOffsetBigBat,
                    animationType = AnimationType.OFFSET,
                    animationSpec = tween(
                        durationMillis = 1000,
                        easing = LinearEasing
                    ),
                    animatable = offsetAnimatable2,
                    isAnimated = true,
                    durationMillis = 1000
                )
            ),
            animationType = AnimationType.OFFSET,
            wrappedStateHolders = listOf(
                startRotation2
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 40.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var duration by remember { mutableStateOf(10) }

        Timer(viewModel.animationTimer, Pink80)
        Row {
            IconButton(onClick = { if (duration > 0) duration-- }, colors = IconButtonDefaults.iconButtonColors(contentColor = Pink80)) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "decrement")
            }
            Text(text = "$duration", color = Pink80, modifier = Modifier.padding(top = 15.dp))
            IconButton(onClick = { if (duration < 10000) duration++ }, colors = IconButtonDefaults.iconButtonColors(contentColor = Pink80)) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "increment")
            }
        }
        var leftColor = remember { NightPurple }
        var rightColor = remember { NightRed }
        if (!isFlyButtonEnabled) {
            leftColor = remember { LightBlue }
            rightColor = remember { LightPink }
        }
        OutlinedButton(
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
            enabled = isFlyButtonEnabled,
            border = BorderStroke( width = 1.dp, color = PinkOrange ),
            modifier = Modifier
                .padding(top = 10.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            leftColor,
                            rightColor,
                        )
                    ), shape = ButtonDefaults.shape
                )
                .height(ButtonDefaults.MinHeight),
            onClick = {
                flyButtonCallback()

                traj.clear()
                traj2.clear()

                traj.add(startOffset)
                traj2.add(startOffset2)

                val path = generateRandomPath(
                    initialOffset = initialOffsetSmallBat,
                    initialRotation = initialRotationSmallBat,
                    maxScreenHeight = screenHeight,
                    maxScreenWidth = screenWidth,
                    numOffsets = duration - 1
                )
                path.add(Pair(initialOffsetSmallBat, initialRotationSmallBat))

                val path2 = generateRandomPath(
                    initialOffset = initialOffsetBigBat,
                    initialRotation = initialRotationBigBat,
                    maxScreenHeight = screenHeight,
                    maxScreenWidth = screenWidth,
                    numOffsets = duration - 1
                )
                path2.add(Pair(initialOffsetBigBat, initialRotationBigBat))

                /*startOffset =
                    StateHolder<Offset, AnimationVector2D>(
                        id = identifier,
                        state = Start(
                            visualDescriptor = VisualDescriptor(
                                currentValue = path[path.size - 1].first,
                                animationType = AnimationType.OFFSET,
                                animationSpec = tween(
                                    durationMillis = 1000,
                                    easing = LinearEasing
                                ),
                                animatable = offsetAnimatable,
                                isAnimated = true,
                                durationMillis = 1000
                            )
                        ),
                        animationType = AnimationType.OFFSET,
                        wrappedStateHolders = listOf(
                            startRotation
                        )
                    )*/

                path.toList().forEach { (currOffset, currRotation) ->

                    val rotationStateHolder = StateHolder<Float, AnimationVector>(
                        id = identifier,
                        state = State.Animated(
                            animation = Animation(
                                animationSpec = tween(
                                    durationMillis = 1000,
                                    easing = LinearEasing
                                ),
                                targetValue = currRotation,
                                durationMillis = 1000
                            )
                        ),
                        animationType = AnimationType.ROTATION
                    )

                    traj.add(
                        StateHolder<Offset, AnimationVector>(
                            id = identifier,
                            state = State.Animated(
                                animation = Animation(
                                    animationSpec = tween(
                                        durationMillis = 1000,
                                        easing = LinearEasing
                                    ),
                                    targetValue = currOffset,
                                    durationMillis = 1000
                                )
                            ),
                            animationType = AnimationType.OFFSET,
                            wrappedStateHolders = listOf(
                                rotationStateHolder
                            )
                        )
                    )
                }
                traj.add(
                    StateHolder(
                        id = identifier,
                        state = State.Pause,
                        animationType = AnimationType.OFFSET,
                        wrappedStateHolders = listOf(StateHolder(
                            id = identifier,
                            state = State.Pause,
                            animationType = AnimationType.ROTATION
                        ))
                    )
                )

                path2.toList().forEach { (currOffset, currRotation) ->

                    val rotationStateHolder = StateHolder<Float, AnimationVector>(
                        id = identifier2,
                        state = State.Animated(
                            animation = Animation(
                                animationSpec = tween(
                                    durationMillis = 1000,
                                    easing = LinearEasing
                                ),
                                targetValue = currRotation,
                                durationMillis = 1000
                            )
                        ),
                        animationType = AnimationType.ROTATION
                    )

                    traj2.add(
                        StateHolder<Offset, AnimationVector>(
                            id = identifier2,
                            state = State.Animated(
                                animation = Animation(
                                    animationSpec = tween(
                                        durationMillis = 1000,
                                        easing = LinearEasing
                                    ),
                                    targetValue = currOffset,
                                    durationMillis = 1000
                                )
                            ),
                            animationType = AnimationType.OFFSET,
                            wrappedStateHolders = listOf(
                                rotationStateHolder
                            )
                        )
                    )
                }
                traj2.add(
                    StateHolder(
                        id = identifier2,
                        state = State.Pause,
                        animationType = AnimationType.OFFSET,
                        wrappedStateHolders = listOf(StateHolder(
                            id = identifier2,
                            state = State.Pause,
                            animationType = AnimationType.ROTATION
                        ))
                    )
                )

                viewModel.animationEmitter.emitTrajectory(traj.toList())
                viewModel.animationEmitter.emitTrajectory2(traj2.toList())
            } ) {
            Text(
                text = "Fly around",
                color = Pink80,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun Timer(timer: SampleViewModel.Timer, color: Color) {
    val timerValue by timer.timer.collectAsStateWithLifecycle()
    Text(
        modifier = Modifier.padding(top = 10.dp),
        text = "Timer: ${timerValue.toInt()}",
        color = color,
        fontSize = 14.sp)
}

internal fun Float.toRadians() = this * PI.toFloat() / 180f

internal val PointZero = PointF(0f, 0f)

internal fun radialToCartesian(
    radius: Float,
    angleRadians: Float,
    center: PointF = PointZero
) = directionVectorPointF(angleRadians) * radius + center

internal fun directionVectorPointF(angleRadians: Float) =
    PointF(cos(angleRadians), sin(angleRadians))

fun calculateOffsets(maxScreenWidth: Float, maxScreenHeight: Float, numOffsets: Int): List<Offset> {
    val offsets = mutableListOf<Offset>()
    for (i in 1..numOffsets) {
        val xi: Float = (maxScreenWidth / numOffsets) * i
        val yi: Float = (maxScreenHeight / numOffsets) * i
        offsets.add(Offset(xi, yi))
    }
    return offsets
}

private fun generateRandomOffset(maxScreenWidth: Float, maxScreenHeight: Float): Offset {
    var xi = Random.nextInt(0, maxScreenWidth.toInt())
    var yi = Random.nextInt(0, maxScreenHeight.toInt())

    return Offset(xi.toFloat(), yi.toFloat())
}

private fun generateRandomPath(
    initialOffset: Offset,
    initialRotation: Float,
    maxScreenWidth: Float,
    maxScreenHeight: Float,
    numOffsets: Int
): MutableList<Pair<Offset, Float>> {
    val result = mutableListOf<Pair<Offset, Float>>()

    var nextOffset = initialOffset


    for (i in 1..numOffsets) {

        if (i == 1) {

            nextOffset = generateRandomOffset(maxScreenWidth = maxScreenWidth, maxScreenHeight = maxScreenHeight)

            val currentRotation = atan2(nextOffset.y - initialOffset.y, nextOffset.x - initialOffset.x) * (180 / Math.PI).toFloat()
            result.add(initialOffset to currentRotation)
        }

        else {
            val currentOffset = nextOffset
            nextOffset = generateRandomOffset(maxScreenWidth = maxScreenWidth, maxScreenHeight = maxScreenHeight)
            val currentRotation = (atan2(nextOffset.y - currentOffset.y, nextOffset.x - currentOffset.x) * (180 / Math.PI).toFloat()) - 180
            result.add(currentOffset to currentRotation)
        }
    }

    return result
}

class CustomRotatingMorphShape(
    private val morph: Morph,
    private val percentage: Float,
    private val rotation: Float,
    private val heightToWidthScaleFactor: Float = 1F,
) : androidx.compose.ui.graphics.Shape {

    private val matrix = Matrix()
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        // Below assumes that you haven't changed the default radius of 1f, nor the centerX and centerY of 0f
        // By default this stretches the path to the size of the container, if you don't want stretching, use the same size.width for both x and y.
        matrix.scale(size.width / 2f, size.height / 2f)
        matrix.translate(1f, 1f)
        //matrix.rotateZ(rotation)

        val path = morph.toPath(progress = percentage).asComposePath()
        path.transform(matrix)

        return Outline.Generic(path)
    }

    fun getPath() = morph.toPath(progress = percentage).asComposePath()
}