package com.cmcy.medialib;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cmcy.medialib.utils.Utils;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.io.File;

import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
  * Description : 图片/视频预览
  * ClassName : PhotoPreviewActivity
  * Author : Cybing
  * Date : 2020/6/2 11:46
 */
public class PhotoPreviewActivity extends Activity
{
	private SubsamplingScaleImageView imageViewPreview;
	private JzvdStd videoplayer;
	private String url = "";
	private int type = 1;// 1图片，2视频
	private Disposable observeOnDataChange;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Utils.setStatusBarColor(this, 0xff000000);
		setContentView(R.layout.activity_media_photo_preview);
		initView();
	}

	protected void initView()
	{
		imageViewPreview = (SubsamplingScaleImageView) findViewById(R.id.imageViewPreview);
		videoplayer = (JzvdStd) findViewById(R.id.videoplayer);
		ImageView button_back = (ImageView) findViewById(R.id.button_back);

		url = getIntent().getStringExtra("url");
		type = getIntent().getIntExtra("type", 1);

		if(type == 1)
		{
			imageViewPreview.setVisibility(View.VISIBLE);
			videoplayer.setVisibility(View.GONE);

			Glide.with(this).load(url+"")
					.downloadOnly(new SimpleTarget<File>() {
				@Override
				public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
					imageViewPreview.setImage(ImageSource.uri(Uri.fromFile(resource)));
				}});
		}
		else
		{
			imageViewPreview.setVisibility(View.GONE);
			videoplayer.setVisibility(View.VISIBLE);

			videoplayer.posterImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			videoplayer.setUp(url, "", JzvdStd.SCREEN_NORMAL);
			videoplayer.setVisibility(View.VISIBLE);

			observeOnDataChange = Observable.just(url).subscribeOn(Schedulers.io()).map(Utils::getVideoUrlBitmap)
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(bitmap -> videoplayer.posterImageView.setImageBitmap(bitmap));

		}

		button_back.setOnClickListener(v -> {
			finish();
		});

	}


	@Override
	public void onBackPressed() {
		if (Jzvd.backPress()) {
			return;
		}
		super.onBackPressed();
	}
	@Override
	protected void onPause() {
		super.onPause();
		Jzvd.releaseAllVideos();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Utils.dispose(observeOnDataChange);
	}
}
