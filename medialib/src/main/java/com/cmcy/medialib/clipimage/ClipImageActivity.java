package com.cmcy.medialib.clipimage;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cmcy.medialib.R;
import com.cmcy.medialib.utils.ImageUtils;
import com.cmcy.medialib.utils.Utils;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * 裁剪图片的Activity
 */
public class ClipImageActivity extends Activity implements View.OnClickListener {

	public static String TEMP_FILE_PATH = "TEMP_FILE_PATH";
	public static String OUTPUT_PATH = "OUTPUT_PATH";

	private ClipImageLayout mClipImageLayout = null;
	private ProgressDialog loadingDialog;
	private Disposable observeOnDataChange;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Utils.setStatusBarColor(this, 0XFF505153);
		setContentView(R.layout.activity_media_clip_image);
		initView();
	}

	protected void initView()
	{
		TextView tv_back = (TextView) findViewById(R.id.btn_back);
		Button tvUse = (Button)findViewById(R.id.commit);
		mClipImageLayout = (ClipImageLayout) findViewById(R.id.clipImageLayout);

		tvUse.setOnClickListener(this);
		tv_back.setOnClickListener(this);

		String path = getIntent().getStringExtra(TEMP_FILE_PATH);
		loadingDialog = new ProgressDialog(this);

		// 有的系统返回的图片是旋转了，有的没有旋转，所以处理
		int degreee = ImageUtils.readPictureDegree(path);
		Bitmap bitmap = ImageUtils.getBitmap(path);
		if (bitmap != null) {
			if (degreee == 0) {
				mClipImageLayout.setImageBitmap(bitmap);
			} else {
				mClipImageLayout.setImageBitmap(ImageUtils.rotaingImageView(degreee, bitmap));
			}
		} else {
			finish();
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.commit) {
			loadingDialog.setMessage("正在剪切...");
			loadingDialog.show();

			Bitmap bitmap = mClipImageLayout.clip();

			observeOnDataChange = Observable.just(bitmap).subscribeOn(Schedulers.io()).map(bitmap1 -> {

				File cacheDir = ClipImageActivity.this.getCacheDir();

				String output_path = cacheDir.getAbsolutePath() + System.currentTimeMillis() + ".jpg";

				ImageUtils.savePhotoToSDCard(bitmap1,output_path);
				File file = ImageUtils.compress(output_path);
				return file != null ? file.getAbsolutePath() : "";
			}).observeOn(AndroidSchedulers.mainThread()).subscribe(filePath -> {

				loadingDialog.dismiss();
				Intent intent = new Intent();
				intent.putExtra(OUTPUT_PATH, filePath);
				setResult(RESULT_OK, intent);

				finish();
			});
		}else if(v.getId() == R.id.btn_back){
			finish();
		}
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		Utils.dispose(observeOnDataChange);
	}


}
