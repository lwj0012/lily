package com.fatjay.subfunction;

import java.util.ArrayList;
import java.util.Arrays;

import com.fatjay.effects.ListViewInterceptor;
import com.fatjay.function.favor;
import com.fatjay.R;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
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

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.morefavor);
		SharedPreferences mPreferences = getSharedPreferences("favor", 0);
		String[] favorboard = mPreferences.getString("favor", null).split("#");
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
