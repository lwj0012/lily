package com.fatjay.subfunction;

import java.util.HashMap;
import java.util.Map;

import com.fatjay.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class imageSelector extends Activity implements OnItemClickListener {
	private ImageAdapter mAdapter;
	private Map<Integer, String> imageMap = new HashMap<Integer, String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
				WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        setContentView(R.layout.image_selector);

        GridView g = (GridView) findViewById(R.id.image_selector_Grid);
        
        mAdapter = new ImageAdapter(this);
        getMap();
        g.setAdapter(mAdapter);
        g.setOnItemClickListener(this);
    }
    
    private void getMap() {
		// TODO Auto-generated method stub
    	imageMap.put(R.drawable.face2, "[:s]");
    	imageMap.put(R.drawable.face0, "[:O]");
    	imageMap.put(R.drawable.face3, "[:|]");
    	imageMap.put(R.drawable.face6, "[:$]");
    	imageMap.put(R.drawable.face7, "[:X]");
    	imageMap.put(R.drawable.face9, "[:'(]");
    	imageMap.put(R.drawable.face10, "[:-|]");
    	imageMap.put(R.drawable.face11, "[:@]");
    	imageMap.put(R.drawable.face12, "[:P]");
    	imageMap.put(R.drawable.face13, "[:D]");
    	imageMap.put(R.drawable.face14, "[:)]");
    	imageMap.put(R.drawable.face15, "[:(]");
    	imageMap.put(R.drawable.face18, "[:Q]");
    	imageMap.put(R.drawable.face19, "[:T]");
    	imageMap.put(R.drawable.face20, "[;P]");
    	imageMap.put(R.drawable.face21, "[;-D]");
    	imageMap.put(R.drawable.face26, "[:!]");
    	imageMap.put(R.drawable.face27, "[:L]");
    	imageMap.put(R.drawable.face32, "[:?]");
    	imageMap.put(R.drawable.face16, "[:U]");
    	imageMap.put(R.drawable.face25, "[:K]");
    	imageMap.put(R.drawable.face29, "[:C-]");
    	imageMap.put(R.drawable.face34, "[;X]");
    	imageMap.put(R.drawable.face36, "[:H]");
    	imageMap.put(R.drawable.face39, "[;bye]");
    	imageMap.put(R.drawable.face4, "[;cool]");
    	imageMap.put(R.drawable.face40, "[:-b]");
    	imageMap.put(R.drawable.face41, "[:-8]");
    	imageMap.put(R.drawable.face42, "[;PT]");
    	imageMap.put(R.drawable.face43, "[;-C]");
    	imageMap.put(R.drawable.face44, "[:hx]");
    	imageMap.put(R.drawable.face47, "[;K]");
    	imageMap.put(R.drawable.face49, "[:E]");
    	imageMap.put(R.drawable.face50, "[:-(]");
    	imageMap.put(R.drawable.face51, "[;hx]");
    	imageMap.put(R.drawable.face52, "[:B]");
    	imageMap.put(R.drawable.face53, "[:-v]");
    	imageMap.put(R.drawable.face54, "[;xx]");
	}

	public class ImageAdapter extends BaseAdapter {
        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return mThumbIds.length;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(30, 30));
                imageView.setAdjustViewBounds(true);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(1, 1, 1, 1);
            } else {
                imageView = (ImageView) convertView;
            }

            imageView.setImageResource(mThumbIds[position]);

            return imageView;
        }

        private Context mContext;

        private Integer[] mThumbIds = {
        		R.drawable.face0,
        		R.drawable.face2,
        		R.drawable.face4,
        		R.drawable.face3,
        		R.drawable.face6,
        		R.drawable.face7,
        		R.drawable.face9,
        		R.drawable.face10,
        		R.drawable.face11,
        		R.drawable.face12,
        		R.drawable.face13,
        		R.drawable.face14,
        		R.drawable.face15,
        		R.drawable.face16,
        		R.drawable.face18,
        		R.drawable.face19,
        		R.drawable.face20,
        		R.drawable.face21,
        		R.drawable.face25,
        		R.drawable.face26,
        		R.drawable.face27,
        		R.drawable.face29,
        		R.drawable.face32,
        		R.drawable.face34,
        		R.drawable.face36,
        		R.drawable.face39,
        		R.drawable.face40,
        		R.drawable.face41,
        		R.drawable.face42,
        		R.drawable.face43,
        		R.drawable.face44,
        		R.drawable.face47,
        		R.drawable.face49,
        		R.drawable.face50,
        		R.drawable.face51,
        		R.drawable.face52,
        		R.drawable.face53,
        		R.drawable.face54
        };
    }
    
    

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		Intent mIntent = new Intent();
		mIntent.putExtra("biaoqing", imageMap.get(mAdapter.mThumbIds[arg2]));
//		Bundle mBundle = new Bundle();
//		mBundle.putString("biaoqing", imageMap.get(mAdapter.mThumbIds[arg2]));
//		mIntent.putExtras(mBundle);
		if (getParent() == null) {
			setResult(RESULT_OK, mIntent);
		} else {
			getParent().setResult(RESULT_OK, mIntent);
		}
		finish();
	}
}