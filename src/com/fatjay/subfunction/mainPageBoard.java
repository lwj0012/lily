package com.fatjay.subfunction;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.fatjay.R;
import com.fatjay.main.LilyActivity;

import android.app.AlertDialog;
import android.app.ExpandableListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;


public class mainPageBoard extends ExpandableListActivity {
	ExpandableListAdapter mAdapter;
	Map<String, String> boardlist = new HashMap<String, String>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mainpage);
		mAdapter = new MyExpandableListAdapter();
		
		getAllBoard();
        setListAdapter(mAdapter);
        registerForContextMenu(getExpandableListView());
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		// TODO Auto-generated method stub
		Intent mIntent = new Intent(mainPageBoard.this, threadList.class);
		Bundle mBundle = new Bundle();
		String boardName = ((MyExpandableListAdapter)mAdapter).children.get(groupPosition).get(childPosition);
		boardName = boardName.split("#")[0];
		mBundle.putString("url", "http://bbs.nju.edu.cn/bbstdoc?board=" + boardName);
		mIntent.putExtras(mBundle);
		mainPageBoard.this.startActivity(mIntent);
		return true;
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

	private String getAllBoard() {
    	String myString = null;
    	((MyExpandableListAdapter)mAdapter).groups.add("    本站系统");
    	((MyExpandableListAdapter)mAdapter).groups.add("    南京大学");
    	((MyExpandableListAdapter)mAdapter).groups.add("    乡情校谊");
    	((MyExpandableListAdapter)mAdapter).groups.add("    电脑技术");
    	((MyExpandableListAdapter)mAdapter).groups.add("    学术科学");
    	((MyExpandableListAdapter)mAdapter).groups.add("    文化艺术");
    	((MyExpandableListAdapter)mAdapter).groups.add("    体育娱乐");
    	((MyExpandableListAdapter)mAdapter).groups.add("    感性休闲");
    	((MyExpandableListAdapter)mAdapter).groups.add("    新闻信息");
    	((MyExpandableListAdapter)mAdapter).groups.add("    百合广角");
    	((MyExpandableListAdapter)mAdapter).groups.add("    校务信箱");
    	((MyExpandableListAdapter)mAdapter).groups.add("    社团群体");

    	
		try {
			String newurlString = "http://bbs.nju.edu.cn/cache/t_forum.js";
            URL mUrl = new URL(newurlString);
            HttpURLConnection getjs = (HttpURLConnection) mUrl.openConnection();
			getjs.setDoOutput(true);
			getjs.setRequestMethod("GET");
			getjs.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
			getjs.connect();
			InputStream in = getjs.getInputStream(); 
            BufferedReader reader = new BufferedReader(new InputStreamReader(in,"gb2312")); 
            String inputLine = null;  
            StringBuffer sb = new StringBuffer();  
            while ((inputLine = reader.readLine()) != null) {  
                sb.append(inputLine);
            }
            reader.close();  
            in.close();  
    		String[] allboard = sb.toString().split("s:");
    		ArrayList<String> sub = null;
    		for (int i=1; i< 13; i++) {
    			sub = new ArrayList<String>();
    			int start = allboard[i].indexOf("[") + 3;
    			int end = allboard[i].indexOf("]}") - 2;
    			String temp = allboard[i].substring( start, end );
    			String[] boardStrings = temp.split(" *',' *");
    			for (int j = 0; j < 6; j++) {
					String tempString = boardStrings[2*j] + "#" + boardStrings[2*j+1];
					sub.add(tempString);
				}
    			((MyExpandableListAdapter)mAdapter).children.add(sub);
    		}
    	} catch (Exception e) {
    		myString = e.getMessage();
    		e.printStackTrace();
    	}
    	return myString;
    }

	public class MyExpandableListAdapter extends BaseExpandableListAdapter {
        // Sample data set.  children[i] contains the children (String[]) for groups[i].
        public ArrayList<String> groups = new ArrayList<String>();
        public ArrayList<ArrayList<String>> children = new ArrayList<ArrayList<String>>();
        
        public Object getChild(int groupPosition, int childPosition) {
            return children.get(groupPosition).get(childPosition);
        }

        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        public int getChildrenCount(int groupPosition) {
        	if (children.size()==0) {
				return 0;
			} else {
				return children.get(groupPosition).size();
			}
        }

        public TextView getGenericView() {
            // Layout parameters for the ExpandableListView
            AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT, 64);

            TextView textView = new TextView(mainPageBoard.this);
            textView.setLayoutParams(lp);
            // Center the text vertically
            textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            // Set the text starting position
            textView.setPadding(40, 0, 0, 0);
            return textView;
        }
        
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                View convertView, ViewGroup parent) {
            TextView textView = getGenericView();
            textView.setText(getChild(groupPosition, childPosition).toString());
            textView.setTextSize(15);
            
            return textView;
        }

        public Object getGroup(int groupPosition) {
            return groups.get(groupPosition);
        }

        public int getGroupCount() {
            return groups.size();
        }

        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                ViewGroup parent) {
            TextView textView = getGenericView();
            textView.setText(getGroup(groupPosition).toString());
            return textView;
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        public boolean hasStableIds() {
            return true;
        }
    }
}