package com.fatjay.function;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fatjay.R;
import com.fatjay.main.LilyActivity;
import com.fatjay.subfunction.hotbrd;
import com.fatjay.subfunction.mainPageBoard;
import com.fatjay.subfunction.recbrd;
import com.fatjay.subfunction.searchBoard;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.animation.AnimationUtils;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class allboard extends TabActivity {
	TabHost mHost;
	Context mContext;
	RadioGroup rGroup;
	public int pageid=0;
	public Map<Integer, ArrayList<String>> data = new HashMap<Integer, ArrayList<String>>();
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.allboard);
        //parsePage();														//性能瓶颈
        mHost = getTabHost();
        mContext = this;
        Intent searchBoard = new Intent(mContext, searchBoard.class);
        mHost.addTab(mHost.newTabSpec("search").setIndicator("搜索板面").setContent(searchBoard));
        Intent mainPage = new Intent(mContext, mainPageBoard.class);
        mHost.addTab(mHost.newTabSpec("mainpage").setIndicator("首页板面").setContent(mainPage));
        Intent hotbrd = new Intent(mContext, hotbrd.class);
        mHost.addTab(mHost.newTabSpec("hotbrd").setIndicator("热门版面").setContent(hotbrd));
        Intent recbrd = new Intent(mContext, recbrd.class);
        mHost.addTab(mHost.newTabSpec("recbrd").setIndicator("推荐板面").setContent(recbrd));

        rGroup = (RadioGroup)findViewById(R.id.allboard_rg);
        RadioButton rb = (RadioButton) rGroup.getChildAt(0);
		rb.setChecked(true);
        rGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				int idx = -1;
				if (checkedId == R.id.allboard_search) {
					idx = 0;
					pageid = idx;
				} else if (checkedId == R.id.allboard_mainpage) {
					idx = 1;
					pageid = idx;
				} else if (checkedId == R.id.allboard_hotbrd) {
					idx = 2;
					pageid = idx;
				} else if (checkedId == R.id.allboard_recbrd) {
					idx = 3;
					pageid = idx;
				} 
				switchActivity(idx);
			}
		});
    }
    
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	        if(keyCode == KeyEvent.KEYCODE_BACK){
	        	LilyActivity.instance.switchActivity(0);
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
        
	void switchActivity(int idx) {
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
		
		RadioButton rb = (RadioButton) rGroup.getChildAt(idx);
		rb.setChecked(true);
	}
	
	void parsePage() {
		Document doc = null;
		int counter = -1;
		Elements info = null;
		String threadurl;
		String threadname;
		String boardurl;
		String boardname;
		try {
			doc = Jsoup.connect("http://bbs.nju.edu.cn/bbstopall").get();
			Elements itemsElements = doc.select("td");
			for (Element each : itemsElements) {
				if (each.select("img").size()!=0) {
					counter ++;
				} else {
					info = each.select("a");
					if (info.size()==0) {
						continue;
					}
					threadurl = info.get(0).attr("abs:href");
					threadname = info.get(0).text();
					boardurl = info.get(1).attr("abs:href");
					boardname = info.get(1).text();
					if (data.containsKey(Integer.valueOf(counter))) {
						data.get(Integer.valueOf(counter)).add(threadurl + '#' + threadname + '#' + boardurl + '#' + boardname);
					} else {
						data.put(Integer.valueOf(counter), new ArrayList<String>());
						(data.get(Integer.valueOf(counter))).add(threadurl + '#' + threadname + '#' + boardurl + '#' + boardname);
					}
					
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
