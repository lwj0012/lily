package com.fatjay.subfunction;

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
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;

import com.fatjay.R;
import com.fatjay.main.LilyActivity;
import com.fatjay.main.userinfo;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class recbrd extends ListActivity {
	Map<String, String> dataMap = new HashMap<String, String>();
	BaseAdapter mAdapter;
	ProgressDialog waitDialog;
	userinfo mUserinfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recbrd);
		mUserinfo = (userinfo) getApplication();
		Button refresh = (Button)findViewById(R.id.recbrd_refresh);
		refresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//waitDialog = ProgressDialog.show(threadList.this, "Loading...", "正在刷新...", true);
				((recAdapter)mAdapter).list.clear();
				dataMap.clear();
				((recAdapter)mAdapter).notifyDataSetChanged();
				getInfo();
			}
		});
		mAdapter = new recAdapter(this);
		
		getInfo();
		
		setListAdapter(mAdapter);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		String boardname = ((recAdapter)mAdapter).list.get(position);
		Intent startread = new Intent(recbrd.this, threadList.class);
		Bundle mBundle = new Bundle();
		String urlString = "http://bbs.nju.edu.cn/bbsdoc?board=" + boardname;
		mBundle.putString("url", urlString);
		startread.putExtras(mBundle);
		recbrd.this.startActivity(startread);
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
	
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
				case 0:
					//((contentAdapter)mAdapter).notifyDataSetChanged();
					waitDialog.cancel();
					((recAdapter)mAdapter).notifyDataSetChanged();
					break;
				default:
					super.handleMessage(msg);
			}
		}
	};

	private ExecutorService executorService = Executors.newFixedThreadPool(5);
	
	private void getInfo() {
		waitDialog = ProgressDialog.show(getParent(), "", "正在加载...", true, true);
		executorService.submit(new Runnable() {
			public void run() {
	    		Message msg = new Message();
	    		msg.what = 0;
				String url = "http://bbs.nju.edu.cn/cache/t_recbrd.js";
				String info = "";
				HttpGet uploadGet = new HttpGet(url);
				HttpClient client = new DefaultHttpClient();
				client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
				HttpResponse httpResponse;
				try {
					httpResponse = client.execute(uploadGet);
					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						info = EntityUtils.toString(httpResponse.getEntity());
						info = new String(info.getBytes(), "gb2312");
					}
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String[] infos = info.split("brd:");
				for (int i = 1; i < infos.length; i++) {
					String temp = infos[i].substring(0, infos[i].indexOf("}"));
					String[] eachStrings = temp.split(",");
					String boardnameString = eachStrings[0].substring(1, eachStrings[0].length()-1);
					String bm = eachStrings[1].substring(4, eachStrings[1].length()-1);
					dataMap.put(boardnameString, bm);
					((recAdapter)mAdapter).list.add(boardnameString);
				}
				handler.sendMessage(msg);
		    }
				
		});
	}
	
	
	private class recAdapter extends BaseAdapter {
		ArrayList<String> list = new ArrayList<String>();
		Context mContext;
		
		public recAdapter(Context context) {
			// TODO Auto-generated constructor stub
			mContext = context;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			if (list.size()==0) {
				return 0;
			} else {
				return list.size();
			}
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			// TODO Auto-generated method stub
			recView hView;
			String[] strings;
			if (arg1 == null) {
				strings = (dataMap.get(list.get(arg0))).split("#");
				String title = list.get(arg0);
				String bm = strings[0];
				hView = new recView(mContext, title, bm);
			} else {
				strings = (dataMap.get(list.get(arg0))).split("#");
				String title = list.get(arg0);
				String bm = strings[0];
				hView = (recView)arg1;
				hView.setTitle(title);
				hView.setBm(bm);
			}
			return hView;
		}
	}
	
	private class recView extends LinearLayout {
		private TextView titleTextView;
		private TextView bmTextView;
		
		recView(Context context, String title, String bm) {
			super(context);
			this.setOrientation(VERTICAL);
			LinearLayout toprow_layout = new LinearLayout(context);
			toprow_layout.setOrientation(VERTICAL);
			
			RelativeLayout first_row = new RelativeLayout(context);
			titleTextView = new TextView(context);
			titleTextView.setText(mUserinfo.boardname.get(title));
			titleTextView.setTextColor(Color.BLUE);
			titleTextView.setTextSize(16);
			RelativeLayout.LayoutParams titleLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			titleLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
			titleTextView.setLayoutParams(titleLayoutParams);

			RelativeLayout second_row = new RelativeLayout(context);
			bmTextView = new TextView(context);
			bmTextView.setText("斑竹： " + bm);
			bmTextView.setTextColor(Color.BLUE);
			bmTextView.setTextSize(16);
			RelativeLayout.LayoutParams authorLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			authorLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
			bmTextView.setLayoutParams(authorLayoutParams);
			
			first_row.addView(titleTextView);
			second_row.addView(bmTextView);
			toprow_layout.addView(first_row);
			toprow_layout.addView(second_row);
			addView(toprow_layout, new LinearLayout.LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		}
		
		public void setTitle(String title) {
			titleTextView.setText(mUserinfo.boardname.get(title));
        }
		
		public void setBm(String bm) {
			bmTextView.setText("斑竹： " + bm);
			bmTextView.setTextColor(Color.BLUE);
			bmTextView.setTextSize(16);
        }		
	}
}
