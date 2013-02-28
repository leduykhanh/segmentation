package com.example.segment;

import java.io.File;
import java.util.ArrayList;

import com.example.utils.Gra;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;



public class DrawView extends View {
    Paint paint = new Paint();
    public ArrayList<Gra> graphics = new ArrayList<Gra>();
   
    @SuppressLint("SdCardPath")
	public String imgSD = "/storage/sdcard0/ex.jpg";
    String root = Environment.getExternalStorageDirectory().toString();
    //public String imgSD = "/data/data/com.example.segment/files/sojisub.jpg";
    //public JNILoad jni = null;
    public Bitmap bmp = null;
    public int typeDraw = 0;
    public int strokeDraw = 0;
    public int width,height;
    public int screenW = 0, screenH = 0;
    Bitmap  bitmap = null;//BitmapFactory.decodeFile(imgSD);
    //Constructor
    public DrawView(Context context,AttributeSet att) {
    	
        super(context,att);
        width = 0;
        height = 0;
        //jni = new JNILoad();
        
        paint.setColor(Color.BLACK);
        paint.setDither(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(5);
    }
    public void add(Gra gra) {
    	graphics.add(gra);
    }
    public void setBitMap(Bitmap bmp){
    	this.bmp = bmp;
    	typeDraw = 1;
    	strokeDraw = 1;
    	
    }
    @Override
    public void onDraw(Canvas canvas) {
    	
    	 Resources res = getResources();
    	 //if (typeDraw == 0)
    	  //bitmap = BitmapFactory.decodeResource(res, R.drawable.ex);
    	 //else
    	 
    	 try{
    		 
    		 //String image = root+"/ex.jpg";
    		 
    		 
    		 Bitmap tempBitmap = BitmapFactory.decodeFile(imgSD);
    		 
    		 if(tempBitmap!=null){
    		 int tempW =  tempBitmap.getWidth();
    		 //width = 11*screenW/14;
    		 width = 500;
        	 height = tempBitmap.getHeight()*width/tempW;
        	 bitmap = Bitmap.createScaledBitmap(tempBitmap, width, height, false);
        	 //Log.d("Dheight",String.format("%10d",height));
    		 }
    		 else bitmap = BitmapFactory.decodeFile(imgSD);
    	 
    	 }
    	 catch (Exception e){
    		 
    		 Log.d("Exception",e.toString());
    	 }
    	 

    	 
    	 switch (typeDraw){
    	 case 0:
    		 canvas.drawBitmap(bitmap, 0, 0, new Paint());
    		 //Log.d("Drawing image","did");
    		 break;
    	 case 1:
    		 canvas.drawBitmap(bmp, 0, 0, new Paint());
    		 //Log.d("DRAWING",bmp.toString());
    		 //Log.d("Drawing Seeds","did");
    		 break;
    	default: canvas.drawBitmap(bitmap, 10, 10, new Paint());
    	 }
    	 
    	 
    	// Log.d("To test","draw bitmap");
    	 if (strokeDraw == 0)
            for(Gra gra : graphics){

            	paint.setColor(gra.color);
            	canvas.drawPath(gra.path, paint);
            }
    }
    public void setPathImg(String p){
    	//imgSD = new String(p.substring(0));
    	
    	imgSD = root + p;
    	//Log.d("Image Path",imgSD);

    	
    }

}