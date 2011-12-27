package com.fatjay.subfunction;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.fatjay.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class threadImage extends Activity {
	String urlString = "";
	Bitmap bit = null;
	static threadImage instance = null;
	
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.imageofthread);
		instance = this;
		Intent mIntent = getIntent();
		Bundle mBundle = mIntent.getExtras();
		urlString = mBundle.getString("image");
		LayoutInflater mInflater = getLayoutInflater();
		ProgressBar mBar = (ProgressBar) mInflater.inflate(R.layout.imageloading, null);
		setContentView(mBar);
		
		ImageView image1 = (ImageView) findViewById(R.id.image);
		getHttpBitmap(urlString);
		//image1.setImageBitmap(bitmap);	//设置Bitmap
	}
	
	private static Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
				case 0://接到从线程内传来的图片bitmap和imageView.
						//这里只是将bitmap传到imageView中就行了。只所以不在线程中做是考虑到线程的安全性。
					LayoutInflater mInflater = threadImage.instance.getLayoutInflater();
					ImageView mBar = (ImageView) mInflater.inflate(R.layout.imageofthread, null);
					mBar.setImageBitmap(threadImage.instance.bit);
					threadImage.instance.setContentView(mBar);
					break;
				default:
					super.handleMessage(msg);
			}
		}
		
	};

	private static ExecutorService executorService = Executors.newFixedThreadPool(5);
	
	public static void getHttpBitmap(final String url) {
		executorService.submit(new Runnable() {
			public void run() {
				URL myFileUrl = null;
				Bitmap bitmap = null;
				try {
					String urlString = url;
					String[] temps = urlString.split("/");
					String filename = temps[temps.length-2] + "_" + temps[temps.length-1];
					File dir = new File("/sdcard/lily");
					if (!dir.exists()) {
						dir.mkdirs();
					}
					File picFile = new File("/sdcard/lily/" + filename);
					if (picFile.exists()) {
						bitmap = BitmapFactory.decodeFile("/sdcard/lily/" + filename);
						threadImage.instance.bit = bitmap;
						Message msg = new Message();
			    		msg.what = 0;
						handler.sendMessage(msg);
					} else {
						myFileUrl = new URL(urlString);
						HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
						conn.setConnectTimeout(10000);
						conn.setDoInput(true);
						conn.connect();
						InputStream is = conn.getInputStream();
						bitmap = BitmapFactory.decodeStream(is);
						is.close();
						threadImage.instance.bit = bitmap;
						BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(picFile));
						bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
						bos.flush();
						bos.close();
						Message msg = new Message();
			    		msg.what = 0;
						handler.sendMessage(msg);
					}
					
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
		});
	}
}
