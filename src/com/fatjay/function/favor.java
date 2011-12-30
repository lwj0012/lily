package com.fatjay.function;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
import com.fatjay.subfunction.searchDlg;

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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AnimationUtils;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class favor extends TabActivity {
	TabHost mHost;
	Context mContext;
	RadioGroup rGroup;
	String originfavor = "";
	userinfo mUserinfo;
	ArrayList<TabHost.TabSpec> tabSpecs = new ArrayList<TabHost.TabSpec>();
	Map<String, Object> data = new HashMap<String, Object>();
	
	private int timeoutSocket = 10000;
	private int timeoutConnection = 10000;
	
	ProgressDialog waitDialog;
	
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
        RadioButton rb = (RadioButton) rGroup.getChildAt(0);
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
			favor.this.startActivityForResult(send, 13);
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
		switch (resultCode) {
		case RESULT_OK:
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
					Toast.makeText(favor.this, "你还没有在百合上面预订任何版面，将默认为你添加三个版面", Toast.LENGTH_LONG);
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
	
		if (idx < n) {
			mHost.getCurrentView().startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_left_out));
		} else if (idx > n) {
			mHost.getCurrentView().startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_right_out));
		}
		mHost.setCurrentTab(idx);
		if (idx < n) {
			mHost.getCurrentView().startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_left_in));
		} else if (idx > n) {
			mHost.getCurrentView().startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_right_in));
		}
		
		RadioButton rb = (RadioButton) rGroup.getChildAt(idx);
		rb.setChecked(true);
	}
	
	
	
	protected void put(String key, String value) {
		data.put(key, value);
	}
	
	protected Object get(String key) {
		return data.get(key);
	}
}
