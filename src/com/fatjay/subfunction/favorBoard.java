package com.fatjay.subfunction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fatjay.R;
import com.fatjay.function.favor;
import com.fatjay.main.LilyActivity;
import com.fatjay.main.userinfo;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class favorBoard extends ListActivity implements ListView.OnScrollListener {
	Map<Integer, String> dataMap = new HashMap<Integer, String>();
	BaseAdapter mAdapter = new listAdapter(this);
	String next = null;
	String orignial = "";
	String url = new String();
	Context mContext;
	ProgressDialog waitDialog;
	Boolean isFavor = false;
	Boolean onlyone = false;
	String boardname;
	userinfo mUserinfo = (userinfo) getApplication();
	int pageId;
	private TextView loadmoreTextView;
	private LinearLayout loading;
	
	private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private GestureDetector gestureDetector;
	View.OnTouchListener gestureListener;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.favorboard);
		mUserinfo = (userinfo) getApplication();
		SharedPreferences favor = getSharedPreferences("favor", 0);
		String favorlist = favor.getString("favor", null);
		Intent mIntent = getIntent();
		mContext = getApplicationContext();
		Bundle mBundle = mIntent.getExtras();
		url = mBundle.getString("url");
		pageId = mBundle.getInt("id");

		waitDialog = ProgressDialog.show(getParent(), "", "正在加载...", true, true);
		getlist(url);
		boardname = url.split("=")[1];
		
		if (favorlist.indexOf(boardname)!=-1) {
			isFavor = true;
		}
		
		final LinearLayout moreLayout = (LinearLayout) LinearLayout.inflate(this, R.layout.list_foot, null); 
        loadmoreTextView = (TextView)moreLayout.findViewById(R.id.list_loadmore);
        //loadmoreTextView.setText("加载下一页");
        loading = (LinearLayout) moreLayout.findViewById(R.id.list_loading);
        
        //LinearLayout loadingLayout = new LinearLayout(this);  
        loadmoreTextView.setOnClickListener(new OnClickListener() {  
  
            @Override  
            public void onClick(View v) {
            	loadmoreTextView.setVisibility(View.GONE);
            	loading.setVisibility(View.VISIBLE);
            	getlist(next);
            }
        });  

		getListView().addFooterView(moreLayout);
		
		setListAdapter(mAdapter);
		getListView().setOnScrollListener(this);
		
		gestureDetector = new GestureDetector(new MyGestureDetector());
		OnTouchListener gesture = new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				Log.d("guesture", "captured...");
				if (gestureDetector.onTouchEvent(event)) {
                    return true;
                }
				return false;
			}
		};
		getListView().setOnTouchListener(gesture);
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (gestureDetector.onTouchEvent(event)) {
                    return true;
                }
                return false;
            }
        };
	}

	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
		/*
		int lastItem = firstVisibleItem + visibleItemCount;
		if (((listAdapter)mAdapter).list.size()!=0) {
			if (lastItem == mAdapter.getCount() && onlyone) {
				onlyone = false;
				getlist(next);
			} else {
				onlyone = true;
			}
		}
		*/
	}

	public void onScrollStateChanged(AbsListView view,int scrollState){
//		switch (scrollState){
//		  // 当不滚动时
//			case OnScrollListener.SCROLL_STATE_IDLE:
//				// 判断滚动到底部
//				if (view.getLastVisiblePosition() >= (((listAdapter)mAdapter).list.size()-2)) {
//					getlist(next);
//				}
//				break;
//		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		System.out.println("clicked!");
		String[] temp = dataMap.get( ((listAdapter)mAdapter).list.get(position) ).split("#");
		String url = temp[4];
		Intent startread = new Intent(favorBoard.this, threadContent.class);
		Bundle mBundle = new Bundle();
		mBundle.putString("url", url);
		mBundle.putString("title", temp[3]);
		mBundle.putString("board", mUserinfo.boardname.get(boardname));
		startread.putExtras(mBundle);
		favorBoard.this.startActivity(startread);
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
					((listAdapter)mAdapter).notifyDataSetChanged();
					waitDialog.cancel();
					loadmoreTextView.setVisibility(View.VISIBLE);
	            	loading.setVisibility(View.GONE);
					break;
				default:
					super.handleMessage(msg);
			}
		}
	};
	
	public void refresh() {
		((listAdapter)mAdapter).list.clear();
		dataMap.clear();
		((listAdapter)mAdapter).notifyDataSetChanged();
		getlist(orignial);
		((listAdapter)mAdapter).notifyDataSetChanged();
	}

	private ExecutorService executorService = Executors.newFixedThreadPool(5);
	
	private void getlist(final String url) {
		executorService.submit(new Runnable() {
			public void run() {
	    		Message msg = new Message();
	    		msg.what = 0;
				String urltemp = url;
				int topthread = 200000;
				if (!urltemp.contains("bbstdoc")) {
					String[] temp = urltemp.split("bbsdoc");
					urltemp = temp[0] + "bbstdoc" + temp[1];
				}
				if (orignial=="") {
					orignial = urltemp;
				}
				boolean nextpageStarter = true;
				try {
					Document doc = Jsoup.connect(urltemp).get();
					Elements raw_threads = doc.select("tr");
					for (Element each : raw_threads ) {
						Elements links = each.select("a");
						if (links.size()==0)
							continue;
						Elements parts = each.select("td");
						if (parts.size()!=6)
							continue;
						String threadID = null;
						if (parts.get(0).select("img").size()!=0)
							threadID = String.valueOf(--topthread);
						else {
							if (nextpageStarter) {
								threadID = parts.get(0).text();
								int i = Integer.valueOf(threadID).intValue() - 22;
								next = orignial + "&start=" + String.valueOf(i);
								nextpageStarter = false;
							} else {
								threadID = parts.get(0).text();
							}
						}
						if (dataMap.containsKey(threadID))
							continue;
						//String threadSTATUS = parts.get(1).text();
						String threadAUTHOR = parts.get(2).text();
						String threadAUTHOR_LINK = parts.get(2).select("a").attr("abs:href");
						String threadDATE = parts.get(3).text();
						String threadTITLE = parts.get(4).select("a").text();
						String threadURL = parts.get(4).select("a").attr("abs:href");
						String threadINFO = parts.get(5).toString();
						threadINFO = threadINFO.substring(threadINFO.indexOf("<font"), threadINFO.indexOf("</td"));
						dataMap.put(Integer.valueOf(threadID), threadAUTHOR + '#' + threadAUTHOR_LINK + '#' + threadDATE + '#' + threadTITLE + '#' + threadURL + '#' + threadINFO);
						((listAdapter)mAdapter).list.add(Integer.valueOf(threadID));
					}
					Collections.sort(((listAdapter)mAdapter).list);
					Collections.reverse(((listAdapter)mAdapter).list);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				handler.sendMessage(msg);
		    }
				
		});
	}
	
	class MyGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                // right to left swipe
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                	favor.instance.switchActivity(pageId + 1);
                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                	favor.instance.switchActivity(pageId - 1);
                }
            } catch (Exception e) {
                // nothing
            }
            return false;
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event))
	        return true;
	    else
	    	return false;
    }
	
	private class listAdapter extends BaseAdapter {
		List<Integer> list = new ArrayList<Integer>();
		Context mContext;
		
		public listAdapter(Context context) {
			// TODO Auto-generated constructor stub
			mContext = context;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			if (list.size() == 0) {
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
			threadView tView;
			String[] strings;
			if (arg1 == null) {
				strings = (dataMap.get(list.get(arg0))).split("#");
				String title = strings[3];
				String time = strings[2];
				String author = strings[0];
				String info = strings[5];
				tView = new threadView(mContext, String.valueOf(list.get(arg0)), title, time, author, info);
			} else {
				strings = (dataMap.get(list.get(arg0))).split("#");
				String title = strings[3];
				String time = strings[2];
				String author = strings[0];
				String info = strings[5];
				tView = (threadView)arg1;
				tView.setNo(String.valueOf(list.get(arg0)));
				tView.setTitle(title);
				tView.setTime(time);
				tView.setAuthor(author);
				tView.setInfo(info);
			}
			return tView;
		}
		
		class threadView extends LinearLayout {
			private TextView noTextView;
			private TextView titleTextView;
			private TextView timeTextView;
			private TextView authorTextView;
			private TextView infoTextView;
			
			threadView(Context context, String id, String title, String time, String author, String info) {
				super(context);
				this.setOrientation(VERTICAL);
				
				LayoutInflater mInflater = getLayoutInflater();
				RelativeLayout item = new RelativeLayout(context);
				item = (RelativeLayout) mInflater.inflate(R.layout.favorboard_list_item, null);
				noTextView = (TextView)(item.findViewById(R.id.favorboard_item_id));
				noTextView.setTextSize(15);
				if (Integer.valueOf(id)>199980) {
					noTextView.setTextColor(Color.RED);
					noTextView.setText("Top");
				} else {
					noTextView.setTextColor(Color.BLUE);
					noTextView.setText(id);
				}
				titleTextView = (TextView)(item.findViewById(R.id.favorboard_item_title));
				titleTextView.setText(title);
				titleTextView.setTextColor(Color.rgb(41, 36, 33));
				titleTextView.setTextSize(20);
				titleTextView.setPadding(4, 0, 0, 0);
				authorTextView = (TextView)(item.findViewById(R.id.favorboard_item_author));
				authorTextView.setText("由 " + author + "发表于 ");
				authorTextView.setTextColor(Color.BLUE);
				authorTextView.setTextSize(16);
				infoTextView = (TextView)(item.findViewById(R.id.favorboard_item_info));
				infoTextView.setText(Html.fromHtml(info));
				infoTextView.setTextSize(16);
				timeTextView = (TextView)(item.findViewById(R.id.favorboard_item_time));
				timeTextView.setText(time);
				timeTextView.setTextColor(Color.BLUE);
				timeTextView.setTextSize(16);
				
				addView(item, new LinearLayout.LayoutParams(
	                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			}
			
			public void setTitle(String title) {
				titleTextView.setTextColor(Color.rgb(41, 36, 33));
				titleTextView.setText(title);
	        }
			
			public void setNo(String id) {
				if (Integer.valueOf(id)>199980) {
					noTextView.setTextColor(Color.RED);
					noTextView.setText("Top");
				} else {
					noTextView.setTextColor(Color.BLUE);
					noTextView.setText(id);
				}
	        }
			
			public void setAuthor(String author) {
				authorTextView.setText("由 " + author + "发表于 ");
	        }
			
			public void setTime(String time) {
				timeTextView.setText(time);
	        }
			
			public void setInfo(String info) {
				infoTextView.setText(Html.fromHtml(info));
	        }
		}
	}
}
