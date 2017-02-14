package com.leafplain.excercise.googlevr;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.vr.sdk.widgets.pano.VrPanoramaEventListener;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;
import com.leafplain.excercise.googlevr.util.AssetStreamUtil;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.util.List;

public class ActivityVRPhoto extends AppCompatActivity {

    private static final String TAG = ActivityVRPhoto.class.getSimpleName();

    private Context mContext = null;
    private RecyclerView recyclerView;
    private List<PhotoItemInfo> images;
    
    /** Actual panorama widget. **/
    private VrPanoramaView panoWidgetView;
    /**
     * Arbitrary variable to track load status. In this example, this variable should only be accessed
     * on the UI thread. In a real app, this variable would be code that performs some UI actions when
     * the panorama is fully loaded.
     */
    public boolean loadImageSuccessful;
//    /** Tracks the file to be loaded across the lifetime of this app. **/
//    private Uri fileUri;
//    /** Configuration information for the panorama. **/
//    private VrPanoramaView.Options panoOptions = new VrPanoramaView.Options();
//    private ImageLoaderTask backgroundImageLoaderTask;

    private static final int  FILE_FROM_LOCAL    = 0;
    private static final int  FILE_FROM_URL      = 1;
//    class PhotoInfo{
//        public int formType = FILE_FROM_LOCAL;
//        public String filePath  = "" ;
//        public VrPanoramaView.Options options = new VrPanoramaView.Options();
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vrphoto);
        mContext = this;

        // ImageLoader 初始設定
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.mipmap.ic_launcher)
                .showImageForEmptyUri(R.mipmap.ic_launcher)
                .showImageOnFail(R.mipmap.ic_launcher).cacheInMemory(true)
                .cacheOnDisk(true).imageScaleType(ImageScaleType.EXACTLY)
//				.displayer(new RoundedBitmapDisplayer(20))
                .bitmapConfig(Bitmap.Config.RGB_565).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
//				.memoryCacheExtraOptions(480, 800)
                .memoryCacheExtraOptions(720, 1028)
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                //.memoryCacheSizePercentage(13) // default
                //.diskCacheSize(50 * 1024 * 1024) // 缓冲大小
                .diskCacheFileCount(100) // 缓冲文件数目
                .defaultDisplayImageOptions(options)
                //.writeDebugLogs()
                .build();

        imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);

        panoWidgetView = (VrPanoramaView) findViewById(R.id.pano_view);
        panoWidgetView.setEventListener(new ActivityEventListener());

//        PhotoInfo photoInfo = new PhotoInfo();
//        VrPanoramaView.Options panoOptions = new VrPanoramaView.Options();
//
//        /*
//        * Below Photo File From Assets
//        * */
////        photoInfo.formType = FILE_FROM_LOCAL;
////        photoInfo.filePath = "girl_2.jpg";
//////        panoOptions.inputType = VrPanoramaView.Options.TYPE_STEREO_OVER_UNDER;
////        panoOptions.inputType = VrPanoramaView.Options.TYPE_MONO;
////        photoInfo.options = panoOptions;
//
//        /*
//        * Below Photo File From URL
//        * */
//        photoInfo.formType = FILE_FROM_URL;
//        photoInfo.filePath = "http://www.roadtovr.com/wp-content/uploads/2014/09/Venice.Still001.jpeg";
////        panoOptions.inputType = VrPanoramaView.Options.TYPE_STEREO_OVER_UNDER;
//        panoOptions.inputType = VrPanoramaView.Options.TYPE_MONO;
//        photoInfo.options = panoOptions;

//        startLoadPhoto(photoInfo);

        recyclerView  = (RecyclerView) findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        parsingData();
    }

    public DisplayImageOptions options;
    public ImageLoader imageLoader ;


    private void parsingData(){
        Log.d(TAG,"parsingData");
        AssetStreamUtil asuObj;
        asuObj = new AssetStreamUtil(mContext);
        String data = asuObj.getAssetString("image.json");
        try {
            PhotoList mPhotoList;
            StringReader jsonSR  = new StringReader(data);
            GsonBuilder gsonb = new GsonBuilder();
            Gson gson = gsonb.create();

            JsonReader reader = new JsonReader(jsonSR);
            reader.setLenient(true);
            mPhotoList = gson.fromJson(reader, PhotoList.class);
            images = mPhotoList.images;

            PhotoItemInfo mPhotoItemInfo = images.get(0);
            startLoadPhoto(mPhotoItemInfo);

            RecyclerViewAdapter mRecyclerViewAdapter = new RecyclerViewAdapter(images);
            recyclerView.setAdapter(mRecyclerViewAdapter);

        } catch (Exception e) {
            Log.d(TAG,"parsingData Exception:"+e.toString());
        }
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private  List<PhotoItemInfo> images;

        public RecyclerViewAdapter(List<PhotoItemInfo> images){
            this.images   = images;
        }

        @Override
        public int getItemCount() {
            return images.size();
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
            PhotoItemInfo mPhotoItemInfo = images.get(position);
            HolderListItem holderListItem  = (HolderListItem) viewHolder;
            holderListItem.titleTV.setText(mPhotoItemInfo.title);
            holderListItem.clickView.setTag(mPhotoItemInfo);
            holderListItem.clickView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PhotoItemInfo mPhotoItemInfo = (PhotoItemInfo) view.getTag();
                    startLoadPhoto(mPhotoItemInfo);

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

    public static class PhotoList implements Serializable {
        public List<PhotoItemInfo> images ;
    }

    public static class PhotoItemInfo implements Serializable {
        public String title = "";
        public String filePath = "";
        public String image = "";
        public String desc = "";
        public int formType ;
        public int optionsType ;
    }

    private void startLoadPhoto(PhotoItemInfo photoInfo){
        final VrPanoramaView.Options panoOptions = new VrPanoramaView.Options();
        panoOptions.inputType   = photoInfo.optionsType;
        int formType            = photoInfo.formType;
        final String filePath = photoInfo.filePath;
        Log.i(TAG, "formType: " + photoInfo.formType);
        Log.i(TAG, "filePath: " + photoInfo.filePath);
        InputStream istr = null;

        if(formType == FILE_FROM_LOCAL){
            AssetManager assetManager = getAssets();
            try {
                istr = assetManager.open(filePath);
                panoWidgetView.loadImageFromBitmap(BitmapFactory.decodeStream(istr), panoOptions);
            } catch (IOException e) {
                Log.e(TAG, "Could not decode default bitmap: " + e);
            }finally {
                if(istr != null){
                    try {
                        istr.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Could not close input stream: " + e);
                    }
                }
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
    }

    @Override
    protected void onPause() {
        panoWidgetView.pauseRendering();
        super.onPause();
        Log.d(TAG,"onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        panoWidgetView.resumeRendering();
        Log.d(TAG,"onResume");
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG,"onDestroy");
        // Destroy the widget and free memory.
        panoWidgetView.shutdown();
        super.onDestroy();
    }

    /**
     * Listen to the important events from widget.
     */
    private class ActivityEventListener extends VrPanoramaEventListener {
        /**
         * Called by pano widget on the UI thread when it's done loading the image.
         */
        @Override
        public void onLoadSuccess() {
            Log.i(TAG, "load Image Successful");
            loadImageSuccessful = true;
        }

        /**
         * Called by pano widget on the UI thread on any asynchronous error.
         */
        @Override
        public void onLoadError(String errorMessage) {
            loadImageSuccessful = false;
            Toast.makeText(
                    ActivityVRPhoto.this, "Error loading pano: " + errorMessage, Toast.LENGTH_LONG)
                    .show();
            Log.e(TAG, "Error loading pano: " + errorMessage);
        }
    }
}
