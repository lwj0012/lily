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
		if (keyCode == KeyEvent.KEYCODE_BACK) {// ���ؼ�
			new AlertDialog.Builder(FingerPaint.this)
			.setTitle("��ʾ")
			.setMessage("���Ϳѻ��������밴���ؼ�")
			.setNegativeButton("����", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			})
			.setPositiveButton("���", new DialogInterface.OnClickListener() {
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
		private Paint mBitmapPaint;// �����Ļ���
		//private Paint mPaint;// ��ʵ�Ļ���
		private float mX, mY;// ��ʱ������
		private static final float TOUCH_TOLERANCE = 1;
		
		// ����Path·���ļ���,��List������ģ��ջ
		private static List<DrawPath> savePath;
		private static List<DrawPath> undoPath;
		// ��¼Path·���Ķ���
		private DrawPath dp;

		private int screenWidth, screenHeight;// ��Ļ�L��

		private class DrawPath {
			public Path path;// ·��
			public Paint paint;// ����
		}

		public FingerView(Context context, int w, int h) {
			super(context);
			screenWidth = w;
			screenHeight = h;

			mBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
			mBitmap.eraseColor(0xffffffff);
			// ����һ��һ�λ��Ƴ�����ͼ��
			mCanvas = new Canvas(mBitmap);

			mBitmapPaint = new Paint(Paint.DITHER_FLAG);
			mPaint = new Paint();
			mPaint.setAntiAlias(true);
			mPaint.setStyle(Paint.Style.STROKE);
			mPaint.setStrokeJoin(Paint.Join.ROUND);// �������Ե
			mPaint.setStrokeCap(Paint.Cap.SQUARE);// ��״
			mPaint.setStrokeWidth(4);// ���ʿ��

			savePath = new ArrayList<DrawPath>();
			undoPath = new ArrayList<DrawPath>();
		}

		@Override
		public void onDraw(Canvas canvas) {
			canvas.drawColor(0x80000000);
			// ��ǰ���Ѿ���������ʾ����
			canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
			if (mPath != null) {
				// ʵʱ����ʾ
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
				// ��x1,y1��x2,y2��һ�����������ߣ���ƽ��(ֱ����mPath.lineToҲ�ǿ��Ե�)
				mPath.lineTo((x + mX) / 2, (y + mY) / 2);
				mX = x;
				mY = y;
			}
		}

		private void touch_up() {
			mPath.lineTo(mX, mY);
			mCanvas.drawPath(mPath, mPaint);
			//��һ��������·����������(�൱����ջ����)
			savePath.add(dp);
			mPath = null;// �����ÿ�
		}

		public void undo() {
			mBitmap = Bitmap.createBitmap(screenWidth, screenHeight,
					Bitmap.Config.ARGB_8888);
			mBitmap.eraseColor(0xffffffff);
			mCanvas.setBitmap(mBitmap);// �������û������൱����ջ��� 
			// ��ջ������������ͼƬ�б����Ļ�����ʹ����������³�ʼ���ķ������ø÷����Ὣ������յ�...
			if (savePath != null && savePath.size() > 0) {
				// �Ƴ����һ��path,�൱�ڳ�ջ����
				undoPath.add(savePath.get(savePath.size()-1));
				savePath.remove(savePath.size() - 1);

				Iterator<DrawPath> iter = savePath.iterator();
				while (iter.hasNext()) {
					DrawPath drawPath = iter.next();
					mCanvas.drawPath(drawPath.path, drawPath.paint);
				}
				invalidate();// ˢ��
			}
		}

		public void redo(){
			mBitmap = Bitmap.createBitmap(screenWidth, screenHeight,
					Bitmap.Config.ARGB_8888);
			mBitmap.eraseColor(0xffffffff);
			mCanvas.setBitmap(mBitmap);// �������û������൱����ջ��� 
			// ��ջ������������ͼƬ�б����Ļ�����ʹ����������³�ʼ���ķ������ø÷����Ὣ������յ�...
			if (undoPath != null && undoPath.size() > 0) {
				// �Ƴ����һ��path,�൱�ڳ�ջ����
				savePath.add(undoPath.get(undoPath.size()-1));
				undoPath.remove(undoPath.size() - 1);

				Iterator<DrawPath> iter = savePath.iterator();
				while (iter.hasNext()) {
					DrawPath drawPath = iter.next();
					mCanvas.drawPath(drawPath.path, drawPath.paint);
				}
				invalidate();// ˢ��
			}
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			float x = event.getX();
			float y = event.getY();

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				// ÿ��down��ȥ����newһ��Path
				mPath = new Path();
				//ÿһ�μ�¼��·�������ǲ�һ����
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
		mPaint.setStrokeCap(oldPaint.getStrokeCap());// ��״
		mPaint.setStrokeWidth(oldPaint.getStrokeWidth());// ���ʿ��
        mPaint.setXfermode(oldPaint.getXfermode());
        mPaint.setAlpha(oldPaint.getAlpha());
		mPaint.setColor(color);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, COLOR_MENU_ID, 0, "��ɫ").setShortcut('3', 'c');
        menu.add(0, EMBOSS_MENU_ID, 0, "����").setShortcut('4', 's');
        menu.add(0, BLUR_MENU_ID, 0, "ˢ��").setShortcut('5', 'z');
        menu.add(0, ERASE_MENU_ID, 0, "��Ƥ��").setShortcut('5', 'z');
        menu.add(0, UNDO_MENU_ID, 0, "����").setShortcut('5', 'z');
        menu.add(0, REDO_MENU_ID, 0, "����").setShortcut('5', 'z');
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
		mPaint.setStrokeJoin(Paint.Join.ROUND);// �������Ե
		mPaint.setStrokeCap(Paint.Cap.SQUARE);// ��״
		mPaint.setStrokeWidth(4);// ���ʿ��
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