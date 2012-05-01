package com.fatjay.function;

import java.io.IOException;
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
import com.fatjay.subfunction.hotThread;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.animation.AnimationUtils;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class hotpot extends TabActivity {
	TabHost mHost;
	Context mContext;
	RadioGroup rGroup;
	public int pageid=0;
	public static hotpot instance;
	public Map<Integer, ArrayList<String>> data = new HashMap<Integer, ArrayList<String>>();
	ArrayList<TabHost.TabSpec> tabSpecs = new ArrayList<TabHost.TabSpec>();
	
	private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	int current;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hotnews);
        
        mHost = getTabHost();
        mContext = this;
        instance = this;
        
        int counter = 0;
        Intent board0 = new Intent(mContext, hotThread.class);
        Bundle mBundle;
        mBundle = new Bundle();
        mBundle.putInt("id", counter);
        board0.putExtras(mBundle);
        counter ++;
        mHost.addTab(mHost.newTabSpec("board1").setIndicator("Board1").setContent(board0));
        Intent board1 = new Intent(mContext, hotThread.class);
        mBundle = new Bundle();
        mBundle.putInt("id", counter);
        board1.putExtras(mBundle);
        counter ++;
        mHost.addTab(mHost.newTabSpec("board2").setIndicator("Board2").setContent(board1));
        Intent board2 = new Intent(mContext, hotThread.class);
        mBundle = new Bundle();
        mBundle.putInt("id", counter);
        board2.putExtras(mBundle);
        counter ++;
        mHost.addTab(mHost.newTabSpec("board3").setIndicator("Board3").setContent(board2));
        Intent board3 = new Intent(mContext, hotThread.class);
        mBundle = new Bundle();
        mBundle.putInt("id", counter);
        board3.putExtras(mBundle);
        counter ++;
        mHost.addTab(mHost.newTabSpec("board4").setIndicator("board4").setContent(board3));
        Intent board4 = new Intent(mContext, hotThread.class);
        mBundle = new Bundle();
        mBundle.putInt("id", counter);
        board4.putExtras(mBundle);
        counter ++;
        mHost.addTab(mHost.newTabSpec("board5").setIndicator("board5").setContent(board4));
        Intent board5 = new Intent(mContext, hotThread.class);
        mBundle = new Bundle();
        mBundle.putInt("id", counter);
        board5.putExtras(mBundle);
        counter ++;
        mHost.addTab(mHost.newTabSpec("board6").setIndicator("board6").setContent(board5));
        Intent board6 = new Intent(mContext, hotThread.class);
        mBundle = new Bundle();
        mBundle.putInt("id", counter);
        board6.putExtras(mBundle);
        counter ++;
        mHost.addTab(mHost.newTabSpec("board7").setIndicator("board7").setContent(board6));
        Intent board7 = new Intent(mContext, hotThread.class);
        mBundle = new Bundle();
        mBundle.putInt("id", counter);
        board7.putExtras(mBundle);
        counter ++;
        mHost.addTab(mHost.newTabSpec("board8").setIndicator("board8").setContent(board7));
        Intent board8 = new Intent(mContext, hotThread.class);
        mBundle = new Bundle();
        mBundle.putInt("id", counter);
        board8.putExtras(mBundle);
        counter ++;
        mHost.addTab(mHost.newTabSpec("board9").setIndicator("board9").setContent(board8));
        Intent board9 = new Intent(mContext, hotThread.class);
        mBundle = new Bundle();
        mBundle.putInt("id", counter);
        board9.putExtras(mBundle);
        counter ++;
        mHost.addTab(mHost.newTabSpec("board10").setIndicator("board10").setContent(board9));
        Intent board10 = new Intent(mContext, hotThread.class);
        mBundle = new Bundle();
        mBundle.putInt("id", counter);
        board10.putExtras(mBundle);
        counter ++;
        mHost.addTab(mHost.newTabSpec("board11").setIndicator("board11").setContent(board10));
        Intent board11 = new Intent(mContext, hotThread.class);
        mBundle = new Bundle();
        mBundle.putInt("id", counter);
        board11.putExtras(mBundle);
        counter ++;
        mHost.addTab(mHost.newTabSpec("board12").setIndicator("board12").setContent(board11));
        
        rGroup = (RadioGroup)findViewById(R.id.hotpot_rg);
        rGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				int idx = 0;
				if (checkedId == R.id.hotpot_rb01) {
					idx = 0;
					pageid = idx;
				} else if (checkedId == R.id.hotpot_rb02) {
					idx = 1;
					pageid = idx;
				} else if (checkedId == R.id.hotpot_rb03) {
					idx = 2;
					pageid = idx;
				} else if (checkedId == R.id.hotpot_rb04) {
					idx = 3;
					pageid = idx;
				} else if (checkedId == R.id.hotpot_rb05) {
					idx = 4;
					pageid = idx;
				} else if (checkedId == R.id.hotpot_rb06) {
					idx = 5;
					pageid = idx;
				} else if (checkedId == R.id.hotpot_rb07) {
					idx = 6;
					pageid = idx;
				} else if (checkedId == R.id.hotpot_rb08) {
					idx = 7;
					pageid = idx;
				} else if (checkedId == R.id.hotpot_rb09) {
					idx = 8;
					pageid = idx;
				} else if (checkedId == R.id.hotpot_rb10) {
					idx = 9;
					pageid = idx;
				} else if (checkedId == R.id.hotpot_rb11) {
					idx = 10;
					pageid = idx;
				} else if (checkedId == R.id.hotpot_rb12) {
					idx = 11;
					pageid = idx;
				}
				switchActivity(idx);
			}
		});
        parsePage();
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
		
		RadioButton rb = (RadioButton) rGroup.getChildAt(idx);
		rb.setChecked(true);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
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
		public boolean onCreateOptionsMenu(Menu menu) {
			// TODO Auto-generated method stub
			menu.add(Menu.NONE, Menu.FIRST + 1, 5, "Refresh...").setIcon(android.R.drawable.ic_menu_help);
	        return true;
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			// TODO Auto-generated method stub
			switch (item.getItemId()) {
			case Menu.FIRST + 1:
				data.clear();
				parsePage();
				//finish();
				break;
			}
			return true;
		}
	 
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    
	    System.exit(0);
	}
	
	class MyGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                // right to left swipe
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                	switchActivity(pageid + 1);
                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                	switchActivity(pageid - 1);
                }
            } catch (Exception e) {
                // nothing
            }
            return false;
        }
    }
    
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        if (gestureDetector.onTouchEvent(event))
//	        return true;
//	    else
//	    	return false;
//    }
	
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
				case 0:
					break;
				default:
					super.handleMessage(msg);
			}
		}
	};

	private ExecutorService executorService = Executors.newFixedThreadPool(5);
	
	void parsePage() {
		executorService.submit(new Runnable() {
			public void run() {
				Message msg = new Message();
	    		msg.what = 0;
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
				handler.sendMessage(msg);
			}
		});
	}
}
