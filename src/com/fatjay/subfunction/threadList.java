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
import android.text.method.SingleLineTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

public class threadList extends ListActivity implements ListView.OnScrollListener {
	Map<Integer, String> dataMap = new HashMap<Integer, String>();
	BaseAdapter mAdapter = new listAdapter(this);
	String next = null;
	String orignial = "";
	String url = new String();
	Context mContext;
	Boolean isFavor = false;
	String boardname = "";
	ProgressDialog waitDialog;
	boolean more = false;
	userinfo mUserinfo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.threadlist);
		mUserinfo = (userinfo) getApplication();
		SharedPreferences favor = getSharedPreferences("favor", 0);
		String favorlist = favor.getString("favor", null);
		Intent mIntent = getIntent();
		mContext = getApplicationContext();
		Bundle mBundle = mIntent.getExtras();
		url = mBundle.getString("url");
		getlist(url);
		boardname = url.split("=")[1];
		
		TextView mTextView = (TextView)findViewById(R.id.threadlist_title);
		mTextView.setText("当前版面: " + mUserinfo.boardname.get(boardname));
		mTextView.setTextSize(20);
		mTextView.setTransformationMethod(SingleLineTransformationMethod.getInstance());
		mTextView.setFocusable(true);
		Button refresh = (Button)findViewById(R.id.threadlist_refresh);
		refresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//waitDialog = ProgressDialog.show(threadList.this, "Loading...", "正在刷新...", true);
				((listAdapter)mAdapter).list.clear();
				dataMap.clear();
				((listAdapter)mAdapter).notifyDataSetChanged();
				getlist(orignial);
				((listAdapter)mAdapter).notifyDataSetChanged();
			}
		});
		final Button favoButton = (Button)findViewById(R.id.threadlist_favor);
		favoButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				SharedPreferences favor = mContext.getSharedPreferences("favor", MODE_PRIVATE);
				SharedPreferences.Editor mEditor = favor.edit();
				if (isFavor) {
					String favorBoard = favor.getString("favor", null);
					if (favorBoard.indexOf(boardname)!=-1) {
						if (favorBoard.indexOf("#" + boardname)!=-1) {
							String[] temp = favorBoard.split("#"+boardname);
							if (temp.length==2) {
								favorBoard = temp[0].concat(temp[1]);
							} else {
								favorBoard = temp[0];
							}
						} else if (favorBoard.indexOf(boardname + "#")!=-1) {
							String[] temp = favorBoard.split(boardname+"#");
							if (temp.length==2) {
								favorBoard = temp[0].concat(temp[1]);
							} else {
								favorBoard = temp[0];
							}
						}
					}
					mEditor.putString("favor", favorBoard);
					mEditor.commit();
					isFavor = false;
					favoButton.setBackgroundResource(R.drawable.collect_cancel);
				} else {
					String favorBoard = favor.getString("favor", null);
					mEditor.putString("favor", boardname + "#" + favorBoard);
					mEditor.commit();
					isFavor = true;
					favoButton.setBackgroundResource(R.drawable.collect_cancel_press);
				}
			}
		});
		Button newThread = (Button)findViewById(R.id.threadlist_add);
		newThread.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent mIntent = new Intent(threadList.this, newThread.class);
				Bundle mBundle = new Bundle();
				mBundle.putString("action", "new");
				mBundle.putString("title", "");
				mBundle.putString("url", url);
				mIntent.putExtras(mBundle);
				threadList.this.startActivity(mIntent);
			}
		});
		
		if (favorlist.indexOf(boardname)!=-1) {
			isFavor = true;
			favoButton.setBackgroundResource(R.drawable.collect_cancel_press);
		} else {
			isFavor = false;
			favoButton.setBackgroundResource(R.drawable.collect_cancel);
		}

		setListAdapter(mAdapter);
		getListView().setOnScrollListener(this);
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
		switch (scrollState){
		  // 当不滚动时
			case OnScrollListener.SCROLL_STATE_IDLE:
				// 判断滚动到底部
				if (view.getLastVisiblePosition() >= (((listAdapter)mAdapter).list.size()-2)) {
					getlist(next);
				}
				break;
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		System.out.println("clicked!");
		String[] temp = dataMap.get( ((listAdapter)mAdapter).list.get(position) ).split("#");
		String url = temp[4];
		Intent startread = new Intent(threadList.this, threadContent.class);
		Bundle mBundle = new Bundle();
		mBundle.putString("url", url);
		mBundle.putString("title", temp[3]);
		mBundle.putString("board", boardname);
		startread.putExtras(mBundle);
		threadList.this.startActivity(startread);
	}
	
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
				case 0:
					//((contentAdapter)mAdapter).notifyDataSetChanged();
					waitDialog.cancel();
					((listAdapter)mAdapter).notifyDataSetChanged();
					break;
				default:
					super.handleMessage(msg);
			}
		}
	};

	private ExecutorService executorService = Executors.newFixedThreadPool(5);
	
	private void getlist(final String url) {
		waitDialog = ProgressDialog.show(this, "", "正在加载...", true, true);
		executorService.submit(new Runnable() {
			public void run() {
	    		Message msg = new Message();
	    		msg.what = 0;
				String urltemp = url;
				int topthread = 200000;
				if (urltemp.contains("bbsdoc")) {
					String[] temp = urltemp.split("bbsdoc");
					urltemp = temp[0] + "bbstdoc" + temp[1];
				}
				if (orignial=="") {
					orignial = urltemp;
				}
				boolean nextpageStarter = true;
				try {
					Document doc = Jsoup.connect(url).get();
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
						if (((listAdapter)mAdapter).list.contains(threadID))
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
				LinearLayout toprow_layout = new LinearLayout(context);
				toprow_layout.setOrientation(VERTICAL);
				
				LinearLayout first_row = new LinearLayout(context);
				first_row.setOrientation(HORIZONTAL);
				noTextView = new TextView(context);
				
				noTextView.setTextSize(15);
				
				if (Integer.valueOf(id)>199980) {
					noTextView.setTextColor(Color.RED);
					noTextView.setText("Top");
				} else {
					noTextView.setTextColor(Color.BLUE);
					noTextView.setText(id);
				}
				titleTextView = new TextView(context);
				titleTextView.setText(title);
				titleTextView.setTextColor(Color.BLACK);
				titleTextView.setTextSize(20);
				titleTextView.setPadding(4, 0, 0, 0);
				first_row.addView(noTextView);
				first_row.addView(titleTextView);
				
				RelativeLayout second_row = new RelativeLayout(context);
				authorTextView = new TextView(context);
				authorTextView.setText("由 " + author + "发表于 ");
				authorTextView.setTextColor(Color.BLUE);
				authorTextView.setTextSize(16);
				RelativeLayout.LayoutParams authorLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT);
				authorLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
				authorTextView.setLayoutParams(authorLayoutParams);
				authorTextView.setId(1);
				
				timeTextView = new TextView(context);
				timeTextView.setText(time);
				timeTextView.setTextColor(Color.BLUE);
				timeTextView.setTextSize(16);
				RelativeLayout.LayoutParams timeLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT);
				timeLayoutParams.addRule(RelativeLayout.RIGHT_OF, 1);
				timeTextView.setPadding(5, 0, 20, 0);
				timeTextView.setLayoutParams(timeLayoutParams);
				
				infoTextView = new TextView(context);
				infoTextView.setText(Html.fromHtml(info));
				infoTextView.setTextSize(16);
				RelativeLayout.LayoutParams infoLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT);
				infoLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
				infoTextView.setLayoutParams(infoLayoutParams);
				
				second_row.addView(authorTextView, authorLayoutParams);
				second_row.addView(timeTextView, timeLayoutParams);
				second_row.addView(infoTextView, infoLayoutParams);
				
				toprow_layout.addView(first_row);
				toprow_layout.addView(second_row);
				addView(toprow_layout, new LinearLayout.LayoutParams(
	                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			}
			
			public void setTitle(String title) {
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
