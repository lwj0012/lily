package com.fatjay.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fatjay.R;
import com.fatjay.main.userinfo;
import com.fatjay.subfunction.maillist;
import com.fatjay.subfunction.searchDlg;
import com.fatjay.subfunction.threadContent;
import com.fatjay.subfunction.threadList;

import android.R.color;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class top extends ListActivity {
	Map<String, String> topList = new HashMap<String, String>();				//key: name of thread, value:url$auther$autherInfoURL
	TopAdapter mAdapter = new TopAdapter(this);
	ProgressDialog waitDialog;
	userinfo mUserinfo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.top);
		mUserinfo = (userinfo) getApplication();
		waitDialog = new ProgressDialog(top.this);
        waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        waitDialog.setMessage("正在加载中...");
        waitDialog.setIndeterminate(true);
        waitDialog.setCancelable(false);
        waitDialog.show();
		getTop();
		Button refreshButton = (Button)findViewById(R.id.top10_refresh);
		refreshButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				waitDialog = new ProgressDialog(top.this);
		        waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		        waitDialog.setMessage("正在加载中...");
		        waitDialog.setIndeterminate(true);
		        waitDialog.setCancelable(false);
		        waitDialog.show();
				((TopAdapter)mAdapter).list.clear();
				topList.clear();
				((TopAdapter)mAdapter).notifyDataSetChanged();
				getTop();
				((TopAdapter)mAdapter).notifyDataSetChanged();
			}
		});
		setListAdapter(mAdapter);
		getListView().setOnCreateContextMenuListener(this);
		
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		System.out.println("clicked!");
		String[] temp = topList.get( ((TopAdapter)mAdapter).list.get(position) ).split("#");
		String url = temp[1];
		Intent startread = new Intent(top.this, threadContent.class);
		Bundle mBundle = new Bundle();
		mBundle.putString("url", url);
		mBundle.putString("title", temp[0]);
		mBundle.putString("board", mUserinfo.boardname.get(temp[4]));
		startread.putExtras(mBundle);
		top.this.startActivity(startread);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
				new AlertDialog.Builder(top.this)
					.setTitle("")
					.setMessage("确定退出？")
					.setNegativeButton(R.string.quit_cancel, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
						}
					})
					.setPositiveButton(R.string.quit_ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							System.exit(0);
						}
				}).show();
				return true;
			case KeyEvent.KEYCODE_SEARCH:
				Intent search = new Intent(top.this, searchDlg.class);
				top.this.startActivity(search);
				return true;
			default:
				return super.onKeyDown(keyCode, event);
		}
	}
	 
	 
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    
	    System.exit(0);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		int menuInfo = ((AdapterContextMenuInfo)item.getMenuInfo()).position;
		switch (item.getItemId()) {
		case 0:
			String[] tempStrings = topList.get(((TopAdapter)mAdapter).list.get(menuInfo)).split("#");
			Intent mIntent = new Intent(top.this, threadList.class);
			Bundle mBundle = new Bundle();
			mBundle.putString("url", "http://bbs.nju.edu.cn/bbstdoc?board=" + tempStrings[4]);
			mIntent.putExtras(mBundle);
			top.this.startActivity(mIntent);
			break;
		default:
			break;
		}
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle("操作:");
		menu.add(0, 0, Menu.FIRST, "打开所在版面");
	}

	 @Override
		public boolean onCreateOptionsMenu(Menu menu) {
			// TODO Auto-generated method stub
			menu.add(Menu.NONE, Menu.FIRST + 1, 5, "查看邮件").setIcon(android.R.drawable.ic_menu_search);
	        return true;
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			// TODO Auto-generated method stub
			switch (item.getItemId()) {
			case Menu.FIRST + 1:
				Intent send = new Intent();
				send.setClass(top.this, maillist.class);
				top.this.startActivity(send);
				break;
			}
			return true;
		}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
				case 0://接到从线程内传来的图片bitmap和imageView.
						//这里只是将bitmap传到imageView中就行了。只所以不在线程中做是考虑到线程的安全性。
					mAdapter.notifyDataSetChanged();
					waitDialog.cancel();
					break;
				default:
					super.handleMessage(msg);
			}
		}
	};

	private ExecutorService executorService = Executors.newFixedThreadPool(5);
	
	private void getTop() {
		executorService.submit(new Runnable() {
			public void run() {
				Message msg = new Message();
	    		msg.what = 0;
		    	int id = 0;
		    	try {
		    		Document doc = Jsoup.connect("http://bbs.nju.edu.cn/bbstop10").get();
		    		Elements blocks = doc.select("tr");
		    		for (Element block : blocks) {
		    			Elements links = block.select("a[href]");
		    			if (links.size()==0)
		    				continue;
		    			links = block.select("td");
		    			String idString = String.valueOf(++id);
		    			String title = links.get(2).select("a").text();
		    			String url = links.get(2).select("a").attr("abs:href");
		    			String username = links.get(3).select("a").text();
		    			String userinfo = links.get(3).select("a").attr("abs:href");
		    			String board = links.get(1).select("a").text();
		    			String boardurl = links.get(1).select("a").attr("abs:href");
		    			String reply = links.get(4).text();
		    			top.this.mAdapter.list.add(idString);
		    			top.this.topList.put(idString, title + '#' + url+'#'+username+'#'+userinfo+'#'+board+'#'+boardurl + '#' + reply);
		    		}
		    	} catch (Exception e) {
		    		e.printStackTrace();
		    	}
		    	handler.sendMessage(msg);
			}
		});
    }
	
	private class TopAdapter extends BaseAdapter {
		public ArrayList<String> list = new ArrayList<String>();
		private Context mContext;
		
		TopAdapter(Context con) {
			mContext = con;
		}
		
		public int getCount() {
			// TODO Auto-generated method stub
			return list.size();
		}

		@Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            return true;
        }
		
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			topView cv = null;
			String[] strings = null;
            if (convertView == null) {
                strings = topList.get(list.get(position)).split("#");
                String title = strings[0];
                String author = strings[2];
                String board = strings[4];
                String reply = strings[6];
                cv = new topView(mContext, title, author, board, reply);
            } else {
            	strings = topList.get(list.get(position)).split("#");
                String title = strings[0];
                String author = strings[2];
                String board = strings[4];
                String reply = strings[6];
                cv = (topView) convertView;
                cv.setTitle(title);
                cv.setAuthor(author);
                cv.setBoard(board);
                cv.setReply(reply);
            }
            return cv;
		}
		
		class topView extends LinearLayout {
			private TextView titleTextView;
			private TextView authorTextView;
			private TextView boardTextView;
			private TextView replyTextView;
			
			public topView(Context context, String title, String author, String board, String reply) {
				super(context);
				this.setOrientation(VERTICAL);
				LinearLayout toprow_layout = new LinearLayout(context);
				toprow_layout.setOrientation(VERTICAL);
				toprow_layout.setBackgroundColor(color.darker_gray);
				
				titleTextView = new TextView(context);
				titleTextView.setText(title);
				//titleTextView.setBackgroundColor(color.darker_gray);
				//titleTextView.setTextColor(color.white);
				titleTextView.setTextSize(22);
				titleTextView.setPadding(4, 0, 0, 0);
				titleTextView.setTextColor(Color.rgb(8, 46, 84));
				
				RelativeLayout inforow_layout = new RelativeLayout(context);
				authorTextView = new TextView(context);
				authorTextView.setText("由 " + author + "发表在 ");
				authorTextView.setTextColor(Color.rgb(135, 206, 235));
				RelativeLayout.LayoutParams authorLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT);
				authorLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
				authorTextView.setLayoutParams(authorLayoutParams);
				authorTextView.setId(1);
				
				boardTextView = new TextView(context);
				boardTextView.setText(mUserinfo.boardname.get(board));
				boardTextView.setTextColor(Color.rgb(135, 206, 235));
				RelativeLayout.LayoutParams boardLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT);
				boardLayoutParams.addRule(RelativeLayout.RIGHT_OF, 1);
				boardTextView.setPadding(5, 0, 20, 0);
				boardTextView.setLayoutParams(boardLayoutParams);
				
				replyTextView = new TextView(context);
				replyTextView.setText(reply + "人跟帖");
				replyTextView.setTextColor(Color.rgb(227, 23, 13));
				RelativeLayout.LayoutParams infoLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT);
				infoLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
				replyTextView.setLayoutParams(infoLayoutParams);
				
				inforow_layout.addView(authorTextView);
				inforow_layout.addView(boardTextView);
				inforow_layout.addView(replyTextView);
				toprow_layout.addView(titleTextView);
				toprow_layout.addView(inforow_layout);

				addView(toprow_layout, new LinearLayout.LayoutParams(
	                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			}
			
			public void setTitle(String title) {
				titleTextView.setText(title);
				titleTextView.setTextColor(Color.rgb(8, 46, 84));
	        }
			
			public void setBoard(String board) {
				boardTextView.setText(mUserinfo.boardname.get(board));
				boardTextView.setTextColor(Color.rgb(135, 206, 235));
			}
			
			public void setReply(String reply) {
				replyTextView.setText(reply + "人跟帖");
				replyTextView.setTextColor(Color.rgb(227, 23, 13));
	        }
			
			public void setAuthor(String author) {
				authorTextView.setText("由 " + author + "发表在 ");
				authorTextView.setTextColor(Color.rgb(135, 206, 235));
	        }
			
		}
		
	}
	
}
