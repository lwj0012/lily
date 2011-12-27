package com.fatjay.subfunction;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
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

public class hotbrd extends ListActivity {
	Map<String, String> dataMap = new HashMap<String, String>();
	BaseAdapter mAdapter;
	ProgressDialog waitDialog;
	userinfo mUserinfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hotbrd);
		mUserinfo = (userinfo) getApplication();
		mAdapter = new hotAdapter(this);
		Button refresh = (Button)findViewById(R.id.hotbrd_refresh);
		refresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//waitDialog = ProgressDialog.show(threadList.this, "Loading...", "正在刷新...", true);
				((hotAdapter)mAdapter).list.clear();
				dataMap.clear();
				((hotAdapter)mAdapter).notifyDataSetChanged();
				getInfo();
			}
		});
		getInfo();
		setListAdapter(mAdapter);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		String boardname = ((hotAdapter)mAdapter).list.get(position);
		Intent startread = new Intent(hotbrd.this, threadList.class);
		Bundle mBundle = new Bundle();
		String urlString = "http://bbs.nju.edu.cn/bbsdoc?board=" + boardname;
		mBundle.putString("url", urlString);
		startread.putExtras(mBundle);
		hotbrd.this.startActivity(startread);
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
					((hotAdapter)mAdapter).notifyDataSetChanged();
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
				String url = "http://bbs.nju.edu.cn/cache/t_hotbrd.js";
				String info = null;
				HttpGet uploadGet = new HttpGet(url);
				HttpClient client = new DefaultHttpClient();
				client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
				uploadGet.addHeader("Content-Type", "text/html;charset=UTF-8");
				HttpResponse httpResponse;
				try {
					httpResponse = client.execute(uploadGet);
					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						info = EntityUtils.toString(httpResponse.getEntity());
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
					String numString = eachStrings[3].substring(3);
					dataMap.put(boardnameString, bm + "#" + numString);
					((hotAdapter)mAdapter).list.add(boardnameString);
				}
				handler.sendMessage(msg);
		    }
				
		});
	}
	
	
	private class hotAdapter extends BaseAdapter {
		ArrayList<String> list = new ArrayList<String>();
		Context mContext;
		
		public hotAdapter(Context context) {
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
			hotView hView;
			String[] strings;
			if (arg1 == null) {
				strings = (dataMap.get(list.get(arg0))).split("#");
				String title = list.get(arg0);
				String hot = strings[1];
				String bm = strings[0];
				hView = new hotView(mContext, title, hot, bm);
			} else {
				strings = (dataMap.get(list.get(arg0))).split("#");
				String title = list.get(arg0);
				String hot = strings[1];
				String bm = strings[0];
				hView = (hotView)arg1;
				hView.setTitle(title);
				hView.setHot(hot);
				hView.setBm(bm);
			}
			return hView;
		}
	}
	
	private class hotView extends LinearLayout {
		private TextView titleTextView;
		private TextView hotTextView;
		private TextView bmTextView;
		
		hotView(Context context, String title, String hot, String bm) {
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
			hotTextView = new TextView(context);
			hotTextView.setText(hot + "人  ");
			if (Integer.valueOf(hot)>100) {
				hotTextView.setTextColor(Color.RED);
			} else {
				hotTextView.setTextColor(Color.BLACK);
			}
			hotTextView.setTextSize(16);
			RelativeLayout.LayoutParams hotLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			hotLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
			hotTextView.setLayoutParams(hotLayoutParams);
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
			first_row.addView(hotTextView);
			second_row.addView(bmTextView);
			toprow_layout.addView(first_row);
			toprow_layout.addView(second_row);
			addView(toprow_layout, new LinearLayout.LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		}
		
		public void setTitle(String title) {
			titleTextView.setText(mUserinfo.boardname.get(title));
        }
		
		public void setHot(String hot) {
			hotTextView.setText(hot + "人  ");
			if (Integer.valueOf(hot)>100) {
				hotTextView.setTextColor(Color.RED);
			} else {
				hotTextView.setTextColor(Color.BLACK);
			}
        }
		
		public void setBm(String bm) {
			bmTextView.setText("斑竹： " + bm);
			bmTextView.setTextColor(Color.BLUE);
			bmTextView.setTextSize(16);
        }		
	}
}
