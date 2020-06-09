package com.cmcy.medialib.presenter;


import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.cmcy.medialib.bean.Folder;
import com.cmcy.medialib.bean.Image;
import com.cmcy.medialib.utils.MediaSelector;
import com.cmcy.medialib.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
  * Description : 本地图片/视频Load
  * ClassName : MediaPresenter
  * Author : Cybing
  * Date : 2020/6/2 15:21
 */
public class MediaPresenter implements MediaContract.MediaModel
{
    private Context mContext;

    private MediaContract.MediaView mediaView;

    private boolean hasFolderLoad = false;

    // 文件夹数据
    private ArrayList<Folder> mResultFolder = new ArrayList<>();

    public MediaPresenter(Activity context, MediaContract.MediaView mediaView){
        this.mContext = context;
        this.mediaView = mediaView;
    }

    @Override
    public void loadMediaList() {
        ((FragmentActivity)mContext).getSupportLoaderManager().initLoader(mediaView.getLoaderModel(), null, mLoaderCallback);
    }

    @Override
    public void loadVideoThumb(List<Image> imageList) {
        if(mediaView.getMediaType() == MediaSelector.VIDEO){
            Observable<Image> listObservable = Observable.fromIterable(imageList);
            listObservable.subscribeOn(Schedulers.io())
                    .map(image -> {
                        Bitmap bitmap = Utils.getVideoUrlBitmap(image.path);
                        image.bitmap = bitmap;
                        return bitmap != null;
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext(isFinished -> {
                        if (isFinished) {
                            mediaView.notifyVideoThumb();
                        }
                    })
                    .subscribe();

        }
    }


    //图片/视频加载器
    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

        private final String[] IMAGE_PROJECTION = {
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Thumbnails._ID };

        private final String[] VIDEO_PROJECTION = {
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Thumbnails._ID };

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            if(id == mediaView.getLoaderModel()) {
                CursorLoader cursorLoader = null;

                switch (mediaView.getMediaType())
                {
                    case MediaSelector.PICTURE://图片

                        cursorLoader = new CursorLoader(mContext,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                                null, null, IMAGE_PROJECTION[2] + " DESC");
                        break;

                    case MediaSelector.VIDEO://视频
                        cursorLoader = new CursorLoader(mContext,
                                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null,
                                null, null, VIDEO_PROJECTION[2] + " DESC");
                        break;
                }
                return cursorLoader;
            }

            return null;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data != null) {
                final List<Image> images = new ArrayList<Image>();
                int count = data.getCount();
                if (count > 0) {
                    data.moveToFirst();
                    String[] currentPro = IMAGE_PROJECTION;
                    switch (mediaView.getMediaType())
                    {
                        case MediaSelector.PICTURE://图片
                            currentPro = IMAGE_PROJECTION;
                            break;
                        case MediaSelector.VIDEO://视频
                            currentPro = VIDEO_PROJECTION;
                            break;
                    }

                    do{
                        String path = data.getString(data.getColumnIndexOrThrow(currentPro[0]));
                        String name = data.getString(data.getColumnIndexOrThrow(currentPro[1]));
                        long dateTime = data.getLong(data.getColumnIndexOrThrow(currentPro[2]));
                        int id = data.getInt(data.getColumnIndexOrThrow(currentPro[3]));

                        Image image = new Image(path, name, dateTime, "", id);

                        images.add(image);

                        if(!hasFolderLoad) {
                            // 获取文件夹名称
                            File imageFile = new File(path);
                            File folderFile = imageFile.getParentFile();
                            Folder folder = new Folder();
                            folder.name = folderFile.getName();
                            folder.path = folderFile.getAbsolutePath();
                            folder.cover = image;
                            if (!mResultFolder.contains(folder)) {
                                List<Image> imageList = new ArrayList<Image>();
                                imageList.add(image);
                                folder.images = imageList;
                                mResultFolder.add(folder);
                            } else {
                                // 更新
                                Folder f = mResultFolder.get(mResultFolder.indexOf(folder));
                                f.images.add(image);
                            }
                        }

                    }while(data.moveToNext());
                    hasFolderLoad = true;

                    mediaView.showMediaList(images);
                    mediaView.showFolderList(mResultFolder);
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };

}

