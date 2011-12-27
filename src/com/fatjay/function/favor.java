package com.fatjay.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.fatjay.R;
import com.fatjay.main.userinfo;
import com.fatjay.subfunction.favorBoard;
import com.fatjay.subfunction.moreFavor;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.animation.AnimationUtils;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class favor extends TabActivity {
	TabHost mHost;
	Context mContext;
	RadioGroup rGroup;
	String originfavor = "";
	userinfo mUserinfo;
	ArrayList<TabHost.TabSpec> tabSpecs = new ArrayList<TabHost.TabSpec>();
	Map<String, Object> data = new HashMap<String, Object>();
	
	
	
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
	        
	        //按下键盘上返回按钮

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
	        }else{        
	            return super.onKeyDown(keyCode, event);
	        }
	    }
	 
	 
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    
	    System.exit(0);
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
