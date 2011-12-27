package com.fatjay.subfunction;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;

import com.fatjay.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.format.Time;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.Toast;

public class quickPost extends Activity implements OnClickListener {
	boolean is_compress = false;
	private String filenameString;
	private String photoPath;
	private String boardname;
	private String TAG = "quickpost";
	private String code;
	private String cookie;
	private String username;
	private String pwd;
	private int compress_rate;
	static Uri photoUri;
	Context mContext;
	String picURL;
	private ProgressDialog waitDialog;
	private static final int CAMERA_WITH_DATA = 3023;  
    private static final int PHOTO_PICKED_WITH_DATA = 3021;
    private static final File PHOTO_DIR = new File(Environment.getExternalStorageDirectory() + "/lily/temp");
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.quickpost);
		mContext = this;
		SharedPreferences favor = mContext.getSharedPreferences("account", 0);
        username = favor.getString("account", null);
        pwd = favor.getString("password", null);
        favor = mContext.getSharedPreferences("compress_rate", 0);
        String temp = favor.getString("rate", null);
        compress_rate = Integer.valueOf(temp);
        
		getIdentify();
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, boards);
		AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.quick_board);
        textView.setAdapter(adapter);
		Button send = (Button)findViewById(R.id.quick_send);
		send.setOnClickListener(this);
		Button cancel = (Button)findViewById(R.id.quick_cancel);
		cancel.setOnClickListener(this);
		Button takePic = (Button)findViewById(R.id.quick_photo);
		takePic.setOnClickListener(this);
		getDATA();
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.quick_board);
		switch (v.getId()) {
		case R.id.quick_send:
			if (textView.getText().toString().equals("")) {
				Toast.makeText(quickPost.this, "请先指定版面", Toast.LENGTH_SHORT);
			} else {
				boolean isIn = false;
				boardname = textView.getText().toString();
				for (String element : boards) {
					if (element.equals(boardname)) {
						isIn = true;
						break;
					}
				}
				if (isIn) {
					if (send()) {
						finish();
					} else {
						Toast.makeText(quickPost.this, "网络似乎有点问题，请稍后重试", Toast.LENGTH_SHORT);
					}
				} else {
					Toast.makeText(quickPost.this, "指定的版面无效", Toast.LENGTH_SHORT);
				}
			}
			break;
		case R.id.quick_photo:
			if (textView.getText().toString().equals("")) {
				Toast.makeText(quickPost.this, "请先指定版面", Toast.LENGTH_SHORT);
			} else {
				boolean isIn = false;
				boardname = textView.getText().toString();
				for (String element : boards) {
					if (element.equals(boardname)) {
						isIn = true;
						break;
					}
				}
				if (isIn) {
					doPickPhotoAction();
					/*
					Time t = new Time();
		            t.setToNow();
					String pathString = Environment.getExternalStorageDirectory().toString() + "/lily/temp";
					String filenameString = String.valueOf(t.year) + String.valueOf(t.month) + String.valueOf(t.monthDay) + String.valueOf(t.hour) + String.valueOf(t.minute) + String.valueOf(t.second) + ".jpeg";
					File outFile = new File(pathString, filenameString);
					photoUri = Uri.fromFile(outFile);
					Intent i = new Intent("android.media.action.IMAGE_CAPTURE");
					i.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
		            startActivityForResult(i, CAMERA_WITH_DATA);
		            */
				} else {
					Toast.makeText(quickPost.this, "指定的版面无效", Toast.LENGTH_SHORT);
				}
				
			}
			break;
		case R.id.quick_cancel:
			this.finish();
			break;
		default:
			break;
		}
	}
	
	@Override  
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
        super.onActivityResult(requestCode, resultCode, data);  
        switch (requestCode) {
		case CAMERA_WITH_DATA: 
            if (resultCode != RESULT_OK) return;
            doCropPhoto(photoUri);
        	break;
		case PHOTO_PICKED_WITH_DATA:
			Bitmap photoCaptured = data.getParcelableExtra("data");
			try {
                photoCaptured = Bitmap.createScaledBitmap(photoCaptured, 600, 800, true);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                is_compress = ((CheckBox)findViewById(R.id.quick_compress)).isChecked();
                if (is_compress) {
                	photoCaptured.compress(Bitmap.CompressFormat.JPEG, compress_rate, baos);
				} else {
					photoCaptured.compress(Bitmap.CompressFormat.JPEG, 70, baos);
				}
                
                byte[] photoBytes = baos.toByteArray();
                Time t=new Time();
                t.setToNow();
                filenameString = String.valueOf(t.year) + String.valueOf(t.month) + String.valueOf(t.monthDay) + String.valueOf(t.hour) + String.valueOf(t.minute) + String.valueOf(t.second) + ".jpeg";
                
                File dir = new File("/sdcard/lily/temp");
				if (!dir.exists()) {
					dir.mkdirs();
				}
                File aFile = new File("/sdcard/lily/temp/" + filenameString);  
                photoPath = aFile.getAbsolutePath();  
                
                if (aFile.exists())
                	aFile.delete();  
                aFile.createNewFile();

                FileOutputStream fos = new FileOutputStream(aFile);  
                fos.write(photoBytes);
                fos.close();
                Log.i(TAG, "写入文件" + "/sdcard/lily/temp/" + filenameString);

            } catch (FileNotFoundException e) {
                e.printStackTrace();  
            } catch (IOException e) {  
                e.printStackTrace();  
            }
            waitDialog = new ProgressDialog(quickPost.this);
	        waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	        waitDialog.setMessage("正在上传照片...");
	        waitDialog.setIndeterminate(true);
	        waitDialog.setCancelable(false);
	        waitDialog.show();
            uploadFile2Svr();
        	break;
		default:
			break;
		}
	}
	
	private void doPickPhotoAction() {  
        Context context = quickPost.this;  
      
        // Wrap our context to inflate list items using correct theme  
        final Context dialogContext = new ContextThemeWrapper(context,  
                android.R.style.Theme_Light);  
        String cancel="返回";  
        String[] choices;  
        choices = new String[2];  
        choices[0] = "创建新图片";  //拍照  
        choices[1] = "从相册选择图片";  //从相册中选择  
        final ListAdapter adapter = new ArrayAdapter<String>(dialogContext,  
                android.R.layout.simple_list_item_1, choices);  
      
        final AlertDialog.Builder builder = new AlertDialog.Builder(  
                dialogContext);  
        builder.setTitle("图片选择");  
        builder.setSingleChoiceItems(adapter, -1,  
                new DialogInterface.OnClickListener() {  
                    public void onClick(DialogInterface dialog, int which) {  
                        dialog.dismiss();  
                        switch (which) {  
                        case 0:{  
                            String status=Environment.getExternalStorageState();  
                            if(status.equals(Environment.MEDIA_MOUNTED)){//判断是否有SD卡  
                            	doTakePhoto();// 用户点击了从照相机获取  
                            }  
                            else{  
                                Toast.makeText(quickPost.this, "没有SD卡", Toast.LENGTH_LONG);
                            }  
                            break;  
                              
                        }  
                        case 1:  
                            doPickPhotoFromGallery();// 从相册中去获取  
                            break;  
                        }  
                    }  
                });  
        builder.setNegativeButton(cancel, new DialogInterface.OnClickListener() {  
      
            @Override  
            public void onClick(DialogInterface dialog, int which) {  
                dialog.dismiss();  
            }  
              
        });  
        builder.create().show();  
    }
	
	protected void doTakePhoto() {  
        try {  
            // Launch camera to take photo for selected contact  
            PHOTO_DIR.mkdirs();// 创建照片的存储目录  
            Time t = new Time();
            t.setToNow();
			String filenameString = String.valueOf(t.year) + String.valueOf(t.month) + String.valueOf(t.monthDay) + String.valueOf(t.hour) + String.valueOf(t.minute) + String.valueOf(t.second) + ".jpeg";
			File outFile = new File(PHOTO_DIR, filenameString);
            final Intent intent = getTakePickIntent(outFile);  
            startActivityForResult(intent, CAMERA_WITH_DATA);  
        } catch (ActivityNotFoundException e) {  
            Toast.makeText(this, "无法创建图片",  
                    Toast.LENGTH_LONG).show();  
        }  
    }  
      
    public static Intent getTakePickIntent(File f) {  
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);  
        photoUri = Uri.fromFile(f);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);  
        return intent;  
    }
	
	protected void doPickPhotoFromGallery() {  
        try {  
            // Launch picker to choose photo for selected contact  
            final Intent intent = getPhotoPickIntent();  
            startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);  
        } catch (ActivityNotFoundException e) {  
            Toast.makeText(this, "无法打开相册",  
                    Toast.LENGTH_LONG).show();  
        }  
    }  
      
    // 封装请求Gallery的intent  
    public static Intent getPhotoPickIntent() {  
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);  
        intent.setType("image/*");  
        intent.putExtra("crop", "true");  
        intent.putExtra("aspectX", 1);  
        intent.putExtra("aspectY", 1);  
        intent.putExtra("outputX", 80);  
        intent.putExtra("outputY", 80);  
        intent.putExtra("return-data", true);  
        return intent;  
    } 
	
	protected void doCropPhoto(Uri file) {  
        try {  
            // 启动gallery去剪辑这个照片  
            final Intent intent = getCropImageIntent(file);  
            startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);  
        } catch (Exception e) {  
            Toast.makeText(this, "无法打开相册",  
                    Toast.LENGTH_LONG).show();  
        }  
    }  

    /**  
    * Constructs an intent for image cropping. 调用图片剪辑程序  
    */  
    public static Intent getCropImageIntent(Uri photoUri) {  
        Intent intent = new Intent("com.android.camera.action.CROP");  
        intent.setDataAndType(photoUri, "image/*");  
        intent.putExtra("crop", "true");  
        intent.putExtra("aspectX", 1);  
        intent.putExtra("aspectY", 1);  
        intent.putExtra("outputX", 80);  
        intent.putExtra("outputY", 80);  
        intent.putExtra("return-data", true);  
        return intent;  
    }  
	
	private boolean getIdentify() {
		try {
			Random random = new Random();
			int s = random.nextInt(99999)%(90000) + 10000;
			if (username == "") {
				Toast.makeText(this, "请先设置帐号信息", Toast.LENGTH_LONG).show();
				finish();
			}
			String urlString = "http://bbs.nju.edu.cn/vd" + String.valueOf(s) + "/bbslogin?type=2&id=" + username + "&pw=" + pwd;
			String doc = Jsoup.connect(urlString).get().toString();
			int t = doc.indexOf("setCookie");
			if (t == -1) {
				return false;
			}
			else {
				String tempString = doc.substring(t);
				tempString = tempString.substring(11, tempString.indexOf(")")-1);
				String[] tm =  tempString.split("\\+");
				String _U_KEY = String.valueOf(Integer.parseInt(tm[1])-2);
				String[] tm2 = tm[0].split("N");
				String _U_UID = tm2[1];
				String _U_NUM = "" + String.valueOf(Integer.parseInt(tm2[0]) + 2);
				cookie = "_U_KEY=" + _U_KEY + "; " + "_U_UID=" + _U_UID + "; " + "_U_NUM=" + _U_NUM + ";";
				code = "vd" + String.valueOf(s);
				return true;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	/*
	 * first==true将第一次拍得图片放到自己的空间
	 * first==false第二次及以后都放在版面
	 */
	private void getDATA() {
		Intent it = getIntent();
		Uri uri;
		Bundle extras;
		InputStream imgIS = null;
		try {
			if (it != null &&  it.getAction() != null && it.getAction().equals(Intent.ACTION_SEND)) {
				extras = it.getExtras();
				if (extras.containsKey("android.intent.extra.STREAM")) {
					Log.i(TAG, "uri++=" + extras.get("android.intent.extra.STREAM"));
					uri = (Uri) extras.get("android.intent.extra.STREAM");
					ContentResolver cr = getContentResolver();
					imgIS = cr.openInputStream(uri);
				}
			}
            Bitmap photoCaptured = BitmapFactory.decodeStream(imgIS);
            photoCaptured = Bitmap.createScaledBitmap(photoCaptured, 600, 800, true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            photoCaptured.compress(Bitmap.CompressFormat.JPEG, compress_rate, baos);
            
            byte[] photoBytes = baos.toByteArray();
            Time t=new Time();
            t.setToNow();
            filenameString = String.valueOf(t.year) + String.valueOf(t.month) + String.valueOf(t.monthDay) + String.valueOf(t.hour) + String.valueOf(t.minute) + String.valueOf(t.second) + ".jpeg";
            File dir = new File("/sdcard/lily/temp");
			if (!dir.exists()) {
				dir.mkdirs();
			}
            File aFile = new File("/sdcard/lily/temp/" + filenameString);  
            photoPath = aFile.getAbsolutePath();  
            if (aFile.exists())
            	aFile.delete();  
            aFile.createNewFile();

            FileOutputStream fos = new FileOutputStream(aFile);  
            fos.write(photoBytes);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }
        waitDialog = new ProgressDialog(quickPost.this);
        waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        waitDialog.setMessage("正在上传照片...");
        waitDialog.setIndeterminate(true);
        waitDialog.setCancelable(false);
        waitDialog.show();
        uploadFirst();
	}
	
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
				case 0://接到从线程内传来的图片bitmap和imageView.
						//这里只是将bitmap传到imageView中就行了。只所以不在线程中做是考虑到线程的安全性。
					waitDialog.dismiss();
					if (!picURL.equals("")) {
			        	String oldString = ((EditText)findViewById(R.id.quick_content)).getText().toString();
			            ((EditText)findViewById(R.id.quick_content)).setText(oldString + picURL);
			            Toast.makeText(quickPost.this, "图片上传成功！", Toast.LENGTH_SHORT);
					} else {
						Toast.makeText(quickPost.this, "图片上传失败！", Toast.LENGTH_SHORT);
					}
					break;
				default:
					super.handleMessage(msg);
			}
		}
		
	};

	private ExecutorService executorService = Executors.newFixedThreadPool(5);
	
	public void uploadFirst() {
		executorService.submit(new Runnable() {
			public void run() {
	    		Message msg = new Message();
	    		msg.what = 0;
	    		picURL = "";
		        HttpClient client = new DefaultHttpClient();
				client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
				String url = "http://bbs.nju.edu.cn/" + code + "/bbsdoupload";
				Log.i(TAG, "准备上传" + url);
				HttpPost imgPost = new HttpPost(url);
				MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
				File file = new File(photoPath);
				try {
					reqEntity.addPart("up", new FileBody(file));
					reqEntity.addPart("exp", new StringBody("", Charset.forName("GB2312")));
					reqEntity.addPart("ptext", new StringBody("", Charset.forName("GB2312")));
					reqEntity.addPart("board", new StringBody("Pictures", Charset.forName("GB2312")));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				imgPost.setEntity(reqEntity);
				imgPost.addHeader("Cookie", cookie);
				imgPost.addHeader(reqEntity.getContentType());
				
				for (int i = 0; i < imgPost.getAllHeaders().length; i++) {
					Log.d(TAG, imgPost.getAllHeaders()[i].toString());
				}
				Log.d(TAG, "executing request " + imgPost.getRequestLine());
		
				String result;
			    HttpResponse httpResponse;
				try {
					httpResponse = client.execute(imgPost);
					if (httpResponse.getStatusLine().getStatusCode() == 200) {
			            result = EntityUtils.toString(httpResponse.getEntity());
			            result = result.substring(result.indexOf("url=")+2, result.indexOf(">")-1);
			            String fileNum = result.substring(result.indexOf("file=")+5, result.indexOf("&name"));
			            url = "http://bbs.nju.edu.cn/" + code + "/bbsupload2?board=Pictures" + "&file=" + fileNum + "&name=" + filenameString + "&exp=&ptext=";
			            Log.i(TAG, "准备获取图片地址" + url);
			            HttpGet uploadGet = new HttpGet(url);
			            uploadGet.addHeader("Cookie", cookie);
			            httpResponse = client.execute(uploadGet);
			            if (httpResponse.getStatusLine().getStatusCode() == 200) {
							result = EntityUtils.toString(httpResponse.getEntity());
							if (result.contains("name=")) {
								String tempString = result.substring(result.indexOf("name=")+5, result.length()-3);
								picURL = "\nhttp://bbs.nju.edu.cn/file/Pictures/" + tempString +"\n";
								Log.i(TAG, "地址" + picURL);
								System.gc();
								File temp = new File("/sdcard/lily/temp/" + filenameString);
					            if (temp.exists())
					            	temp.delete();
							}
						}
					}
					else {
			            Log.d(TAG, "Fail");
			            System.gc();
			            File temp = new File("/sdcard/lily/temp/" + filenameString);
			            if (temp.exists())
			            	temp.delete();
					} 
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				handler.sendMessage(msg);
			}
		});
    }
	
	public void uploadFile2Svr() {
		executorService.submit(new Runnable() {
			public void run() {
	    		Message msg = new Message();
	    		msg.what = 0;
	    		picURL = "";
		        HttpClient client = new DefaultHttpClient();
				client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
				String url = "http://bbs.nju.edu.cn/" + code + "/bbsdoupload";
				
				HttpPost imgPost = new HttpPost(url);
				MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
				File file = new File(photoPath);
				try {
					reqEntity.addPart("up", new FileBody(file));
					reqEntity.addPart("exp", new StringBody("", Charset.forName("GB2312")));
					reqEntity.addPart("ptext", new StringBody("", Charset.forName("GB2312")));
					reqEntity.addPart("board", new StringBody(boardname, Charset.forName("GB2312")));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				imgPost.setEntity(reqEntity);
				imgPost.addHeader("Cookie", cookie);
				imgPost.addHeader(reqEntity.getContentType());
				
				for (int i = 0; i < imgPost.getAllHeaders().length; i++) {
					Log.d(TAG, imgPost.getAllHeaders()[i].toString());
				}
				Log.d(TAG, "executing request " + imgPost.getRequestLine());
		
				String result;
			    HttpResponse httpResponse;
				try {
					Log.d(TAG, "before excuting...");
					httpResponse = client.execute(imgPost);
					Log.d(TAG, "after excuting...");
					if (httpResponse.getStatusLine().getStatusCode() == 200) {
			            result = EntityUtils.toString(httpResponse.getEntity());
			            result = result.substring(result.indexOf("url=")+2, result.indexOf(">")-1);
			            String fileNum = result.substring(result.indexOf("file=")+5, result.indexOf("&name"));
			            url = "http://bbs.nju.edu.cn/" + code + "/bbsupload2?board=" + boardname + "&file=" + fileNum + "&name=" + filenameString + "&exp=&ptext=";
			            Log.i(TAG, url);
			            HttpGet uploadGet = new HttpGet(url);
			            uploadGet.addHeader("Cookie", cookie);
			            httpResponse = client.execute(uploadGet);
			            if (httpResponse.getStatusLine().getStatusCode() == 200) {
							result = EntityUtils.toString(httpResponse.getEntity());
							if (result.contains("name=")) {
								String tempString = result.substring(result.indexOf("name=")+5, result.length()-3);
								picURL = "\nhttp://bbs.nju.edu.cn/file/" + boardname + "/" + tempString +"\n";
								Log.i(TAG, "地址" + picURL);
								System.gc();
							}
						}
					}
					else {
			            Log.d(TAG, "Fail");
			            System.gc();
					} 
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				handler.sendMessage(msg);
			}
		});
    }

	public boolean sendThread() {
		String contentString = ((EditText)findViewById(R.id.quick_content)).getText().toString();
		String title = ((EditText)findViewById(R.id.quick_title)).getText().toString();
		String newurlString;
		try {
			newurlString = "http://bbs.nju.edu.cn/" + code + "/bbssnd?board=" + boardname;

			HttpPost httpRequest = new HttpPost(newurlString);
		    ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();
		    postData.add(new BasicNameValuePair("title", title));
		    postData.add(new BasicNameValuePair("pid", "0"));
		    postData.add(new BasicNameValuePair("reid", "0"));
		    postData.add(new BasicNameValuePair("signature", "1"));
		    postData.add(new BasicNameValuePair("autocr", "on"));
		    postData.add(new BasicNameValuePair("text", contentString));
		    httpRequest.addHeader("Cookie", cookie);
		    httpRequest.setEntity(new UrlEncodedFormEntity(postData, "GB2312"));
		    HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				Toast.makeText(getApplicationContext(), "发送成功！",
					     Toast.LENGTH_SHORT).show();
				return true;
			} else {
				Toast.makeText(getApplicationContext(), "发送失败！",
					     Toast.LENGTH_SHORT).show();
				return false;
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean send() {
		boolean success = sendThread();
		int trycount = 3;
		while (!success && trycount > 0) {
			getIdentify();
			success = sendThread();
			trycount --;
		}
		if (!success) {
			Toast.makeText(this, "你的网络似乎有点问题...", Toast.LENGTH_SHORT);
			return false;
		} else {
			return true;
		}
	}
	
	
	
	static final String[] boards = new String[] {"1937_12_13","7th_Art","Abroad","AcademicReport","Actuary",
		"AD_Art","Advice","Aerospace","Agent","AI","AlbumShow","Algorithm","America","AnHui","Announce","AntiMalfeasant",
		"AntiRumor","Apple","Archaism","Archeology","ASCIIArt","Association_Union","Astrology","AtmosphereSci","AutoSpeed",
		"Badminton","Basketball","BBSDev","BBSHelp","bbslists","BeiJing","Billiards","BitTorrent","Bless","Blog","BMManager",
		"BNGames","Board","BoardGame","BoardManage","BodilyForm","Borland","Bowling","Boxing_Fight","Boys","Bridge","Britain",
		"Budaixi","bulletin","C_Inter","Calligraphy","CAS","CCAS","CCP","CFD","ChangZhou","Chat","Chemistry","Chess","ChinaFootball",
		"ChinaNews","Chorus","Chrematistics","Christianity","ChunQiu_ZhanGuo","Civil_Servant","Classical_Poem","ClassicalCulture",
		"ClassicalMusic","Collections","Comic","Complain","Computer_ABC","Consultant","Contest","Couplet","CPA","CPlusPlus","CPU",
		"Cross_Strait","CrossShow","Cube","CUG","CUMT","Cycling","D_Chinese","D_Computer","D_EarthScience","D_EE","D_History",
		"D_Materials","D_Maths","D_Philosophy","D_Physics","D_SocialSec","Dance","Database","Debate","Detective","Deutsch","DigiMusic",
		"DigitalWorld","DII","Discovery","DiscZone","DongBei","DotaAllstars","DotNet","Drama","Drawing","Dream","Drink","DV_Studio",
		"E_Business","E_Sports","EarthSciences","Economics","Education","EEtechnology","ElectronicMusic","Embedded","Emprise","English",
		"EnglishCorner","Esperanto","Esquire","ExcellentBM","ExchangeStudent","F1","F_Literature","FairyTale","FamilyLife","FanBu",
		"Fantasy","Fashion","FDU","FEA","Feelings","Fiction","Finance","Fishing","Fitness","Flash","FleaMarket","Flowers","Folk_Country",
		"Folk_Music","FOOD","Fortran","Forum","French","Friendship","FuJian","GAFA","GEC","Geography","Girls","GIS","GJ","GoToUniversity",
		"Graphics","GRE_TOEFL","GreatTurn","GreeceRome","GreenEarth","GuangDong","GuangXi","Guilt","Guitar","GuQin","Hacker","HaiNan",
		"HandiCraft","Hardware","HeBei","HeNan","HHU","HiFi","History","HKU","Hometown","HomoSky","HotZone","HPC","HuaiAn","HuBei","Human",
		"HuNan","ID","IELTS","IFA_IS","IFIS","Image","Info_Manage","Inner_Mongolia","Intern","IR","ITClub","ITExam","J_Ent","Japanese",
		"Java","Jazz_Blues","JiangXi","JLU","JobAndWork","JobExpress","Joke","Journalism","JSSports","KaoYan","KaraOK","Korea","Law",
		"LectureHall","LianYunGang","Life","LifeLeague","LifeScience","LilyDigest","LilyFestival","LilyLinks","LilyStudio","Linguistics",
		"LinuxUnix","LostToFind","Love","LSCMA","LZU","M_Academic","M_CMHER","M_Gonghui","M_Graduate","M_GraduateUnion","M_Guard",
		"M_Hospital","M_Job","M_League","M_Library","M_Logistic","M_NIC","M_Student","M_StudentUnion","Magic","MaJiang","Management",
		"MARC","Marketing_Zone","Marvel","Mathematics","MathTools","Mediastudy","Medicine","Memory","Microwave","Military","Mobile",
		"Model_Space","Modern_Poem","Movies","MSTClub","MSWindows","MudLife","Musical","Mythlegend","Names","NanJing","NanoST","NanTong",
		"Nature","NetResources","Network","NewAge","newcomers","Nirvana","NJ_HOUSE","NJAU","NJMU","NJNU","NJU_Graduate","NJU_HOME",
		"NJU_TIC","NJU_Youth","NJU_zhixing","NJUExpress","NJUMUN","NJUPT","NJUT","NKU","NoteBook","notepad","Novel","NUAA","NUST","NZY",
		"OfficeStaff","OLGames","Olympics","Orchestra","OUC","OurCustom","Ourselves","Overseas","Paint","PartTimeJob","Party_of_Killer",
		"PCGames","Peer_Edu","PeerCounseling","People","PersonalCorpus","PetsEden","Philosophy","Photography","Physics","Piano","Pictures",
		"PKU","Politics","PopMusic","Postdoc","Program","Psychology","PuKouCampus","Python","QuYi","Radio","Reading","ReadyForJob",
		"RealEstate","RedCross","Renju","Riddle","RockMusic","RoomChating","RunForEver","Russia","S_Astronomy","S_Atmosphere","S_Business",
		"S_Chemistry","S_Education","S_Environment","S_ForeignLang","S_Geography","S_GOV","S_Graduate","S_Information","S_Journalism",
		"S_Law","S_LifeScience","S_Medicine","S_MSE","S_Sociology","S_Software","SanGuo","SAU","SCDA","Sculpture","SE_Association",
		"Seasons","SEU","ShanDong","ShangHai","ShanXi","Shopping","ShortMessage","Shows","SIFE_NJU","SiGuo","Single","SiYuan","SJ",
		"SJTU","Skating","Smoking","SoftEng","Software","SPA","Spanish","SportsNews","Stock","StoneCity","StoneStory","Story","Sudoku",
		"SuperGirls","SuQian","SuZhou","Swimming","sysop","TableTennis","Taekwondo","Taiwan","TaiZhou","TCM","Tennis","test","TeX",
		"Theoretical_CS","Thesis","THU","TianJian","TianJin","Tibet","Traffic_Info","Train","Travel","TV","TVGames","UrbanPlan",
		"US_JP_Research","USTC","V_Suggestions","VC","Vegetarian","Virus","Volleyball","Volunteer","vote","VoteBoard","WarAndPeace",
		"WebDesign","WebGames","WeiQi","West_Volunteer","WesternstyleChess","WHU","Wisdom","WorldFootball","WorldNews","WuShu","WuXi",
		"XiBei","XiNan","xinhongji","XinJiang","XJTU","XMU","XuZhou","YanCheng","YangTaiChi","YangtzeDelta","YangZhou","YOGA","ZheJiang",
		"ZhenJiang","ZhuangXiu","Zjl_Online","ZJU","ZSU"};

}
