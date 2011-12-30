package com.fatjay.subfunction;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.conn.ConnectTimeoutException;

import com.fatjay.effects.ListViewInterceptor;
import com.fatjay.function.favor;
import com.fatjay.main.userinfo;
import com.fatjay.R;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class moreFavor extends ListActivity {
	
	/** Called when the activity is first created. */
	private MyAdapter adapter = null;
	private ArrayList<String> array = null;
	String[] favorboard;
	
	userinfo mUserinfo;
	
	ProgressDialog waitDialog;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.morefavor);
		mUserinfo = (userinfo) getApplication();
		SharedPreferences mPreferences = getSharedPreferences("favor", 0);
		favorboard = mPreferences.getString("favor", "D_Computer#Joke#Pictures").split("#");
		array = new ArrayList<String>( Arrays.asList(favorboard) );
		adapter = new MyAdapter();
		Button submmit = (Button)findViewById(R.id.morefavor_submmit);
		submmit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String preference = "";
				int size = adapter.getList().size();
				for (int i = 0; i < size; i++) {
					preference += adapter.getList().get(i);
					if (i<size-1) {
						preference += "#";
					}
				}
				SharedPreferences mPreferences = getSharedPreferences("favor", 0);
				SharedPreferences.Editor mEditor = mPreferences.edit();
				mEditor.putString("favor", preference);
				if (!mEditor.commit()) {
					Intent it = new Intent(); 
					Bundle bundle=it.getExtras();
					bundle.putString("favor",preference);
					moreFavor.this.setResult(RESULT_CANCELED, it);
					Toast.makeText(moreFavor.this, "save configration failed...", Toast.LENGTH_SHORT).show();
					Intent restartFavor = new Intent(); 
					restartFavor.setClass(moreFavor.this, favor.class);
					moreFavor.this.startActivity(restartFavor);
					finish();
				} else {
					//favor.instance.finish();
					Intent restartFavor = new Intent(); 
					restartFavor.setClass(moreFavor.this, favor.class);
					Bundle mBundle = new Bundle();
					mBundle.putString("favor", preference);
					restartFavor.putExtras(mBundle);
					setResult(RESULT_OK, restartFavor);
					finish();
				}
			}
		});
		setListAdapter(adapter);
		ListViewInterceptor tlv = (ListViewInterceptor) getListView();
		tlv.setDropListener(onDrop);
		tlv.getAdapter();
	}
	
	private ListViewInterceptor.DropListener onDrop = new ListViewInterceptor.DropListener() {
		@Override
		public void drop(int from, int to) {
			String item = adapter.getItem(from);
			adapter.remove(item);
			adapter.insert(item, to);
		}
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.add(Menu.NONE, Menu.FIRST + 1, 5, "同步收藏到小百合").setIcon(android.R.drawable.ic_menu_edit);
        return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case Menu.FIRST + 1:
			waitDialog = ProgressDialog.show(this, "", "正在同步收藏...", true, true);
			sync_to_bbs();
			break;
		}
		return true;
	}
	
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
				case 0:
					waitDialog.cancel();
					Toast.makeText(moreFavor.this, "同步收藏成功！", Toast.LENGTH_LONG);
					break;
				case 1:
					waitDialog.cancel();
					Toast.makeText(moreFavor.this, "同步收藏不成功，请检查网络~", Toast.LENGTH_LONG);
				default:
					super.handleMessage(msg);
			}
		}
	};

	private ExecutorService executorService = Executors.newFixedThreadPool(5);
	
	public void sync_to_bbs() {
		executorService.submit(new Runnable() {
			public void run() {
	    		Message msg = new Message();
	    		msg.what = 0;
				String code = mUserinfo.getCode();
				String cookie = mUserinfo.getCookies();
				String urlString = "http://bbs.nju.edu.cn/" + code + "/bbsmybrd?type=1&confirm1=1";
				try {
					StringBuilder sb = new StringBuilder();
		
					if(favorboard.length!=0){
						for (int i = 0; i < favorboard.length; i++) {
		            		sb.append(favorboard[i]).append("=").append(URLEncoder.encode("on", "utf-8"));
		            		sb.append("&");
		            	}
						sb.append("confirm1=1");
					}
		            byte[] entity = sb.toString().getBytes();
		
		            URL url = new URL(urlString);
		            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		            conn.setConnectTimeout(5000);
		            conn.setRequestMethod("POST");
		            conn.setDoOutput(true);
		            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		            conn.setRequestProperty("Content-Length", entity.length+"");
		            conn.setRequestProperty("Cookie", cookie);
		            OutputStream os = conn.getOutputStream();
		            os.write(entity);
		            if(conn.getResponseCode()==200){
		            	handler.sendMessage(msg);
		            } else {
		            	msg.what = 1;
		            	handler.sendMessage(msg);
					}
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ConnectTimeoutException e) {
					// TODO: handle exception
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
		    }
		});
	}
	
	class MyAdapter extends ArrayAdapter<String> {
		
		MyAdapter() {
			super(moreFavor.this, R.layout.mylistview, array);
		}
		public ArrayList<String> getList() {
			return array;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			if (row == null) {
				LayoutInflater inflater = getLayoutInflater();
				row = inflater.inflate(R.layout.mylistview, parent, false);
			}
			TextView label = (TextView) row.findViewById(R.id.label);
			label.setText(array.get(position));
			return (row);
		}
	}
}
