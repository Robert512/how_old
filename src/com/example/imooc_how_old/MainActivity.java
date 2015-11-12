package com.example.imooc_how_old;

import org.json.JSONObject;

import com.facepp.error.FaceppParseException;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {

	
	private static final int PICK_CODE = 0x110;
	private ImageView mPhoto;
	private Button mGetImage;
	private Button mDetect;
	private TextView mTip;
	private View mWaiting;
	
	private String mCurrentPhotoStr;
	
	private Bitmap mPhotoImg;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		initViews();
		initEvents();
		
	}

	private void initEvents() {
		mGetImage.setOnClickListener(this);
		mDetect.setOnClickListener(this);
		
	}

	private void initViews() {
		
		mPhoto = (ImageView) findViewById(R.id.id_photo);
		mGetImage = (Button) findViewById(R.id.id_getImage);
		mDetect = (Button) findViewById(R.id.id_detect);
		mTip = (TextView) findViewById(R.id.id_tip);
		mWaiting = findViewById(R.id.id__waiting);
		
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		
		if(requestCode == PICK_CODE)
		{
			if(intent != null)
			{
				//通过游标来获取图片的路径
				Uri uri = intent.getData();
				Cursor cursor = getContentResolver().query(uri,null,null,null,null);
				cursor.moveToFirst();
				
				int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
				mCurrentPhotoStr = cursor.getString(idx);
				cursor.close();
				
				//图片二进制获取的数据不能大于3M，根据照片的尺寸来压缩
				resizePhoto();
				
				mPhoto.setImageBitmap(mPhotoImg);
				mTip.setText("Click Detect ==>");
				
			}
		}
		
		super.onActivityResult(requestCode, resultCode, intent);
	}
	
	private void resizePhoto()
	{
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(mCurrentPhotoStr,options);
		
		double ratio = Math.max(options.outWidth *1.0d /1024f, options.outHeight * 1.0d / 1024f);
		
		options.inSampleSize = (int)Math.ceil(ratio);
		options.inJustDecodeBounds = false;  //加载图片

		mPhotoImg = BitmapFactory.decodeFile(mCurrentPhotoStr, options);
		
	}

	@Override
	public void onClick(View v) {
		switch(v.getId())
		{
		case R.id.id_getImage:
			Intent intent = new Intent(Intent.ACTION_PICK);
			intent.setType("image/*");
			startActivityForResult(intent, PICK_CODE);
		case R.id.id_detect:
			FaceppDetect.detect(mPhotoImg, new FaceppDetect.CallBack() {
				
				@Override
				public void success(JSONObject result) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void error(FaceppParseException exception) {
					// TODO Auto-generated method stub
					
				}
			});
			
		}
		
	}

	

}
