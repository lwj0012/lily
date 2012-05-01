package com.fatjay.subfunction;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;

import com.fatjay.R;
import com.fatjay.main.userinfo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class mail extends Activity implements OnClickListener {
	userinfo mUserinfo;
	String mailto;
	String urlString;
	String pidString = "";
	String titleString = "";
	String action;
	String mailId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mail_body);
		mUserinfo = (userinfo) getApplication();
		action = getIntent().getExtras().getString("action");
		
		if (action.equals("reply")) {
			String temp = getIntent().getExtras().getString("url");
			mailto = temp.substring(temp.indexOf("userid=")+7);
			mailto = mailto.substring(0, mailto.indexOf("&"));
			pidString = temp.substring(temp.indexOf("pid=")+4);
			pidString = pidString.substring(0, pidString.indexOf("&"));
			titleString = temp.substring(temp.indexOf("title=")+6);
			try {
				titleString = URLDecoder.decode(titleString, "utf-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			((EditText)findViewById(R.id.mail_title)).setText(titleString);
			((EditText)findViewById(R.id.mail_title)).setEnabled(false);
			((EditText)findViewById(R.id.mail_to)).setText(mailto);
			((EditText)findViewById(R.id.mail_to)).setEnabled(false);
		} else if (action.equals("advice")) {
			mailto = getIntent().getExtras().getString("mailto");
			((EditText)findViewById(R.id.mail_to)).setText(mailto);
			((EditText)findViewById(R.id.mail_to)).setEnabled(false);
			((EditText)findViewById(R.id.mail_title)).setText("意见与反馈");
		} else {
			mailto = getIntent().getExtras().getString("mailto");
			((EditText)findViewById(R.id.mail_to)).setText(mailto);
			((EditText)findViewById(R.id.mail_to)).setEnabled(false);
			((EditText)findViewById(R.id.mail_title)).setText("无主题");
		}
		
		Button send = (Button)findViewById(R.id.mail_ok);
		send.setOnClickListener(this);
		Button cancelButton = (Button)findViewById(R.id.mail_cancel);
		cancelButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.mail_ok:
			if (!send()) {
				getIdentify();
				send();
			} else {
				finish();
			}
			break;
		case R.id.mail_cancel:
			finish();
		default:
			break;
		}
	}
	
	private boolean getIdentify() {
		try {
			Random random = new Random();
			int s = random.nextInt(99999)%(90000) + 10000;
			String username = mUserinfo.getUsername();
			String pwd = mUserinfo.getPwd();
			if (username == "") {
				Toast.makeText(getApplicationContext(), "请先设置帐号信息", Toast.LENGTH_LONG).show();
				finish();
			}
			String urlString = "http://bbs.nju.edu.cn/vd" + String.valueOf(s) + "/bbslogin?type=2&id=" + username + "&pw=" + pwd;
			String doc = Jsoup.connect(urlString).get().toString();
			int t = doc.indexOf("setCookie");
			if (t == -1) {
				return false;
			}
			else {
				String tempString = doc.substring(t);
				tempString = tempString.substring(11, tempString.indexOf(")")-1);
				String[] tm =  tempString.split("\\+");
				String _U_KEY = String.valueOf(Integer.parseInt(tm[1])-2);
				String[] tm2 = tm[0].split("N");
				String _U_UID = tm2[1];
				String _U_NUM = "" + String.valueOf(Integer.parseInt(tm2[0]) + 2);
				mUserinfo.setCookies("_U_KEY=" + _U_KEY + "; " + "_U_UID=" + _U_UID + "; " + "_U_NUM=" + _U_NUM + ";");
				mUserinfo.setUsername(username);
				mUserinfo.setPwd(pwd);
				mUserinfo.setCode("vd" + String.valueOf(s));
				return true;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean send() {
		String code = mUserinfo.getCode();
		String cookie = mUserinfo.getCookies();
		String contentString = ((EditText)findViewById(R.id.mail_content)).getText().toString();
		String title = ((EditText)findViewById(R.id.mail_title)).getText().toString();
		String recivicer = ((EditText)findViewById(R.id.mail_to)).getText().toString();
		String newurlString;
		try {
			HttpResponse httpResponse;
			if (action.equals("reply")) {
				newurlString = "http://bbs.nju.edu.cn/" + code + "/bbssndmail?pid=" + pidString + "&userid=" + recivicer;
				HttpPost httpRequest = new HttpPost(newurlString);
			    ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();
			    postData.add(new BasicNameValuePair("action", mailId));
			    postData.add(new BasicNameValuePair("signature", "1"));
			    postData.add(new BasicNameValuePair("text", contentString));
			    postData.add(new BasicNameValuePair("title", title));
			    postData.add(new BasicNameValuePair("userid", recivicer));
			    httpRequest.addHeader("Cookie", cookie);
			    httpRequest.setEntity(new UrlEncodedFormEntity(postData, "GB2312"));
			    httpResponse = new DefaultHttpClient().execute(httpRequest);
				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					Toast.makeText(getApplicationContext(), "发送成功！",
						     Toast.LENGTH_SHORT).show();
					return true;
				} else {
					Toast.makeText(getApplicationContext(), "发送失败！",
						     Toast.LENGTH_SHORT).show();
					return false;
				}
			} else {
				newurlString = "http://bbs.nju.edu.cn/" + code + "/bbssndmail?pid=0&userid=" + recivicer;
				HttpPost httpRequest = new HttpPost(newurlString);
			    ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();
			    postData.add(new BasicNameValuePair("signature", "1"));
			    postData.add(new BasicNameValuePair("text", contentString));
			    postData.add(new BasicNameValuePair("title", title));
			    postData.add(new BasicNameValuePair("userid", recivicer));
			    httpRequest.addHeader("Cookie", cookie);
			    httpRequest.setEntity(new UrlEncodedFormEntity(postData, "GB2312"));
			    httpResponse = new DefaultHttpClient().execute(httpRequest);
				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					Toast.makeText(getApplicationContext(), "发送成功！",
						     Toast.LENGTH_SHORT).show();
					return true;
				} else {
					Toast.makeText(getApplicationContext(), "发送失败！",
						     Toast.LENGTH_SHORT).show();
					return false;
				}
			}
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

}
