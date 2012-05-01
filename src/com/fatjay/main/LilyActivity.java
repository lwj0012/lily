package com.fatjay.main;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.jsoup.Jsoup;

import com.fatjay.R;
import com.fatjay.function.allboard;
import com.fatjay.function.favor;
import com.fatjay.function.hotpot;
import com.fatjay.function.options;
import com.fatjay.function.top;

import dalvik.system.VMRuntime;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Display;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class LilyActivity extends TabActivity {
	TabHost mHost;
	Context mContext;
	RadioGroup rGroup;
	userinfo mUserinfo;
	ImageView spotlight;
	int current_select = 0;
	private int width;
	public static LilyActivity instance;
	Map<String, Object> data = new HashMap<String, Object>();
	
	private final static float TARGET_HEAP_UTILIZATION = 0.75f;
	private final static int CWJ_HEAP_SIZE = 6* 1024* 1024;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        VMRuntime.getRuntime().setTargetHeapUtilization(TARGET_HEAP_UTILIZATION);
        VMRuntime.getRuntime().setMinimumHeapSize(CWJ_HEAP_SIZE); 
        
        setContentView(R.layout.main);
        mUserinfo = (userinfo) getApplication();
        mContext = this;
        getIdentify();
        mHost = getTabHost();
        instance = this;
        Display display = getWindowManager().getDefaultDisplay(); 
		display.getHeight();  
		width = display.getWidth();
        spotlight = (ImageView)findViewById(R.id.select_spot);
        
        SharedPreferences favor = mContext.getSharedPreferences("favor", 0);
        String favorBoard = favor.getString("favor", null);
        if (favorBoard == null) {
        	SharedPreferences.Editor mEditor = favor.edit();
            mEditor.putString("favor", "D_Computer#Pictures");
            mEditor.commit();
		}
        favor = mContext.getSharedPreferences("account", 0);
        String username = favor.getString("account", null);
        String password = favor.getString("password", null);
        if (username != null && password!=null) {
			mUserinfo.setUsername(username);
			mUserinfo.setPwd(password);
		}
        
        mHost.addTab(mHost.newTabSpec("top").setIndicator("top").setContent(new Intent(mContext, top.class)));
        mHost.addTab(mHost.newTabSpec("favor").setIndicator("Favor").setContent(new Intent(mContext, favor.class)));
        mHost.addTab(mHost.newTabSpec("hotpot").setIndicator("HotPot").setContent(new Intent(mContext, hotpot.class)));
        mHost.addTab(mHost.newTabSpec("allboard").setIndicator("AllBoard").setContent(new Intent(mContext, allboard.class)));
        mHost.addTab(mHost.newTabSpec("options").setIndicator("Options").setContent(new Intent(mContext, options.class)));

        rGroup = (RadioGroup)findViewById(R.id.rg);
        
        rGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				int idx = -1;
				if (checkedId == R.id.rb01) {
					idx = 0;
				} else if (checkedId == R.id.rb02) {
					idx = 1;
				} else if (checkedId == R.id.rb03) {
					idx = 2;
				} else if (checkedId == R.id.rb04) {
					idx = 3;
				} else if (checkedId == R.id.rb05) {
					idx = 4;
				}
				switchActivity(idx);
			}
		});
    }
        
	public void switchActivity(int idx) {
		int n = mHost.getCurrentTab();
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
		
		Animation spotAnimation = new TranslateAnimation( (float)(n*width/5), (float)(idx*width/5), 0.0f, 0.0f);
		spotAnimation.setFillAfter(true);
		spotAnimation.setDuration(400);
		spotlight.startAnimation(spotAnimation);
		RadioButton rb = (RadioButton) rGroup.getChildAt(idx);
		rb.setChecked(true);
	}

	private boolean getIdentify() {
		try {
			Random random = new Random();
			int s = random.nextInt(99999)%(90000) + 10000;
			SharedPreferences favor = mContext.getSharedPreferences("account", 0);
	        String username = favor.getString("account", null);
	        String password = favor.getString("password", null);
			if (username==null ) {
				Toast.makeText(getApplicationContext(), "请先设置帐号信息", Toast.LENGTH_LONG).show();
				return false;
			}
			String urlString = "http://bbs.nju.edu.cn/vd" + String.valueOf(s) + "/bbslogin?type=2&id=" + username + "&pw=" + password;
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
				mUserinfo.setCookies("_U_KEY=" + _U_KEY + "; " + "_U_UID=" + _U_UID + "; " + "_U_NUM=" + _U_NUM + ";");
				mUserinfo.setUsername(username);
				mUserinfo.setPwd(password);
				mUserinfo.setCode("vd" + String.valueOf(s));
				Toast.makeText(getApplicationContext(), "登录成功!", Toast.LENGTH_SHORT);
				return true;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	protected void put(String key, String value) {
		data.put(key, value);
	}
	
	protected Object get(String key) {
		return data.get(key);
	}

}