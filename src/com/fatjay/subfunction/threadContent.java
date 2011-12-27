package com.fatjay.subfunction;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.String;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.XMLReader;

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
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.Html.TagHandler;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class threadContent extends ListActivity {
	BaseAdapter mAdapter = null;
	Map<String, String> commentsMap = new HashMap<String, String>();
	ArrayList<String> contents = new ArrayList<String>();
	String titleString = "";
	String urlString = "";
	String board = "";
	ProgressDialog waitDialog;
	String pid = "-2";
	String rex = "http://[([a-zA-Z0-9]|.|/|\\-)]+/.[(jpg)|(bmp)|(gif)|(png)]";
	String rex1 = "[a-zA-z]+://(\\w+(-\\w+)*)(\\.(\\w+(-\\w+)*))*(\\?\\S*)?";
	String filerex = "";
	Pattern imagePattern;
	Pattern filePattern;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.threadcontent);
        Bundle mBundle = getIntent().getExtras();
        urlString = mBundle.getString("url");
        titleString = mBundle.getString("title");
        board = mBundle.getString("board");
        mAdapter = new contentAdapter(this);
        Button refreshButton = (Button)findViewById(R.id.threadcontent_refresh);
        refreshButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				((contentAdapter)mAdapter).no.clear();
				commentsMap.clear();
				contents.clear();
				((contentAdapter)mAdapter).notifyDataSetChanged();
				waitDialog = new ProgressDialog(threadContent.this);
		        waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		        waitDialog.setMessage("正在刷新...");
		        waitDialog.setIndeterminate(true);
		        waitDialog.setCancelable(false);
		        waitDialog.show();
				getAllBoard();
				((contentAdapter)mAdapter).notifyDataSetChanged();
			}
		});
        TextView titleTextView = (TextView)findViewById(R.id.threadcontent_title);
        titleTextView.setText("当前版面：" + board);
        titleTextView.setTextSize(24);
        
        Button addButton = (Button)findViewById(R.id.threadcontent_add);
        addButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (pid.equals("-3")) {
					Toast.makeText(threadContent.this, "此贴无法回复", Toast.LENGTH_LONG);
				} else {
					Intent mIntent = new Intent(threadContent.this, newThread.class);
					Bundle mBundle = new Bundle();
					String[] tempString = commentsMap.get("0").split("#");
					mBundle.putString("action", "reply");
					mBundle.putString("url", "http://bbs.nju.edu.cn/" + tempString[2]);
					mBundle.putString("title", titleString);
					mBundle.putString("pid", threadContent.this.pid);
					mBundle.putString("author", tempString[1]);
					mIntent.putExtras(mBundle);
					threadContent.this.startActivity(mIntent);
				}
			}
		});
        waitDialog = new ProgressDialog(threadContent.this);
        waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        waitDialog.setMessage("帖子正在加载中...");
        waitDialog.setIndeterminate(true);
        waitDialog.setCancelable(true);
        waitDialog.show();
        
        imagePattern = Pattern.compile(rex, Pattern.DOTALL);
        filePattern = Pattern.compile(filerex, Pattern.DOTALL);
        
        getAllBoard();
        setListAdapter(mAdapter);
        getListView().setClickable(true);
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
	
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
				case 0://接到从线程内传来的图片bitmap和imageView.
						//这里只是将bitmap传到imageView中就行了。只所以不在线程中做是考虑到线程的安全性。
					((contentAdapter)mAdapter).notifyDataSetChanged();
					waitDialog.cancel();
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
		                	if (inputLine.length()<=38) {
		                		tempString = tempString + "<br/>";
		    				}
		                	threadContent.this.contents.set(contentcounter, tempString);
						}
		            }
		            reader.close();  
		            in.close();  
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
		    			if (first) {
							title = titleString;
							first = false;
						} else {
							title = "Re: " + titleString;
						}
		    			/*
		    			int i = raw.indexOf("题:");
		    			String title = raw.substring(i);
		    			raw = title;
		    			title = title.substring(5, title.indexOf("发信站: "));
		    			*/
		    			
		    			/*
		    			if (raw.contains("信区:")) {
		    				board = raw.substring(raw.indexOf("信区:")+3, i-1);
						}
						
		    			if (raw.contains("(") && raw.contains(")")) {
		    				time = raw.substring(raw.indexOf("(")+1, raw.indexOf(")"));
						}
						*/

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
		    			
		    			content = content.replace("http://bbs.nju.edu.cn/file", "<pic>http://bbs.nju.edu.cn/file");
		    			content = content.replace("jpg", "jpg</pic><br/>");
		    			content = content.replace("JPG", "JPG</pic><br/>");
		    			content = content.replace("gif", "gif</pic><br/>");
		    			content = content.replace("GIF", "GIF</pic><br/>");
		    			content = content.replace("png", "png</pic><br/>");
		    			content = content.replace("PNG", "PNG</pic><br/>");
		    			content = content.replace("jpeg", "jpeg</pic><br/>");
		    			content = content.replace("JPEG", "JPEG</pic><br/>");
		    			
		    			/*
		    			Matcher imageMatcher = imagePattern.matcher(content);
		    			while (imageMatcher.find()) {
							String tempString = imageMatcher.group();
							content.replace(tempString, "<pic>" + tempString + "</pic>");
						}
		    			*/
		    			if (content.indexOf("--")!=-1) {
		    				content = content.substring(0, content.lastIndexOf("--"));
						}
		    			content = content.replaceAll("\\[(1;.*?|37;1|32|33)m", "");
		    			threadContent.this.commentsMap.put(no, title + '#' + author + '#' + replylink + '#' + time + '#' + board + '#' + content);
		    			((contentAdapter)threadContent.this.mAdapter).no.add(no);
		    			counter += 1;
		    		}
		    		
		    		userinfo mUserinfo = (userinfo)getApplication();
		    		String code = mUserinfo.getCode();
		    		String cookie = mUserinfo.getCookies();
		    		String tempString = threadContent.this.commentsMap.get("0").split("#")[2];
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
            	strings = commentsMap.get(no.get(position)).split("#");
            	String title = strings[0];
            	String author = strings[1];
            	String time = strings[3];
            	String content = strings[5];
            	String board = strings[4];
            	String id = no.get(position);
            	cv = new contentView(mContext, id, title, content, author, time, board);
                       // mDialogue[position]);
            } else {
            	strings = commentsMap.get(no.get(position)).split("#");
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
		private TextView boardTextView;
		TextView contentWebView;
        Context mContext;
		
		public contentView(Context context, String id, String title,
							String content, String author, String time,
							String board) {
			super(context);
			// TODO Auto-generated constructor stub
			this.setOrientation(VERTICAL);			
			mContext = context;
			LinearLayout topLayout = new LinearLayout(context);
			topLayout.setOrientation(VERTICAL);
			RelativeLayout mRow1 = new RelativeLayout(context);
			RelativeLayout mRow2 = new RelativeLayout(context);
			
			titleTextView = new TextView(context);
			titleTextView.setText(titleString);
			titleTextView.setSingleLine(false);
			titleTextView.setWidth(300);
			RelativeLayout.LayoutParams paramstitle = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			paramstitle.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
			
			noTextView = new TextView(context);
			String link = "<reply>回复 " + id + " 楼</reply>";
			noTextView.setMovementMethod(LinkMovementMethod.getInstance());
			noTextView.setClickable(false);
			noTextView.setText(Html.fromHtml(link, null, new CustomTagHandler()));
			RelativeLayout.LayoutParams paramsNo = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
																				RelativeLayout.LayoutParams.WRAP_CONTENT);
			paramsNo.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
			noTextView.setLayoutParams(paramsNo);
			
			authorTextView = new TextView(context);
			authorTextView.setMovementMethod(LinkMovementMethod.getInstance());
			authorTextView.setClickable(false);
			authorTextView.setText(Html.fromHtml("<user>作者：" + author + "</user>", null, new CustomTagHandler()));
			RelativeLayout.LayoutParams paramsAuthor = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			paramsAuthor.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
			authorTextView.setLayoutParams(paramsAuthor);
			timeTextView = new TextView(context);
			timeTextView.setText(time);
			RelativeLayout.LayoutParams paramsTime = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			paramsTime.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
			timeTextView.setLayoutParams(paramsTime);
			boardTextView = new TextView(context);
			boardTextView.setText(board);

			contentWebView = new TextView(context);
			contentWebView.setClickable(false);
			contentWebView.setTextColor(Color.BLACK);
			contentWebView.setPadding(4, 0, 4, 0);
			contentWebView.setMovementMethod(LinkMovementMethod.getInstance());			
			contentWebView.setText(Html.fromHtml(content, null, new CustomTagHandler()));
			mRow1.addView(titleTextView);
			mRow1.addView(noTextView);
			mRow2.addView(authorTextView);
			mRow2.addView(timeTextView);
			
			topLayout.addView(mRow1);
			topLayout.addView(mRow2);
			topLayout.addView(contentWebView, LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

			addView(topLayout, new LinearLayout.LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		}
		
		public void setTitle(String title) {
			titleTextView.setText(title);
        }
		
		public void setNo(String no) {
			noTextView.setMovementMethod(LinkMovementMethod.getInstance());
			noTextView.setClickable(false);
			noTextView.setText(Html.fromHtml("<reply>回复 " + no + " 楼</reply>", null, new CustomTagHandler()));
		}
		
		public void setTime(String time) {
			timeTextView.setText(time);
        }
		
		public void setAuthor(String author) {
			authorTextView.setMovementMethod(LinkMovementMethod.getInstance());
			authorTextView.setClickable(false);
			authorTextView.setText(Html.fromHtml("<user>作者：" + author + "</user>", null, new CustomTagHandler()));
        }

        public void setDialogue(String words) {
        	words = "<html>" + words;
        	words = words + "</html>";
        	contentWebView.setMovementMethod(LinkMovementMethod.getInstance());
			contentWebView.setClickable(false);
			contentWebView.setText(Html.fromHtml(words, null, new CustomTagHandler()));
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
	        output.setSpan(new PicSpan(tempString), PicstartIndex, PicstopIndex,  
	                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);  
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
	        	Intent mIntent = new Intent();
	        	mIntent.setClass(threadContent.this, threadImage.class);
	        	Bundle mBundle = new Bundle();
	        	mBundle.putString("image", urlString);
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
	        	String[] tempString = commentsMap.get(no).split("#");
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
}