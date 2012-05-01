package com.fatjay.subfunction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.fatjay.R;
import com.fatjay.main.userinfo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Paint.FontMetrics;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class mailContent extends Activity implements OnClickListener {
	userinfo mUserinfo;
	ProgressDialog waitDialog;
	String contentString = "";
	String replylink;
	String mailid;
	String titleString;
	String senderString;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mail_content);
		mUserinfo = (userinfo) getApplication();
		Button compose = (Button)findViewById(R.id.mail_content_reply);
		compose.setOnClickListener(this);
		Button cancel = (Button)findViewById(R.id.mail_content_cancel);
		cancel.setOnClickListener(this);
		getMail();
	}
	
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.mail_content_reply:
			Intent reply = new Intent(mailContent.this, mail.class);
			Bundle extrasBundle = new Bundle();
			extrasBundle.putString("url", replylink);
			extrasBundle.putString("action", "reply");
			reply.putExtras(extrasBundle);
			mailContent.this.startActivity(reply);
			break;
		case R.id.mail_content_cancel:
			finish();
			break;
		default:
			break;
		}
		
	}
	
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			TextView contentTextView = (TextView)findViewById(R.id.mail_content_main);
			switch (msg.what) {
				case 0:
					waitDialog.cancel();
					contentTextView.setText(contentString);
					break;
				case 1:
					waitDialog.cancel();
					contentTextView.setText("邮件加载出现问题，请确认网络连接正常");
				default:
					super.handleMessage(msg);
			}
		}
	};

	private ExecutorService executorService = Executors.newFixedThreadPool(5);
	
	
	void getMail() {
		waitDialog = ProgressDialog.show(this, "", "正在打开邮件...", true, true);
		executorService.submit(new Runnable() {
			public void run() {
				Message msg = new Message();
				String urlString = getIntent().getExtras().getString("url");
				mailid = urlString.substring(urlString.indexOf("file=")+5);
				mailid = mailid.substring(0, mailid.indexOf("&"));
				String cookie = mUserinfo.getCookies();
				urlString = "http://bbs.nju.edu.cn/" + mUserinfo.getCode() + "/" + urlString;
				
				try {
					URL mUrl = new URL(urlString);
					HttpURLConnection send = (HttpURLConnection) mUrl.openConnection();
					send.setDoOutput(true);
					send.setRequestMethod("GET");
					send.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
					if(cookie!=null && cookie.length()>0){
						send.setRequestProperty("Cookie", cookie);
		            }
					send.connect();
					InputStream in = send.getInputStream(); 
					BufferedReader reader = new BufferedReader(new InputStreamReader(in,"gb2312")); 
					String inputLine = null;  
					StringBuffer sb = new StringBuffer();
					
					int counter = 0;
					while ((inputLine = reader.readLine()) != null) {
						sb.append(inputLine);
						if (inputLine.contains("<textarea")) {
							senderString = inputLine.substring(inputLine.indexOf(":")+2);
							senderString = senderString.substring(0, senderString.indexOf("("));
							counter++;
							continue;
						}
						if (inputLine.contains("标  题")) {
							titleString = inputLine.substring(inputLine.indexOf(":")+1);
							counter++;
							continue;
						}
						if (counter>=2 && counter<4) {
							counter++;
						} else if (counter>=4 && !inputLine.contains("</textarea>")) {
							contentString += inputLine;
							if (inputLine.length()<=38) {
								contentString = contentString + "\n";
		    				}
						}
						if (inputLine.contains("</textarea>")) {
							while ((inputLine = reader.readLine()) != null) {
								sb.append(inputLine);
							}
						}
					}
					reader.close();  
					in.close();
					Document document = Jsoup.parse(sb.toString());
					Elements mElements = document.select("a");
					replylink = mElements.get(mElements.size()-3).toString();
					replylink = replylink.substring(replylink.indexOf("href=\"")+6);
					replylink = replylink.substring(0, replylink.indexOf("\">"));
					replylink = replylink.replace("&amp;", "&");
					msg.what = 0;
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				handler.sendMessage(msg);
			}
		});
	}


	

}
