package com.fatjay.subfunction;

import com.fatjay.R;
import com.fatjay.effects.GalleryAdapter;
import com.fatjay.effects.MyGallery;

import android.app.Activity;
import android.os.Bundle;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.Gallery;
import android.widget.AdapterView.OnItemSelectedListener;

public class threadImage extends Activity implements OnTouchListener {
	public static int screenWidth;
	public static int screenHeight;
	private MyGallery gallery;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.imageofthread);

		String tempString = getIntent().getExtras().getString("image").substring(2);
		String[] picStrings = tempString.split("##");
		
		gallery = (MyGallery) findViewById(R.id.gallery);
		gallery.setVerticalFadingEdgeEnabled(false);
		gallery.setHorizontalFadingEdgeEnabled(false);
		gallery.setAdapter(new GalleryAdapter(this, picStrings));

		screenWidth = getWindow().getWindowManager().getDefaultDisplay().getWidth();
		screenHeight = getWindow().getWindowManager().getDefaultDisplay().getHeight();
	}

	float beforeLenght = 0.0f;
	float afterLenght = 0.0f;
	boolean isScale = false;
	float currentScale = 1.0f;
	private class GalleryChangeListener implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			currentScale = 1.0f;
			isScale = false;
			beforeLenght = 0.0f;
			afterLenght = 0.0f;
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_POINTER_DOWN:
			beforeLenght = spacing(event);
			if (beforeLenght > 5f) {
				isScale = true;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (isScale) {
				afterLenght = spacing(event);
				if (afterLenght < 5f)
					break;
				float gapLenght = afterLenght - beforeLenght;
				if (gapLenght == 0) {
					break;
				} else if (Math.abs(gapLenght) > 5f) {
					float scaleRate = gapLenght / 854;
					Animation myAnimation_Scale = new ScaleAnimation(currentScale, currentScale + scaleRate, currentScale, currentScale + scaleRate, Animation.RELATIVE_TO_SELF, 0.5f,
							Animation.RELATIVE_TO_SELF, 0.5f);
					myAnimation_Scale.setDuration(100);
					myAnimation_Scale.setFillAfter(true);
					myAnimation_Scale.setFillEnabled(true);

					currentScale = currentScale + scaleRate;
					gallery.getSelectedView().setLayoutParams(new Gallery.LayoutParams((int) (480 * (currentScale)), (int) (854 * (currentScale))));

					beforeLenght = afterLenght;
				}
				return true;
			}
			break;
		case MotionEvent.ACTION_POINTER_UP:
			isScale = false;
			break;
		}
		return false;
	}

	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}
}



//package com.fatjay.subfunction;
//
//import java.io.BufferedOutputStream;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.HttpURLConnection;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//import com.fatjay.R;
//
//import android.app.Activity;
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.view.LayoutInflater;
//import android.view.Window;
//import android.view.WindowManager;
//import android.widget.ImageView;
//import android.widget.ProgressBar;
//
//public class threadImage extends Activity {
//	String urlString = "";
//	Bitmap bit = null;
//	static threadImage instance = null;
//	
//	public void onCreate(Bundle savedInstanceState) {
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//		super.onCreate(savedInstanceState);
//		//setContentView(R.layout.imageofthread);
//		instance = this;
//		Intent mIntent = getIntent();
//		Bundle mBundle = mIntent.getExtras();
//		urlString = mBundle.getString("image");
//		LayoutInflater mInflater = getLayoutInflater();
//		ProgressBar mBar = (ProgressBar) mInflater.inflate(R.layout.imageloading, null);
//		setContentView(mBar);
//		
//		ImageView image1 = (ImageView) findViewById(R.id.image);
//		getHttpBitmap(urlString);
//		//image1.setImageBitmap(bitmap);	//设置Bitmap
//	}
//	
//	private static Handler handler = new Handler() {
//
//		@Override
//		public void handleMessage(Message msg) {
//			// TODO Auto-generated method stub
//			switch (msg.what) {
//				case 0://接到从线程内传来的图片bitmap和imageView.
//						//这里只是将bitmap传到imageView中就行了。只所以不在线程中做是考虑到线程的安全性。
//					LayoutInflater mInflater = threadImage.instance.getLayoutInflater();
//					ImageView mBar = (ImageView) mInflater.inflate(R.layout.imageofthread, null);
//					mBar.setImageBitmap(threadImage.instance.bit);
//					threadImage.instance.setContentView(mBar);
//					break;
//				default:
//					super.handleMessage(msg);
//			}
//		}
//		
//	};
//
//	private static ExecutorService executorService = Executors.newFixedThreadPool(5);
//	
//	public static void getHttpBitmap(final String url) {
//		executorService.submit(new Runnable() {
//			public void run() {
//				URL myFileUrl = null;
//				Bitmap bitmap = null;
//				try {
//					String urlString = url;
//					String[] temps = urlString.split("/");
//					String filename = temps[temps.length-2] + "_" + temps[temps.length-1];
//					File dir = new File("/sdcard/lily");
//					if (!dir.exists()) {
//						dir.mkdirs();
//					}
//					File picFile = new File("/sdcard/lily/" + filename);
//					if (picFile.exists()) {
//						bitmap = BitmapFactory.decodeFile("/sdcard/lily/" + filename);
//						threadImage.instance.bit = bitmap;
//						Message msg = new Message();
//			    		msg.what = 0;
//						handler.sendMessage(msg);
//					} else {
//						myFileUrl = new URL(urlString);
//						HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
//						conn.setConnectTimeout(10000);
//						conn.setDoInput(true);
//						conn.connect();
//						InputStream is = conn.getInputStream();
//						bitmap = BitmapFactory.decodeStream(is);
//						is.close();
//						threadImage.instance.bit = bitmap;
//						BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(picFile));
//						bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
//						bos.flush();
//						bos.close();
//						Message msg = new Message();
//			    		msg.what = 0;
//						handler.sendMessage(msg);
//					}
//					
//				} catch (MalformedURLException e) {
//					e.printStackTrace();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}				
//			}
//		});
//	}
//}
