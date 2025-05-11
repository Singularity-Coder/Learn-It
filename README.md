![alt text](https://github.com/Singularity-Coder/Instant-Script/blob/main/assets/logo192.png)
# Learn It (🚧 Work-In-Progress 🚧)
Master your memory using spaced repetition!

## Concept
Welcome to "Learn It" App. Master complex topics using spaced repetition. You cannot take your sweet time to understand a topic. The world won't wait for you. The only way to keep up in such scenarios is to memorise the content. Your brain will constantly try to find patterns and make sense of what you remembered. After several revisions you will eventually understand it. This App will help you revise efficiently using spaced repetition technique which is scientifically tested.

As Jon Von Neumann says, "In mathematics, you don't understand things, you just get used to them." Some people think this is a wrong statement but this is a fact. Humans don't see recursion and iteration in their daily lives. These kind of topics will only make sense when you get used to them.

## Screenshots
![alt text](https://github.com/Singularity-Coder/Instant-Script/blob/main/assets/sc1.5.png)
![alt text](https://github.com/Singularity-Coder/Instant-Script/blob/main/assets/sc2.png)
![alt text](https://github.com/Singularity-Coder/Instant-Script/blob/main/assets/sc3.png)
![alt text](https://github.com/Singularity-Coder/Instant-Script/blob/main/assets/sc4.png)
![alt text](https://github.com/Singularity-Coder/Instant-Script/blob/main/assets/sc5.png)
![alt text](https://github.com/Singularity-Coder/Instant-Script/blob/main/assets/sc6.5.png)
![alt text](https://github.com/Singularity-Coder/Instant-Script/blob/main/assets/sc7.png)
![alt text](https://github.com/Singularity-Coder/Instant-Script/blob/main/assets/sc8.5.png)
![alt text](https://github.com/Singularity-Coder/Instant-Script/blob/main/assets/sc9.5.png)

## Tech stack & Open-source libraries
- Minimum SDK level 31
-  [Kotlin](https://kotlinlang.org/) based, [Coroutines](https://github.com/Kotlin/kotlinx.coroutines) + [Flow](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/) for asynchronous.
- Jetpack
  - Lifecycle: Observe Android lifecycles and handle UI states upon the lifecycle changes.
  - ViewModel: Manages UI-related data holder and lifecycle aware. Allows data to survive configuration changes such as screen rotations.
  - DataBinding: Binds UI components in your layouts to data sources in your app using a declarative format rather than programmatically.
  - Room: Constructs Database by providing an abstraction layer over SQLite to allow fluent database access.
  - [Hilt](https://dagger.dev/hilt/): for dependency injection.
  - WorkManager: WorkManager allows you to schedule work to run one-time or repeatedly using flexible scheduling windows.
- Architecture
  - MVVM Architecture (View - DataBinding - ViewModel - Model)
  - Repository Pattern
- [Retrofit2 & OkHttp3](https://github.com/square/retrofit): Construct the REST APIs and paging network data.
- [gson](https://github.com/google/gson): A Java serialization/deserialization library to convert Java Objects into JSON and back.
- [Material-Components](https://github.com/material-components/material-components-android): Material design components for building ripple animation, and CardView.
- [Coil](https://github.com/coil-kt/coil): Image loading for Android and Compose Multiplatform.
- [Lottie](https://github.com/airbnb/lottie-android): Render After Effects animations natively on Android and iOS, Web, and React Native.
- [Balloon](https://github.com/skydoves/Balloon): Modernized and sophisticated tooltips, fully customizable with an arrow and animations for Android.

## Architecture
![alt text](https://github.com/Singularity-Coder/Instant-Script/blob/main/assets/arch.png)

This App is based on the MVVM architecture and the Repository pattern, which follows the [Google's official architecture guidance](https://developer.android.com/topic/architecture).

The overall architecture of this App is composed of two layers; the UI layer and the data layer. Each layer has dedicated components and they have each different responsibilities.