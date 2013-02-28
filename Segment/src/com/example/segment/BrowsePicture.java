package com.example.segment;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.opencv.*;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import com.example.utils.AStroke;
import com.example.utils.Gra;

@SuppressLint({ "NewApi", "NewApi" })
public class BrowsePicture extends Activity {

    //YOU CAN EDIT THIS TO WHATEVER YOU WANT
    private static final int SELECT_PICTURE = 1;

    private String selectedImagePath;
    //ADDED
    private String filemanagerstring;
    DrawView drawView= null;// to draw
    Path path = null;
    Gra gra = null;
    int cl = 0;//color
    public int width = 0, height = 0;
    public int penType = -1;
    public ArrayList<Point> plist = null; 
    AStroke stroke_info = null;
    int stroke_size_fg;
    int stroke_size_bg;
    int stroke_size_hb;
    int stroke_size_sb;
	ControlCenter control;
	Mat seedsImage = null;
	int drawStarted = 0;//drawing started to draw the image
	int newProcess = 0;// start a new image
	ProgressDialog progressBar;
	private int progressBarStatus = 0;
	private long progress = 0;
	private Handler progressBarHandler = new Handler();
	int screenW = 0, screenH = 0;
	String fname = "Image";
	String displayText = " Welcome to Segmentation " +
							"			";
   static {
	    if (!OpenCVLoader.initDebug()) {
	        Log.d("Opencv Load","cannot load");
	    }
	}

    @Override 
    public boolean onTouchEvent(MotionEvent event){
        if(event.getAction() == MotionEvent.ACTION_DOWN){
        	//Log.d("Mouse Action", "Down");
        	Log.d("ScreenW", screenW+"");
            path = new Path();
            Paint paint = drawView.paint;
            gra = new Gra(path,cl);
        	width = drawView.width;
        	height = drawView.height;
        	if (drawStarted==0)seedsImage = new Mat (height,width,CvType.CV_8UC4,new Scalar(4));
        	drawStarted = 1;
        	
            path.moveTo((float) (event.getX()-3*screenW/14-10-10), event.getY() - 80);
            path.lineTo((float) (event.getX()-3*screenW/14-10-10), event.getY() - 80);
          }
        else if(event.getAction() == MotionEvent.ACTION_MOVE){
        	if(penType == 4){
        		float tempX = event.getX()-3*screenW/14-10;
        		float tempY = event.getY() - 80;
        		if(drawView.graphics.size()>0){
        			for(int i = 0;i<drawView.graphics.size();i++)
        			{	
        				int j = 0;
        				while(j<drawView.graphics.get(i).x.size()&&drawView.graphics.get(i).x.get(j)!=null&&drawView.graphics.get(i).y.get(j)!=null){
        				if(drawView.graphics.get(i).x.get(j)>tempX-100
        				 &&drawView.graphics.get(i).x.get(j)<tempX+100
        				 &&drawView.graphics.get(i).y.get(j)>tempY-100
        				 &&drawView.graphics.get(i).y.get(j)<tempY+100)
        					{	
        					drawView.graphics.remove(i);
        					break;
        						}
        				j++;	
        				}
        			}
        		}
        	}
        	else{
	            path.lineTo((float) (event.getX()-3*screenW/14-10), event.getY() - 80);
	            gra.add((float) (event.getX()-3*screenW/14-10),event.getY() - 80) ;
	            
	            drawView.add(gra);
	            Point p1 = new Point();
	            p1.x = event.getX()-(3/14)*screenW;
	            p1.y = event.getY() - 80;
	            plist.add(p1);
        	}
          }
        else if(event.getAction() == MotionEvent.ACTION_UP){
        	  //Log.d("Mouse Action", "Up");
            path.lineTo((float) (event.getX()-3*screenW/14-10), event.getY() - 80);
            
            if(penType==5){
                //control.m_boundingBox.width=point.x-control.m_boundingBox.x;
                //control.m_boundingBox.height=point.y-control.m_boundingBox.y;
            }
            else if(penType>=0){

                //this->isLeftDown=0;
                stroke_info = new AStroke(plist);
                if(penType==0){
                    //stroke_info.strokeSize=stroke_size_fg;
                    control.m_foregroundStrokeList.add(stroke_info);
                    control.m_bForegroundStrokeUpdated = true; // wenxian
                }
                else if(penType==1){
                    //stroke_info.strokeSize=stroke_size_bg;
                	//Log.d("Action UP", "seting background");
                    control.m_backgroundStrokeList.add(stroke_info);
                    control.m_bBackgroundStrokeUpdated = true; // wenxian
                }
                else if(penType==2){
                    //stroke_info.strokeSize=stroke_size_hb;
                    control.m_hardBoundaryStrokeList.add(stroke_info);
                    control.m_bHardBoundaryStrokeUpdated = true; // wenxian
                }
                else if(penType==3){
                    //stroke_info.strokeSize=stroke_size_sb;
                    control.m_softBoundaryStrokeList.add(stroke_info);
                    control.m_bSoftBoundaryStrokeUpdated = true; // wenxian
                }
                else{
                    //stroke_info.strokeSize=stroke_size_fg;
                    //control.m_eraserStrokeList.add(stroke_info);
                    control.m_bEraserStrokeUpdated = true; // wenxian

                }
                createSeeds();
                plist.clear();
            }
            
          }
        Log.d("Size",drawView.graphics.size()+"");
    	drawView.invalidate();
    	return false;
    	
    }



    public void onCreate(Bundle savedInstanceState) {
    	
    	super.onCreate(savedInstanceState);
    	
        setContentView(R.layout.activity_main);
        View relativeLayout = findViewById(R.id.main);

        
        TextView  tv = new TextView(this);
        plist = new ArrayList<Point>(200);
        stroke_info = new AStroke();
        Display display = getWindowManager().getDefaultDisplay();
        screenW = display.getWidth();
        screenH = display.getHeight();


        //seedsImage = new Mat (width,height,CvType.CV_8UC4,new Scalar(4));
        //tv.setText( jni.fuck() );
       // Log.d("JNI CPP",String.format("%2d",jni.test()));
        
        tv.setId(10);
        //tv.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
        RelativeLayout.LayoutParams text_relativeParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        text_relativeParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        text_relativeParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
       // ((RelativeLayout) relativeLayout).addView(tv);
       // this.addContentView(tv, text_relativeParams);
  
        drawView = (DrawView) findViewById(R.id.DrawView1);
        drawView.screenH = screenH;
        drawView.screenW = screenW;
        control = new ControlCenter();
        final TextView statusText = (TextView)findViewById(R.id.statusTex);
        //Log.d("To test","drawview created");
        tv.setText( displayText);
        
        ((Button) findViewById(R.id.open))
        .setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {

                // in onCreate or any event where your want the user to
                // select a file
            	
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        "Select Picture"), SELECT_PICTURE);
                newProcess = 1;
 
            }
        });
        ((Button) findViewById(R.id.segment))
        .setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
            	statusText.setText("Segmenting...");
            	progressBarHandler.postDelayed(new Runnable() {
            		public void run() {
            			drawView.setBackgroundColor(Color.WHITE);
                        control.Segment();
                        statusText.setText("Done...");
                        drawView.setBitMap(control.segmentedImage);
                        drawView.invalidate();
                        drawStarted = 0;
            		}
            	}, 2000);
                
                
    			// prepare for a progress bar dialog
                /*		progressBar = new ProgressDialog(arg0.getContext());
    			progressBar.setCancelable(true);
    			progressBar.setMessage("File downloading ...");
    			progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    			progressBar.setProgress(0);
    			progressBar.setMax(100);
    			progressBar.show();
     
    			//reset progress bar status
    			progressBarStatus = 0;
     
    			//reset filesize
    			progress = 0;
     
    			new Thread(new Runnable() {
    			  public void run() {
    				if (progressBarStatus < 100) {
     
    				  // process some tasks
    				  //progressBarStatus = doSomeTasks();
     
    				  // your computer is too fast, sleep 1 second
    				  //try {
    					//Thread.sleep(200);
    				  //} catch (InterruptedException e) {
    				//	e.printStackTrace();
    				  //}
     
    				  // Update the progress bar
    				  progressBarHandler.post(new Runnable() {
    					public void run() {
    					  progressBar.setProgress(progressBarStatus);
    					}
    				  });
    				  
    				  progressBarStatus = 100;
    				}
     
    				// ok, file is downloaded,
    				if (progressBarStatus >= 100) {
     
    					// sleep 2 seconds, so that you can see the 100%
    					//try {
    						//Thread.sleep(2000);
    					//} catch (InterruptedException e) {
    						//e.printStackTrace();
    					//}
     
    					// close the progress bar dialog
    					progressBar.dismiss();
    				}
    			  }
    		       }).start();*/
     
    	           }
            
        });
        
        ((Button) findViewById(R.id.background))
        .setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
            	//drawView.paint.setColor(Color.RED);
            	statusText.setText("Background...");
            	cl = Color.BLUE;
            	penType = 1;
            	drawStarted = 0;

            }
        });
        ((Button) findViewById(R.id.foreground))
        .setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
            	//drawView.paint.setColor(Color.BLUE);
            	statusText.setText("Foreground...");
            	cl = Color.RED;
            	penType = 0;

            }
        });
        ((Button) findViewById(R.id.image))
        .setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
            	//drawView.paint.setColor(Color.BLUE);
            	
            	drawView.setBitMap(control.image);
            	drawView.invalidate();

            }
        });
        ((Button) findViewById(R.id.seeds))
        .setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                
                viewSeeds();
                drawStarted = 0;
            }
        });
        ((Button) findViewById(R.id.save))
        .setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
            	try{
            	control.saveImage(fname);
            	}
            	catch(IOException e){
            		Log.d("IOEX",e.getMessage());
            	}
 
            }
        });
        
        ((Button) findViewById(R.id.eraser))
        .setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
            	//drawView.paint.setColor(Color.RED);
            	statusText.setText("Eraser...");
            	cl = Color.TRANSPARENT;
            	penType = 4;
            	drawStarted = 0;

            }
        });
        
    }
    public void createSeeds(){
    	
    	if((height+width) > 0) {
    		

    	    control.DumpStrokePointsListToImage2(seedsImage);
    	    control.image_seeds = seedsImage;
    	    control.image = Bitmap.createBitmap(drawView.bitmap);
    	   // Log.d("Iheight",String.format("%10d",control.image.getHeight()));

    	    /*for(int i=0;i<height;i++){
    	        for(int j=0;j<width;j++){
    	            //COLORREF c=RGB(CV_IMAGE_ELEM(seeds,uchar, i, j*3+2),CV_IMAGE_ELEM(seeds,uchar, i, j*3+1),CV_IMAGE_ELEM(seeds,uchar, i, j*3));
    	        	byte buff[] = new byte[(int) (seeds.total() * seeds.channels())];
    	        	seeds.get(width,height,buff);
    	        	Log.d(String.format("%d",i),String.format("%d",j));
    	            //if( c!=RGB(0,0,0 )){
    	              //      seedsImage.setPixel(j,i, c);

    	                //                }
    	        	seedsImage.put(width, height, buff);
    	                               }
    	        }*/
    	    
    	    }
    	    
    	}
    public void viewSeeds(){
    	//createSeeds();
    	//Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    	Bitmap bmp = null;
    	Mat tmp = new Mat (height,width,CvType.CV_8UC4,new Scalar(4));
    	try {
        	Point p1 = new Point(1,1), p2 = new Point(100,100);
        	Core.line(seedsImage, p1, p2, new Scalar(0,0,255), 3, 8, 0);
        	//Core.line(seedsImage,  new Point(1,100),  new Point(100,1), new Scalar(255,0,0), 3, 8, 0);
        	Imgproc.cvtColor(seedsImage, tmp, Imgproc.COLOR_RGB2RGBA,4);
      		//Log.d("Channel",String.format("%d",seedsImage.channels()));

    	bmp = Bitmap.createBitmap(tmp.cols(), tmp.rows(), Bitmap.Config.ARGB_8888);
    	//Log.d("tmp.cols()",String.format("%d", tmp.size()));
    	Utils.matToBitmap(tmp, bmp);
    	//Utils.matToBitmap(seedsImage, bmp);
    	}
  
    	catch (CvException e){Log.d("Exception",e.getMessage());}
    	drawView.setBitMap(bmp);
    	drawView.invalidate();
    }
    public void reset(){
    	drawStarted = 0;
    	
    }

    //UPDATED
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
        	//Log.d("K","Result OK  set");
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();

                //OI FILE Manager
                filemanagerstring = selectedImageUri.getPath();

                //MEDIA GALLERY
                selectedImagePath = getPath(selectedImageUri);

                //DEBUG PURPOSE - you can delete this if you want
                if(selectedImagePath!=null){
                	int i = selectedImagePath.lastIndexOf('/');
                	fname = new String(selectedImagePath.substring(i));
                	//Log.d("fname",fname);
                    drawView.setPathImg(fname);
                    drawView.invalidate();

                	//Log.d("height",String.format("%10d",height));
                }
                else System.out.println("selectedImagePath is null");
                if(filemanagerstring!=null)
                		{
                    System.out.println(filemanagerstring);
                    //Log.d("full filemanager string", filemanagerstring);

                					}
                else System.out.println("filemanagerstring is null");

                //NOW WE HAVE OUR WANTED STRING
                if(selectedImagePath!=null)
                    System.out.println("selectedImagePath is the right one for you!");
                else
                    System.out.println("filemanagerstring is the right one for you!");
            }
        }
        else {
        	Log.d("K","Result OK not set");
        }
    }

    //UPDATED!
    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        @SuppressWarnings("deprecation")
		Cursor cursor = managedQuery(uri, projection, null, null, null);
        if(cursor!=null)
        {
            //HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            //THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
            .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        else return null;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            //case R.id.new_game:
                //newGame();
                //return true;
            //case R.id.help:
              //  showHelp();
                //return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

  
}