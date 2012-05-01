/**  
 * GalleryAdapter.java
 * @version 1.0
 * @author Haven
 * @createTime 2011-12-9 下午05:04:34
 */
package com.fatjay.effects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Gallery;


public class GalleryAdapter extends BaseAdapter {

	private Context context;
	private String images[] = {};

	public GalleryAdapter(Context context, String[] filenameStrings) {
		this.context = context;
		images = filenameStrings;
	}

	@Override
	public int getCount() {
		return images.length;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Bitmap bmp = BitmapFactory.decodeFile("/sdcard/lily/" + images[position]);
//		Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), images[position]);
		MyImageView view = new MyImageView(context, bmp.getWidth(), bmp.getHeight());
		view.setLayoutParams(new Gallery.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		view.setImageBitmap(bmp);
		return view;
	}

}