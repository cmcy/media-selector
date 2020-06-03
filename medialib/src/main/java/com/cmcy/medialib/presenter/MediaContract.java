package com.cmcy.medialib.presenter;

import com.cmcy.medialib.bean.Folder;
import com.cmcy.medialib.bean.Image;

import java.util.List;

/**
  * Description : 本地图片/视频Load
  * ClassName : MediaContract
  * Author : Cybing
  * Date : 2020/6/2 15:03
 */
public interface MediaContract
{

    /***
     * MediaModel
     * @author Cybing on 2020/6/2 15:21
     */
    interface MediaModel
    {
        /**
         * 加载图片/视频列表
         */
        void loadMediaList();

        /**
         * 加载视频缩略图
         */
        void loadVideoThumb(List<Image> imageList);
    }




    /***
     * MediaView
     * @author Cybing on 2020/6/2 15:21
     */
    interface MediaView
    {

        /**
         * 显示图片/视频列表
         */
        void showMediaList(List<Image> imageList);


        /**
         * 显示文件夹列表
         */
        void showFolderList(List<Folder> folderList);

        /**
         * 更新视频缩略图
         */
        void notifyVideoThumb();

        /**
         * 加载图片还是视频
         * @return MediaSelector.PICTURE， MediaSelector.VIDEO
         */
        int getMediaType();

        /**
         * 加载模式
         * @return load all
         */
        int getLoaderModel();
    }


}
