package com.leafplain.excercise.googlevr;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.leafplain.excercise.googlevr.util.AssetStreamUtil;

import java.io.Serializable;
import java.io.StringReader;
import java.util.List;

public class VideoListActivity extends AppCompatActivity {

    public static final String TAG = "VideoListActivity";
    private Context mContext = null;
    private RecyclerView recyclerView;
    private List<VideoInfo> videos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);
        mContext = this;

        recyclerView  = (RecyclerView) findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        parsingData();
    }

    private void parsingData(){
        Log.d(TAG,"parsingData");
        AssetStreamUtil asuObj;
        asuObj = new AssetStreamUtil(mContext);
        String data = asuObj.getAssetString("video.json");
        try {
            VideoList mVideoList;
            StringReader jsonSR  = new StringReader(data);
            GsonBuilder gsonb = new GsonBuilder();
            Gson gson = gsonb.create();

            JsonReader reader = new JsonReader(jsonSR);
            reader.setLenient(true);
            mVideoList = gson.fromJson(reader, VideoList.class);
            videos = mVideoList.videos;


            RecyclerViewAdapter mRecyclerViewAdapter = new RecyclerViewAdapter(videos);
            recyclerView.setAdapter(mRecyclerViewAdapter);

        } catch (Exception e) {
            Log.d(TAG,"parsingURLData Exception:"+e.toString());
        }
    }


    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private  List<VideoInfo> videos;

        public RecyclerViewAdapter(List<VideoInfo> videos){
            this.videos   = videos;
        }

        @Override
        public int getItemCount() {
            return videos.size();
        }

        @Override
        public int getItemViewType(int position) {
            return 1;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            RecyclerView.ViewHolder viewHolder = null;
            LayoutInflater mInflater = LayoutInflater.from(parent.getContext());
            View vHolderEvent = mInflater.inflate(R.layout.item_cell, parent, false);
            viewHolder = new HolderListItem(vHolderEvent);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            VideoInfo mVideoInfo = videos.get(position);
            HolderListItem holderListItem  = (HolderListItem) viewHolder;
            holderListItem.titleTV.setText(mVideoInfo.title);
            holderListItem.clickView.setTag(mVideoInfo);
            holderListItem.clickView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    VideoInfo mVideoInfo = (VideoInfo) view.getTag();

                    Intent intentVrVideo = new Intent();
                    intentVrVideo.putExtra("data", mVideoInfo);
                    intentVrVideo.setClass(mContext, ActivityVRVideo.class);
                    mContext.startActivity(intentVrVideo);
                }
            });
        }

        public void destroy(){
        }
    }

    public class HolderListItem extends RecyclerView.ViewHolder{
        public View clickView;
        public TextView titleTV;

        public HolderListItem(View itemView) {
            super(itemView);
            clickView  = itemView.findViewById(R.id.clickView);
            titleTV    = (TextView) itemView.findViewById(R.id.titleTV);
        }
    }

    public static class VideoList implements Serializable {
        public List<VideoInfo> videos ;
    }

    public static class VideoInfo implements Serializable {
        public String title = "";
        public String filePath = "";
        public String image = "";
        public String desc = "";
        public int formType ;
        public int optionsType ;
    }

}
