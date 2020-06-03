package com.cmcy.medialib.bean;

import java.util.List;

/**
  * Description : 文件夹
  * ClassName : Folder
  * Author : Cybing
  * Date : 2020/6/2 11:47
 */
public class Folder {
    public String name;
    public String path;
    public Image cover;
    public List<Image> images;

    @Override
    public boolean equals(Object o) {
        try {
            Folder other = (Folder) o;
            return this.path.equalsIgnoreCase(other.path);
        }catch (ClassCastException e){
            e.printStackTrace();
        }
        return super.equals(o);
    }
}
