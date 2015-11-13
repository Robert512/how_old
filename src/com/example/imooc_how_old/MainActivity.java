package com.example.imooc_how_old;




import net.youmi.android.AdManager;
import net.youmi.android.banner.AdSize;
import net.youmi.android.banner.AdView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facepp.error.FaceppParseException;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener{

	
	private static final int PICK_CODE = 0x110;
	private ImageView mPhoto;
	private Button mGetImage;
	private Button mDetect;
	private TextView mTip;
	private View mWaiting;
	
	private String mCurrentPhotoStr;
	
	private Bitmap mPhotoImg;
	private Paint mPaint;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AdManager.getInstance(this).init("699cbbbc9642657a","f73bd865a6b5e7fb", false);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		
		initViews();
		initEvents();
		
		mPaint = new Paint();
		
		//实例化广告条
	    AdView adView = new AdView(this, AdSize.FIT_SCREEN);
	    //获取要嵌入广告条的布局
	    LinearLayout adLayout=(LinearLayout)findViewById(R.id.adLayout);
	    //将广告条加入到布局中
	    adLayout.addView(adView);

		
	}

	
	private void initViews() {
		
		mPhoto = (ImageView) findViewById(R.id.id_photo);
		mGetImage = (Button) findViewById(R.id.id_getImage);
		mDetect = (Button) findViewById(R.id.id_detect);
		mTip = (TextView) findViewById(R.id.id_tip);
		mWaiting = findViewById(R.id.id__waiting);
	
	}
	
	
	private void initEvents() {
		mGetImage.setOnClickListener(this);
		mDetect.setOnClickListener(this);
		//mGetImage.setOnTouchListener(this);
		//mDetect.setOnTouchListener(this);
		
		
	}

	
	
	//选择好图片返回时调用，加载选中的图片
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
				
				//图片二进制获取的数据不能大于3M，根据照片的尺寸来压缩（这里其实要求是1M）
				resizePhoto();
				
				mPhoto.setImageBitmap(mPhotoImg);
				mTip.setText("点击检测吧   ==>");
				
			}
		}
		
		super.onActivityResult(requestCode, resultCode, intent);
	}
	
	
	//压缩图片
	private void resizePhoto()
	{
		Log.d("TAG","resizePhoto");
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(mCurrentPhotoStr,options);
		
		double ratio = Math.max(options.outWidth *1.0d /1024f, options.outHeight * 1.0d / 1024f);
		
		options.inSampleSize = (int)Math.ceil(ratio);
		options.inJustDecodeBounds = false;  //加载图片

		mPhotoImg = BitmapFactory.decodeFile(mCurrentPhotoStr, options);
		Log.d("TAG","resizePhoto1");
		
	}
	
	
	private static final int MSG_SUCESS = 0x111;
	private static final int MSG_ERROR = 0x112;
	
	
	private Handler mHandler = new Handler(){
		
		//强烈建议写上@Override,因为我写handleMessage出错了
		@Override
		public void handleMessage(Message msg)
		{
			switch(msg.what)
			{
			case MSG_SUCESS:
				Log.d("TAG","sucess1");
				mWaiting.setVisibility(View.GONE);
				Log.d("TAG","sucess2");
				JSONObject rs = (JSONObject) msg.obj;
				
				//绘图
				prepareRsBitmap(rs);
				
				mPhoto.setImageBitmap(mPhotoImg);
				
				break;
				
			case MSG_ERROR:
				mWaiting.setVisibility(View.GONE);

				String errorMsg = (String) msg.obj;
				if(TextUtils.isEmpty(errorMsg))
				{
					mTip.setText("检测出错了~");
				}else
				{
					mTip.setText(errorMsg);
					
				}
				
				break;
			default :
				break;
			}
		}
	};
	

	@Override
	public void onClick(View v) {
		switch(v.getId())
		{
		case R.id.id_getImage:
			Log.d("TAG","getImage1");
			Intent intent = new Intent(Intent.ACTION_PICK);
			intent.setType("image/*");
			startActivityForResult(intent, PICK_CODE);
			Log.d("TAG","getImage2");
			break;   //真的太他妈坑了！！！
			
			
		case R.id.id_detect:
			Log.d("TAG","detect1");
			mWaiting.setVisibility(View.VISIBLE);
			
			
			if(mCurrentPhotoStr != null && !mCurrentPhotoStr.trim().equals(""))
			{
				resizePhoto();
			}else
			{
				mPhotoImg = BitmapFactory.decodeResource(getResources(), R.drawable.t4);
			}
			
			
			FaceppDetect.detect(mPhotoImg, new FaceppDetect.CallBack() {
				
				@Override
				public void success(JSONObject result) {
					Log.d("TAG","sucess");
					Message msg = Message.obtain();
					msg.what = MSG_SUCESS;
					msg.obj = result;
					mHandler.sendMessage(msg);
					
				}
				
				@Override
				public void error(FaceppParseException exception) {
					Log.d("TAG","error");
					Message msg = Message.obtain();
					msg.what = MSG_ERROR;
					msg.obj = exception.getErrorMessage();
					mHandler.sendMessage(msg);
					
				}
			});
			break;   //真的太他妈坑了！！！
		default:
			break;
		}
		
	}

	
	
	
	/*@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch(v.getId())
		{
		case R.id.id_getImage:
			if(event.getAction() == MotionEvent.ACTION_DOWN)
			{
				mGetImage.
			}
			
			Log.d("TAG","getImage1");
			Intent intent = new Intent(Intent.ACTION_PICK);
			intent.setType("image/*");
			startActivityForResult(intent, PICK_CODE);
			Log.d("TAG","getImage2");
			break;   //真的太他妈坑了！！！
			
			
		case R.id.id_detect:
			Log.d("TAG","detect1");
			mWaiting.setVisibility(View.VISIBLE);
			
			
			if(mCurrentPhotoStr != null && !mCurrentPhotoStr.trim().equals(""))
			{
				resizePhoto();
			}else
			{
				mPhotoImg = BitmapFactory.decodeResource(getResources(), R.drawable.t4);
			}
			
			
			FaceppDetect.detect(mPhotoImg, new FaceppDetect.CallBack() {
				
				@Override
				public void success(JSONObject result) {
					Log.d("TAG","sucess");
					Message msg = Message.obtain();
					msg.what = MSG_SUCESS;
					msg.obj = result;
					mHandler.sendMessage(msg);
					
				}
				
				@Override
				public void error(FaceppParseException exception) {
					Log.d("TAG","error");
					Message msg = Message.obtain();
					msg.what = MSG_ERROR;
					msg.obj = exception.getErrorMessage();
					mHandler.sendMessage(msg);
					
				}
			});
			break;   //真的太他妈坑了！！！
		default:
			break;
		}
		
		
		return false;
	}*/

	
	
	
	
	
	protected void prepareRsBitmap(JSONObject rs)
	{
		
		Bitmap bitmap = Bitmap.createBitmap(mPhotoImg.getWidth(),mPhotoImg.getHeight(),mPhotoImg.getConfig());
		Canvas canvas = new Canvas(bitmap);
		
		canvas.drawBitmap(mPhotoImg, 0, 0, null);
		
		try {
			JSONArray faces = rs.getJSONArray("face");
			
			int faceCount = faces.length();
			
			if(faceCount == 0)
			{
				mTip.setText("没有检测到人脸哦~");
			}else{
				mTip.setText("检测到" + faceCount + "张脸");
			}
			
			
			for(int i=0;i<faceCount;i++)
			{
				JSONObject face = faces.getJSONObject(i);
				JSONObject posObj = face.getJSONObject("position");
				
				float x =  (float) posObj.getJSONObject("center").getDouble("x");
				float y =  (float) posObj.getJSONObject("center").getDouble("y");
				
				float w = (float) posObj.getDouble("width");
				float h = (float) posObj.getDouble("height");
				
				x = x / 100 * bitmap.getWidth();
				y = y / 100 * bitmap.getHeight();
				
				w = w / 100 * bitmap.getWidth();
				h = h / 100 * bitmap.getHeight();
				
				mPaint.setColor(0xffffffff);
				mPaint.setStrokeWidth(3);
						
				//画box
				canvas.drawLine(x - w /2, y - h /2, x - w / 2, y + h /2, mPaint);
				canvas.drawLine(x - w /2, y - h /2, x + w / 2, y - h /2, mPaint);
				canvas.drawLine(x + w /2, y + h /2, x - w / 2, y + h /2, mPaint);
				canvas.drawLine(x + w /2, y + h /2, x + w / 2, y - h /2, mPaint);
				
				int age = face.getJSONObject("attribute").getJSONObject("age").getInt("value");
				String gender = face.getJSONObject("attribute").getJSONObject("age").getString("value");
				
				Bitmap ageBitmap = buildAgeBitMap(age,"Male".equals(gender));
				
				
				//编写缩放的代码
				int ageWidth = ageBitmap.getWidth();
				int ageHeight = ageBitmap.getHeight();
				
				if(bitmap.getWidth() < mPhoto.getWidth() && bitmap.getHeight() < mPhoto.getHeight())
				{
					float ratio = Math.max(bitmap.getWidth() * 1.0f/mPhoto.getWidth(), bitmap.getHeight() * 1.0f/mPhoto.getHeight());
				
					ageBitmap = Bitmap.createScaledBitmap(ageBitmap, (int)(ageWidth * ratio), (int)(ageHeight * ratio), false);  //这里要用int类型
	
					
				}
				
				canvas.drawBitmap(ageBitmap, x - ageBitmap.getWidth(), y - h/2 - ageBitmap.getHeight(), null);
				
						
				
				mPhotoImg = bitmap;
			}
		} catch (JSONException e) {
			
			e.printStackTrace();
		}
		
		
	}


	private Bitmap buildAgeBitMap(int age, boolean isMale) {
		TextView tv = (TextView) mWaiting.findViewById(R.id.id_age_and_gender);
		tv.setText(age + "");
		if(isMale)
		{
			tv.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.male),null,null,null);
			
		}else
		{
			tv.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.female),null,null,null);
		}
		
		tv.setDrawingCacheEnabled(true);
		Bitmap bitmap = Bitmap.createBitmap(tv.getDrawingCache());
		tv.destroyDrawingCache();
		
		return bitmap;
	}


	
	

}
