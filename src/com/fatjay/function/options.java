package com.fatjay.function;

import java.io.IOException;
import java.util.Random;
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
import com.fatjay.main.LilyActivity;
import com.fatjay.main.about_the_author;
import com.fatjay.main.userinfo;
import com.fatjay.subfunction.mail;
import com.fatjay.subfunction.moreFavor;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class options extends Activity implements OnItemSelectedListener {
	userinfo mUserinfo = null;
	LilyActivity main;
	Context mContext;
	private Spinner spinner;
	private ArrayAdapter<String> adapter;
	private static final String[] m={"20","30","40","50","60","80"};
	
	private int timeoutSocket = 10000;
	private int timeoutConnection = 10000;
	
	ProgressDialog waitDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);
		mContext = this;
		main = (LilyActivity)getParent();
		mUserinfo = (userinfo) getApplication();
		SharedPreferences favor = mContext.getSharedPreferences("account", 0);
        String userString = favor.getString("account", null);
        String passwordString = favor.getString("password", null);
        ((EditText)findViewById(R.id.username)).setText(userString);
        ((EditText)findViewById(R.id.password)).setText(passwordString);
        
		Button mButton = (Button)findViewById(R.id.setting_test);
		mButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				getIdentify();
				waitDialog = ProgressDialog.show(getParent(), "", "正在同步收藏...", true, true);
				loadFav();
			}
		});
		spinner = (Spinner)findViewById(R.id.setting_photo);
		adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,m);  
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);  
        spinner.setAdapter(adapter);  
        spinner.setOnItemSelectedListener(this);  
        spinner.setVisibility(View.VISIBLE);
        favor = mContext.getSharedPreferences("compress_rate", 0);
        String compress_rate = favor.getString("rate", null);
        for (int i = 0; i < m.length; i++) {
			if (m[i].equals(compress_rate) ) {
				spinner.setSelection(i);
			}
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
        	main.switchActivity(0);
        	return true;
        }else{
            return super.onKeyDown(keyCode, event);
        }
    }
	
	private boolean getIdentify() {
		try {
			Random random = new Random();
			int s = random.nextInt(99999)%(90000) + 10000;
			String username = ((EditText)findViewById(R.id.username)).getText().toString();
			String pwd = ((EditText)findViewById(R.id.password)).getText().toString();
			String urlString = "http://bbs.nju.edu.cn/vd" + String.valueOf(s) + "/bbslogin?type=2&id=" + username + "&pw=" + pwd;
			String doc = Jsoup.connect(urlString).get().toString();
			int t = doc.indexOf("setCookie");
			if (t == -1) {
				Toast.makeText(getApplicationContext(), "log in failed! try again!", Toast.LENGTH_SHORT).show();
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
				mUserinfo.setCookies("_U_KEY=" + _U_KEY + "; " + "_U_UID=" + _U_UID + "; " + "_U_NUM=" + _U_NUM + ";");
				mUserinfo.setUsername(username);
				mUserinfo.setCode("vd" + String.valueOf(s));
				mUserinfo.setPwd(pwd);
				
				CheckBox rem = (CheckBox)findViewById(R.id.setting_remember);
				if (rem.isChecked()) {
					SharedPreferences favor = mContext.getSharedPreferences("account", 0);
			        SharedPreferences.Editor mEditor = favor.edit();
			        mEditor.putString("account", username);
			        mEditor.putString("password", pwd);
			        mEditor.commit();
				}
				
		        Toast.makeText(getApplicationContext(), "log in successfully!", Toast.LENGTH_SHORT).show();
		        ((LilyActivity)getParent()).switchActivity(0);
				return true;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), "log in failed! try again!", Toast.LENGTH_SHORT).show();
			return false;
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		SharedPreferences favor = mContext.getSharedPreferences("compress_rate", 0);
        SharedPreferences.Editor mEditor = favor.edit();
        mEditor.putString("rate", m[arg2]);
        mEditor.commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.add(Menu.NONE, Menu.FIRST + 1, 5, "意见反馈").setIcon(android.R.drawable.ic_menu_search);
		menu.add(Menu.NONE, Menu.FIRST + 2, 5, "关于本软件").setIcon(android.R.drawable.ic_dialog_info);
        return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case Menu.FIRST + 1:
			Intent send = new Intent();
			send.setClass(options.this, mail.class);
			Bundle mBundle = new Bundle();
			mBundle.putString("action", "advice");
			mBundle.putString("mailto", "lwj0012");
			send.putExtras(mBundle);
			options.this.startActivity(send);
			break;
		case Menu.FIRST + 2:
			Intent about = new Intent(options.this, about_the_author.class);
			options.this.startActivity(about);
		}
		return true;
	}
	
	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
	
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
				case 0:
					waitDialog.cancel();
					Intent mIntent = new Intent(options.this, moreFavor.class);
					options.this.startActivity(mIntent);
					break;
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
					Toast.makeText(options.this, "你还没有在百合上面预订任何版面，将默认为你添加三个版面", Toast.LENGTH_LONG);
				} else {
					mEditor.putString("favor", sync_favor.substring(0, sync_favor.length()-1));
					mEditor.commit();
				}
				handler.sendMessage(msg);
			}
		});
	}
}
