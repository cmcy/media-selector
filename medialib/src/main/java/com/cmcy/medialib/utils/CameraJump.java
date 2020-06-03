package com.cmcy.medialib.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.cmcy.medialib.utils.permission.RxPermissions;

import java.io.File;

import io.reactivex.disposables.Disposable;

/**
  * Description : 跳转到系统相机
  * ClassName : CameraJump
  * Author : Cybing
  * Date : 2020/6/2 11:46
 */
public class CameraJump
{

    public static final int REQUEST_CAMERA = 0x20;

    public static File mTmpFile;//选择相机临时存储

    private static Disposable disposable;

    /**
     * 选择相机
     * @param type 1拍照， 2摄像
     */
    public static void showCameraAction(Activity activity, int type) {
        disposable = RxPermissions.request(activity, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) {
                        startCamera(activity, type);
                    } else {
                        Toast.makeText(activity, "权限拒绝", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    /**
     * 选择相机
     * @param type 1拍照， 2摄像
     */
    public static void showCameraAction(Fragment fragment, int type) {
        disposable = RxPermissions.request(fragment, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) {
                        startCamera(fragment, type);
                    } else {
                        Toast.makeText(fragment.getActivity(), "权限拒绝", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 跳转到系统相机
     * @param activity
     * @param type 1拍照， 2摄像
     */
    private static void startCamera(Activity activity, int type) {
        Intent cameraIntent = getCameraIntent(activity, type);
        if(cameraIntent != null){
            activity.startActivityForResult(cameraIntent, REQUEST_CAMERA);
        }
    }

    /**
     * 跳转到系统相机
     * @param fragment
     * @param type 1拍照， 2摄像
     */
    private static void startCamera(Fragment fragment, int type) {
        Intent cameraIntent = getCameraIntent(fragment.getActivity(), type);
        if(cameraIntent != null){
            fragment.startActivityForResult(cameraIntent, REQUEST_CAMERA);
        }
    }


    private static Intent getCameraIntent(Activity activity, int type){
        if(RxPermissions.lacksPermission(activity, Manifest.permission.CAMERA)) {

            // 跳转到系统照相机

            Intent cameraIntent = new Intent();

            if(type == 2){
                cameraIntent.setAction(MediaStore.ACTION_VIDEO_CAPTURE);
//                cameraIntent.addCategory("android.intent.category.DEFAULT");
            }else {
                cameraIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
            }
            if(cameraIntent.resolveActivity(activity.getPackageManager()) != null){
                // 设置系统相机拍照后的输出路径
                mTmpFile = Utils.createTmpFile(activity, type == 2 ? ".mp4" : ".jpg");
                Uri photoURI = FileProvider.getUriForFile(activity, activity.getApplicationContext().getPackageName() + ".provider", mTmpFile);



                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                return cameraIntent;
            }else{
                Toast.makeText(activity, "没有找到系统的相机", Toast.LENGTH_SHORT).show();
            }
        }

        return null;
    }

}
