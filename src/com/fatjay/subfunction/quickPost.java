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
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;

import com.fatjay.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
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
	
    private int timeoutConnection = 10000;  
    private int timeoutSocket = 10000;
    
    private int IMAGE_MAX_SIZE = 1000;
	
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
        ((EditText)findViewById(R.id.quick_title)).setText("无主题");
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
				Toast.makeText(getApplicationContext(), "请先指定版面", Toast.LENGTH_SHORT);
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
						Toast.makeText(getApplicationContext(), "网络似乎有点问题，请稍后重试", Toast.LENGTH_SHORT);
					}
				} else {
					Toast.makeText(getApplicationContext(), "指定的版面无效", Toast.LENGTH_SHORT);
				}
			}
			break;
		case R.id.quick_photo:
			if (textView.getText().toString().equals("")) {
				Toast.makeText(getApplicationContext(), "请先指定版面", Toast.LENGTH_SHORT);
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
					for (int i=0; i<boards.length; i ++) {
						if (boardname.equals(boards[i])) {
							if (i>=451) {
								boardname = boards[i-451];
							}
						}
					}
					doPickPhotoAction();
				} else {
					Toast.makeText(getApplicationContext(), "指定的版面无效", Toast.LENGTH_SHORT);
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
        Bitmap photoCaptured = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        switch (requestCode) {
			case CAMERA_WITH_DATA: 
	            if (resultCode != RESULT_OK) return;
	            if (photoUri == null) {
					return;
				}
				try {
					photoCaptured = decodeStream(photoUri);
	                ByteArrayOutputStream baos = new ByteArrayOutputStream();
	
	                is_compress = ((CheckBox)findViewById(R.id.quick_compress)).isChecked();
	                if (is_compress) {
	                	photoCaptured.compress(Bitmap.CompressFormat.JPEG, compress_rate, baos);
					} else {
						photoCaptured.compress(Bitmap.CompressFormat.JPEG, 80, baos);
					}
	                
	                byte[] photoBytes = baos.toByteArray();
	                Time t=new Time();
	                t.setToNow();
	                String year = String.valueOf(t.year);
	                String month = String.valueOf(t.month+1);
	                String day = String.valueOf(t.monthDay);
	                String hour;
	                if (t.hour<10) {
	    				hour = "0" + String.valueOf(t.hour);
	    			} else {
	    				hour = String.valueOf(t.hour);
	    			}
	                String minute;
	                if (t.minute<10) {
	                	minute = "0" + String.valueOf(t.minute);
	    			} else {
	    				minute = String.valueOf(t.minute);
	    			}
	                String second;
	                if (t.second<10) {
	                	second = "0" + String.valueOf(t.second);
	    			} else {
	    				second = String.valueOf(t.second);
	    			}
	    			filenameString = "lily_" + year + month + day + hour + minute + second + ".jpeg";
	    			
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
		        waitDialog.setMessage("请稍候...");
		        waitDialog.setTitle("正在上传照片");
		        waitDialog.setIndeterminate(true);
		        waitDialog.setCancelable(false);
		        waitDialog.show();
	            uploadFile2Svr();
	        	break;
			case PHOTO_PICKED_WITH_DATA:
				if (resultCode != RESULT_OK) {
					return;
				}
				Uri picUri = data.getData();
				if (picUri == null) {
					return;
				}
				try {
					photoCaptured = decodeStream(picUri);
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
	                String year = String.valueOf(t.year);
	                String month = String.valueOf(t.month+1);
	                String day = String.valueOf(t.monthDay);
	                String hour;
	                if (t.hour<10) {
	    				hour = "0" + String.valueOf(t.hour);
	    			} else {
	    				hour = String.valueOf(t.hour);
	    			}
	                String minute;
	                if (t.minute<10) {
	                	minute = "0" + String.valueOf(t.minute);
	    			} else {
	    				minute = String.valueOf(t.minute);
	    			}
	                String second;
	                if (t.second<10) {
	                	second = "0" + String.valueOf(t.second);
	    			} else {
	    				second = String.valueOf(t.second);
	    			}
	    			filenameString = "lily_" + year + month + day + hour + minute + second + ".jpeg";
	    			
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
                                Toast.makeText(getApplicationContext(), "没有SD卡", Toast.LENGTH_LONG);
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
            String year = String.valueOf(t.year);
            String month = String.valueOf(t.month+1);
            String day = String.valueOf(t.monthDay);
            String hour;
            if (t.hour<10) {
				hour = "0" + String.valueOf(t.hour);
			} else {
				hour = String.valueOf(t.hour);
			}
            String minute;
            if (t.minute<10) {
            	minute = "0" + String.valueOf(t.minute);
			} else {
				minute = String.valueOf(t.minute);
			}
            String second;
            if (t.second<10) {
            	second = "0" + String.valueOf(t.second);
			} else {
				second = String.valueOf(t.second);
			}
			String filenameString = "lily_" + year + month + day + hour + minute + second + ".jpeg";
			File outFile = new File(PHOTO_DIR, filenameString);
            final Intent intent = getTakePickIntent(outFile);  
            startActivityForResult(intent, CAMERA_WITH_DATA);  
        } catch (ActivityNotFoundException e) {  
            Toast.makeText(getApplicationContext(), "无法创建图片",  
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
            Toast.makeText(getApplicationContext(), "无法打开相册",  
                    Toast.LENGTH_LONG).show();  
        }  
    }  
      
    // 封装请求Gallery的intent  
    public static Intent getPhotoPickIntent() {  
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);  
        intent.setType("image/*");
        intent.putExtra("return-data", true);  
        return intent;  
    } 
    
    private Bitmap decodeStream(Uri uri){
        Bitmap b = null;
        try {
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            InputStream fis = getContentResolver().openInputStream(uri);
            BitmapFactory.decodeStream(fis,null,o);

            int scale = 1;
            if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
                scale = (int)Math.pow(2, (int) Math.round(Math.log(IMAGE_MAX_SIZE / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
            }

            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            //fis = new FileInputStream(file);
            fis = getContentResolver().openInputStream(uri);
            b = BitmapFactory.decodeStream(fis, null, o2);
            fis.close();
        } catch (IOException e) {
        }
        return b;
    }
	
    /*
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
    */

    /**  
    * Constructs an intent for image cropping. 调用图片剪辑程序  
    */  
    /*
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
    */
	
	private boolean getIdentify() {
		try {
			Random random = new Random();
			int s = random.nextInt(99999)%(90000) + 10000;
			if (username == "") {
				Toast.makeText(getApplicationContext(), "请先设置帐号信息", Toast.LENGTH_LONG).show();
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
		Uri uri = null;
		Bundle extras;
		try {
			if (it != null &&  it.getAction() != null && it.getAction().equals(Intent.ACTION_SEND)) {
				extras = it.getExtras();
				if (extras.containsKey("android.intent.extra.STREAM")) {
					Log.i(TAG, "uri++=" + extras.get("android.intent.extra.STREAM"));
					uri = (Uri) extras.get("android.intent.extra.STREAM");
				}
			}
			if (uri==null) {
				return;
			}
            Bitmap photoCaptured = decodeStream(uri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            photoCaptured.compress(Bitmap.CompressFormat.JPEG, compress_rate, baos);
            
            byte[] photoBytes = baos.toByteArray();
            Time t=new Time();
            t.setToNow();
            String year = String.valueOf(t.year);
            String month = String.valueOf(t.month+1);
            String day = String.valueOf(t.monthDay);
            String hour;
            if (t.hour<10) {
				hour = "0" + String.valueOf(t.hour);
			} else {
				hour = String.valueOf(t.hour);
			}
            String minute;
            if (t.minute<10) {
            	minute = "0" + String.valueOf(t.minute);
			} else {
				minute = String.valueOf(t.minute);
			}
            String second;
            if (t.second<10) {
            	second = "0" + String.valueOf(t.second);
			} else {
				second = String.valueOf(t.second);
			}
            filenameString = "lily_" + year + month + day + hour + minute + second + ".jpeg";
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
        waitDialog.setMessage("请稍候...");
        waitDialog.setTitle("正在上传照片");
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
				case 0:
					waitDialog.dismiss();
					if (!picURL.equals("")) {
			        	String oldString = ((EditText)findViewById(R.id.quick_content)).getText().toString();
			            ((EditText)findViewById(R.id.quick_content)).setText(oldString + picURL);
			            Toast.makeText(getApplicationContext(), "图片上传成功！", Toast.LENGTH_SHORT);
					} else {
						Toast.makeText(getApplicationContext(), "图片上传失败！", Toast.LENGTH_SHORT);
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
		        HttpClient client;
		        BasicHttpParams httpParameters = new BasicHttpParams();// Set the timeout in milliseconds until a connection is established.  
			    HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			    HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
			    client = new DefaultHttpClient(httpParameters);
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
					} 
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					handler.sendMessage(msg);
				} catch (ConnectTimeoutException e) {
					// TODO: handle exception
					Toast.makeText(getApplicationContext(), "网络似乎有些问题，如果要上传刚才的图片，请到媒体库中选中", Toast.LENGTH_LONG);
					picURL = "";
					handler.sendMessage(msg);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					handler.sendMessage(msg);
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
	    		HttpClient client;
		        BasicHttpParams httpParameters = new BasicHttpParams();// Set the timeout in milliseconds until a connection is established.  
			    HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			    HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
			    client = new DefaultHttpClient(httpParameters);
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
								File temp = new File("/sdcard/lily/temp/" + filenameString);
					            if (temp.exists())
					            	temp.delete();
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
					handler.sendMessage(msg);
				} catch (ConnectTimeoutException e) {
					// TODO: handle exception
					Toast.makeText(getApplicationContext(), "网络似乎有些问题，如果要上传刚才的图片，请到媒体库中选中", Toast.LENGTH_LONG);
					picURL = "";
					handler.sendMessage(msg);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					handler.sendMessage(msg);
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
			Toast.makeText(getApplicationContext(), "你的网络似乎有点问题...", Toast.LENGTH_SHORT);
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
		"ZhenJiang","ZhuangXiu","Zjl_Online","ZJU","ZSU", "南京大屠杀", "第七艺术", "飞越重洋", "学术讲座", "保险精算", "广告艺术"
		, "百合共创", "宇航", "代理", "人工智能", "个人写真", "算法"
		, "美利坚之窗", "淮水皖风", "站务公告栏", "反腐倡廉", "反谣言中心", "苹果电脑"
		, "古文观止", "考古", "ASCII艺术", "社团联合会主席信箱", "星月童话", "大气科学论坛"
		, "车迷世界", "羽毛球", "篮球", "BBS的安装与设置", "新手求助", "本站的各类统计列表与记录"
		, "首都北京", "台球", "比特洪流", "温馨祝福", "博客论坛", "版主监督和管理"
		, "南大战网游戏", "版主竞选与请辞", "桌面游戏", "版面开设与变更", "高矮胖瘦", "Borland世界"
		, "保龄球馆", "拳击与格斗", "男生世界", "桥牌联谊会", "行走英伦", "布袋戏"
		, "校务公告", "国际学院", "中国书法", "中国科学院", "中美中心", "礼仪中心"
		, "计算流体论坛", "锦绣常州", "聊天版", "化学版", "象棋", "中国足球"
		, "国内新闻", "声乐艺术爱好者（南大合唱团）", "理财"
		, "基督教研究", "春秋战国 ", "公务员之家", "古典诗词", "古韵悠长", "古典音乐"
		, "收藏爱好", "动漫世界", "投诉与举报", "电脑菜鸟要学飞", "咨询", "大学生竞赛"
		, "对联", "注册会计师", "C++程序设计语言", "中国药科大学", "穿越海峡", "十字绣坊"
		, "魔方", "中国地质大学", "中国矿业大学", "自行车运动", "中文系", "计算机系"
		, "地球科学与工程学院", "电子科学与工程学院", "历史系"
		, "材料科学系", "数学系", "哲学系", "物理学院", "社会保障系", "舞蹈天地"
		, "数据库系统", "辩者无敌", "侦探推理", "德文堂", "数码音乐设备", "数码世界"
		, "匡亚明学院_强化部", "神秘之旅", "碟碟不休", "白山黑水", "远古之守护", ".net技术", "戏剧春秋", "绘画艺术", "青春有梦"
		, "酒吧与咖啡馆", "DV工作室", "电子商务特区", "电子竞技", "地球科学", "经济学", "教育论坛", "电子技术版", "电子音乐"
		, "嵌入式系统", "武侠小说", "英语世界", "英语聊天版", "世说新语", "时尚男生"
		, "版务评优", "交换生天地", "一级方程式赛车", "外国文学", "七色花", "家庭生活"
		, "反哺学社", "奇幻天地", "美丽流行风", "复旦大学", "对外交流协会", "感情世界"
		, "科幻世界", "金融天下", "碧波垂钓", "健美与健身", "闪客世界", "跳蚤市场"
		, "花草园艺", "民谣及乡村音乐", "国乐飘香（民乐团）", "雅舍谈吃", "Fortran语言", "百合论坛"
		, "浪漫法兰西", "友情久久", "八闽畅怀", "天文爱好者协会", "研究生英语俱乐部", "地理科学"
		, "女生天地", "地理信息科学", "够级艺术", "高考招生信息", "电脑图形处理", "GRE&TOEFL专题讨论"
		, "脑筋急转弯", "希腊罗马", "南大环境保护协会", "粤是故乡名", "八桂大地", "罪与罚"
		, "吉它", "古琴社", "黑客的摇篮", "天涯海角", "精致手工", "硬件工作室"
		, "燕赵大地", "九州之中", "河海大学", "发烧天堂", "历史", "香港大学"
		, "游子情深", "同一片天空", "战场", "高性能计算", "淮水楚云", "荆楚大地"
		, "人类漫谈", "三湘四水", "掀起你的盖头来", "清谈雅思", "美术研究院_雕塑艺术研究所", "海外教育学院"
		, "图像世界", "信息管理技术", "塞外风情", "实习", "国际关系", "IT俱乐部"
		, "IT认证考试", "和风艺影", "日语学习", "Java语言", "爵士蓝调", "江南西道"
		, "吉林大学", "创业与求职", "就业特快", "笑话版", "新闻传播研究", "江苏体育"
		, "考研天地", "卡拉永远OK", "恋恋韩风", "法律学", "学术交流", "花果山下"
		, "生活", "生命协会", "生命科学", "百合精华", "百合站庆", "百合友情链接"
		, "小百合工作室项目反馈", "语言与语言学", "Linux和Unix", "失物招领", "情爱悠悠", "物流与供应链管理协会"
		, "兰州大学", "教务处处长信箱", "心理中心主任信箱", "工会主席信箱版", "研究生院院长信箱", "研究生会主席信箱"
		, "保卫处处长信箱", "校医院院长信箱", "就业创业指导中心主任信箱", "团委书记信箱", "图书馆馆长信箱", "后勤工作信箱"
		, "网络中心主任信箱", "学生工作处处长信箱", "学生会主席信箱", "魔术", "麻将", "管理学"
		, "模式动物研究所", "营销学社", "鬼故事", "数学版", "数学工具软件", "媒介文化研究"
		, "医学与健康", "似水流年", "电磁场与微波技术", "军事科学", "手机天地", "模型空间"
		, "现代诗歌", "露天电影院", "微软技术俱乐部", "美丽的微软窗口", "MUD人生", "音乐剧之家"
		, "神话传说", "姓名文化", "古都南京", "纳米科技", "江风海韵", "人与自然"
		, "网络资源", "网络世界", "新世纪音乐", "新手上路", "桫椤双树园", "房屋租赁"
		, "南京农业大学", "南京医科大学", "南京师范大学", "南大研究生报", "南大和园", "南京大学腾讯创新俱乐部"
		, "南大青年报", "南京大学知行社", "南大校园生活", "模拟联合国协会", "南京邮电大学", "南京工业大学"
		, "南开大学", "本本梦工厂 ", "酸甜苦辣留言版", "小说", "南京航空航天大学", "南京理工大学"
		, "南京中医药大学", "上班一族", "网络游戏", "奥林匹克运动", "南大交响乐团", "中国海洋大学"
		, "民俗民风", "百合原创", "海外游子", "涂鸦论坛", "兼职工作信息", "杀手的童话"
		, "电脑游戏", "青春伊甸园", "朋辈咨询", "人物", "个人文集", "宠物乐园"
		, "哲学与思考", "摄影艺术", "物理学", "钢琴艺术", "贴图版", "北京大学"
		, "政治科学", "流行音乐天地", "博士后之家", "程序员的休闲室", "心理健康", "浦园风景线"
		, "Python语言", "曲苑杂谈", "空中梦想家", "读书", "阳光工作室", "房地产"
		, "红十字运动", "五子连珠", "射一射老虎", "摇滚乐世界", "寝室夜话", "田径"
		, "风雪俄罗斯", "天文与空间科学学院", "大气科学学院", "商学院", "化学化工学院", "教育研究院"
		, "环境学院", "外国语学院", "地理学院", "政府管理学院", "研究生之家", "信息管理学院"
		, "新闻传播学院", "法学院", "生命科学院", "医学院", "工程管理学院", "社会学院"
		, "软件学院", "三国风云", "建筑与城市规划学院", "学生职业发展协会", "雕塑艺术", "阳光海岸（华南网友版）"
		, "欧美电视剧", "东南大学", "齐鲁青未了", "寻梦海上花", "三晋梦萦", "购物天堂"
		, "短信大家聊", "综艺大秀场", "国际大学生企业家联盟", "缥缈四国", "单身一族", "南大思源社"
		, "升级艺术", "上海交通大学", "溜冰人生", "淡烟人生", "软件工程", "软件天地"
		, "南大学生心理协会", "西班牙语", "体坛快讯", "股市风云", "石头城", "红楼逸梦"
		, "故事会", "快乐数独", "超级女声", "西楚下相", "雨渍东吴", "游泳"
		, "站长的工作室", "乒乓球", "跆拳道", "宝岛之恋", "古韵泰州", "古意中医"
		, "网球天地", "这是站内测试版", "科技文献排版", "理论计算机科学", "论文", "清华大学"
		, "南大天健社", "九河下梢天津卫 ", "雪域桑烟", "交通信息", "汽笛声声", "遍览天下"
		, "电视", "电视游戏", "城市规划", "美日研究", "中国科技大学", "校长信箱"
		, "Visual C++ 版", "素食者", "可恶可怕的病毒", "排球版", "青年志愿者协会", "本站各项投票与结果"
		, "选举版", "百年好合", "网站设计", "网页游戏", "围棋", "支教岁月"
		, "国际象棋", "武汉大学", "儒释道", "世界足球", "国际新闻", "中华武术"
		, "梁溪寄畅", "壮哉大西北", "风起西南", "新鸿基社", "我们新疆亚克西", "西安交通大学"
		, "厦门大学", "古彭汉风", "登瀛渔火", "杨式太极拳协会", "长江三角洲发展论坛", "五亭烟雨"
		, "南大瑜伽", "钱江潮", "古城镇江", "家居装修", "珠江路热线", "浙江大学", "中山大学"};

}
