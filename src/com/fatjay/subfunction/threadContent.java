package com.fatjay.subfunction;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.String;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.XMLReader;

import com.fatjay.R;
import com.fatjay.main.userinfo;

import android.R.anim;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.Html.ImageGetter;
import android.text.Html.TagHandler;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class threadContent extends ListActivity implements OnItemClickListener {
	userinfo mUserinfo = null;
	static BaseAdapter mAdapter = null;
	Map<String, String> commentsMap = new HashMap<String, String>();
	ArrayList<String> contents = new ArrayList<String>();
	static Map<Integer, String> picMap = new HashMap<Integer, String>();
	Map<String, Integer> hashPicMap = new HashMap<String, Integer>();
	
	private Button replyButton, mainButton, quickButton, refreshButton, jumpButton;
	private Animation animationTranslate, animationRotate, animationScale;
	private static int width, height;
	private LayoutParams params = new LayoutParams(0, 0);
	private static Boolean isClick = false;
	
	String titleString = "";
	String urlString = "";
	String board = "";
	ProgressDialog waitDialog;
	String pid = "-2";
	String rex = "http://bbs.nju.edu.cn/file[a-zA-Z0-9\\-/_+=.~!%@?#%&;:$]+?(jpg|jpeg|png|gif)";
	String rex1 = "[a-zA-z]+://(\\w+(-\\w+)*)(\\.(\\w+(-\\w+)*))*(\\?\\S*)?";
	String filerex = "";
	Pattern imagePattern;
	Pattern filePattern;
	int IMAGE_MAX_SIZE = 300;
	int SCREEN_WIDTH;
	static ListView mListView = null;
 	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.threadcontent);
        mUserinfo = (userinfo) getApplication();
        Bundle mBundle = getIntent().getExtras();
        urlString = mBundle.getString("url");
        titleString = mBundle.getString("title");
        board = mBundle.getString("board");
        mAdapter = new contentAdapter(this);
        WindowManager manage = getWindowManager();
        Display display = manage.getDefaultDisplay();
        SCREEN_WIDTH = display.getWidth();
        initialButton();
        waitDialog = new ProgressDialog(threadContent.this);
        waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        waitDialog.setMessage("帖子正在加载中...");
        waitDialog.setIndeterminate(true);
        waitDialog.setCancelable(true);
        waitDialog.show();
        
        imagePattern = Pattern.compile(rex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        filePattern = Pattern.compile(filerex, Pattern.DOTALL);
        
        getAllBoard();
        setListAdapter(mAdapter);
        mListView = getListView();
        getListView().setClickable(true);
        getListView().setOnItemClickListener(this);
        getListView().setOnCreateContextMenuListener(this);
    }
    
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		executorService.shutdownNow();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
		case RESULT_OK:
			((contentAdapter)mAdapter).no.clear();
			commentsMap.clear();
			contents.clear();
			waitDialog = new ProgressDialog(threadContent.this);
	        waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	        waitDialog.setMessage("正在刷新...");
	        waitDialog.setIndeterminate(true);
	        waitDialog.setCancelable(true);
	        waitDialog.show();
	        ((contentAdapter)mAdapter).notifyDataSetChanged();
			getAllBoard();
			((contentAdapter)mAdapter).notifyDataSetChanged();
			break;
		case RESULT_CANCELED:
			 break;
		default:
			break;
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
				case 0://接到从线程内传来的图片bitmap和imageView.
						//这里只是将bitmap传到imageView中就行了。只所以不在线程中做是考虑到线程的安全性。
					((contentAdapter)mAdapter).notifyDataSetChanged();
					waitDialog.cancel();
					Log.d("threadcontent", "end");
					break;
				default:
					super.handleMessage(msg);
			}
		}
	};

	private ExecutorService executorService = Executors.newFixedThreadPool(5);
	
	
	private void getAllBoard() {
		executorService.submit(new Runnable() {
			public void run() {
		    	try {
		    		Paint mPaint;
		    		mPaint = new Paint();
		            mPaint.setAntiAlias(true);
		            mPaint.setStrokeWidth(5);
		            mPaint.setStrokeCap(Paint.Cap.ROUND);
		            mPaint.setTextSize(2);
		            mPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.ITALIC));
		            
		    		Message msg = new Message();
		    		msg.what = 0;
		            Boolean contentstart = false;
		            int contentcounter = -1;
		            URL mUrl = new URL(urlString + "&start=-1");
		            HttpURLConnection send = (HttpURLConnection) mUrl.openConnection();
					send.setDoOutput(true);
					send.setRequestMethod("GET");
					//send.setRequestProperty("Content-Language","en-CA");
					send.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
					send.connect();
					InputStream in = send.getInputStream(); 
		            BufferedReader reader = new BufferedReader(new InputStreamReader(in,"gb2312")); 
		            String inputLine = null;  
		            StringBuffer sb = new StringBuffer();
		            while ((inputLine = reader.readLine()) != null) {
		                sb.append(inputLine);
		                if (inputLine.indexOf("<textarea")!=-1) {
							contentstart = true;
							contentcounter ++;
							threadContent.this.contents.add("");
						}
		                if (inputLine.indexOf("/table")!=-1) {
							contentstart = false;
						}
		                if (contentstart) {
		                	String tempString = threadContent.this.contents.get(contentcounter) + inputLine;
		                	
		                	float[] widths = new float[inputLine.length()];
		                    mPaint.getTextWidths(inputLine, 0, inputLine.length(), widths);
		                    float width = 0.0f;
		                    for (int i = 0; i < inputLine.length(); i++) {
								width += widths[i];
							}
		                    if (width < 75 && width > 0) {
		                    	tempString = tempString + "<br/>";
							}

		                	threadContent.this.contents.set(contentcounter, tempString);
						}
		            }
		            reader.close();  
		            in.close();
		            
		            Log.d("threadcontent", "start");
		            
		            int counter = 0;
		            Document doc = Jsoup.parse(sb.toString());
					Elements mElements = doc.select("table");
					boolean first = true;
		    		for (Element element : mElements) {
		    			Elements partElements = element.select("tr");
		    			if (partElements.size()!=2) {
							continue;
						}
		    			String replylink;
		    			String author;
		    			if (partElements.get(0).toString().contains("回复本文")) {
		    				replylink = partElements.get(0).select("a").get(1).attr("href");
		    				author = partElements.get(0).select("a").get(2).text();
						} else {
							replylink = "";
							author = partElements.get(0).select("a").get(1).text();
						}
		    			String time = "";
		    			String board = "";
						String no = String.valueOf(counter);
		    			String title = "";
		    			if (titleString.indexOf("○")==0) {
							titleString = titleString.substring(1);
						}
		    			if (first) {
							title = titleString;
							first = false;
						} else {
							title = "Re: " + titleString;
						}
		    			String content = contents.get(counter);
		    			if (content.contains("发信站")) {
							content = content.substring(content.indexOf("发信站"));
							if (content.contains(")")) {
								time = content.substring(content.indexOf("(") + 1, content.indexOf(")"));
								content = content.substring(content.indexOf(")") + 1);
							}
						} else {
							time = "";
						}
		    			content = content.substring(content.indexOf(">")+1);
		    			
		    			Matcher m = imagePattern.matcher(content);
		    			ArrayList<String> picArrayList = new ArrayList<String>();
		    			while (m.find()) {
							picArrayList.add(m.group(0));
						}
		    			String picElement = "";
		    			for (int i = 0; i < picArrayList.size(); i++) {
							content = content.replace(picArrayList.get(i), "<br/><pic><img src=\"" + picArrayList.get(i) + "\"></pic><br/>");
							String[] temps = picArrayList.get(i).split("/");
							picElement = picElement + "##" + temps[temps.length-2] + "_" + temps[temps.length-1];
							hashPicMap.put(temps[temps.length-2] + "_" + temps[temps.length-1], counter);
						}
		    			
		    			picMap.put(counter, picElement);
		    			
		    			content = "<font>" + content;
		    			content = content.replaceAll("\\[([0-9]+;)*30(;[0-9]+)*m", "</font><font color=\"black\">");
		    			content = content.replaceAll("\\[([0-9]+;)*31(;[0-9]+)*m", "</font><font color=\"red\">");
		    			content = content.replaceAll("\\[([0-9]+;)*32(;[0-9]+)*m", "</font><font color=\"green\">");
		    			content = content.replaceAll("\\[([0-9]+;)*33(;[0-9]+)*m", "</font><font color=\"#EB8E55\">");
		    			content = content.replaceAll("\\[([0-9]+;)*34(;[0-9]+)*m", "</font><font color=\"blue\">");
		    			content = content.replaceAll("\\[([0-9]+;)*35(;[0-9]+)*m", "</font><font color=\"#FF7F50\">");
		    			content = content.replaceAll("\\[([0-9]+;)*36(;[0-9]+)*m", "</font><font color=\"#00FFFF\">");
		    			content = content.replaceAll("\\[([0-9]+;)*37(;[0-9]+)*m", "</font><font color=\"black\">");
		    			content = content.replaceAll("\\[0{0,1}m", "</font><font>");
		    			content = content + "</font>";
		    			
		    			content = content.replaceAll("\\[:s\\]", "<img src=\"face2\">");
		    			content = content.replaceAll("\\[:O\\]", "<img src=\"face0\">");
		    			content = content.replaceAll("\\[:\\|\\]", "<img src=\"face3\">");
		    			content = content.replaceAll("\\[:\\$\\]", "<img src=\"face6\">");
		    			content = content.replaceAll("\\[:X\\]", "<img src=\"face7\">");
		    			content = content.replaceAll("\\[:'\\(\\]", "<img src=\"face9\">");
		    			content = content.replaceAll("\\[:-\\|\\]", "<img src=\"face10\">");
		    			content = content.replaceAll("\\[:@\\]", "<img src=\"face11\">");
		    			content = content.replaceAll("\\[:P\\]", "<img src=\"face12\">");
		    			content = content.replaceAll("\\[:D\\]", "<img src=\"face13\">");
		    			content = content.replaceAll("\\[:\\)\\]", "<img src=\"face14\">");
		    			content = content.replaceAll("\\[:\\(\\]", "<img src=\"face15\">");
		    			content = content.replaceAll("\\[:Q\\]", "<img src=\"face18\">");
		    			content = content.replaceAll("\\[:T\\]", "<img src=\"face19\">");
		    			content = content.replaceAll("\\[;P\\]", "<img src=\"face20\">");
		    			content = content.replaceAll("\\[;-D\\]", "<img src=\"face21\">");
		    			content = content.replaceAll("\\[:!\\]", "<img src=\"face26\">");
		    			content = content.replaceAll("\\[:L\\]", "<img src=\"face27\">");
		    			content = content.replaceAll("\\[:?\\]", "<img src=\"face32\">");
		    			content = content.replaceAll("\\[:U\\]", "<img src=\"face16\">");
		    			content = content.replaceAll("\\[:K\\]", "<img src=\"face25\">");
		    			content = content.replaceAll("\\[:C-\\]", "<img src=\"face29\">");
		    			content = content.replaceAll("\\[;X\\]", "<img src=\"face34\">");
		    			content = content.replaceAll("\\[:H\\]", "<img src=\"face36\">");
		    			content = content.replaceAll("\\[;bye\\]", "<img src=\"face39\">");
		    			content = content.replaceAll("\\[;cool\\]", "<img src=\"face4\">");
		    			content = content.replaceAll("\\[:-b\\]", "<img src=\"face40\">");
		    			content = content.replaceAll("\\[:-8\\]", "<img src=\"face41\">");
		    			content = content.replaceAll("\\[;PT\\]", "<img src=\"face42\">");
		    			content = content.replaceAll("\\[;-C\\]", "<img src=\"face43\">");
		    			content = content.replaceAll("\\[:hx\\]", "<img src=\"face44\">");
		    			content = content.replaceAll("\\[;K\\]", "<img src=\"face47\">");
		    			content = content.replaceAll("\\[:E\\]", "<img src=\"face49\">");
		    			content = content.replaceAll("\\[:-\\(\\]", "<img src=\"face50\">");
		    			content = content.replaceAll("\\[;hx\\]", "<img src=\"face51\">");
		    			content = content.replaceAll("\\[:B\\]", "<img src=\"face52\">");
		    			content = content.replaceAll("\\[:-v\\]", "<img src=\"face53\">");
		    			content = content.replaceAll("\\[;xx\\]", "<img src=\"face54\">");
		    			
		    			if (content.indexOf("--")!=-1) {
		    				content = content.substring(0, content.lastIndexOf("--"));
						}
//		    			content = content.replaceAll("\\[(1;.*?|37;1|32|33)m", "");
		    			threadContent.this.commentsMap.put(no, title + "##" + author + "##" + replylink + "##" + time + "##" + board + "##" + content);
		    			((contentAdapter)threadContent.mAdapter).no.add(no);
		    			counter += 1;
		    		}
		    		
		    		userinfo mUserinfo = (userinfo)getApplication();
		    		String code = mUserinfo.getCode();
		    		String cookie = mUserinfo.getCookies();
		    		String tempString = threadContent.this.commentsMap.get("0").split("##")[2];
		    		if (tempString.equals("")) {
						pid = "-3";
					} else {
						tempString = "http://bbs.nju.edu.cn/" + code + "/" + tempString;
			    		mUrl = new URL(tempString);
						HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
						if(cookie!=null && cookie.length()>0){
			                conn.setRequestProperty("Cookie", cookie);
			            }
						conn.setRequestMethod("GET");
						conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
						conn.connect();
						in = conn.getInputStream(); 
			            BufferedReader reader1 = new BufferedReader(new InputStreamReader(in,"gb2312")); 
			            inputLine = null;
			            while ((inputLine = reader1.readLine()) != null) {  
			                if ( inputLine.contains("name=pid") ) {
			                	String temp = inputLine.substring(inputLine.indexOf("name=pid"));
			                	if (temp.indexOf("value='")!=-1 && temp.indexOf("'>")!=-1) {
			                		threadContent.this.pid = temp.substring(temp.indexOf("value='") + 7,
				                			temp.indexOf("'>"));
			                		break;
								}
			                }
			            }
			            reader1.close();
			            in.close();
					}
		    		handler.sendMessage(msg);
		    	} catch (Exception e) {
		    		e.printStackTrace();
		    	}
			}
		
		});
    }
	
	private class contentAdapter extends BaseAdapter {
		private Context mContext;
		ArrayList<String> no = new ArrayList<String>();
		//ArrayList<String> contentArrayList = new ArrayList<String>();
		
		public contentAdapter(Context context) {
            mContext = context;
        }
		
		public int getCount() {
			// TODO Auto-generated method stub
			return no.size();
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
			contentView cv = null;
			String[] strings = null;
            if (convertView == null) {
            	strings = commentsMap.get(no.get(position)).split("##");
            	String title = strings[0];
            	String author = strings[1];
            	String time = strings[3];
            	String content = strings[5];
            	String board = strings[4];
            	String id = no.get(position);
            	cv = new contentView(mContext, id, title, content, author, time, board);
                       // mDialogue[position]);
            } else {
            	strings = commentsMap.get(no.get(position)).split("##");
            	cv = (contentView) convertView;
            	cv.setTitle(strings[0]);
            	cv.setDialogue(strings[5]);
            	cv.setAuthor(strings[1]);
            	cv.setTime(strings[3]);
            	cv.setNo(no.get(position));
            }
            return cv;
		}
	}
	
	private class contentView extends LinearLayout {
		private TextView titleTextView;
		private TextView noTextView;
		private TextView authorTextView;
		private TextView timeTextView;
		private TextView contentTextView;
        public contentView(Context context, String id, String title,
							String content, String author, String time,
							String board) {
			super(context);
			// TODO Auto-generated constructor stub
			this.setOrientation(VERTICAL);

			LayoutInflater mInflater = getLayoutInflater();
			RelativeLayout item = new RelativeLayout(context);
			item = (RelativeLayout) mInflater.inflate(R.layout.content_item, null);
			item.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
			
			titleTextView = (TextView)(item.findViewById(R.id.thread_content_item_title));
			titleTextView.setText(title);
			titleTextView.setClickable(false);
			titleTextView.setMovementMethod(null);
			titleTextView.setFocusable(false);
			titleTextView.setFocusableInTouchMode(false);
			titleTextView.setPadding(5, 0, 5, 0);
			
			noTextView = (TextView)(item.findViewById(R.id.thread_content_item_reply));
			String link = "<reply>回复 " + id + " 楼</reply>";
			noTextView.setMovementMethod(LinkMovementMethod.getInstance());
			noTextView.setText(Html.fromHtml(link, null, new CustomTagHandler()));
			noTextView.setClickable(false);
			noTextView.setFocusable(false);
			noTextView.setFocusableInTouchMode(false);
			noTextView.setPadding(0, 0, 5, 0);
			
			authorTextView = (TextView)(item.findViewById(R.id.thread_content_item_author));
			authorTextView.setMovementMethod(LinkMovementMethod.getInstance());
			authorTextView.setText(Html.fromHtml("<user>作者：" + author + "</user>", null, new CustomTagHandler()));
			authorTextView.setClickable(false);
			authorTextView.setPadding(5, 0, 0, 0);

			timeTextView = (TextView)(item.findViewById(R.id.thread_content_item_time));
			timeTextView.setText(time);
			timeTextView.setClickable(false);
			timeTextView.setFocusable(false);
			timeTextView.setFocusableInTouchMode(false);
			timeTextView.setMovementMethod(null);
			timeTextView.setPadding(5, 0, 5, 0);
			

			contentTextView = (TextView)(item.findViewById(R.id.thread_content_item_content));
			contentTextView.setTextColor(Color.BLACK);
			contentTextView.setPadding(5, 0, 5, 0);
			contentTextView.setMovementMethod(LinkMovementMethod.getInstance());			
			contentTextView.setText(Html.fromHtml(content, new AsyncImageDownloader(), new CustomTagHandler()));
			contentTextView.setClickable(false);
			contentTextView.setAutoLinkMask(Linkify.ALL);
//			contentWebView.setFocusable(false);
//			contentWebView.setFocusableInTouchMode(false);

			addView(item, new LinearLayout.LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		}
		
		public void setTitle(String title) {
			titleTextView.setClickable(false);
			titleTextView.setFocusable(false);
			titleTextView.setFocusableInTouchMode(false);
			titleTextView.setText(title);
        }
		
		public void setNo(String no) {
			noTextView.setMovementMethod(LinkMovementMethod.getInstance());
			noTextView.setClickable(false);
			noTextView.setFocusable(false);
			noTextView.setFocusableInTouchMode(false);
			noTextView.setText(Html.fromHtml("<reply>回复 " + no + " 楼</reply>", null, new CustomTagHandler()));
		}
		
		public void setTime(String time) {
			timeTextView.setClickable(false);
			timeTextView.setFocusable(false);
			timeTextView.setFocusableInTouchMode(false);
			timeTextView.setText(time);
        }
		
		public void setAuthor(String author) {
			authorTextView.setMovementMethod(LinkMovementMethod.getInstance());
			authorTextView.setClickable(false);
			authorTextView.setFocusable(false);
			authorTextView.setFocusableInTouchMode(false);
			authorTextView.setText(Html.fromHtml("<user>作者：" + author + "</user>", null, new CustomTagHandler()));
        }

        public void setDialogue(String words) {
        	words = "<html>" + words;
        	words = words + "</html>";
        	contentTextView.setMovementMethod(LinkMovementMethod.getInstance());
        	contentTextView.setClickable(true);
//			contentWebView.setFocusable(false);
//			contentWebView.setFocusableInTouchMode(false);
			contentTextView.setText(Html.fromHtml(words, new AsyncImageDownloader(), new CustomTagHandler()));
        }
	}
	
	public class CustomTagHandler implements TagHandler {  
		  
	    private int PicstartIndex = 0;  
	    private int PicstopIndex = 0;
	    
	    private int ReplystartIndex = 0;  
	    private int ReplystopIndex = 0;
	    
	    private int UserstartIndex = 0;  
	    private int UserstopIndex = 0;  
	  
	    public void handleTag(boolean opening, String tag, Editable output,  
	            XMLReader xmlReader) {  
	        if (tag.toLowerCase().equals("pic")) {  
	            if (opening) {
	                startPic(tag, output, xmlReader);  
	            } else {  
	            	endPic(tag, output, xmlReader);  
	            }
	        } else if (tag.toLowerCase().equals("reply")) {  
	            if (opening) {
	                startReply(tag, output, xmlReader);  
	            } else {
	            	endReply(tag, output, xmlReader);  
	            }
	        } else if (tag.toLowerCase().equals("user")) {  
	            if (opening) {
	                startUser(tag, output, xmlReader);  
	            } else {
	            	endUser(tag, output, xmlReader);  
	            }
	        }
	  
	    }
	  
	    public void startPic(String tag, Editable output, XMLReader xmlReader) {  
	    	PicstartIndex = output.length();  
	    }
	  
	    public void endPic(String tag, Editable output, XMLReader xmlReader) {  
	    	PicstopIndex = output.length();
	        String tempString = output.toString().substring(PicstartIndex, PicstopIndex);
	        tempString = Html.toHtml(output);
	        tempString = tempString.substring(tempString.lastIndexOf("src=")+5);
	        tempString = tempString.substring(0, tempString.lastIndexOf("\">"));
	        output.setSpan(new PicSpan(tempString), PicstartIndex, PicstopIndex,  
	                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);  
	        //output.getSpans(arg0, arg1, arg2)
	    }
	    
	    public void startReply(String tag, Editable output, XMLReader xmlReader) {  
	    	ReplystartIndex = output.length();  
	    }
	  
	    public void endReply(String tag, Editable output, XMLReader xmlReader) {  
	    	ReplystopIndex = output.length();
	        String tempString = output.toString().substring(ReplystartIndex, ReplystopIndex);
	        output.setSpan(new ReplySpan(tempString), ReplystartIndex, ReplystopIndex,
	                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
	    }
	    
	    public void startUser(String tag, Editable output, XMLReader xmlReader) {  
	    	UserstartIndex = output.length();  
	    }
	  
	    public void endUser(String tag, Editable output, XMLReader xmlReader) {  
	    	UserstopIndex = output.length();
	        String tempString = output.toString().substring(UserstartIndex, UserstopIndex);
	        output.setSpan(new UserSpan(tempString), UserstartIndex, UserstopIndex,
	                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
	    }
	  
	  
	    private class PicSpan extends ClickableSpan implements OnClickListener {  
	    	String urlString;
	    	public PicSpan(String url) {
				// TODO Auto-generated constructor stub
	    		urlString = url;
			}
	        @Override  
	        public void onClick(View v) {  
	        	String[] temps = urlString.split("/");
				String file = temps[temps.length-2] + "_" + temps[temps.length-1];
				Integer floorInteger = hashPicMap.get(file);
	        	Intent mIntent = new Intent();
	        	mIntent.setClass(threadContent.this, threadImage.class);
	        	Bundle mBundle = new Bundle();
	        	mBundle.putString("image", picMap.get(floorInteger));
	        	mIntent.putExtras(mBundle);
	        	threadContent.this.startActivity(mIntent);
	        }
	    } 
	    
	    private class UserSpan extends ClickableSpan implements OnClickListener {  
	    	String username;
	    	public UserSpan(String name) {
				// TODO Auto-generated constructor stub
	    		name = name.substring(3);
	    		username = name;
			}
	        @Override  
	        public void onClick(View v) {
	        	Intent mIntent = new Intent(threadContent.this, mail.class);
	        	Bundle mBundle = new Bundle();
	        	mBundle.putString("mailto", username);
	        	mBundle.putString("action", "new");
	        	mIntent.putExtras(mBundle);
	        	threadContent.this.startActivity(mIntent);
	        }
	    }
	    
	    private class ReplySpan extends ClickableSpan implements OnClickListener {  
	    	String no;
	    	public ReplySpan(String id) {
				// TODO Auto-generated constructor stub
	    		id = id.substring(3, id.indexOf(" 楼"));
	    		no = id;
			}
	        @Override  
	        public void onClick(View v) {
	        	String[] tempString = commentsMap.get(no).split("##");
	        	Intent mIntent = new Intent(threadContent.this, newThread.class);
				Bundle mBundle = new Bundle();
				mBundle.putString("action", "reply");
				mBundle.putString("url", "http://bbs.nju.edu.cn/" + tempString[2]);
				mBundle.putString("title", titleString);
				mBundle.putString("author", tempString[1]);
				mBundle.putString("pid", threadContent.this.pid);
				mIntent.putExtras(mBundle);
				threadContent.this.startActivityForResult(mIntent, 0);
	        }
	    }
	}
	
	public class AsyncImageDownloader implements ImageGetter {
		HashMap<String , SoftReference<Drawable>> imgcache;
		
		public AsyncImageDownloader() {
			imgcache = new HashMap<String, SoftReference<Drawable>>();
		}

		@Override
		public Drawable getDrawable(String source) {
			// TODO Auto-generated method stub
			Drawable drawable = null;
			if (imgcache.containsKey(source)) {
				SoftReference<Drawable> softReference = imgcache.get(source);
				drawable = softReference.get();
				if (drawable != null) {
					return drawable;
				}
			} else {
				String[] temps = source.split("/");
				if (temps.length == 1) {											//表情
					String sourceName = getPackageName() + ":drawable/" 
						+ source;
					int id = getResources().getIdentifier(sourceName, null, null);
					if (id != 0) {
						drawable = getResources().getDrawable(id);
						if (drawable != null) {
							drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
									drawable.getIntrinsicHeight());
						}
					}
					SoftReference<Drawable> softReference = new SoftReference<Drawable>(drawable);
					imgcache.put(source, softReference);
					return drawable;
				}
				String filename = temps[temps.length-2] + "_" + temps[temps.length-1];
				File dir = new File("/sdcard/lily");
				if (!dir.exists()) {
					dir.mkdirs();
				}
				File picFile = new File("/sdcard/lily/" + filename);
				if (picFile.exists()) {
					Uri uri = Uri.fromFile(picFile);
					Bitmap b = null;
			        try {
			            BitmapFactory.Options o = new BitmapFactory.Options();
			            o.inJustDecodeBounds = true;	            
			            InputStream fis = getContentResolver().openInputStream(uri);
			            BitmapFactory.decodeStream(fis,null,o);

			            int scale = 1;
			            if ( o.outWidth > SCREEN_WIDTH) {									//o.outHeight > IMAGE_MAX_SIZE ||
			                scale = (int)Math.pow(2, (int) Math.round(Math.log((SCREEN_WIDTH-30) / (double) o.outWidth) / Math.log(0.5)));
			            }

			            //Decode with inSampleSize
			            BitmapFactory.Options o2 = new BitmapFactory.Options();
			            int floor = hashPicMap.get(filename);
			            String[] picStrings = picMap.get(floor).split("##");
			            if (picStrings.length > 20 ) {
							o2.inSampleSize = scale*8;
						} else {
							o2.inSampleSize = scale +2;
						}
			            //fis = new FileInputStream(file);
			            fis = getContentResolver().openInputStream(uri);
			            System.gc();
			            b = BitmapFactory.decodeStream(fis, null, o2);
			            fis.close();
			        } catch (IOException e) {
			        }
					drawable = new BitmapDrawable(b);
					drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
					SoftReference<Drawable> softReference = new SoftReference<Drawable>(drawable);
					imgcache.put(source, softReference);
					/*
					Message msg = new Message();
		    		msg.what = 0;
		    		imgHandler.sendMessage(msg);
		    		*/
				} else {
					getImage(source);
				}
			}
			return drawable;
		}
	}
	
	public static Handler imgHandler = new Handler() {
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
				case 0://接到从线程内传来的图片bitmap和imageView.
						//这里只是将bitmap传到imageView中就行了。只所以不在线程中做是考虑到线程的安全性。
//					getListView().invalidate();
					boolean isAll = true;
					String[] fileString = picMap.get(0).split("##");
					File file;
					for (int i = 0; i < fileString.length; i++) {
						file = new File("/sdcard/lily/" + fileString[i]);
						if (!file.exists()) {
							isAll = false;
							break;
						}
					}
					if (isAll) {
						//((contentAdapter)mAdapter).notifyDataSetChanged();
						mListView.invalidateViews();
					}
					
					break;
				case 1:
					//图片下载完成
					((contentAdapter)mAdapter).notifyDataSetChanged();
					mListView.invalidateViews();
				default:
					super.handleMessage(msg);
			}
		}
	};
	
	public void getImage(final String url) {
		executorService.submit(new Runnable() {
			public void run() {
				URL myFileUrl = null;
				Bitmap bitmap = null;
				try {
					String urlString = url;
					String[] temps = urlString.split("/");
					String filename = temps[temps.length-2] + "_" + temps[temps.length-1];
					File dir = new File("/sdcard/lily");
					if (!dir.exists()) {
						dir.mkdirs();
					}
					File picFile = new File("/sdcard/lily/" + filename);
					myFileUrl = new URL(urlString);
					HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
					conn.setConnectTimeout(10000);
					conn.setDoInput(true);
					conn.connect();
					InputStream is = conn.getInputStream();
					bitmap = BitmapFactory.decodeStream(is);
					is.close();
					BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(picFile));
					bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
					bos.flush();
					bos.close();
					Message msg = new Message();
		    		msg.what = 0;
		    		imgHandler.sendMessage(msg);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		Log.d("list", "list item clicked...");
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		int menuInfo = ((AdapterContextMenuInfo)item.getMenuInfo()).position;
		switch (item.getItemId()) {
			case 0:
				String fileString = commentsMap.get(String.valueOf(menuInfo)).split("##")[2];
				String boardString = fileString.substring(fileString.indexOf("=")+1, fileString.indexOf("&"));
				fileString = fileString.substring(fileString.lastIndexOf("=")+1);
				Intent mIntent = new Intent(threadContent.this, cccDlg.class);
				Bundle mBundle = new Bundle();
				mBundle.putString("source", boardString);
				mBundle.putString("file", fileString);
				mIntent.putExtras(mBundle);
				threadContent.this.startActivity(mIntent);
				break;
			case 1:
				shareThread(menuInfo);
				break;
			case 2:
				editThread(menuInfo);
				break;
			case 3:
				deleteThread(menuInfo);
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
		int id = ((AdapterContextMenuInfo)menuInfo).position;
		String authorString = commentsMap.get(String.valueOf(id)).split("##")[1];
		String currentUserString = mUserinfo.getUsername();
		menu.setHeaderTitle("操作:");
		menu.add(0, 0, Menu.FIRST+1, "转载本文");
		menu.add(0, 1, Menu.FIRST+2, "分享本文");
		if (currentUserString.equals(authorString)) {
			menu.add(0, 2, Menu.FIRST+3, "编辑本文");
			menu.add(0, 3, Menu.FIRST+4, "删除本文");
		}
	}
	
	private void editThread(int position) {
		
	}
	
	private void shareThread(int position) {
		Intent intent=new Intent(Intent.ACTION_SEND);   
		intent.setType("text/plain");
		String[] contentStrings = commentsMap.get(String.valueOf(position)).split("##");
        intent.putExtra(Intent.EXTRA_SUBJECT, contentStrings[0] + "--转载自：小百合 by Lily");   
        intent.putExtra(Intent.EXTRA_TEXT, contentStrings[5]);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Intent.createChooser(intent, getTitle()));
	}
	
	private void deleteThread(int position) {
		String code = mUserinfo.getCode();
		String cookie = mUserinfo.getCookies();
		String fileString = commentsMap.get(String.valueOf(position)).split("##")[2];
		String boardString = fileString.substring(fileString.indexOf("=")+1, fileString.indexOf("&"));
		fileString = fileString.substring(fileString.indexOf("&"));
		String newurlString = "http://bbs.nju.edu.cn/" + code + "/bbsdel?board=" + boardString + fileString;
		HttpGet httpRequest = new HttpGet(newurlString);
		httpRequest.addHeader("Cookie", cookie);

	    HttpResponse httpResponse = null;
		try {
			httpResponse = new DefaultHttpClient().execute(httpRequest);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			Toast.makeText(getApplicationContext(), "删除成功！",
				     Toast.LENGTH_SHORT).show();
		}
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
		
//		buttonMode = (Button) findViewById(R.id.content_mode);
//		buttonMode.setLayoutParams(params);
		
		jumpButton = (Button) findViewById(R.id.content_jump);
		jumpButton.setLayoutParams(params);
		
		refreshButton = (Button) findViewById(R.id.content_refresh);
		refreshButton.setLayoutParams(params);
		
		quickButton = (Button) findViewById(R.id.content_quick_reply);
		quickButton.setLayoutParams(params);

		replyButton = (Button) findViewById(R.id.content_reply);
		replyButton.setLayoutParams(params);
		
		mainButton = (Button) findViewById(R.id.content_function);		
		mainButton.setLayoutParams(params);
		
		mainButton.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				// TODO Auto-generated method stub					
				if(isClick == false)
				{
					isClick = true;
					mainButton.startAnimation(animRotate(-45.0f, 0.5f, 0.5f));					
					replyButton.startAnimation(animTranslate(0.0f, -150.0f, 10, height - 248, replyButton, 80));
					quickButton.startAnimation(animTranslate(75.0f, -130.0f, 85, height - 228, quickButton, 100));
					refreshButton.startAnimation(animTranslate(130.0f, -75.0f, 140, height - 173, refreshButton, 120));
					jumpButton.startAnimation(animTranslate(150.0f, -0.0f, 160, height - 98, jumpButton, 140));
//					buttonMode.startAnimation(animTranslate(150.0f, 0.0f, 160, height - 200, buttonMode, 160));
				}
				else
				{
					isClick = false;
					mainButton.startAnimation(animRotate(90.0f, 0.5f, 0.5f));
					replyButton.startAnimation(animTranslate(0.0f, 150.0f, 10, height - 98, replyButton, 180));
					quickButton.startAnimation(animTranslate(-75.0f, 138.0f, 10, height - 98, quickButton, 160));
					refreshButton.startAnimation(animTranslate(-130.0f, 75.0f, 10, height - 98, refreshButton, 140));
					jumpButton.startAnimation(animTranslate(-150.0f, 0.0f, 10, height - 98, jumpButton, 120));
//					buttonMode.startAnimation(animTranslate(-150.0f, 0.0f, 10, height - 200, buttonMode, 80));
				}
			}
		});
		replyButton.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				replyButton.startAnimation(setAnimScale(2.5f, 2.5f));
				quickButton.startAnimation(setAnimScale(0.0f, 0.0f));	
				refreshButton.startAnimation(setAnimScale(0.0f, 0.0f));
				jumpButton.startAnimation(setAnimScale(0.0f, 0.0f));
//				buttonMode.startAnimation(setAnimScale(0.0f, 0.0f));
				mainButton.startAnimation(setAnimScale(0.0f, 0.0f));
//				String url = "http://bbs.nju.edu.cn/bbstdoc?board=" + favorsStrings[mHost.getCurrentTab()];
//				Intent mIntent = new Intent(favor.this, newThread.class);
//				Bundle mBundle = new Bundle();
//				mBundle.putString("action", "new");
//				mBundle.putString("title", "");
//				mBundle.putString("url", url);
//				mIntent.putExtras(mBundle);
//				if (isClick == true) {
//					isClick = false;
//					mainButton.startAnimation(animRotate(90.0f, 0.5f, 0.45f));
//					
//					replyButton.startAnimation(animTranslate(0.0f, 150.0f, 10, height - 98, replyButton, 180));
//					quickButton.startAnimation(animTranslate(-75.0f, 138.0f, 10, height - 98, quickButton, 160));
//					refreshButton.startAnimation(animTranslate(-130.0f, 75.0f, 10, height - 98, refreshButton, 140));
//					jumpButton.startAnimation(animTranslate(-150.0f, 0.0f, 10, height - 98, jumpButton, 120));
//					buttonMode.startAnimation(animTranslate(-150.0f, 0.0f, 10, height - 200, buttonMode, 80));
//				}
//				favor.this.startActivity(mIntent);
			}
		});
		quickButton.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				quickButton.startAnimation(setAnimScale(2.5f, 2.5f));	
				replyButton.startAnimation(setAnimScale(0.0f, 0.0f));	
				refreshButton.startAnimation(setAnimScale(0.0f, 0.0f));
				jumpButton.startAnimation(setAnimScale(0.0f, 0.0f));
//				buttonMode.startAnimation(setAnimScale(0.0f, 0.0f));
				mainButton.startAnimation(setAnimScale(0.0f, 0.0f));
//				String url = "http://bbs.nju.edu.cn/bbstdoc?board=" + favorsStrings[mHost.getCurrentTab()];
//				Intent mIntent = new Intent(favor.this, newThread.class);
//				Bundle mBundle = new Bundle();
//				mBundle.putString("action", "new");
//				mBundle.putString("title", "");
//				mBundle.putString("url", url);
//				mIntent.putExtras(mBundle);
//				if (isClick == true) {
//					isClick = false;
//					mainButton.startAnimation(animRotate(90.0f, 0.5f, 0.45f));
//					replyButton.startAnimation(animTranslate(0.0f, 150.0f, 10, height - 98, replyButton, 180));
//					quickButton.startAnimation(animTranslate(-75.0f, 138.0f, 10, height - 98, quickButton, 160));
//					refreshButton.startAnimation(animTranslate(-130.0f, 75.0f, 10, height - 98, refreshButton, 140));
//					jumpButton.startAnimation(animTranslate(-150.0f, 0.0f, 10, height - 98, jumpButton, 120));
//					buttonMode.startAnimation(animTranslate(-150.0f, 0.0f, 10, height - 200, buttonMode, 80));
//				}
//				favor.this.startActivity(mIntent);
			}
		});
		refreshButton.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				refreshButton.startAnimation(setAnimScale(2.5f, 2.5f));
				quickButton.startAnimation(setAnimScale(0.0f, 0.0f));	
				replyButton.startAnimation(setAnimScale(0.0f, 0.0f));	
				jumpButton.startAnimation(setAnimScale(0.0f, 0.0f));
//				buttonMode.startAnimation(setAnimScale(0.0f, 0.0f));
				mainButton.startAnimation(setAnimScale(0.0f, 0.0f));
				if (isClick == true) {
					isClick = false;
					mainButton.startAnimation(animRotate(90.0f, 0.5f, 0.45f));
					replyButton.startAnimation(animTranslate(0.0f, 150.0f, 10, height - 98, replyButton, 180));
					quickButton.startAnimation(animTranslate(-75.0f, 138.0f, 10, height - 98, quickButton, 160));
					refreshButton.startAnimation(animTranslate(-130.0f, 75.0f, 10, height - 98, refreshButton, 140));
					jumpButton.startAnimation(animTranslate(-150.0f, 0.0f, 10, height - 98, jumpButton, 120));
//					buttonMode.startAnimation(animTranslate(-150.0f, 0.0f, 10, height - 200, buttonMode, 80));
				}
			}
		});
		jumpButton.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				jumpButton.startAnimation(setAnimScale(2.5f, 2.5f));
				refreshButton.startAnimation(setAnimScale(0.0f, 0.0f));
				quickButton.startAnimation(setAnimScale(0.0f, 0.0f));	
				replyButton.startAnimation(setAnimScale(0.0f, 0.0f));	
//				buttonMode.startAnimation(setAnimScale(0.0f, 0.0f));
				mainButton.startAnimation(setAnimScale(0.0f, 0.0f));
				
				if (isClick == true) {
					isClick = false;
					mainButton.startAnimation(animRotate(90.0f, 0.5f, 0.45f));
					replyButton.startAnimation(animTranslate(0.0f, 150.0f, 10, height - 98, replyButton, 180));
					quickButton.startAnimation(animTranslate(-75.0f, 138.0f, 10, height - 98, quickButton, 160));
					refreshButton.startAnimation(animTranslate(-130.0f, 75.0f, 10, height - 98, refreshButton, 140));
					jumpButton.startAnimation(animTranslate(-150.0f, 0.0f, 10, height - 98, jumpButton, 120));
//					buttonMode.startAnimation(animTranslate(-150.0f, 0.0f, 10, height - 200, buttonMode, 80));
				}
			}
		});
	}
	
	protected Animation setAnimScale(float toX, float toY) 
	{
		// TODO Auto-generated method stub
		animationScale = new ScaleAnimation(1f, toX, 1f, toY, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.45f);
		animationScale.setInterpolator(threadContent.this, anim.accelerate_decelerate_interpolator);
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
}

