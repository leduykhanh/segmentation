package com.example.segment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Environment;
import android.util.Log;

import com.example.utils.AStroke;

public class ControlCenter {
	public ArrayList<AStroke> m_foregroundStrokeList, m_backgroundStrokeList, m_hardBoundaryStrokeList,
	m_softBoundaryStrokeList, m_eraserStrokeList;
    boolean m_bForegroundStrokeUpdated;
    boolean m_bBackgroundStrokeUpdated;
    boolean m_bHardBoundaryStrokeUpdated;
    boolean m_bSoftBoundaryStrokeUpdated;
    boolean m_bEraserStrokeUpdated;
    boolean m_bForegroundStrokeLoaded;
    boolean m_bBackgroundStrokeLoaded;
    boolean m_bHardBoundaryStrokeLoaded;
    boolean m_bSoftBoundaryStrokeLoaded;
    Mat image_seeds;
    public Bitmap image,segmentedImage;
    public grabCut segmentor;
    public final int BLACK = Color.rgb(0,0,0);
    public final int WHITE = Color.rgb(255,255,255);
    public final int YELLOW = Color.rgb(221,255,86);
    public final int RED = Color.rgb(247,3,76);
    public final int BLUE = Color.rgb(3,11,247);
    public final int GREEN = Color.rgb(11,247,3);
    enum StrokeType{
    	  STROKE_FG , // foreground stroke
    	  STROKE_BG , // background stroke
    	  STROKE_HB , // hard boundary stroke
    	  STROKE_SB , // soft boundary stroke
    	  STROKE_ER , // soft boundary stroke

    	  STROKE_BB , // for drawing bounding box
    	  STROKE_INVALID 
    	};

    public ControlCenter(){

    	m_bForegroundStrokeUpdated = false;
    	m_bBackgroundStrokeUpdated = false;
    	m_bHardBoundaryStrokeUpdated = false;
    	m_bSoftBoundaryStrokeUpdated = false;

    	m_bForegroundStrokeLoaded = false;
    	m_bBackgroundStrokeLoaded = false;
    	m_bHardBoundaryStrokeLoaded = false;
    	m_bSoftBoundaryStrokeLoaded = false;
    	image_seeds= null;
    	image = null;
    	m_foregroundStrokeList = new ArrayList<AStroke>(100);
    	m_backgroundStrokeList= new ArrayList<AStroke>(100);
    	m_hardBoundaryStrokeList= new ArrayList<AStroke>(100);
    	m_softBoundaryStrokeList= new ArrayList<AStroke>(100);
    	m_eraserStrokeList = new ArrayList<AStroke>(100);

    }
    //void SetParam( Parameters *param ){ memcpy( &m_param, param, sizeof(Parameters) ); }
    //void SetStrokeType(StrokeType stroke);
    boolean DumpStrokePointsListToImagePerList(ArrayList<AStroke> strokeList, Mat pimage, StrokeType type)
    {
    	//Log.d("Type",String.format("%s", type));
    	//Log.d("strokeList.size()",String.format("%d", strokeList.size()));
    	
    	if(strokeList.size() == 0)
    		return false;
    	
    	ArrayList<Point> stroke = new ArrayList<Point>(100);
    	int k;

    	Scalar pencolor;
    	
    	int thickness;

    	switch(type)
    	{
    	case STROKE_FG: pencolor = new Scalar(255,0,0); // CV_RGB(255,0,0);
    		    		break;
    	case STROKE_BG: pencolor = new Scalar(0,0,255); // CV_RGB(0,0,255);
    		    		break;
    	case STROKE_HB: pencolor =new Scalar(255,255,0); // CV_RGB(255,255,0); 
    		break;
    	case STROKE_SB: pencolor = new Scalar(0,255,0); // CV_RGB(0,255,0); 
    		break;
    	case STROKE_ER: pencolor = new Scalar(0,0,0); // CV_RGB(0,0,0); 
    		break;
    	default: return false;
    	}

    	for(AStroke iter_stroke : strokeList)
    	{
    		Point p1 = new Point(), p2 = new Point();

    		stroke = iter_stroke.StrokePoints;
    		//Log.d("ControlCenter",stroke.size()+"");
    		thickness = iter_stroke.strokeSize;

    		p1.x = (int)(Math.round((stroke.get(0)).x));
    		p1.y = (int)(Math.round(((stroke.get(0))).y));
    		
    		for(Point iter_point : stroke)
    		{
    			p2.x = (int)(Math.round((iter_point).x));
    			p2.y = (int)(Math.round((iter_point).y));
    			//Log.d("p1.x",String.format("%f",p1.x));
    			//Log.d("p2.x",String.format("%f",p2.x));
    			if(p2.x>=0 && p2.x<pimage.width() || p2.y>=0 && p2.y<pimage.height())
    			{
    				if( p1.x != p2.x || p1.y != p2.y )
    				{
    					Core.line(pimage, p1, p2, pencolor, thickness, 8, 0);
    					
    					//Log.d("Lining",String.format("%s", type));
    					switch(type){
    					case STROKE_FG:
    						pimage.put((int) p1.y, (int)p1.x,1.0,1.0,5.0,5.0 );
    						break;
    					case STROKE_BG:
    						pimage.put((int) p1.y, (int)p1.x,5.0,5.0,1.0,1.0 );
    						break;
    					//CV_IMAGE_ELEM( pimage, uchar, pt.y, pt.x*3) = b;
    					//CV_IMAGE_ELEM( pimage, uchar, pt.y, pt.x*3+1) = g;
    					//CV_IMAGE_ELEM( pimage, uchar, pt.y, pt.x*3+2) = r;
    					}
    				}
    				p1.x = p2.x;
    				p1.y = p2.y;
    			}
    		}
    	}

    	return true;
    }

    public void DumpStrokePointsListToImage2(Mat pimage)
    {

    	if( m_foregroundStrokeList.size()!=0)
    		DumpStrokePointsListToImagePerList(m_foregroundStrokeList, pimage, StrokeType.STROKE_FG);

    	if(m_backgroundStrokeList.size()!=0)
    		DumpStrokePointsListToImagePerList(m_backgroundStrokeList, pimage, StrokeType.STROKE_BG);

    	if(m_hardBoundaryStrokeList.size()!=0)
    		DumpStrokePointsListToImagePerList(m_hardBoundaryStrokeList, pimage, StrokeType.STROKE_HB);

    	if(m_softBoundaryStrokeList.size()!=0)
    		DumpStrokePointsListToImagePerList(m_softBoundaryStrokeList, pimage, StrokeType.STROKE_SB);
    	
    	if(m_eraserStrokeList.size()!=0)
    		DumpStrokePointsListToImagePerList(m_eraserStrokeList, pimage, StrokeType.STROKE_ER);

    	return;
    }
    public double Segment(){
    	if(image!=null){
    		segmentedImage = Bitmap.createBitmap(image.getWidth(),image.getHeight(),Bitmap.Config.ARGB_8888);
    		segmentor = new grabCut();
    		Mat tmp = new Mat (image.getHeight(),image.getWidth(),CvType.CV_8UC4,new Scalar(4));
    		tmp = segmentor.Segment(image,image_seeds);
    		tmp = segmentor.blob(tmp,m_foregroundStrokeList);
    		for(int i =0;i < image.getWidth();i++)
    			for (int j = 0; j < image.getHeight();j++){
    				int tempc = image.getPixel(i, j);
    				
    				int tempc1 = Color.rgb(255-Color.red(tempc), Color.green(tempc), Color.blue(tempc));
    				double[] p = tmp.get(j, i);
    				//Log.e("KKKKK",(int)p[1]+"");
    				/*
    				switch((int)p[1]){
    				case 1: segmentedImage.setPixel(i,j, BLACK);
    				break;
    				case 2: segmentedImage.setPixel(i,j, WHITE);
    				break;
    				case 3: segmentedImage.setPixel(i,j, YELLOW);
    				break;
    				case 4: segmentedImage.setPixel(i,j, RED);
    				break;
    				case 5: segmentedImage.setPixel(i,j, BLUE);
    				break;
    				case 0: segmentedImage.setPixel(i,j, GREEN);
    				break;
    				 default: segmentedImage.setPixel(i,j, tempc);
    				}
    				*/
    				int test = -1;
    				for (int t=0;t<1000;t++){
    					if((int)p[1] == segmentor.listIndex[t]) {test =1;break;}
    					
    				}
    				if(test == 1)  segmentedImage.setPixel(i,j, tempc); 
    				else segmentedImage.setPixel(i,j, GREEN);
    				//if((int)p[1]==segmentor.goldenIndex) segmentedImage.setPixel(i,j, tempc);
    				//else segmentedImage.setPixel(i,j, GREEN);
    				
    				//int a = (int)(p[0]*255);
    				//segmentedImage.setPixel(i,j,Color.rgb(a,a,a));
    				}				
    		Log.e("GOLDENINDEX",segmentor.goldenIndex+"");	
    			}
    		
    	
    	return 1;
    	};
    public void blobDetect(Bitmap image){
    	
    	
    }
    public void saveImage(String name) throws IOException{
    	ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    	segmentedImage.compress(Bitmap.CompressFormat.JPEG, 40, bytes);

    	//you can create a new file name "test.jpg" in sdcard folder.
    	File f = new File(Environment.getExternalStorageDirectory() + File.separator + "result"+name);
    	f.createNewFile();
    	//write the bytes in file
    	FileOutputStream fo = new FileOutputStream(f);
    	fo.write(bytes.toByteArray());

    	// remember close de FileOutput
    	fo.close();
    }
    public void viewImage(){};
    public void viewSeeds(){};
    public void viewAlpha(){};
    public void viewForeground(){};
    public void viewBackground(){};
    public void viewSwitchDrawStrokes(){};

}
