package com.fatjay.subfunction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
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

import android.R.anim;
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
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

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
	
	private Button buttonNew, buttonDelete, buttonPhoto, buttonRefresh, buttonFavor, buttonMode;
	private Animation animationTranslate, animationRotate, animationScale;
	private static int width, height;
	private LayoutParams params = new LayoutParams(0, 0);
	private TextView loadmoreTextView;
	private LinearLayout loading;
	private static Boolean isClick = false;
	
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
		initialButton();
		waitDialog = ProgressDialog.show(this, "", "正在加载...", true, true);
		getlist(url);
		boardname = url.split("=")[1];
		
		TextView mTextView = (TextView)findViewById(R.id.threadlist_title);
		mTextView.setText("当前版面: " + mUserinfo.boardname.get(boardname));
		mTextView.setTextSize(20);
		mTextView.setTransformationMethod(SingleLineTransformationMethod.getInstance());
		mTextView.setFocusable(true);
		buttonFavor = (Button)findViewById(R.id.list_favor);
		if (favorlist.indexOf(boardname)!=-1) {
			isFavor = true;
			buttonFavor.setBackgroundResource(R.drawable.ic_button_favorite_add);
		} else {
			isFavor = false;
			buttonFavor.setBackgroundResource(R.drawable.ic_button_favorite_delete);
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
	}


	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
	}

	public void onScrollStateChanged(AbsListView view,int scrollState){
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
					loadmoreTextView.setVisibility(View.VISIBLE);
	            	loading.setVisibility(View.GONE);
					break;
				default:
					super.handleMessage(msg);
			}
		}
	};

	private ExecutorService executorService = Executors.newFixedThreadPool(5);
	
	private void getlist(final String url) {
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
	
private void initialButton() 
	{
		// TODO Auto-generated method stub
		Display display = getWindowManager().getDefaultDisplay(); 
		height = display.getHeight();  
		width = display.getWidth();
		Log.v("width  & height is:", String.valueOf(width) + ", " + String.valueOf(height));
		
		android.widget.RelativeLayout.LayoutParams params = new android.widget.RelativeLayout.LayoutParams(0, 0);
		params.height = 50;
		params.width = 50;
		params.setMargins(10, height - 98, 0, 0);
		
		buttonMode = (Button) findViewById(R.id.list_mode);
		buttonMode.setLayoutParams(params);
		
		buttonFavor = (Button) findViewById(R.id.list_favor);
		buttonFavor.setLayoutParams(params);
		
		buttonRefresh = (Button) findViewById(R.id.list_refresh);
		buttonRefresh.setLayoutParams(params);
		
		buttonPhoto = (Button) findViewById(R.id.list_camera);
		buttonPhoto.setLayoutParams(params);

		buttonNew = (Button) findViewById(R.id.list_thought);
		buttonNew.setLayoutParams(params);
		
		buttonDelete = (Button) findViewById(R.id.list_function);		
		buttonDelete.setLayoutParams(params);
		
		buttonDelete.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				// TODO Auto-generated method stub					
				if(isClick == false)
				{
					isClick = true;
					buttonDelete.startAnimation(animRotate(-45.0f, 0.5f, 0.5f));					
					buttonNew.startAnimation(animTranslate(0.0f, -180.0f, 10, height - 240, buttonNew, 80));
					buttonPhoto.startAnimation(animTranslate(30.0f, -150.0f, 60, height - 230, buttonPhoto, 100));
					buttonRefresh.startAnimation(animTranslate(70.0f, -120.0f, 110, height - 210, buttonRefresh, 120));
					buttonFavor.startAnimation(animTranslate(80.0f, -110.0f, 150, height - 180, buttonFavor, 140));
					buttonMode.startAnimation(animTranslate(90.0f, -60.0f, 170, height - 140, buttonMode, 160));
				}
				else
				{
					isClick = false;
					buttonDelete.startAnimation(animRotate(90.0f, 0.5f, 0.5f));
					buttonNew.startAnimation(animTranslate(0.0f, 140.0f, 10, height - 98, buttonNew, 180));
					buttonPhoto.startAnimation(animTranslate(-60.0f, 120.0f, 10, height - 98, buttonPhoto, 160));
					buttonRefresh.startAnimation(animTranslate(-115.0f, 105.0f, 10, height - 98, buttonRefresh, 140));
					buttonFavor.startAnimation(animTranslate(-150.0f, 70.0f, 10, height - 98, buttonFavor, 120));
					buttonMode.startAnimation(animTranslate(-170.0f, 0.0f, 10, height - 98, buttonMode, 80));
				}
			}
		});
		buttonNew.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				buttonNew.startAnimation(setAnimScale(2.5f, 2.5f));
				buttonPhoto.startAnimation(setAnimScale(0.0f, 0.0f));	
				buttonRefresh.startAnimation(setAnimScale(0.0f, 0.0f));
				buttonFavor.startAnimation(setAnimScale(0.0f, 0.0f));
				buttonMode.startAnimation(setAnimScale(0.0f, 0.0f));
				buttonDelete.startAnimation(setAnimScale(0.0f, 0.0f));
				Intent mIntent = new Intent(threadList.this, newThread.class);
				Bundle mBundle = new Bundle();
				mBundle.putString("action", "new");
				mBundle.putString("title", "");
				mBundle.putString("url", url);
				mIntent.putExtras(mBundle);
				if (isClick == true) {
					isClick = false;
					buttonDelete.startAnimation(animRotate(90.0f, 0.5f, 0.45f));
					buttonNew.startAnimation(animTranslate(0.0f, 140.0f, 10, height - 98, buttonNew, 180));
					buttonPhoto.startAnimation(animTranslate(-60.0f, 120.0f, 10, height - 98, buttonPhoto, 160));
					buttonRefresh.startAnimation(animTranslate(-115.0f, 105.0f, 10, height - 98, buttonRefresh, 140));
					buttonFavor.startAnimation(animTranslate(-150.0f, 70.0f, 10, height - 98, buttonFavor, 120));
					buttonMode.startAnimation(animTranslate(-170.0f, 0.0f, 10, height - 98, buttonMode, 80));
				}
				threadList.this.startActivity(mIntent);
			}
		});
		buttonPhoto.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{			
				buttonPhoto.startAnimation(setAnimScale(2.5f, 2.5f));	
				buttonNew.startAnimation(setAnimScale(0.0f, 0.0f));	
				buttonRefresh.startAnimation(setAnimScale(0.0f, 0.0f));
				buttonFavor.startAnimation(setAnimScale(0.0f, 0.0f));
				buttonMode.startAnimation(setAnimScale(0.0f, 0.0f));
				buttonDelete.startAnimation(setAnimScale(0.0f, 0.0f));
				Intent mIntent = new Intent(threadList.this, newThread.class);
				Bundle mBundle = new Bundle();
				mBundle.putString("action", "new");
				mBundle.putString("title", "");
				mBundle.putString("url", url);
				mIntent.putExtras(mBundle);
				if (isClick == true) {
					isClick = false;
					buttonDelete.startAnimation(animRotate(90.0f, 0.5f, 0.45f));
					buttonNew.startAnimation(animTranslate(0.0f, 140.0f, 10, height - 98, buttonNew, 180));
					buttonPhoto.startAnimation(animTranslate(-60.0f, 120.0f, 10, height - 98, buttonPhoto, 160));
					buttonRefresh.startAnimation(animTranslate(-115.0f, 105.0f, 10, height - 98, buttonRefresh, 140));
					buttonFavor.startAnimation(animTranslate(-150.0f, 70.0f, 10, height - 98, buttonFavor, 120));
					buttonMode.startAnimation(animTranslate(-170.0f, 0.0f, 10, height - 98, buttonMode, 80));
				}
				threadList.this.startActivity(mIntent);
			}
		});
		buttonRefresh.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				buttonRefresh.startAnimation(setAnimScale(2.5f, 2.5f));
				buttonPhoto.startAnimation(setAnimScale(0.0f, 0.0f));	
				buttonNew.startAnimation(setAnimScale(0.0f, 0.0f));	
				buttonFavor.startAnimation(setAnimScale(0.0f, 0.0f));
				buttonMode.startAnimation(setAnimScale(0.0f, 0.0f));
				buttonDelete.startAnimation(setAnimScale(0.0f, 0.0f));
				if (isClick == true) {
					isClick = false;
					buttonDelete.startAnimation(animRotate(90.0f, 0.5f, 0.45f));
					buttonNew.startAnimation(animTranslate(0.0f, 140.0f, 10, height - 98, buttonNew, 180));
					buttonPhoto.startAnimation(animTranslate(-60.0f, 120.0f, 10, height - 98, buttonPhoto, 160));
					buttonRefresh.startAnimation(animTranslate(-115.0f, 105.0f, 10, height - 98, buttonRefresh, 140));
					buttonFavor.startAnimation(animTranslate(-150.0f, 70.0f, 10, height - 98, buttonFavor, 120));
					buttonMode.startAnimation(animTranslate(-170.0f, 0.0f, 10, height - 98, buttonMode, 80));
				}
				((listAdapter)mAdapter).list.clear();
				dataMap.clear();
				((listAdapter)mAdapter).notifyDataSetChanged();
				getlist(orignial);
				((listAdapter)mAdapter).notifyDataSetChanged();
			}
		});
		buttonFavor.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				buttonFavor.startAnimation(setAnimScale(2.5f, 2.5f));
				buttonRefresh.startAnimation(setAnimScale(0.0f, 0.0f));
				buttonPhoto.startAnimation(setAnimScale(0.0f, 0.0f));	
				buttonNew.startAnimation(setAnimScale(0.0f, 0.0f));	
				buttonMode.startAnimation(setAnimScale(0.0f, 0.0f));
				buttonDelete.startAnimation(setAnimScale(0.0f, 0.0f));
				
				if (isClick == true) {
					isClick = false;
					buttonDelete.startAnimation(animRotate(90.0f, 0.5f, 0.45f));
					buttonNew.startAnimation(animTranslate(0.0f, 140.0f, 10, height - 98, buttonNew, 180));
					buttonPhoto.startAnimation(animTranslate(-60.0f, 120.0f, 10, height - 98, buttonPhoto, 160));
					buttonRefresh.startAnimation(animTranslate(-115.0f, 105.0f, 10, height - 98, buttonRefresh, 140));
					buttonFavor.startAnimation(animTranslate(-150.0f, 70.0f, 10, height - 98, buttonFavor, 120));
					buttonMode.startAnimation(animTranslate(-170.0f, 0.0f, 10, height - 98, buttonMode, 80));
				}
				
				SharedPreferences favor = mContext.getSharedPreferences("favor", MODE_PRIVATE);
				SharedPreferences.Editor mEditor = favor.edit();
				String favorBoard = favor.getString("favor", null);
				if (isFavor) {
					String[] tempStrings = favorBoard.split("#");
					List<String> list = new LinkedList<String>();
			        for(int i = 0; i < tempStrings.length; i++) {
			            if(!list.contains(tempStrings[i]) && !tempStrings[i].equals(boardname)) {
			                list.add(tempStrings[i]);
			            }
			        }
			        favorBoard = "";
			        for (int j = 0; j < list.size(); j++) {
						favorBoard = favorBoard + "#" + list.get(j);
					}
			        favorBoard = favorBoard.substring(1);
					mEditor.putString("favor", favorBoard);
					mEditor.commit();
					isFavor = false;
					buttonFavor.setBackgroundResource(R.drawable.ic_button_favorite_delete);
				} else {
					mEditor.putString("favor", boardname + "#" + favorBoard);
					mEditor.commit();
					isFavor = true;
					buttonFavor.setBackgroundResource(R.drawable.composer_favor_checked);
				}
			}
		});
		buttonMode.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				buttonMode.startAnimation(setAnimScale(2.5f, 2.5f));
				buttonRefresh.startAnimation(setAnimScale(0.0f, 0.0f));
				buttonPhoto.startAnimation(setAnimScale(0.0f, 0.0f));	
				buttonNew.startAnimation(setAnimScale(0.0f, 0.0f));	
				buttonFavor.startAnimation(setAnimScale(0.0f, 0.0f));
				buttonDelete.startAnimation(setAnimScale(0.0f, 0.0f));
			}
		});
	}
	
	protected Animation setAnimScale(float toX, float toY) 
	{
		// TODO Auto-generated method stub
		animationScale = new ScaleAnimation(1f, toX, 1f, toY, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.45f);
		animationScale.setInterpolator(threadList.this, anim.accelerate_decelerate_interpolator);
		animationScale.setDuration(500);
		animationScale.setFillAfter(false);
		return animationScale;
	}
	
	protected Animation animRotate(float toDegrees, float pivotXValue, float pivotYValue) 
	{
		// TODO Auto-generated method stub
		animationRotate = new RotateAnimation(0, toDegrees, Animation.RELATIVE_TO_SELF, pivotXValue, Animation.RELATIVE_TO_SELF, pivotYValue);
		animationRotate.setAnimationListener(new AnimationListener() 
		{
			
			@Override
			public void onAnimationStart(Animation animation) 
			{
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) 
			{
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) 
			{
				// TODO Auto-generated method stub
				animationRotate.setFillAfter(true);
			}
		});
		return animationRotate;
	}
	
	protected Animation animTranslate(float toX, float toY, final int lastX, final int lastY,
			final Button button, long durationMillis) 
	{
		// TODO Auto-generated method stub
		animationTranslate = new TranslateAnimation(0, toX, 0, toY);				
		animationTranslate.setAnimationListener(new AnimationListener()
		{
						
			@Override
			public void onAnimationStart(Animation animation)
			{
				// TODO Auto-generated method stub
								
			}
						
			@Override
			public void onAnimationRepeat(Animation animation) 
			{
				// TODO Auto-generated method stub
							
			}
						
			@Override
			public void onAnimationEnd(Animation animation)
			{
				// TODO Auto-generated method stub
				params = new LayoutParams(0, 0);
				params.height = 50;
				params.width = 50;											
				params.setMargins(lastX, lastY, 0, 0);
				button.setLayoutParams(params);
				button.clearAnimation();
						
			}
		});																								
		animationTranslate.setDuration(durationMillis);
		return animationTranslate;
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
				toprow_layout.setBackgroundResource(R.drawable.bg_list);
				
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
