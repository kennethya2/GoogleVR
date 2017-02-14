package com.leafplain.excercise.googlevr.util;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by kennethyeh on 2017/2/14.
 */

public class AssetStreamUtil {
    AssetManager assetManager ;
    InputStream inputStream = null;
    String returnStr="";

    public AssetStreamUtil(Context mContext){
        assetManager = mContext.getAssets();
    };

    public String getAssetString(String fileName) {
        try
        {
            inputStream = assetManager.open(fileName);
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            byte[] bytes = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(bytes)) > 0){
                byteStream.write(bytes, 0, len);}
            returnStr = new String(byteStream.toByteArray(), "UTF-8");
        }
        catch (IOException e){returnStr="";}
        finally
        {
            if (inputStream != null)
                try {
                    inputStream.close();
                    inputStream = null;
                }
                catch (IOException e) {}
        }
        return returnStr;
    }
}