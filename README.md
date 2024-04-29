<p align="center"><img src="https://github.com/CodeBoy722/MediaFacer_Kotlin/blob/master/media/mediafacer_logo.png" width="150"></p>
<h1 align="center"><b>MediaFacer</b></h1>
<h4 align="center">Mediafacer is an Android library that leverages the Android MediaStore Apis primarily for quick retrieval of media content (Video, Audio, Images) from the Mediastore with support for both internal and external storage Medium and built-in pagination.</h4>

[![](https://jitpack.io/v/CodeBoy722/MediaFacer_Kotlin.svg)](https://jitpack.io/#CodeBoy722/MediaFacer_Kotlin) [![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)

<p align="center">
    <a href="https://apkpure.com/mediafacer/com.codeboy.mediafacerkotlin"><img src="https://github.com/CodeBoy722/MediaFacer_Kotlin/blob/master/media/google-play-badge.png" width="170"></a>
</p>

## Screenshots

<p align="center">
    <img src="https://github.com/CodeBoy722/MediaFacer_Kotlin/blob/master/media/audios_query.png" width=200>
    <img src="https://github.com/CodeBoy722/MediaFacer_Kotlin/blob/master/media/images_query.png" width=200>
    <img src="https://github.com/CodeBoy722/MediaFacer_Kotlin/blob/master/media/videos_query.png" width=200>
    <img src="https://github.com/CodeBoy722/MediaFacer_Kotlin/blob/master/media/media_picker.png" width=200>
</p>

## Library Installation and Usage

### Add the library to your android project
```gradle
 allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}   
```

Add the dependency in the app-level build.gradle file

```gradle
dependencies {
	   implementation 'com.github.CodeBoy722:MediaFacer_Kotlin:1.2'
	}
```
If your project is a JetPack Compose project, add the below material UI dependency in your app-level build.gradle to avoid Android resource-linking issues 

```gradle
dependencies {
	   implementation 'com.google.android.material:material:1.9.0' // or above 
	}
```

### Permissions

- Storage (`READ_MEDIA_AUDIO`, `READ_MEDIA_VIDEO`, `READ_MEDIA_IMAGES`, `READ_EXTERNAL_STORAGE`) to read audio, video, and image and media files depending on your usecase
- Services (`FOREGROUND_SERVICE`, `WAKE_LOCK`) to keep the music playing even if the app itself is in background

 ### Read Article/Documentation on Medium 
 [MediaFacer: The ultimate tool for building Android media apps quickly (Part 1)](https://medium.com/codex/mediafacer-the-ultimate-tool-for-building-android-media-apps-quickly-part-1-9b6ca154b507?sk=f66510afc25698da489850e180cfbff5)

## License

MIT License

Copyright (c) 2023 Codeboy

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
