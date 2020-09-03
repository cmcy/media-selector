# media-selector

[![](https://jitpack.io/v/cmcy/media-selector.svg)](https://jitpack.io/#cmcy/media-selector)


一个Android本地图片视频选择器，视频支持预览和缩略图。


### 注意事项

- lib中的权限请求用的是github开源的RxPermissions，对源码做了些修改改成了static方式调用 github连接https://github.com/cmcy/RxPermissions
- master分支开发环境为 AS 3.6.3 + Gradle 5.6.4  compileSDK 29 使用的androidx，导入项目报版本错误时，请手动修改为自己的版本。
- 使用Android support的开发者请切换到master-support分支
- 请参考Demo的实现，进行了解本库。可以使用Gradle引入，也可以下载源码进行修改。
- 如有问题，欢迎提出。** 如有问题可在issues留言或者发送到邮箱bxqn1404@163.com。**

## 使用方式
#### 1. 作为module导入
把demo中的medialib作为一个module导入你的工程。


#### 2. gradle依赖

```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    // For developers using AndroidX in their applications
    implementation 'com.github.cmcy:media-selector:#lastVersion'
 
    // For developers using the Android Support Library
    implementation 'com.github.cmcy:media-selector:1.1.1'
}
```


## 具体使用


  如果项目没有配置FileProvider，则需要配置一下

```
        <application
            ...
            android:requestLegacyExternalStorage="true">
        
            <provider
                android:name="androidx.core.content.FileProvider"
                android:authorities="${applicationId}.provider"
                android:exported="false"
                android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/provider_paths"/>
        </provider>
        
        </application>
```

  android 10.0需要在application加上android:requestLegacyExternalStorage="true"



#### 详细使用方式：
```
MediaSelector.get(this)
            .showCamera(true)//默认显示，可以不用设置
            .setSelectMode(MediaSelector.MODE_MULTI)//默认多选
            .setMaxCount(20)//默认最多选择5张，设置单选后此设置无效
            .setMediaType(MediaSelector.PICTURE)//默认选择图片
            .setDefaultList(imageAdapter.getSelect())//默认选中的图片/视频
            .setListener(new MediaSelectorListener())//选择完成的回调, （可以设置回调或者用onActivityResult方式接收）
            .jump();
```

使用回调的方式接收返回图片/视频
```
.setListener(new MediaSelector.MediaSelectorListener() {
    @Override
    public void onMediaResult(List<String> resultList) {
        //选择的图片/视频
    }
})
```

使用onActivityResult方式接收返回图片/视频
```
@Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == MediaSelector.REQUEST_IMAGE && resultCode == RESULT_OK){

            List<String> resultList = data.getStringArrayListExtra(MediaSelector.EXTRA_RESULT);

            Log.e("TAG", "size-->" + resultList.size());
        }
    }
```

## 效果图
<img src="https://github.com/cmcy/media-selector/blob/master/screenshot/Screenshot_20200602_195058_com.example.applicatio.jpg"   width="30%"><img src="https://github.com/cmcy/media-selector/blob/master/screenshot/Screenshot_20200602_195110_com.example.applicatio.jpg"   width="30%"> <img src="https://github.com/cmcy/media-selector/blob/master/screenshot/Screenshot_20200602_195124_com.example.applicatio.jpg"   width="30%">

<img src="https://github.com/cmcy/media-selector/blob/master/screenshot/Screenshot_20200602_195134_com.example.applicatio.jpg"   width="30%"> <img src="https://github.com/cmcy/media-selector/blob/master/screenshot/Screenshot_20200602_195151_com.example.applicatio.jpg"   width="30%">


## CSDN地址
https://blog.csdn.net/u012364659/article/details/106523681



### 具体的使用方式，请参考Demo代码。觉得不错的可以点点star

## License

```
Copyright 2019 sendtion

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
