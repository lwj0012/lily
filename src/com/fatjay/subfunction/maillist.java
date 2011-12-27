package com.fatjay.subfunction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fatjay.R;
import com.fatjay.main.userinfo;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class maillist extends ListActivity implements OnScrollListener {
	userinfo mUserinfo;
	Context mContext;
	ProgressDialog waitDialog;
	Map<Integer, String> dataMap = new HashMap<Integer, String>();
	BaseAdapter mAdapter = new mailAdapter(this);
	String next = null;
	String orignial = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mail_list);
		mUserinfo = (userinfo) getApplication();
		mContext = getApplicationContext();
		getlist("http://bbs.nju.edu.cn/" + mUserinfo.getCode() + "/bbsmail");
		Button refresh = (Button)findViewById(R.id.mail_list_refresh);
		refresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				((mailAdapter)mAdapter).list.clear();
				dataMap.clear();
				((mailAdapter)mAdapter).notifyDataSetChanged();
				getlist(orignial);
				((mailAdapter)mAdapter).notifyDataSetChanged();
			}
		});
		
		Button compose = (Button)findViewById(R.id.mail_list_add);
		compose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent newmail = new Intent(maillist.this, mail.class);
				Bundle mBundle = new Bundle();
				mBundle.putString("action", "new");
				mBundle.putString("mailto", "");
				newmail.putExtras(mBundle);
				maillist.this.startActivity(newmail);
			}
		});

		setListAdapter(mAdapter);
		getListView().setOnScrollListener(this);
	}
	
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
	}

	public void onScrollStateChanged(AbsListView view,int scrollState){
		switch (scrollState){
		  // 当不滚动时
			case OnScrollListener.SCROLL_STATE_IDLE:
				// 判断滚动到底部
				if (view.getLastVisiblePosition() >= (((mailAdapter)mAdapter).list.size()-2)) {
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
		String[] temp = dataMap.get( ((mailAdapter)mAdapter).list.get(position) ).split("#");
		String url = temp[3];
		Intent startread = new Intent(maillist.this, mailContent.class);
		Bundle mBundle = new Bundle();
		mBundle.putString("url", url);
		mBundle.putString("reid", temp[0]);
		startread.putExtras(mBundle);
		maillist.this.startActivity(startread);
	}
	
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
				case 0:
					//((contentAdapter)mAdapter).notifyDataSetChanged();
					waitDialog.cancel();
					((mailAdapter)mAdapter).notifyDataSetChanged();
					break;
				default:
					super.handleMessage(msg);
			}
		}
	};

	private ExecutorService executorService = Executors.newFixedThreadPool(5);
	
	private void getlist(final String url) {
		waitDialog = ProgressDialog.show(this, "", "正在载入邮件列表...", true, true);
		executorService.submit(new Runnable() {
			public void run() {
	    		Message msg = new Message();
	    		msg.what = 0;
				String urltemp = url;
				if (orignial.equals("")) {
					orignial = urltemp;
				}
				boolean nextpageStarter = true;
				try {
					String result = null;
					HttpPost httpRequest = new HttpPost(urltemp);
				    httpRequest.addHeader("Cookie", mUserinfo.getCookies());
				    HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						 result = EntityUtils.toString(httpResponse.getEntity());
					}
					Elements raw_threads = Jsoup.parse(result).select("tr");
					for (Element each : raw_threads ) {
						Elements links = each.select("a");
						if (links.size()==0)
							continue;
						Elements parts = each.select("td");
						if (parts.size()!=6)
							continue;
						String mailID = null;
						if (nextpageStarter) {
							mailID = parts.get(0).text();
							int i = Integer.valueOf(mailID).intValue() - 20;
							next = orignial + "?&start=" + String.valueOf(i);
							nextpageStarter = false;
						} else {
							mailID = parts.get(0).text();
						}
						if (((mailAdapter)mAdapter).list.contains(mailID))
							continue;
						//String threadSTATUS = parts.get(1).text();
						String mailAUTHOR = parts.get(3).text();
						String mailDATE = parts.get(4).text();
						String mailTITLE = parts.get(5).select("a").text();
						String mailURL = parts.get(5).select("a").toString();
						mailURL = mailURL.replace("&amp;", "&");
						mailURL = mailURL.substring(mailURL.indexOf("\"")+1, mailURL.indexOf("\">"));
						String mailINFO;
						if (parts.get(2).select("img").size()>0) {
							mailINFO = "n";
						} else if (parts.get(2).text().toString().contains("r")) {
							mailINFO = "r";
						} else {
							mailINFO = " ";
						}
						dataMap.put(Integer.valueOf(mailID), mailAUTHOR + '#' + mailDATE + '#' + mailTITLE + '#' + mailURL + '#' + mailINFO);
						((mailAdapter)mAdapter).list.add(Integer.valueOf(mailID));
					}
					Collections.sort(((mailAdapter)mAdapter).list);
					Collections.reverse(((mailAdapter)mAdapter).list);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				handler.sendMessage(msg);
		    }
		});
	}
	
	private class mailAdapter extends BaseAdapter {
		List<Integer> list = new ArrayList<Integer>();
		Context mContext;
		
		public mailAdapter(Context context) {
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
			mailView tView;
			String[] strings;
			if (arg1 == null) {
				strings = (dataMap.get(list.get(arg0))).split("#");
				String title = strings[2];
				String time = strings[1];
				String author = strings[0];
				String info = strings[4];
				tView = new mailView(mContext, title, time, author, info);
			} else {
				strings = (dataMap.get(list.get(arg0))).split("#");
				String title = strings[2];
				String time = strings[1];
				String author = strings[0];
				String info = strings[4];
				tView = (mailView)arg1;
				tView.setTitle(title);
				tView.setTime(time);
				tView.setAuthor(author);
				tView.setInfo(info);
			}
			return tView;
		}
		
		class mailView extends LinearLayout {
			private TextView titleTextView;
			private TextView timeTextView;
			private TextView authorTextView;
			private TextView infoTextView;
			
			mailView(Context context, String title, String time, String author, String info) {
				super(context);
				this.setOrientation(VERTICAL);
				LinearLayout toprow_layout = new LinearLayout(context);
				toprow_layout.setOrientation(VERTICAL);
				
				RelativeLayout first_row = new RelativeLayout(context);
				
				RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT);
				titleParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
				
				titleTextView = new TextView(context);
				titleTextView.setText(title);
				titleTextView.setTextColor(Color.BLACK);
				titleTextView.setTextSize(20);
				titleTextView.setPadding(4, 0, 0, 0);
				titleTextView.setLayoutParams(titleParams);
				infoTextView = new TextView(context);
				infoTextView.setText(Html.fromHtml(info));
				infoTextView.setTextSize(16);
				if (info.equals("n")) {
					infoTextView.setText(Html.fromHtml("<font color=\"red\">" + "新邮件" + "</font>"));
				} else if (info.equals("r")) {
					infoTextView.setText(Html.fromHtml("<font color=\"blue\">" + "已回复" + "</font>"));
				} else if (info.equals("")) {
					infoTextView.setText(Html.fromHtml("<font color=\"orange\">" + "未回复" + "</font>"));
				}
				
				RelativeLayout.LayoutParams infoLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT);
				infoLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
				infoTextView.setLayoutParams(infoLayoutParams);

				first_row.addView(titleTextView);
				first_row.addView(infoTextView);
				
				RelativeLayout second_row = new RelativeLayout(context);
				authorTextView = new TextView(context);
				authorTextView.setText("由 " + author + "发送于 ");
				authorTextView.setTextColor(Color.BLUE);
				authorTextView.setTextSize(16);
				authorTextView.setPadding(5, 0, 0, 0);
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
				second_row.addView(authorTextView, authorLayoutParams);
				second_row.addView(timeTextView, timeLayoutParams);
				
				toprow_layout.addView(first_row);
				toprow_layout.addView(second_row);
				addView(toprow_layout, new LinearLayout.LayoutParams(
	                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			}
			
			public void setTitle(String title) {
				titleTextView.setText(title);
	        }
			
			public void setAuthor(String author) {
				authorTextView.setText("由 " + author + "发送于 ");
	        }
			
			public void setTime(String time) {
				timeTextView.setText(time);
	        }
			
			public void setInfo(String info) {
				if (info.equals("n")) {
					infoTextView.setText(Html.fromHtml("<font color=\"red\">" + "新邮件" + "</font>"));
				} else if (info.equals("r")) {
					infoTextView.setText(Html.fromHtml("<font color=\"blue\">" + "已回复" + "</font>"));
				} else if (info.equals("")) {
					infoTextView.setText(Html.fromHtml("<font color=\"orange\">" + "未回复" + "</font>"));
				}
	        }
		}
	}
	
	
}
