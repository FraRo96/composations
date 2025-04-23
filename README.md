
# Composations

[![Apache License 2.0](https://img.shields.io/badge/license-Apache%202.0-blue?style=flat-square)](https://choosealicense.com/licenses/apache-2.0/)

*Composations* (composable-realtime-animations) is a lightweight Kotlin/Android library designed to bring your streaming-based animations to life by creating seamless, fluid, reactive animations based on streams of realtime data.




## Showcases



## Why Composations?

In many modern applications, you may find yourself in situations where static animations just won't do. There are times when you need animations that dynamically respond to a stream of real-time data:

- Financial Dashboards: animating fluctuating stock prices or market indices in a visually engaging manner.
- Navigation/maps apps: think of an app that tracks a delivery rider in real-time. As the rider moves along their route, their position is represented by a marker on the map. Using this library, the marker smoothly transitions between GPS coordinates, providing an accurate and engaging visual representation of the rider’s journey.
- IoT Monitoring: Displaying sensor data, like temperature or humidity changes, with fluid visual cues.
- Weather Applications: Transitioning between weather conditions and temperature changes as new data arrives.
## Installation

Install with npm.



- Step 1. Add the JitPack repository to your build file


    Add it in your settings.gradle.kts at the end of repositories:

        dependencyResolutionManagement {
            repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
            repositories {
                mavenCentral()
                maven { url = uri("https://jitpack.io") }
            }
        }

- Step 2. Add the dependency

        dependencies {
            implementation("com.github.FraRo96:composations:latest")
        }


[Library](https://jitpack.io/#fraro96/composations) version:

[![](https://jitpack.io/v/fraro96/composations.svg)](https://jitpack.io/#fraro96/composations)
## How Does It Work

In order for *Composations* to work, you should run:

- a *composable function*, whose offset and/or rotation properties you want to animate in real time.
- a *stream provider*, a service providing streams of offsets and/or rotations values.
- a *UI collected flow* collecting values from the stream provider and updating the UI accordingly (recomposition of the composables).

Supported animations are by now for offset and rotation. Some new properties can be added on further developments.

## API Reference

*Composations* allows you to control and dynamically change the most important parameters of the animation of a composable function. Some special classes have been designed to achieve this.

#### VisualDescriptor
This object sets the animation up and should be created as soon as the first or the second element of the animation is available.

```kotlin
  open class VisualDescriptor<T,V: AnimationVector>(
    var currentValue: T,
    var targetValue: T? = null,
    var durationMillis: Int,
    val animationType: AnimationType,
    var animatable: Animatable<T, V>,
    var animationSpec: AnimationSpec<T>,
    var isAnimated: Boolean
)
```

| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `currentValue` | `Float` or `Offset` (*) | **Required**. The starting value for the animation. |
| `targetValue` | `Float` or `Offset` (*) | **Optional**. The final value for the animation. Absent if static. |
| `durationMillis` | `Int` | **Required**. The duration in millis for the animation to go from `currentValue` to `targetValue`.|
| `animationType` | `AnimationType` | **Required**. The enum for the animation type. Can be `AnimationType.ROTATION` or `AnimationType.OFFSET`. |
| `animatable` | `Animatable` | **Required**. The animatable from `androidx.compose.animation.core`. |
| `animationSpec` | `AnimationSpec` | **Required**. The specs from `androidx.compose.animation.core`. |
| `isAnimated` | `Boolean` | **Required**. Set to true to enable the animation. |

(*) *currently supported animations are for rotation (`Float`) and offset (`Offset`). The type is generic so different animation types can be future supported.*

#### Animation

This object sets the updates of an existing animation based on new values arriving from the stream provider.

```kotlin
class Animation<T>(
  val animationSpec: AnimationSpec<T>,
  val durationMillis: Int,
  val targetValue: T
)
```

| Parameter | Type     | Description                       |
| :-------- | :------- | :-------------------------------- |
| `animationSpec` | `AnimationSpec` | **Required**. The updated specs from `androidx.compose.animation.core`. |
| `durationMillis` | `Int` | **Required**. The new duration in millis for the animation to go from the current value of the animation to `targetValue`.|
| `targetValue` | `Float` or `Offset` (*) | **Required**. The new final value for the animation. |




#### State

This interface wraps the previous objects (`VisualDescriptor`, `Animation`) into a State of the animation.
```kotlin
sealed interface State<T,V: AnimationVector> {
    data class Start<T,V: AnimationVector> (
        val visualDescriptor: VisualDescriptor<T,V>
    ) : State<T,V>
    data class Animated<T> (val animation: Animation<T>) : State<T, AnimationVector>
    data class Fixed<T> (val targetValue: T) : State<T, AnimationVector>
    object Pause : State<Any, AnimationVector>
    data object Stop: State<Any, AnimationVector>
}
```
#### StateHolder

```State``` is further wrapped into a ```StateHolder``` which implements a decorator pattern for multiple animations (rotation **and** offset) at the same time. See the chapter **Multiple Animations Usage Example** for more.

```kotlin
class StateHolder<T,V: AnimationVector>(
    private val state: State<T,V>,
    private val animationType: AnimationType,
    wrappedStateHolders: List<StateHolder<*,*>>? = null,
)
```


#### RealtimeBox

```kotlin
@Composable
fun RealtimeBox(
    animationState: StateHolder<*, *>?,
    initialOffset: Offset,
    initialRotation: Float? = null,
    isStartedCallback: (() -> Unit)? = null,
    isStoppedCallback: (() -> Unit)? = null,
    composableContent: @Composable (() -> Unit)
)
```

An animated realtime composable which serves as a wrapper for the composable function (`composableContent`) you want to animate. Any animation, e.g. changing offset and rotation, of the `RealtimeBox` will effect also your composable function. `RealtimeBox` will animate any composable you provide.

| Parameter | Type     | Description                       |
| :-------- | :------- | :-------------------------------- |
| `animationState` | `StateHolder<*, *>` | **Optional**. The current state of the animation. If null, the box won't do any animations. |
| `initialOffset` | `Offset` | **Required**. Initial offset for the `RealtimeBox` and for the `composableContent`.|
| `initialRotation` | `Float` | **Optional**. Initial rotation for the `RealtimeBox` and for the `composableContent`.|
| `isStartedCallback()` | `(() -> Unit)` | **Optional**. Callback invoked when animation starts. |
| `isStoppedCallback()` | `(() -> Unit)` | **Optional**. Callback invoked when animation stops. |
| `composableContent` | `@Composable (() -> Unit)` | **Required**. Composable function representing any UI component you'd like to animate. Any change in `animationState` of `RealtimeBox` will also affect this. |





## Basic Usage Example


`StateHolder<*,*>` holds the current state of your animation which could either be `Start`, `Animated`, `Pause` or `Stop`.

To start the animation, create a `Start` state:

```kotlin
StateHolder<Offset, AnimationVector2D>(
    state = Start(
        visualDescriptor = VisualDescriptor(
            currentValue = firstOffset, // FIRST OFFSET FROM STREAM PROVIDER
            targetValue = secondOffset, // SECOND OFFSET FROM STREAM PROVIDER, OR NULL
            animationType = AnimationType.OFFSET,
            animationSpec = tween( // could be any animationSpec
                durationMillis = expectedDuration, //eg 1000 (ms)
                easing = LinearEasing
            ),
            animatable = Animatable(
                initialValue = firstOffset,
                typeConverter = Offset.VectorConverter),
            isAnimated = true,
            durationMillis = expectedDuration
        )
    ),
    animationType = AnimationType.OFFSET
)
```

When a new target value for the animation arrives, wrap it into this object:

```kotlin
StateHolder<Offset, AnimationVector>(
    state = State.Animated(
        animation = Animation(
            animationSpec = tween( //could be the same as before, or updated too
                durationMillis = newExpectedDuration,
                easing = LinearEasing
            ),
            targetValue = newOffset, // NEW OFFSET FROM STREAM PROVIDER
            durationMillis = newExpectedDuration
        )
    ),
    animationType = AnimationType.OFFSET
)
```
Then, if you want the animation to stop/pause:
```kotlin
StateHolder(
    state = State.Stop, // or State.Pause
    animationType = AnimationType.OFFSET
)
```

Now what's left is to emit all the states inside a flow of type `StateFlow<StateHolder<*,*>?>` which is safely collected from the UI:

`val uiFlow by animationFlow.collectAsStateWithLifecycle()`

and pass its delegate value as a parameter of a `RealtimeBox`:

```kotlin
RealtimeBox(
    animationState = uiFlow,
    initialOffset = Offset(0f,0f),
    initialRotation = 0f
) {
    // PLACE ANY COMPOSABLE HERE
    Box(
        Modifier
            .size(40.dp)
            .background(Color.Red)
    )
}

```



## Basic Functioning Illustration

When your *stream provider* starts to provide a sequence of offset values, when the first or the second value arrives, you should have an idea of how the animation will be, e.g. the initial and final values, duration, animationSpecs. You can start to setup the animation when the first value arrives, or else you can wait until the second value comes, as this will let you set an animation starting from the first value going towards the second one.

Once the *stream provider* provides us with new target values, we want to stop the current animation at the point where it is, then we want to restart a new animation from that point value, towards the new target value from the stream. This concept of destroying a running animation and recreating a new one starting from the point where the previous was is called *additive animation* and Jetpack Compose is able to do it seamlessly.
## Multiple Animations Usage Example

Use `wrappedStateHolders` of `StateHolder` class to animate multiple properties at the same time. For example, you can wrap a rotation animation state holder inside an offset animation state holder.

For `Start`:

```
    val startRotation = remember {
        StateHolder<Float, AnimationVector1D>(
            state = Start(
                visualDescriptor = VisualDescriptor(
                    currentValue = 0f,
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

    StateHolder<Offset, AnimationVector2D>(
        state = Start(
            visualDescriptor = VisualDescriptor(
                currentValue = Offset(0f,0f),
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

```
and for `Animated`:

```
val rotationStateHolder = StateHolder<Float, AnimationVector>(
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

StateHolder<Offset, AnimationVector>(
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
```
## Usage Examples (Repositories links)

- [Basic usage repo](https://github.com/FraRo96/ComposationsVeryBasicTest.git) - simple squares animating on the screen from viewModel stream.
- [Cute interactive environment in Jetpack Compose](https://github.com/FraRo96/floating-bats-sample-app.git) - A colorful animation using shapes, using viewModel as a source for animation stream.
- [Customizing Mapbox animated markers with Jetpack Compose](https://github.com/FraRo96/MapboxAnimationsSample.git) - Showcases how to smoothly update in realtime a custom marker/annotation on a Mapbox map, using an external source of animation data stream (ie. a file).