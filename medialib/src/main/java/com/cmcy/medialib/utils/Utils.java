package com.cmcy.medialib.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Window;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.reactivex.disposables.Disposable;

/**
  * Description : 工具类集合
  * ClassName : Utils
  * Author : Cybing
  * Date : 2020/6/2 10:09
 */
public class Utils {

    private static Map<String, Bitmap> mediaVideoPic = new HashMap<>();

    /**
     * 修改状态栏颜色
     * @param colorId 颜色
     */
    public static void setStatusBarColor(Activity activity, int colorId) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.setStatusBarColor(colorId);
//            if(colorId != 0){
//                // 如果亮色，设置状态栏文字为黑色
//                if (SystemBarTintManager.isLightColor(colorId)) {
//                    window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
//                } else {
//                    window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
//                }
//            }
        }
    }


    public static String timeFormat(long timeMillis, String pattern){
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.CHINA);
        return format.format(new Date(timeMillis));
    }

    public static String formatPhotoDate(long time){
        return timeFormat(time, "yyyy-MM-dd");
    }

    public static String formatPhotoDate(String path){
        File file = new File(path);
        if(file.exists()){
            long time = file.lastModified();
            return formatPhotoDate(time);
        }
        return "1970-01-01";
    }


    /**
     * 文件是否存在
     * @param path
     * @return
     */
    public static boolean exists(String path) {
        return new File(path).exists();
    }

    /**
     * 创建临时文件
     * @param context
     * @param fileFormat
     * @return
     */
    public static File createTmpFile(Context context, String fileFormat){

        String state = Environment.getExternalStorageState();
        File tmpFile = null;
        if(state.equals(Environment.MEDIA_MOUNTED)){
            // 已挂载
            File pic = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
            String fileName = "multi_image_"+timeStamp+"";
            tmpFile = new File(pic, fileName + fileFormat);

        }else{
            File cacheDir = context.getCacheDir();
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
            String fileName = "multi_image_"+timeStamp+"";
            tmpFile = new File(cacheDir, fileName + fileFormat);
            return tmpFile;
        }

        if (!tmpFile.exists()) {
            try
            {
                tmpFile.createNewFile();
            } catch (IOException e)
            {
                e.printStackTrace();
                return null;
            }
        }

        return tmpFile;
    }


    /**
     * 是否是http开头
     */
    public static boolean isHttpHead(String url)
    {
        return !TextUtils.isEmpty(url) && (url.toLowerCase().startsWith("http://") || url.toLowerCase().startsWith("https://"));
    }


    /**
     * 根据视频url获取第一帧图像
     * @param url
     * @return
     */
    public static Bitmap getVideoUrlBitmap(String url)
    {
        Bitmap bitmap = mediaVideoPic.get(url);

        try
        {
            if(bitmap == null)
            {
                // 获取预览图
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();

                if (isHttpHead(url)) {
                    mmr.setDataSource(url, new HashMap<String, String>());
                } else
                {
                    mmr.setDataSource(url);
                }

                Bitmap previewBitmap = mmr.getFrameAtTime();

                // 缩放
                int PREVIEW_VIDEO_IMAGE_HEIGHT = 300; // Pixels
                int videoWidth = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                int videoHeight = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
                int videoViewWidth = PREVIEW_VIDEO_IMAGE_HEIGHT * videoWidth / videoHeight;
                if(videoViewWidth == 0)return null;
//                bitmap = Bitmap.createScaledBitmap(previewBitmap, videoViewWidth, PREVIEW_VIDEO_IMAGE_HEIGHT, true);
                bitmap = Bitmap.createBitmap(previewBitmap);
                mediaVideoPic.put(url, bitmap);
                mmr.release();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return bitmap;
    }



    /**
     * RecyclerView GridLayoutManager item数量根据屏幕大小来显示
     * @param context
     * @param columnWidthDp
     * @return
     */
    public static int calculateNoOfColumns(Context context, float columnWidthDp) { // For example columnWidthdp=180
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (screenWidthDp / columnWidthDp + 0.5); // +0.5 for correct rounding to int.
        return noOfColumns;
    }





    public static void dispose(Disposable disposable) {
        if (disposable != null && !disposable.isDisposed())
            disposable.dispose();
    }
}
