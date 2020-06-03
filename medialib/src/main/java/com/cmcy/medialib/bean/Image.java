package com.cmcy.medialib.bean;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
  * Description : 图片实体
  * ClassName : Image
  * Author : Cybing
  * Date : 2020/6/2 11:47
 */
public class Image implements Serializable
{
    public String path;
    public String name;
    public long time;
    public int id;
    public String thumbPath;
    public Bitmap bitmap;

    public Image(String path, String name, long time, String thumbPath, int id){
        this.path = path;
        this.name = name;
        this.time = time;
        this.thumbPath = thumbPath;
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        try {
            Image other = (Image) o;
            return this.path.equalsIgnoreCase(other.path);
        }catch (ClassCastException e){
            e.printStackTrace();
        }
        return super.equals(o);
    }
}
