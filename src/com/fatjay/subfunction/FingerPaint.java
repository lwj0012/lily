package com.fatjay.subfunction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fatjay.effects.ColorPickerDialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.*;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

public class FingerPaint extends Activity implements ColorPickerDialog.OnColorChangedListener {

	private FingerView	tuyaView = null;
	private MaskFilter	mEmboss;
    private MaskFilter	mBlur;
    private static Paint	mPaint;
    
	private String filenameString; 

    private static final int COLOR_MENU_ID = Menu.FIRST;
    private static final int EMBOSS_MENU_ID = Menu.FIRST + 1;
    private static final int BLUR_MENU_ID = Menu.FIRST + 2;
    private static final int ERASE_MENU_ID = Menu.FIRST + 3;
    private static final int UNDO_MENU_ID = Menu.FIRST + 4;
    private static final int REDO_MENU_ID = Menu.FIRST + 5;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		
		filenameString = getIntent().getExtras().getString("filename");
		
		mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFFFFFFFF);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);
        
        mEmboss = new EmbossMaskFilter(new float[] { 1, 1, 1 },
                                       0.4f, 6, 3.5f);

        mBlur = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);

		tuyaView = new FingerView(this, dm.widthPixels, dm.heightPixels);
		setContentView(tuyaView);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {// 返回键
			new AlertDialog.Builder(FingerPaint.this)
			.setTitle("提示")
			.setMessage("完成涂鸦？点错了请按返回键")
			.setNegativeButton("放弃", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			})
			.setPositiveButton("完成", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					String fileUrl = Environment.getExternalStorageDirectory().toString() + "/lily/temp/" + filenameString;
					try {
						FileOutputStream fos = new FileOutputStream(new File(fileUrl));
						tuyaView.mBitmap.compress(CompressFormat.JPEG, 100, fos);
						fos.flush();
						fos.close();
						setResult(RESULT_OK);
						finish();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
		}).show();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
	static class FingerView extends View {

		private Bitmap mBitmap;
		private Canvas mCanvas;
		private Path mPath;
		private Paint mBitmapPaint;// 画布的画笔
		//private Paint mPaint;// 真实的画笔
		private float mX, mY;// 临时点坐标
		private static final float TOUCH_TOLERANCE = 1;
		
		// 保存Path路径的集合,用List集合来模拟栈
		private static List<DrawPath> savePath;
		private static List<DrawPath> undoPath;
		// 记录Path路径的对象
		private DrawPath dp;

		private int screenWidth, screenHeight;// 屏幕長寬

		private class DrawPath {
			public Path path;// 路径
			public Paint paint;// 画笔
		}

		public FingerView(Context context, int w, int h) {
			super(context);
			screenWidth = w;
			screenHeight = h;

			mBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
			mBitmap.eraseColor(0xffffffff);
			// 保存一次一次绘制出来的图形
			mCanvas = new Canvas(mBitmap);

			mBitmapPaint = new Paint(Paint.DITHER_FLAG);
			mPaint = new Paint();
			mPaint.setAntiAlias(true);
			mPaint.setStyle(Paint.Style.STROKE);
			mPaint.setStrokeJoin(Paint.Join.ROUND);// 设置外边缘
			mPaint.setStrokeCap(Paint.Cap.SQUARE);// 形状
			mPaint.setStrokeWidth(4);// 画笔宽度

			savePath = new ArrayList<DrawPath>();
			undoPath = new ArrayList<DrawPath>();
		}

		@Override
		public void onDraw(Canvas canvas) {
			canvas.drawColor(0x80000000);
			// 将前面已经画过得显示出来
			canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
			if (mPath != null) {
				// 实时的显示
				canvas.drawPath(mPath, mPaint);
			}
		}

		private void touch_start(float x, float y) {
			mPath.moveTo(x, y);
			mX = x;
			mY = y;
		}

		private void touch_move(float x, float y) {
			float dx = Math.abs(x - mX);
			float dy = Math.abs(mY - y);
			if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
				// 从x1,y1到x2,y2画一条贝塞尔曲线，更平滑(直接用mPath.lineTo也是可以的)
				mPath.lineTo((x + mX) / 2, (y + mY) / 2);
				mX = x;
				mY = y;
			}
		}

		private void touch_up() {
			mPath.lineTo(mX, mY);
			mCanvas.drawPath(mPath, mPaint);
			//将一条完整的路径保存下来(相当于入栈操作)
			savePath.add(dp);
			mPath = null;// 重新置空
		}

		public void undo() {
			mBitmap = Bitmap.createBitmap(screenWidth, screenHeight,
					Bitmap.Config.ARGB_8888);
			mBitmap.eraseColor(0xffffffff);
			mCanvas.setBitmap(mBitmap);// 重新设置画布，相当于清空画布 
			// 清空画布，但是如果图片有背景的话，则使用上面的重新初始化的方法，用该方法会将背景清空掉...
			if (savePath != null && savePath.size() > 0) {
				// 移除最后一个path,相当于出栈操作
				undoPath.add(savePath.get(savePath.size()-1));
				savePath.remove(savePath.size() - 1);

				Iterator<DrawPath> iter = savePath.iterator();
				while (iter.hasNext()) {
					DrawPath drawPath = iter.next();
					mCanvas.drawPath(drawPath.path, drawPath.paint);
				}
				invalidate();// 刷新
			}
		}

		public void redo(){
			mBitmap = Bitmap.createBitmap(screenWidth, screenHeight,
					Bitmap.Config.ARGB_8888);
			mBitmap.eraseColor(0xffffffff);
			mCanvas.setBitmap(mBitmap);// 重新设置画布，相当于清空画布 
			// 清空画布，但是如果图片有背景的话，则使用上面的重新初始化的方法，用该方法会将背景清空掉...
			if (undoPath != null && undoPath.size() > 0) {
				// 移除最后一个path,相当于出栈操作
				savePath.add(undoPath.get(undoPath.size()-1));
				undoPath.remove(undoPath.size() - 1);

				Iterator<DrawPath> iter = savePath.iterator();
				while (iter.hasNext()) {
					DrawPath drawPath = iter.next();
					mCanvas.drawPath(drawPath.path, drawPath.paint);
				}
				invalidate();// 刷新
			}
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			float x = event.getX();
			float y = event.getY();

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				// 每次down下去重新new一个Path
				mPath = new Path();
				//每一次记录的路径对象是不一样的
				dp = new DrawPath();
				dp.path = mPath;
				dp.paint = mPaint;
				touch_start(x, y);
				invalidate();
				break;
			case MotionEvent.ACTION_MOVE:
				touch_move(x, y);
				invalidate();
				break;
			case MotionEvent.ACTION_UP:
				touch_up();
				invalidate();
				break;
			}
			return true;
		}

	}

	@Override
	public void colorChanged(int color) {
		// TODO Auto-generated method stub
		Paint oldPaint = mPaint;
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStyle(oldPaint.getStyle());
		mPaint.setStrokeJoin(oldPaint.getStrokeJoin());
		mPaint.setStrokeCap(oldPaint.getStrokeCap());// 形状
		mPaint.setStrokeWidth(oldPaint.getStrokeWidth());// 画笔宽度
        mPaint.setXfermode(oldPaint.getXfermode());
        mPaint.setAlpha(oldPaint.getAlpha());
		mPaint.setColor(color);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, COLOR_MENU_ID, 0, "颜色").setShortcut('3', 'c');
        menu.add(0, EMBOSS_MENU_ID, 0, "立体").setShortcut('4', 's');
        menu.add(0, BLUR_MENU_ID, 0, "刷子").setShortcut('5', 'z');
        menu.add(0, ERASE_MENU_ID, 0, "橡皮擦").setShortcut('5', 'z');
        menu.add(0, UNDO_MENU_ID, 0, "撤销").setShortcut('5', 'z');
        menu.add(0, REDO_MENU_ID, 0, "重做").setShortcut('5', 'z');
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (tuyaView.savePath.size() <= 0) {
			menu.findItem(UNDO_MENU_ID).setEnabled(false);
		} else {
			menu.findItem(UNDO_MENU_ID).setEnabled(true);
		}
        if (tuyaView.undoPath.size() <= 0) {
			menu.findItem(REDO_MENU_ID).setEnabled(false);
		} else {
			menu.findItem(REDO_MENU_ID).setEnabled(true);
		}
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	mPaint = new Paint();
    	mPaint.setAntiAlias(true);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);// 设置外边缘
		mPaint.setStrokeCap(Paint.Cap.SQUARE);// 形状
		mPaint.setStrokeWidth(4);// 画笔宽度
        mPaint.setXfermode(null);
        mPaint.setAlpha(0xFF);

        switch (item.getItemId()) {
            case COLOR_MENU_ID:
                new ColorPickerDialog(this, this, mPaint.getColor()).show();
                return true;
            case EMBOSS_MENU_ID:
                if (mPaint.getMaskFilter() != mEmboss) {
                    mPaint.setMaskFilter(mEmboss);
                } else {
                    mPaint.setMaskFilter(null);
                }
                return true;
            case BLUR_MENU_ID:
                if (mPaint.getMaskFilter() != mBlur) {
                    mPaint.setMaskFilter(mBlur);
                } else {
                    mPaint.setMaskFilter(null);
                }
                return true;
            case ERASE_MENU_ID:
                mPaint.setXfermode(new PorterDuffXfermode(
                                                        PorterDuff.Mode.CLEAR));
                return true;
            case UNDO_MENU_ID:
            	tuyaView.undo();
                return true;
            case REDO_MENU_ID:
                tuyaView.redo();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
	
}