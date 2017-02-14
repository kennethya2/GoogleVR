package com.leafplain.excercise.googlevr;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.vr.sdk.widgets.video.VrVideoEventListener;
import com.google.vr.sdk.widgets.video.VrVideoView;
import com.leafplain.excercise.googlevr.util.VideoTimeToRead;

import java.io.IOException;

import static com.leafplain.excercise.googlevr.R.drawable.play;

public class ActivityVRVideo extends AppCompatActivity {

    private static final String TAG = ActivityVRVideo.class.getSimpleName();

    /**
     * Preserve the video's state when rotating the phone.
     */
    private static final String STATE_IS_PAUSED = "isPaused";
    private static final String STATE_PROGRESS_TIME = "progressTime";
    /**
     * The video duration doesn't need to be preserved, but it is saved in this example. This allows
     * the seekBar to be configured during {@link #onRestoreInstanceState(Bundle)} rather than waiting
     * for the video to be reloaded and analyzed. This avoid UI jank.
     */
    private static final String STATE_VIDEO_DURATION = "videoDuration";

    /**
     * Arbitrary constants and variable to track load status. In this example, this variable should
     * only be accessed on the UI thread. In a real app, this variable would be code that performs
     * some UI actions when the video is fully loaded.
     */
    public static final int LOAD_VIDEO_STATUS_UNKNOWN = 0;
    public static final int LOAD_VIDEO_STATUS_SUCCESS = 1;
    public static final int LOAD_VIDEO_STATUS_ERROR = 2;

    private int loadVideoStatus = LOAD_VIDEO_STATUS_UNKNOWN;

    /** Tracks the file to be loaded across the lifetime of this app. **/
    private Uri fileUri;

    /** Configuration information for the video. **/
    private VrVideoView.Options videoOptions = new VrVideoView.Options();

//    private VideoLoaderTask backgroundVideoLoaderTask;

    /**
     * The video view and its custom UI elements.
     */
    protected VrVideoView videoWidgetView;

    private TextView currentTimeTV;
    private TextView durationTimeTV;

    /**
     * Seeking UI & progress indicator. The seekBar's progress value represents milliseconds in the
     * video.
     */
    private SeekBar seekBar;
    private TextView statusText;

    private ImageButton stopBTN;
    private ImageButton playBTN;
    private ImageButton volumeToggle;
    private boolean isMuted;

    /**
     * By default, the video will start playing as soon as it is loaded. This can be changed by using
     * {@link VrVideoView#pauseVideo()} after loading the video.
     */
    private boolean isPaused = false;

    private static final int  FILE_FROM_LOCAL    = 0;
    private static final int  FILE_FROM_URL      = 1;
    class VideoInfo{
        public int formType = FILE_FROM_LOCAL;
        public String filePath  = "" ;
        public VrVideoView.Options options = new VrVideoView.Options();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vrvideo);

        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(new SeekBarListener());
        statusText = (TextView) findViewById(R.id.status_text);


        // Bind input and output objects for the view.
        videoWidgetView = (VrVideoView) findViewById(R.id.video_view);
        videoWidgetView.setEventListener(new ActivityEventListener());

        currentTimeTV   = (TextView) findViewById(R.id.currentTimeTV);
        durationTimeTV  = (TextView) findViewById(R.id.durationTimeTV);

        stopBTN = (ImageButton) findViewById(R.id.stopBTN);
        stopBTN.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stop();
            }
        });

        playBTN = (ImageButton) findViewById(R.id.playBTN);
        playBTN.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                togglePause();
            }
        });

        volumeToggle = (ImageButton) findViewById(R.id.volume_toggle);
        volumeToggle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setIsMuted(!isMuted);
            }
        });

        loadVideoStatus = LOAD_VIDEO_STATUS_UNKNOWN;

        VideoInfo videoInfo = new VideoInfo();
        VrVideoView.Options options = new VrVideoView.Options();

        /*
        * Below Video File From Assets
        * */
//        videoInfo.formType = FILE_FROM_LOCAL;
//        videoInfo.filePath = "congo.mp4";
//        options.inputType = VrVideoView.Options.TYPE_STEREO_OVER_UNDER;
////      options.inputType = VrVideoView.Options.TYPE_MONO;
//        videoInfo.options = options;

        /*
        * Below Video File From URL
        * */
        videoInfo.formType = FILE_FROM_URL;
        videoInfo.filePath ="https://s3-ap-northeast-1.amazonaws.com/vr-resource/video/360%C2%B0+Icebergs+of+Greenland.+Part+I.+4%D0%9A+aerial+video.mp4";
        options.inputType = VrVideoView.Options.TYPE_MONO;
        videoInfo.options = options;

        startLoadVideo(videoInfo);
    }

    private void startLoadVideo(VideoInfo videoInfo){
        try {
            int formType    = videoInfo.formType;
            String filePath = videoInfo.filePath;
            Log.i(TAG, "formType: " + videoInfo.formType);
            Log.i(TAG, "filePath: " + videoInfo.filePath);
            if(formType == FILE_FROM_LOCAL){
                videoWidgetView.loadVideoFromAsset(filePath, videoInfo.options);
            }
            if(formType == FILE_FROM_URL){
                Uri uri =  Uri.parse(filePath);
                videoWidgetView.loadVideo(uri, videoInfo.options);
            }

        } catch (IOException e) {
            // An error here is normally due to being unable to locate the file.
            loadVideoStatus = LOAD_VIDEO_STATUS_ERROR;
            // Since this is a background thread, we need to switch to the main thread to show a toast.
            videoWidgetView.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ActivityVRVideo.this, "Error opening file. ", Toast.LENGTH_LONG).show();
                }
            });
            Log.e(TAG, "Could not open video: " + e);
        }
    }

    public int getLoadVideoStatus() {
        return loadVideoStatus;
    }

    private void setIsMuted(boolean isMuted) {
        this.isMuted = isMuted;
        volumeToggle.setImageResource(isMuted ? R.drawable.volume_off : R.drawable.volume_on);
        videoWidgetView.setVolume(isMuted ? 0.0f : 1.0f);
    }

    public boolean isMuted() {
        return isMuted;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putLong(STATE_PROGRESS_TIME, videoWidgetView.getCurrentPosition());
        savedInstanceState.putLong(STATE_VIDEO_DURATION, videoWidgetView.getDuration());
        savedInstanceState.putBoolean(STATE_IS_PAUSED, isPaused);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        long progressTime = savedInstanceState.getLong(STATE_PROGRESS_TIME);
        videoWidgetView.seekTo(progressTime);
        seekBar.setMax((int) savedInstanceState.getLong(STATE_VIDEO_DURATION));
        seekBar.setProgress((int) progressTime);

        isPaused = savedInstanceState.getBoolean(STATE_IS_PAUSED);
        if (isPaused) {
            videoWidgetView.pauseVideo();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG,"onResume");
        // Prevent the view from rendering continuously when in the background.
        videoWidgetView.pauseRendering();
        // If the video is playing when onPause() is called, the default behavior will be to pause
        // the video and keep it paused when onResume() is called.
        isPaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
        // Resume the 3D rendering.
        videoWidgetView.resumeRendering();
        // Update the text to account for the paused video in onPause().
        updateStatusText();
    }

    @Override
    protected void onDestroy() {
        // Destroy the widget and free memory.
        Log.d(TAG,"onDestroy");
        videoWidgetView.shutdown();
        super.onDestroy();
    }

    private void togglePause() {
        if (isPaused) {
            Log.d(TAG,"playVideo");
            videoWidgetView.playVideo();
            playBTN.setImageResource(R.drawable.pause);
        } else {
            Log.d(TAG,"pauseVideo");
            videoWidgetView.pauseVideo();
            playBTN.setImageResource(R.drawable.play);
        }
        isPaused = !isPaused;
        updateStatusText();
    }

    private void updateStatusText() {
//        StringBuilder status = new StringBuilder();
//        status.append(isPaused ? "Paused: " : "Playing: ");
//        status.append(String.format("%.2f", videoWidgetView.getCurrentPosition() / 1000f));
//        status.append(" / ");
//        status.append(videoWidgetView.getDuration() / 1000f);
//        status.append(" seconds.");
//        statusText.setText(status.toString());

        currentTimeTV.setText(VideoTimeToRead.getTimeStr(videoWidgetView.getCurrentPosition() / 1000));
        durationTimeTV.setText(VideoTimeToRead.getTimeStr(videoWidgetView.getDuration() / 1000));
    }

    private void stop(){
        videoWidgetView.pauseVideo();
        isPaused = true;
        videoWidgetView.seekTo(0);
        playBTN.setImageResource(play);
    }

    /**
     * When the user manipulates the seek bar, update the video position.
     */
    private class SeekBarListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                videoWidgetView.seekTo(progress);
                updateStatusText();
            } // else this was from the ActivityEventHandler.onNewFrame()'s seekBar.setProgress update.
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) { }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) { }
    }

    /**
     * Listen to the important events from widget.
     */
    private class ActivityEventListener extends VrVideoEventListener {
        /**
         * Called by video widget on the UI thread when it's done loading the video.
         */
        @Override
        public void onLoadSuccess() {
            Log.i(TAG, "Sucessfully loaded video " + videoWidgetView.getDuration());
            loadVideoStatus = LOAD_VIDEO_STATUS_SUCCESS;
            seekBar.setMax((int) videoWidgetView.getDuration());
            updateStatusText();
            playBTN.setImageResource(R.drawable.pause);
            isPaused = false;
        }

        /**
         * Called by video widget on the UI thread on any asynchronous error.
         */
        @Override
        public void onLoadError(String errorMessage) {
            // An error here is normally due to being unable to decode the video format.
            loadVideoStatus = LOAD_VIDEO_STATUS_ERROR;
            Toast.makeText(
                    ActivityVRVideo.this, "Error loading video: " + errorMessage, Toast.LENGTH_LONG)
                    .show();
            Log.e(TAG, "Error loading video: " + errorMessage);
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
            updateStatusText();
            seekBar.setProgress((int) videoWidgetView.getCurrentPosition());
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
}
