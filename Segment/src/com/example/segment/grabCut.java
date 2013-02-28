package com.example.segment;

import java.util.ArrayList;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import com.example.utils.AStroke;
import com.example.utils.Graph;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

public class grabCut {
	public final int MASK_BG = 0;
	public final int MASK_FG = 255;
	Mat m_hardSegmentation = null;
	int[][] gmm_hardseg = null;
	NLinks[][] m_NLinks;
	Graph mGraph;
	int lambda = 50;
	double m_beta = 1;
	public int goldenIndex = -1;
	public int [] listIndex = new int [1000];
	Mat Gseeds = null;
	public grabCut(){
	 
		
	}
	public double CalcGMMProbability(Bitmap pimage, Mat pseeds, Mat pGMMProbability,GMM backgroundGMM, GMM foregroundGMM)
	{
		//Log.d("pseedsH",pseeds.height()+"");
		//Log.d("pseedsW",pseeds.width()+"");
		Gseeds = new Mat(pseeds.height(),pseeds.width(),CvType.CV_32FC1,new Scalar(4));
		int[][] gmm_component = new int[pseeds.width()][pseeds.height()];
		gmm_hardseg= new int[pseeds.width()][pseeds.height()];
		//Log.d("GCCompnt",gmm_component.length +"");
		int[][] m_hardseg = new int[pseeds.width()][pseeds.height()];
		String label;
		double back, fore;
		int c;//Color
		double forepmax = 0, forepmin=1000, backpmax=0, backpmin=1000; 
		double maxvalx, minvalx, maxvaly, minvaly;
		double ftemp;
		double kldistance;
		int count;
		Mat probBG, probFG;
		for( int i = 0; i < pseeds.height(); i++ )
		{
			for( int j = 0; j < pseeds.width(); j++ )
			{
					
				double[] d = pseeds.get(i, j);
				if(d[0]==5.0)
				{
					//m_trimap->set( j, i, TrimapBG ); 
					gmm_hardseg[j][i]= MASK_BG ;
					Gseeds.put(i, j,5.0);
					//Log.d("Matd",d[0]+"");

				}
				//else if( CV_IMAGE_ELEM(m_seeds, uchar, i, j) == STROKE_FG )
				else
				{
					//m_trimap->set( j, i, TrimapFG ); 
					gmm_hardseg[j][i]= MASK_FG ;
					Gseeds.put(i, j,1.0);
				}
			}
	    }
		
		for( int i = 0; i < pseeds.height(); i++ )
		{
			for( int j = 0; j < pseeds.width(); j++ )
			{
				//if( CV_IMAGE_ELEM( m_seeds, uchar, i, j ) == STROKE_BG )
				double[] d = pseeds.get(i, j);
				if(d[0]==5.0)
				{
					//m_trimap->set( j, i, TrimapBG ); 
					m_hardseg[j][i]= MASK_BG ;

				}
				//else if( CV_IMAGE_ELEM(m_seeds, uchar, i, j) == STROKE_FG )
				else
				{
					//m_trimap->set( j, i, TrimapFG ); 
					m_hardseg[j][i]= MASK_FG ;

				}
			}
	    }
		GMM.BuildGMMs(backgroundGMM, foregroundGMM, gmm_component, pimage, gmm_hardseg);
		//GMM.LearnGMMs(backgroundGMM, foregroundGMM, gmm_component, pimage, gmm_hardseg);
		RefineOnce(backgroundGMM, foregroundGMM, gmm_component, pimage, gmm_hardseg);
		probBG = new Mat (pseeds.height(),pseeds.width(),CvType.CV_32FC1,new Scalar(4));
		probFG = new Mat (pseeds.height(),pseeds.width(),CvType.CV_32FC1,new Scalar(4));
		
		for( int i = 0; i < pimage.getHeight(); i++ )
		{
			for( int j = 0; j < pimage.getWidth(); j++ )
			{
				c = pimage.getPixel(j, i);
				double btmp = backgroundGMM.p(c);
				
				if(btmp<backpmin) backpmin = btmp;
				else if(btmp>backpmax) backpmax=btmp;
				
				double ftmp = foregroundGMM.p(c);
				if(ftmp<forepmin) forepmin = ftmp;
				else if(ftmp>forepmax) forepmax=ftmp;
				probBG.put(i, j, btmp);
				probFG.put(i, j, ftmp);
			}
		}
		maxvaly = 1;
		minvaly = 1e-20;

		maxvalx = Math.max( backpmax, forepmax );
		minvalx = Math.min( backpmin, forepmin );

		kldistance = 0;
		count = 0;
		
		for(int i=0; i<pseeds.height(); i++)
		{
			for(int j=0; j<pseeds.width(); j++)
			{
				// 	  if(CV_IMAGE_ELEM(pseeds,uchar,i,j) == STROKE_FG || CV_IMAGE_ELEM(pseeds,uchar,i,j) == STROKE_BG)
				// 	    continue;

				back = probBG.get( i, j )[0];
				fore = probFG.get( i, j )[0];

				back = (float)(minvaly + (back - minvalx) * (maxvaly - minvaly) / (maxvalx - minvalx));
				fore = (float)(minvaly + (fore - minvalx) * (maxvaly - minvaly) / (maxvalx - minvalx));
				// 	  back = minvaly + (back - backpmin) * (maxvaly - minvaly) / (backpmax - backpmin);
				// 	  fore = minvaly + (fore - forepmin) * (maxvaly - minvaly) / (forepmax - forepmin);

				ftemp = fore;
				fore = -Math.log( back );
				back = -Math.log( ftemp );

				//kldistance += fabs( (back-fore) );
				kldistance += Math.abs( (back-fore)/(back+fore) );
				count++;

				 probBG.put(i, j, back);
				 probFG.put(i, j, fore);

				if(fore+back>0)
			pGMMProbability.put(i, j,  fore/(fore+back)); // minval almost 0 and maxval almost 1
			}
		}
		kldistance /= count;
		return kldistance;
	}
	public int RefineOnce(  GMM m_backgroundGMM,GMM  m_foregroundGMM, int[][] m_GMMcomponent, Bitmap m_image, int[][] m_hardSeg)
	{
		double flow = 0;

		// Steps 4 and 5: Learn new GMMs from current segmentation
		GMM.LearnGMMs( m_backgroundGMM, m_foregroundGMM, m_GMMcomponent, m_image, m_hardSeg );

		// Step 6: Run GraphCut and update segmentation
		//initGraph( m_backgroundGMM, m_foregroundGMM,m_image);
		//if( m_graph )
			//flow = m_graph->maxflow( );

		//int changed = UpdateHardSegmentation( );
		
		
		// Build debugging images
	
		//BuildImages( );
	

		//return changed;
		return 1;
	}
	public void updateHardSegmentation( Bitmap m_image){
		int x, y;
		Log.d("PROCESS","Update Hard");
		int H = m_image.getHeight();
		int W = m_image.getWidth();
		
		for(y = 0; y < H; y++)
		{
			for(x = 0; x <W; x++)
			{
				
		if (mGraph.what_segment((int)x+y*W) == Graph.termType.SOURCE)
			m_hardSegmentation.put(y, x,  MASK_FG);
		else
			m_hardSegmentation.put(y, x , MASK_BG);
			}
		}
	}
	public void initGraph(GMM m_backgroundGMM,GMM m_foregroundGMM, Bitmap mImage){
		mGraph = new Graph();
		int H = mImage.getHeight();
		int W = mImage.getWidth();
		Log.e("PROCESS","initGraph");
		m_NLinks = new NLinks[W][H];
		for (int i =0 ;i<W;i++)
			for (int j = 0;j<H;j++)
				m_NLinks[i][j] = new NLinks();
		computeNLinks(mImage);
		for(int y = 0;y<H;y++)
			for(int x=0;x<W;x++)
				{
				mGraph.add_node();
				int c = mImage.getPixel(x, y);
				double fore= -Math.log(m_backgroundGMM.p(c));
				double back = -Math.log(m_foregroundGMM.p(c));
				mGraph.set_tweights(x+y, fore, back);
				}
		// Set N-Link weights from precomputed values
		for(int y = 0;y<H;y++)
			for(int x=0;x<W;x++){
				if( x > 0 && y < H-1 )
					mGraph.add_edge(x+y*W, x-1+(y+1)*W, m_NLinks[x][y].upleft, m_NLinks[x][y].upleft);

				if( y < H-1 )
					mGraph.add_edge(x+y*W, x+(y+1)*W, m_NLinks[x][y].up, m_NLinks[x][y].up);

				if( x < W-1 && y < H-1 )
					mGraph.add_edge(x+y*W, x+1+(y+1)*W, m_NLinks[x][y].upright, m_NLinks[x][y].upright);

				if( x < W-1 )
					mGraph.add_edge(x+y*W, (x+1+y*W), m_NLinks[x][y].right, m_NLinks[x][y].right);
			}
	}
	public void computeNLinks(Bitmap m_image){
		int x, y;
		int H = m_image.getHeight();
		int W = m_image.getWidth();
		for(y = 0; y < H ; y++)
		{
			for(x = 0; x < W ; x++)
			{
				if(x > 0 && y < H-1)
					m_NLinks[x][y].upleft = computeNLink(m_image,x, y, x-1, y+1);

				if(y < H-1)
					m_NLinks[x][y].up = computeNLink(m_image,x, y, x, y+1);

				if(x < W-1 && y < H-1)
					m_NLinks[x][y].upright = computeNLink(m_image,x, y, x+1, y+1);

				if(x < W-1)
					m_NLinks[x][y].right = computeNLink(m_image,x, y, x+1, y);
			}
		}
		
	}
	public double computeNLink(Bitmap m_image,int x1,  int y1,  int x2, int y2){
		double cost = 0;
		int c1 = m_image.getPixel(x1, y1);
		int c2 = m_image.getPixel(x2, y2);
		computeBeta(m_image);
		cost = lambda * Math.exp( -m_beta * distance2(c1,c2) ) / distance(x1,y1,x2,y2);
		return cost;
	}
	void computeBeta(Bitmap m_image){
		Log.e("PROCESS","beta");
		float result;
		int x, y, edges;
		int c, c1, c2, c3, c4;

		result = 0;
		edges = 0;
		int H = m_image.getHeight();
		int W = m_image.getWidth();
		for(y = 0; y < H; y++)
		{
			for(x = 0; x < W; x++)
			{
				c = m_image.getPixel(x, y);

				if (x > 0 && y < H-1) // upleft
				{
					c1 =m_image.getPixel(x-1, y+1);
					result += distance2( c, c1 );
					edges++;
				}
				if (y < H-1) // up
				{
					c2 = m_image.getPixel( x,y+1);
					result += distance2( c, c2 );
					edges++;
				}
				if (x < W-1 && y < H-1) // upright
				{
					c3 = m_image.getPixel( x+1,y+1);
					result += distance2( c, c3 );
					edges++;
				}
				if (x < W-1) // right
				{
					c4 = m_image.getPixel(x+1,y);
					result += distance2( c, c4 );
					edges++;
				}
			}
		}

		m_beta = ( float )( 1.0/( 2*result/edges ) );
	}
	//Blob alrgorithm
	Mat blob(Mat m,ArrayList<AStroke> strokeList){
		Mat result = new Mat (m.height(),m.width(),CvType.CV_32FC3,new Scalar(4));
		int index = 1;
		/*
		ArrayList<Point>  linked[] = new ArrayList[500]; 
		for (int i=0;i<500;i++){
			linked[i] = new ArrayList<Point>(3000);
		}*/
		
		//Setle the back and fore
		for (int y=0;y<m.height();y++)
			for(int x = 0;x<m.width();x++)
				{
				double val = m.get(y, x)[0];
				if(val<0.95) result.put(y, x, 1.0,1.0,1.0);
				else result.put(y, x, 5.0,0.0,0.0); 
				}
		for (int y=0;y<m.height();y++)
			for(int x = 0;x<m.width();x++)
				{
				double val = result.get(y, x)[0];
				if (val == 1.0){
					Point p = new Point(x, y);
					double W =0,E = 0, S = 0, N = 0;

					if (y-1<0||result.get(y-1, x)[0]==5.0) N = 10000;
					else 
						{
						if (result.get(y-1, x)[0]==val) N = result.get(y-1,x)[1];}
					
					if (y-1<0 || x-1<0||result.get(y-1, x-1)[0]==5.0) S = 10000;
					else {
						if (result.get(y-1, x-1)[0]==val) S = result.get(y-1,x-1)[1];}
					if (x-1<0 ||result.get(y, x-1)[0]==5.0) W = 10000;
					else {
						if (result.get(y, x-1)[0]==val) W = result.get(y,x-1)[1];}
					if (x+1>m.width()-1||y-1<0||result.get(y-1, x+1)[0]==5.0) E = 10000;
					else {
						if (result.get(y-1, x+1)[0]==val) E = result.get(y-1,x+1)[1];}

					if(N+E+W+S==40000){
					result.put(y, x, 1.0,(double)index,1.0);
					index++;
					}
					else{
						double min = Math.min(Math.min(E,W),Math.min(S, N));
						result.put(y, x, 1.0,min,1.0);
						//if (min<500) linked[(int)min].add(p);
						/*if(goldenIndex<0){
							ArrayList<Point> stroke = new ArrayList<Point>(100);
					    	for(AStroke iter_stroke : strokeList)
					    	{
					    		stroke = iter_stroke.StrokePoints;
					    		
					    		for(Point iter_point : stroke)
					    		{	//
					    			if(x == (int)(Math.round((iter_point).x))&& y == (int)(Math.round((iter_point).y)))
					    				
					    				{goldenIndex = (int)min; 
					    				Log.e("GOIN","GOLDEN");
					    				Log.e(Math.round((iter_point).x)+"",Math.round((iter_point).y)+"");
					    				break;
					    				      }
					    				}
					    			}
								}
								*/
						if(goldenIndex<0){
						//if(true){
							if(Gseeds.get(y, x)[0] == 1.0){
															goldenIndex = (int)min;
															listIndex[index]=(int)min;
		    												Log.e("GOIN","GOLDEN");
		    											//	break;
		    												}
						}
					}

					}
				}
		/*for (int i=0;i<500;i++)
			for(Point p:linked[i]){
			int tx = (int)p.x;
			int ty = (int)p.y;
			result.put(ty, tx, 1.0,(double)i,1.0);
		}*/
		return result;
	}
	double distance( int x1, int y1, int x2, int y2 )
	{
		  return Math.sqrt( (float)((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2)) );
		}
	double distance2(int c1,int c2){
		return ((Color.red(c1)-Color.red(c2))*(Color.red(c1)-Color.red(c2))+(Color.green(c1)-Color.green(c2))*(Color.green(c1)-Color.green(c2))+(Color.blue(c1)-Color.blue(c2))*(Color.blue(c1)-Color.blue(c2)));
	}
	public Mat Segment(Bitmap image,Mat seeds){
		Mat result = new Mat (seeds.height(),seeds.width(),CvType.CV_32FC1,new Scalar(4));
		Mat m_distance = new Mat (seeds.height(),seeds.width(),CvType.CV_32FC1,new Scalar(4));
		Mat m_init = new Mat (seeds.height(),seeds.width(),CvType.CV_32FC1,new Scalar(4));
		Mat m_image_foreground = new Mat (seeds.height(),seeds.width(),CvType.CV_32FC1,new Scalar(4));
		int chns = seeds.channels();
		
		//int[]		Boundary_x = new int[5000];
		//int[]		Boundary_y = new int[5000];
		int     count=0;
	    int		maxDist=20;
		double   tempDist;
	    int    i;
	    m_hardSegmentation = new Mat (seeds.height(),seeds.width(),CvType.CV_32FC1,new Scalar(4));

		GMM foregroundGMM1 = new GMM( 5 );
		GMM backgroundGMM1 = new GMM( 5 );
		double kldistance= CalcGMMProbability( image, seeds, result,backgroundGMM1,foregroundGMM1);
		
		Log.d("distance",kldistance+"");
		//change hardeg
	    //updateHardSegmentation( image);
	/*	for( int y = 0; y < seeds.height(); y++ ){
			for( int x = 0; x < seeds.width(); x++ ){
				
				if(( m_hardSegmentation.get(y, x )[0]==MASK_FG)&&(((x-1>0)&&(y-1>0)
						&&(m_hardSegmentation.get(y-1, x-1 )[0]==MASK_BG))
						||((x-1>0)&&(m_hardSegmentation.get(y, x-1 )[0]==MASK_BG))
						||((x-1>0)&&(y+1<seeds.height())&&(m_hardSegmentation.get(y+1, x-1 )[0]==MASK_BG))
						||((y-1>0)&&(m_hardSegmentation.get(y-1, x )[0]==MASK_BG)
						||((y+1<seeds.height())&&(m_hardSegmentation.get(y+1, x )[0]==MASK_BG))
						||((x+1<seeds.width())&&(y-1>0)&&(m_hardSegmentation.get(y-1, x+1 )[0] )==MASK_BG))
						||((x+1<seeds.width())&&(m_hardSegmentation.get(y, x+1 )[0]==MASK_BG))
						||((x+1<seeds.width())&&(y+1<seeds.height())&&(m_hardSegmentation.get(y+1, x+1 )[0]==MASK_BG))))
				{
					Boundary_x[count]=x;
					Boundary_y[count]=y;
					count+=1;
				}
			}
		}
		
		for(i=0;i<count;i++){
			
			 for (int x=Boundary_x[i]-maxDist; x< Boundary_x[i]+maxDist+1; x++)
				 for (int y=Boundary_y[i]-maxDist; y< Boundary_y[i]+maxDist+1; y++)
			{
				if((x>=0)&&(x<seeds.width())&&(y>=0)&&(y<seeds.height())){
					tempDist= Math.sqrt((float)((x-Boundary_x[i])*(x-Boundary_x[i])+(y-Boundary_y[i])*(y-Boundary_y[i])));
					if(tempDist<m_distance.get(y, x)[0]){
						 m_distance.put(y, x, tempDist,1.0,1.0,1.0);
					}
				}
			}
	    }
		*/
		
		/*for( int y = 0; y <seeds.height(); y++ ){
			for( int x = 0; x < seeds.width(); x++ ){
				if(	(int)m_distance.get(y,x)[0] >=maxDist){
					m_distance.put(y, x, 1.0,1.0,1.0,1.0);;
				}
	
				else{
					double tmp = m_distance.get(y,x)[0]/maxDist;
					m_distance.put(y, x, tmp,1.0,1.0,1.0);

				}
	
				if((int)m_distance.get(y,x)[0]>=maxDist/2){
					double tmp  = 1-2*Math.pow(((maxDist-m_distance.get(y,x)[0])/maxDist),2);
					m_distance.put(y, x, tmp,1.0,1.0,1.0);
				}
				else{
					double tmp=2*Math.pow((m_distance.get(y,x)[0]/maxDist),2);
					m_distance.put(y, x, tmp,1.0,1.0,1.0);
				}
	
			}
		}
		*/
		
		return result;
		//return m_distance;
	};
}
class NLinks{
	  public double upleft;
	  public double up;
	  public double upright;
	  public double right;
}
