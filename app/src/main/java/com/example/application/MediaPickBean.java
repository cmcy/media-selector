package com.example.application;

import android.graphics.Bitmap;


/**
  * Description : 图片选择
  * ClassName : MediaPickBean
  * Author : Cybing
  * Date : 2020/5/21 11:12
 */
public class MediaPickBean {

    private String uri;
    private String url;
    private Bitmap bitmap;

    private long totalLength;
    private long currentLength;

    private int homeworkState;

    public int getHomeworkState() {
        return homeworkState;
    }

    public void setHomeworkState(int homeworkState) {
        this.homeworkState = homeworkState;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public Bitmap getBitmap()
    {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap)
    {
        this.bitmap = bitmap;
    }

    public long getTotalLength()
    {
        return totalLength;
    }

    public void setTotalLength(long totalLength)
    {
        this.totalLength = totalLength;
    }

    public long getCurrentLength()
    {
        return currentLength;
    }

    public void setCurrentLength(long currentLength)
    {
        this.currentLength = currentLength;
    }
}
