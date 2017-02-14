package com.leafplain.excercise.googlevr;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button mVideoBTN;
    private Button mPhotoBTN;

    private AppCompatActivity mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        mVideoBTN = (Button) findViewById(R.id.videoBTN);
        mVideoBTN.setOnClickListener(clickListener);

        mPhotoBTN = (Button) findViewById(R.id.photoBTN);
        mPhotoBTN.setOnClickListener(clickListener);
    }

    private View.OnClickListener clickListener= new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            int id = view.getId();
            switch (id) {
                case (R.id.videoBTN):
//                    Intent intentVrVideo = new Intent();
//                    intentVrVideo.setClass(mContext, ActivityVRVideo.class);
//                    mContext.startActivity(intentVrVideo);

                    Intent intentVideoList = new Intent();
                    intentVideoList.setClass(mContext, VideoListActivity.class);
                    mContext.startActivity(intentVideoList);

                    break;
                case (R.id.photoBTN):
                    Intent intentVrPhoto = new Intent();
                    intentVrPhoto.setClass(mContext, ActivityVRPhoto.class);
                    mContext.startActivity(intentVrPhoto);
                    break;
            }
        }
    };
}
