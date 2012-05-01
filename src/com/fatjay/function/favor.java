package com.fatjay.function;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fatjay.R;
import com.fatjay.main.userinfo;
import com.fatjay.subfunction.favorBoard;
import com.fatjay.subfunction.moreFavor;
import com.fatjay.subfunction.newThread;
import com.fatjay.subfunction.searchDlg;

import android.R.anim;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout.LayoutParams;

public class favor extends TabActivity {
	TabHost mHost;
	Context mContext;
	RadioGroup rGroup;
	String originfavor = "";
	userinfo mUserinfo;
	ArrayList<TabHost.TabSpec> tabSpecs = new ArrayList<TabHost.TabSpec>();
	Map<String, Object> data = new HashMap<String, Object>();
	ProgressDialog waitDialog;
	private int timeoutSocket = 10000;
	private int timeoutConnection = 10000;
	private String[] favorsStrings;
	
	private Button buttonNew, buttonMain, buttonPhoto, buttonRefresh, buttonFavor, buttonMode;
	private Animation animationTranslate, animationRotate, animationScale;
	private static int width, height;
	private LayoutParams params = new LayoutParams(0, 0);
	private static Boolean isClick = false;
	private static Boolean isFavor = true;
	
	private static final int FAVOR_ORGANIZE = 2012;
	
	public static favor instance;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favo);
        mUserinfo = (userinfo) getApplication();
        instance = this;
        mContext = this;
        mHost = getTabHost();
        SharedPreferences favor = mContext.getSharedPreferences("favor", 0);
        String favorBoard = favor.getString("favor", null);
        originfavor = favorBoard;
        initialButton();
        String[] favorlist = favorBoard.split("#");
        rGroup = (RadioGroup)findViewById(R.id.favor_rg);
        rGroup.setBackgroundColor(Color.rgb(128, 138, 135));
        rGroup.setScrollbarFadingEnabled(true);
        int counter = 0;
        LayoutInflater mInflater = getLayoutInflater();
        for (String board : favorlist) {
        	Intent mIntent = new Intent(favor.this, favorBoard.class);
        	Bundle mBundle = new Bundle();
        	mBundle.putString("url", "http://bbs.nju.edu.cn/bbstdoc?board=" + board);
        	mBundle.putInt("id", counter);
        	mIntent.putExtras(mBundle);
        	TabHost.TabSpec mSpec = mHost.newTabSpec(board);
        	mSpec.setContent(mIntent);
        	mSpec.setIndicator(board);
        	tabSpecs.add(mSpec);

        	RadioButton mRadioButton = new RadioButton(this);
        	mRadioButton = (RadioButton) mInflater.inflate(R.layout.radiobutton, null);
        	mRadioButton.setText(mUserinfo.boardname.get(board));
        	rGroup.addView(mRadioButton, counter);
        	
        	counter ++;
        }
        
        for (TabHost.TabSpec tabSpec : tabSpecs) {
			mHost.addTab(tabSpec);
		}
        favorsStrings = favorlist;
        RadioButton rb = (RadioButton) rGroup.getChildAt(0);
		if (favorBoard.contains(favorsStrings[0])) {
			isFavor = true;
			buttonFavor.setBackgroundResource(R.drawable.ic_button_favorite_add);
		} else {
			isFavor = false;
			buttonFavor.setBackgroundResource(R.drawable.ic_button_favorite_delete);
		}
		rb.setChecked(true);
        rGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				int idx = -1;
				int childcount = group.getChildCount();
				for (int i = 0; i < childcount; i++) {
					if (group.getChildAt(i).getId()==checkedId) {
						idx = i;
						break;
					}
				}
				switchActivity(idx);
			}
		});
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.add(Menu.NONE, Menu.FIRST + 1, 5, "管理收藏").setIcon(android.R.drawable.ic_menu_edit);
		menu.add(Menu.NONE, Menu.FIRST + 2, 6, "同步收藏").setIcon(android.R.drawable.ic_menu_add);
        return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case Menu.FIRST + 1:
			Intent send = new Intent();
			send.setClass(favor.this, moreFavor.class);
			favor.this.startActivityForResult(send, FAVOR_ORGANIZE);
			//finish();
			break;
		case Menu.FIRST + 2:
			waitDialog = ProgressDialog.show(this, "", "正在同步收藏...", true, true);
			loadFav();
			break;
		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case FAVOR_ORGANIZE:
				if (resultCode != RESULT_OK) {
					return;
				}
				Bundle mBundle = data.getExtras();
				String newFavor = mBundle.getString("favor");
				String[] newStrings = newFavor.split("#");
				if (!originfavor.equals(newFavor)) {
					mHost.setCurrentTab(0);
					mHost.clearAllTabs();
					tabSpecs.clear();
					rGroup.removeAllViews();
			        int counter = 0;
			        LayoutInflater mInflater = getLayoutInflater();
			        for (String board : newStrings) {
			        	Intent mIntent = new Intent(favor.this, favorBoard.class);
			        	Bundle newBundle = new Bundle();
			        	newBundle.putString("url", "http://bbs.nju.edu.cn/bbstdoc?board=" + board);
			        	mBundle.putInt("id", counter);
			        	mIntent.putExtras(newBundle);
			        	TabHost.TabSpec mSpec = mHost.newTabSpec(board);
			        	mSpec.setContent(mIntent);
			        	mSpec.setIndicator(board);
			        	tabSpecs.add(mSpec);
			        	
			        	RadioButton mRadioButton = new RadioButton(this);
			        	mRadioButton = (RadioButton) mInflater.inflate(R.layout.radiobutton, null);
			        	mRadioButton.setText(mUserinfo.boardname.get(board));
			        	rGroup.addView(mRadioButton, counter);
			        	
			        	counter ++;
			        }
			        for (TabHost.TabSpec tabSpec : tabSpecs) {
						mHost.addTab(tabSpec);
					}
			        favorsStrings = newStrings;
			        mHost.setCurrentTab(0);
			        RadioButton rb = (RadioButton) rGroup.getChildAt(0);
					rb.setChecked(true);
				}
				break;
	
			default:
				break;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			new AlertDialog.Builder(this)
				.setTitle("")
				.setMessage("Are u sure to quit?")
				.setNegativeButton(R.string.quit_cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				})
				.setPositiveButton(R.string.quit_ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						finish();
					}
				}).show();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_SEARCH) {
			Intent search = new Intent(favor.this, searchDlg.class);
			favor.this.startActivity(search);
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}
	 
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    
	    System.exit(0);
	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			Intent mIntent = new Intent(favor.this, moreFavor.class);
			switch (msg.what) {
				case 0:
					waitDialog.cancel();
					favor.this.startActivity(mIntent);
					break;
				case 1:
					Toast.makeText(getApplicationContext(), "你还没有在百合上面预订任何版面，将默认为你添加三个版面", Toast.LENGTH_LONG);
					favor.this.startActivity(mIntent);
				default:
					super.handleMessage(msg);
			}
		}
	};

	private ExecutorService executorService = Executors.newFixedThreadPool(5);
	
	public void loadFav() {
		executorService.submit(new Runnable() {
			public void run() {
	    		Message msg = new Message();
	    		msg.what = 0;
				SharedPreferences favor = mContext.getSharedPreferences("favor", 0);
				Editor mEditor = favor.edit();
				String code = mUserinfo.getCode();
				String cookie = mUserinfo.getCookies();
				String urlString = "http://bbs.nju.edu.cn/" + code + "/bbsmybrd";
				String sync_favor = "";
				String result = null;
				try {
					System.gc();
					HttpClient client;
			        BasicHttpParams httpParameters = new BasicHttpParams();// Set the timeout in milliseconds until a connection is established.  
				    HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection );
				    HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket );
				    client = new DefaultHttpClient(httpParameters);
					client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
					HttpGet uploadGet = new HttpGet(urlString);
		            uploadGet.addHeader("Cookie", cookie);
		            HttpResponse httpResponse = client.execute(uploadGet);
		            if (httpResponse.getStatusLine().getStatusCode() == 200) {
						result = EntityUtils.toString(httpResponse.getEntity());
		            }
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ConnectTimeoutException e) {
					// TODO: handle exception
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Document doc = Jsoup.parse(result);
				Elements boards = doc.select("input[checked]");
				for (Element board : boards) {
					String boardName = board.nextSibling().toString();
					boardName = boardName.substring(1, boardName.length());
					boardName = boardName.substring(boardName.indexOf(">")+1);
					boardName = boardName.substring(0, boardName.indexOf("("));
					sync_favor = sync_favor + boardName + "#";
				}
				if (sync_favor.equals("")) {
					mEditor.putString("favor", "D_Computer#Joke#Pictures");
					mEditor.commit();
					msg.what = 1;
					handler.sendMessage(msg);
				} else {
					mEditor.putString("favor", sync_favor.substring(0, sync_favor.length()-1));
					mEditor.commit();
					handler.sendMessage(msg);
				}
				
		    }
		});
	}
	
	public void switchActivity(int idx) {
		int n = mHost.getCurrentTab();
		SharedPreferences favor = mContext.getSharedPreferences("favor", MODE_PRIVATE);
		String favorBoard = favor.getString("favor", null);
		if (favorBoard.contains(favorsStrings[idx])) {
			isFavor = true;
			buttonFavor.setBackgroundResource(R.drawable.ic_button_favorite_add);
		} else {
			isFavor = false;
			buttonFavor.setBackgroundResource(R.drawable.ic_button_favorite_delete);
		}
		
		if (idx < n) {
			mHost.getCurrentView().startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_right_out));
		} else if (idx > n) {
			mHost.getCurrentView().startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_left_out));
		}
		mHost.setCurrentTab(idx);
		if (idx < n) {
			mHost.getCurrentView().startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_right_in));
		} else if (idx > n) {
			mHost.getCurrentView().startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_left_in));
		}
		
		RadioButton rb = (RadioButton) rGroup.getChildAt(idx);
		rb.setChecked(true);
	}

	private void initialButton() 
	{
		// TODO Auto-generated method stub
		Display display = getWindowManager().getDefaultDisplay(); 
		height = display.getHeight();  
		width = display.getWidth();
		Log.v("width  & height is:", String.valueOf(width) + ", " + String.valueOf(height));
		
		android.widget.RelativeLayout.LayoutParams params = new android.widget.RelativeLayout.LayoutParams(0, 0);
		params.height = 50;
		params.width = 50;
		params.setMargins(10, height - 200, 0, 0);
		
		buttonMode = (Button) findViewById(R.id.favor_mode);
		buttonMode.setLayoutParams(params);
		
		buttonFavor = (Button) findViewById(R.id.favor_favor);
		buttonFavor.setLayoutParams(params);
		
		buttonRefresh = (Button) findViewById(R.id.favor_refresh);
		buttonRefresh.setLayoutParams(params);
		
		buttonPhoto = (Button) findViewById(R.id.favor_camera);
		buttonPhoto.setLayoutParams(params);

		buttonNew = (Button) findViewById(R.id.favor_new);
		buttonNew.setLayoutParams(params);
		
		buttonMain = (Button) findViewById(R.id.favor_function);		
		buttonMain.setLayoutParams(params);
		
		buttonMain.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				// TODO Auto-generated method stub					
				if(isClick == false)
				{
					isClick = true;
					buttonMain.startAnimation(animRotate(-45.0f, 0.5f, 0.5f));					
					buttonNew.startAnimation(animTranslate(0.0f, -150.0f, 10, height - 350, buttonNew, 80));
					buttonPhoto.startAnimation(animTranslate(57.0f, -138.0f, 67, height - 338, buttonPhoto, 100));
					buttonRefresh.startAnimation(animTranslate(105.0f, -105.0f, 115, height - 305, buttonRefresh, 120));
					buttonFavor.startAnimation(animTranslate(138.0f, -57.0f, 148, height - 257, buttonFavor, 140));
					buttonMode.startAnimation(animTranslate(150.0f, 0.0f, 160, height - 200, buttonMode, 160));
				}
				else
				{
					isClick = false;
					buttonMain.startAnimation(animRotate(90.0f, 0.5f, 0.5f));
					buttonNew.startAnimation(animTranslate(0.0f, 150.0f, 10, height - 200, buttonNew, 180));
					buttonPhoto.startAnimation(animTranslate(-57.0f, 138.0f, 10, height - 200, buttonPhoto, 160));
					buttonRefresh.startAnimation(animTranslate(-105.0f, 105.0f, 10, height - 200, buttonRefresh, 140));
					buttonFavor.startAnimation(animTranslate(-138.0f, 57.0f, 10, height - 200, buttonFavor, 120));
					buttonMode.startAnimation(animTranslate(-150.0f, 0.0f, 10, height - 200, buttonMode, 80));
				}
			}
		});
		buttonNew.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				buttonNew.startAnimation(setAnimScale(2.5f, 2.5f));
				buttonPhoto.startAnimation(setAnimScale(0.0f, 0.0f));	
				buttonRefresh.startAnimation(setAnimScale(0.0f, 0.0f));
				buttonFavor.startAnimation(setAnimScale(0.0f, 0.0f));
				buttonMode.startAnimation(setAnimScale(0.0f, 0.0f));
				buttonMain.startAnimation(setAnimScale(0.0f, 0.0f));
				String url = "http://bbs.nju.edu.cn/bbstdoc?board=" + favorsStrings[mHost.getCurrentTab()];
				Intent mIntent = new Intent(favor.this, newThread.class);
				Bundle mBundle = new Bundle();
				mBundle.putString("action", "new");
				mBundle.putString("title", "");
				mBundle.putString("url", url);
				mIntent.putExtras(mBundle);
				if (isClick == true) {
					isClick = false;
					buttonMain.startAnimation(animRotate(90.0f, 0.5f, 0.45f));
					
					buttonNew.startAnimation(animTranslate(0.0f, 150.0f, 10, height - 200, buttonNew, 180));
					buttonPhoto.startAnimation(animTranslate(-57.0f, 138.0f, 10, height - 200, buttonPhoto, 160));
					buttonRefresh.startAnimation(animTranslate(-105.0f, 105.0f, 10, height - 200, buttonRefresh, 140));
					buttonFavor.startAnimation(animTranslate(-138.0f, 57.0f, 10, height - 200, buttonFavor, 120));
					buttonMode.startAnimation(animTranslate(-150.0f, 0.0f, 10, height - 200, buttonMode, 80));
				}
				favor.this.startActivity(mIntent);
			}
		});
		buttonPhoto.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				buttonPhoto.startAnimation(setAnimScale(2.5f, 2.5f));	
				buttonNew.startAnimation(setAnimScale(0.0f, 0.0f));	
				buttonRefresh.startAnimation(setAnimScale(0.0f, 0.0f));
				buttonFavor.startAnimation(setAnimScale(0.0f, 0.0f));
				buttonMode.startAnimation(setAnimScale(0.0f, 0.0f));
				buttonMain.startAnimation(setAnimScale(0.0f, 0.0f));
				String url = "http://bbs.nju.edu.cn/bbstdoc?board=" + favorsStrings[mHost.getCurrentTab()];
				Intent mIntent = new Intent(favor.this, newThread.class);
				Bundle mBundle = new Bundle();
				mBundle.putString("action", "new");
				mBundle.putString("title", "");
				mBundle.putString("url", url);
				mIntent.putExtras(mBundle);
				if (isClick == true) {
					isClick = false;
					buttonMain.startAnimation(animRotate(90.0f, 0.5f, 0.45f));
					buttonNew.startAnimation(animTranslate(0.0f, 150.0f, 10, height - 200, buttonNew, 180));
					buttonPhoto.startAnimation(animTranslate(-57.0f, 138.0f, 10, height - 200, buttonPhoto, 160));
					buttonRefresh.startAnimation(animTranslate(-105.0f, 105.0f, 10, height - 200, buttonRefresh, 140));
					buttonFavor.startAnimation(animTranslate(-138.0f, 57.0f, 10, height - 200, buttonFavor, 120));
					buttonMode.startAnimation(animTranslate(-150.0f, 0.0f, 10, height - 200, buttonMode, 80));
				}
				favor.this.startActivity(mIntent);
			}
		});
		buttonRefresh.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				buttonRefresh.startAnimation(setAnimScale(2.5f, 2.5f));
				buttonPhoto.startAnimation(setAnimScale(0.0f, 0.0f));	
				buttonNew.startAnimation(setAnimScale(0.0f, 0.0f));	
				buttonFavor.startAnimation(setAnimScale(0.0f, 0.0f));
				buttonMode.startAnimation(setAnimScale(0.0f, 0.0f));
				buttonMain.startAnimation(setAnimScale(0.0f, 0.0f));
				if (isClick == true) {
					isClick = false;
					buttonMain.startAnimation(animRotate(90.0f, 0.5f, 0.45f));
					buttonNew.startAnimation(animTranslate(0.0f, 150.0f, 10, height - 200, buttonNew, 180));
					buttonPhoto.startAnimation(animTranslate(-57.0f, 138.0f, 10, height - 200, buttonPhoto, 160));
					buttonRefresh.startAnimation(animTranslate(-105.0f, 105.0f, 10, height - 200, buttonRefresh, 140));
					buttonFavor.startAnimation(animTranslate(-138.0f, 57.0f, 10, height - 200, buttonFavor, 120));
					buttonMode.startAnimation(animTranslate(-150.0f, 0.0f, 10, height - 200, buttonMode, 80));
				}
				Activity currentActivity = getCurrentActivity();
				if (currentActivity instanceof favorBoard) {
					((favorBoard) currentActivity).refresh();
				}
			}
		});
		buttonFavor.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				buttonFavor.startAnimation(setAnimScale(2.5f, 2.5f));
				buttonRefresh.startAnimation(setAnimScale(0.0f, 0.0f));
				buttonPhoto.startAnimation(setAnimScale(0.0f, 0.0f));	
				buttonNew.startAnimation(setAnimScale(0.0f, 0.0f));	
				buttonMode.startAnimation(setAnimScale(0.0f, 0.0f));
				buttonMain.startAnimation(setAnimScale(0.0f, 0.0f));
				
				if (isClick == true) {
					isClick = false;
					buttonMain.startAnimation(animRotate(90.0f, 0.5f, 0.45f));
					buttonNew.startAnimation(animTranslate(0.0f, 150.0f, 10, height - 200, buttonNew, 180));
					buttonPhoto.startAnimation(animTranslate(-57.0f, 138.0f, 10, height - 200, buttonPhoto, 160));
					buttonRefresh.startAnimation(animTranslate(-105.0f, 105.0f, 10, height - 200, buttonRefresh, 140));
					buttonFavor.startAnimation(animTranslate(-138.0f, 57.0f, 10, height - 200, buttonFavor, 120));
					buttonMode.startAnimation(animTranslate(-150.0f, 0.0f, 10, height - 200, buttonMode, 80));
				}
				
				SharedPreferences favor = mContext.getSharedPreferences("favor", MODE_PRIVATE);
				SharedPreferences.Editor mEditor = favor.edit();
				String favorBoard = favor.getString("favor", null);
				String boardname = favorsStrings[mHost.getCurrentTab()];
				if (isFavor) {
					String[] tempStrings = favorBoard.split("#");
					List<String> list = new LinkedList<String>();
			        for(int i = 0; i < tempStrings.length; i++) {
			            if(!list.contains(tempStrings[i]) && !tempStrings[i].equals(boardname)) {
			                list.add(tempStrings[i]);
			            }
			        }
			        favorBoard = "";
			        for (int j = 0; j < list.size(); j++) {
						favorBoard = favorBoard + "#" + list.get(j);
					}
			        favorBoard = favorBoard.substring(1);
					mEditor.putString("favor", favorBoard);
					mEditor.commit();
					isFavor = false;
					buttonFavor.setBackgroundResource(R.drawable.ic_button_favorite_delete);
				} else {
					mEditor.putString("favor", boardname + "#" + favorBoard);
					mEditor.commit();
					isFavor = true;
					buttonFavor.setBackgroundResource(R.drawable.ic_button_favorite_add);
				}
			}
		});
		buttonMode.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				buttonMode.startAnimation(setAnimScale(2.5f, 2.5f));
				buttonRefresh.startAnimation(setAnimScale(0.0f, 0.0f));
				buttonPhoto.startAnimation(setAnimScale(0.0f, 0.0f));	
				buttonNew.startAnimation(setAnimScale(0.0f, 0.0f));	
				buttonFavor.startAnimation(setAnimScale(0.0f, 0.0f));
				buttonMain.startAnimation(setAnimScale(0.0f, 0.0f));
				if (isClick == true) {
					isClick = false;
					buttonMain.startAnimation(animRotate(90.0f, 0.5f, 0.45f));
					buttonNew.startAnimation(animTranslate(0.0f, 150.0f, 10, height - 200, buttonNew, 180));
					buttonPhoto.startAnimation(animTranslate(-57.0f, 138.0f, 10, height - 200, buttonPhoto, 160));
					buttonRefresh.startAnimation(animTranslate(-105.0f, 105.0f, 10, height - 200, buttonRefresh, 140));
					buttonFavor.startAnimation(animTranslate(-138.0f, 57.0f, 10, height - 200, buttonFavor, 120));
					buttonMode.startAnimation(animTranslate(-150.0f, 0.0f, 10, height - 200, buttonMode, 80));
				}
			}
		});
	}
	
	protected Animation setAnimScale(float toX, float toY) 
	{
		// TODO Auto-generated method stub
		animationScale = new ScaleAnimation(1f, toX, 1f, toY, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.45f);
		animationScale.setInterpolator(favor.this, anim.accelerate_decelerate_interpolator);
		animationScale.setDuration(500);
		animationScale.setFillAfter(false);
		return animationScale;
	}
	
	protected Animation animRotate(float toDegrees, float pivotXValue, float pivotYValue) 
	{
		// TODO Auto-generated method stub
		animationRotate = new RotateAnimation(0, toDegrees, Animation.RELATIVE_TO_SELF, pivotXValue, Animation.RELATIVE_TO_SELF, pivotYValue);
		animationRotate.setAnimationListener(new AnimationListener() 
		{
			
			@Override
			public void onAnimationStart(Animation animation) 
			{
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) 
			{
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) 
			{
				// TODO Auto-generated method stub
				animationRotate.setFillAfter(true);
			}
		});
		return animationRotate;
	}
	
	protected Animation animTranslate(float toX, float toY, final int lastX, final int lastY,
			final Button button, long durationMillis) 
	{
		// TODO Auto-generated method stub
		animationTranslate = new TranslateAnimation(0, toX, 0, toY);				
		animationTranslate.setAnimationListener(new AnimationListener()
		{
						
			@Override
			public void onAnimationStart(Animation animation)
			{
				// TODO Auto-generated method stub
								
			}
						
			@Override
			public void onAnimationRepeat(Animation animation) 
			{
				// TODO Auto-generated method stub
							
			}
						
			@Override
			public void onAnimationEnd(Animation animation)
			{
				// TODO Auto-generated method stub
				params = new LayoutParams(0, 0);
				params.height = 50;
				params.width = 50;											
				params.setMargins(lastX, lastY, 0, 0);
				button.setLayoutParams(params);
				button.clearAnimation();
			}
		});																								
		animationTranslate.setDuration(durationMillis);
		return animationTranslate;
	}
}
