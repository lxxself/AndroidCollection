package org.chillax.screenshot;

import java.io.File;

import org.chillax.test.MainActivity;
import org.chillax.test.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ScreenshotImg extends Activity {

	private LinearLayout imgSave;
	private ImageView imgView,imgScreenshot;
	private String imgPath;
	
	private static final int NONE = 0;
	private static final int DRAG = 1;
	private static final int ZOOM = 2;

	private int mode = NONE;
	private float oldDist;
	private Matrix matrix = new Matrix();
	private Matrix savedMatrix = new Matrix();
	private PointF start = new PointF();
	private PointF mid = new PointF();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.img_screenshot);

		imgView = (ImageView)findViewById(R.id.screenshot_img);
		imgScreenshot = (ImageView)findViewById(R.id.screenshot);
		imgSave = (LinearLayout)findViewById(R.id.img_save);
		
		Intent i = getIntent();
		imgPath = i.getStringExtra("ImgPath");
		
		Bitmap bitmap = getImgSource(imgPath);
		
		if(bitmap!=null){
			imgView.setImageBitmap(bitmap);
			imgView.setOnTouchListener(touch);
			imgSave.setOnClickListener(imgClick);
		}
		
	}
	
	OnClickListener imgClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			imgView.setDrawingCacheEnabled(true);
	        Bitmap bitmap = Bitmap.createBitmap(imgView.getDrawingCache());
	        
	        int w = imgScreenshot.getWidth();  
	        int h = imgScreenshot.getHeight();  
	        
	        int left = imgScreenshot.getLeft();   
	        int right = imgScreenshot.getRight();   
	        int top = imgScreenshot.getTop();   
	        int bottom = imgScreenshot.getBottom();
	        
	        Bitmap targetBitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
	                
	        Canvas canvas = new Canvas(targetBitmap);
	        Path path = new Path();
	        
	        path.addCircle((float)((right-left) / 2),((float)((bottom-top)) / 2), (float)(w / 2),
	        Path.Direction.CCW);
	        
	        canvas.clipPath(path);
	        
	        canvas.drawBitmap(bitmap,new Rect(left,top,right,bottom),new Rect(left,top,right,bottom),null);
	        
	        MainActivity.saveFile(targetBitmap, imgPath);
	        
	        Toast.makeText(getBaseContext(), "保存成功", Toast.LENGTH_LONG).show();
	        
	        finish();
		}
	};
	
	/**
	 * 触摸事件
	 */
	OnTouchListener touch = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			ImageView view = (ImageView) v;
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				savedMatrix.set(matrix); // 把原始 Matrix对象保存起来
				start.set(event.getX(), event.getY()); // 设置x,y坐标
				mode = DRAG;
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
				mode = NONE;
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				oldDist = spacing(event);
				if (oldDist > 10f) {
					savedMatrix.set(matrix);
					midPoint(mid, event); // 求出手指两点的中点
					mode = ZOOM;
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if (mode == DRAG) {
					matrix.set(savedMatrix);
					
					matrix.postTranslate(event.getX() - start.x, event.getY()
							- start.y);
				} else if (mode == ZOOM) {
					float newDist = spacing(event);
					if (newDist > 10f) {
						matrix.set(savedMatrix);
						float scale = newDist / oldDist;
						matrix.postScale(scale, scale, mid.x, mid.y);
					}
				}
				break;
			}
			System.out.println(event.getAction());
			view.setImageMatrix(matrix);
			return true;
		}
	};

	//求两点距离
	private float spacing(MotionEvent event) {
	float x = event.getX(0) - event.getX(1);
	float y = event.getY(0) - event.getY(1);
	return FloatMath.sqrt(x * x + y * y);
	}

	//求两点间中点
	private void midPoint(PointF point, MotionEvent event) {
	float x = event.getX(0) + event.getX(1);
	float y = event.getY(0) + event.getY(1);
	point.set(x / 2, y / 2);
	}
	
	/**
	 * 從指定路徑讀取圖片資源
	 */
	public Bitmap getImgSource(String pathString) {
		
		Bitmap bitmap = null;
		BitmapFactory.Options opts = new BitmapFactory.Options();
//		opts.inSampleSize = 2;
		
		try {
			File file = new File(pathString);
			if (file.exists()) {
				bitmap = BitmapFactory.decodeFile(pathString, opts);
			}
			if (bitmap == null) {
				return null;
			} else {
				return bitmap;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
