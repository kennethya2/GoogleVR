package com.leafplain.excercise.googlevr.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Created by kennethyeh on 2017/2/14.
 */

public class VideoTimeToRead {

    private static final int ONE_MIN    = 60;
    private static final int ONE_HOUR   = 60*ONE_MIN;
    private static final NumberFormat numberFormat = new DecimalFormat("00");
    public static synchronized String getTimeStr(long secondsTime) {
        String displayTime="";

        if(secondsTime<ONE_MIN){
            displayTime = numberFormat.format(0)+":"+numberFormat.format(secondsTime);
        }else if(secondsTime<ONE_HOUR){
            int min     = (int) (secondsTime/ONE_MIN);
            int second  = (int) (secondsTime%ONE_MIN);
            displayTime = numberFormat.format(min)+":"+numberFormat.format(second);
        }else{
            int hour        = (int) (secondsTime/ONE_HOUR);
            int modHour     = (int) (secondsTime%ONE_HOUR);
            int min         = (int) (modHour/ONE_MIN);
            int second      = (int) (modHour%ONE_MIN);
            displayTime = numberFormat.format(hour)+":"+numberFormat.format(min)+":"+numberFormat.format(second);
        }
        return displayTime;
    }
}
