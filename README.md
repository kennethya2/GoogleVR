# GoogleVR

參考：

[VR Overview](https://developers.google.com/vr/concepts/vrview)

[VR View in Android](https://developers.google.com/vr/android/samples/vrview)


### MainActivity
----
GoogleVR Exercise 入口

- VR Video

- VR Photo 

<img src="https://raw.githubusercontent.com/kennethya2/GoogleVR/master/markdown/main_activity.png" width="220">

加入dependencies library
<pre><code>
dependencies {
    compile 'com.google.vr:sdk-videowidget:1.10.0'
    compile 'com.google.vr:sdk-panowidget:1.10.0'
}
</code></pre>


### Demo 1. VR Video
----

#### 1. VR Video List

VideoListActivity

<img src="https://raw.githubusercontent.com/kennethya2/GoogleVR/master/markdown/video_list.png" width="220">

- 從本地載入影片列表 video.json from assets
<pre><code>
{
  "videos": [
    {
      "title": "The Walking Dead",
      "filePath": "https://github.com/kennethya2/GoogleVR/raw/master/vr-resource/video/the-walking-dead.mp4",
      "image": "",
      "desc": "",
      "formType":1,
      "optionsType":1

    },
    {
      "title": "Car Racing",
      "filePath": "https://github.com/kennethya2/GoogleVR/raw/master/vr-resource/video/racing.mp4",
      "image": "",
      "desc": "",
      "formType":1,
      "optionsType":1
    }
]
}
</code></pre>

fromType:0 以Asset位置取得影片
 
fromType:1 以url位置取得影片



單一圖像類型：

optionsType:1 VrVideoView.Options.TYPE_MONO

<pre><code>
ppublic static final int TYPE_MONO

Each video frame is a monocular equirectangular panorama.

Each frame image is expected to cover 360 degrees along its horizontal axis.

Constant Value: 1
</code></pre>

兩圖像上下交疊：

optionsType:2 VrVideoView.Options.TYPE_STEREO_OVER_UNDER

<pre><code>
public static final int TYPE_STEREO_OVER_UNDER

Each video frame contains two vertically-stacked equirectangular panoramas. The top part of the frame contains pixels for the left eye, while the bottom part of the frame contains pixels for the right eye.

Constant Value: 2
</code></pre>

[VrVideoView Options 定義](https://developers.google.com/vr/android/reference/com/google/vr/sdk/widgets/video/VrVideoView.Options.html#TYPE_MONO)

#### 2. Click Item To Watch VR Video

ActivityVRVideo

- 綁定元件
<pre><code>
	// Bind input and output objects for the view.
    videoWidgetView = (VrVideoView) findViewById(R.id.video_view);
    videoWidgetView.setEventListener(new ActivityEventListener());
</code></pre>

- 載入VR影片
<pre><code>
if(formType == FILE_FROM_LOCAL){
	videoWidgetView.loadVideoFromAsset(filePath, videoInfo.options);
}
if(formType == FILE_FROM_URL){
	Uri uri =  Uri.parse(filePath);
	videoWidgetView.loadVideo(uri, videoInfo.options);
}
</code></pre>

- 播放監聽事件
<pre><code>
private class ActivityEventListener extends VrVideoEventListener {
        /**
         * Called by video widget on the UI thread when it's done loading the video.
         */
        @Override
        public void onLoadSuccess() {
        	...
        }

        /**
         * Called by video widget on the UI thread on any asynchronous error.
         */
        @Override
        public void onLoadError(String errorMessage) {
        	...
        }

        @Override
        public void onClick() {
            togglePause();
        }

        /**
         * Update the UI every frame.
         */
        @Override
        public void onNewFrame() {
        	...
        }

        /**
         * Make the video play in a loop. This method could also be used to move to the next video in
         * a playlist.
         */
        @Override
        public void onCompletion() {
            videoWidgetView.seekTo(0);
        }
    }
</code></pre>



Portrait Mode

<img src="https://raw.githubusercontent.com/kennethya2/GoogleVR/master/markdown/vr_video1.png" width="220">

Landscape Mode

<img src="https://raw.githubusercontent.com/kennethya2/GoogleVR/master/markdown/vr_video2.png" width="400">


### Demo 2. VR Photo
----

ActivityVRPhoto

- 從本地載入圖片列表 image.json from assets
<pre><code>
{
  "images": [
    {
      "title": "River",
      "filePath": "https://raw.githubusercontent.com/kennethya2/GoogleVR/master/vr-resource/image/river.jpeg",
      "image": "",
      "desc": "",
      "formType":1,
      "optionsType":1
    },
    {
      "title": "Oracle Arena",
      "filePath": "https://raw.githubusercontent.com/kennethya2/GoogleVR/master/vr-resource/image/oakland-arena-panorama.jpg",
      "image": "",
      "desc": "",
      "formType":1,
      "optionsType":1
    }
    ...
  ]
}
</code></pre>

- 載入VR圖片
<pre><code>
if(formType == FILE_FROM_LOCAL){
	AssetManager assetManager = getAssets();
	try {
		istr = assetManager.open(filePath);          
		panoWidgetView.loadImageFromBitmap(BitmapFactory.decodeStream(istr), panoOptions);
    } catch (IOException e) {
                Log.e(TAG, "Could not decode default bitmap: " + e);
    }
 }

if(formType == FILE_FROM_URL){
	imageLoader.loadImage(filePath, new SimpleImageLoadingListener() {
	@Override
		public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
		Log.d(TAG, "bmp==null:" + (loadedImage == null));
		panoWidgetView.loadImageFromBitmap(loadedImage, panoOptions);
		}
	});
}
</code></pre>

Portrait Mode

<img src="https://raw.githubusercontent.com/kennethya2/GoogleVR/master/markdown/vr_vimage1.png" width="220">

Landscape Mode

<img src="https://raw.githubusercontent.com/kennethya2/GoogleVR/master/markdown/vr_vimage2.png" width="400">
