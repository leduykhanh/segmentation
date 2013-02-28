package com.example.utils;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import android.graphics.Color;
import android.util.Log;

public class GaussianFitter {

	  int [] s;			// sum of r,g, and b// Color
	//int s;
	  float[][]  p;		// matrix of products (i.e. r*r, r*g, r*b), some values are duplicated.

	  int count;	// count of color samples added to the gaussian
	  public GaussianFitter(){
		  p = new float[3][3];
		  s = new int[3];
		//  s = 0;
		  p[0][0] = 0; p[0][1] = 0; p[0][2] = 0;
		  p[1][0] = 0; p[1][1] = 0; p[1][2] = 0;
		  p[2][0] = 0; p[2][1] = 0; p[2][2] = 0;
		  count = 0;
	  }
	  public void add(int c)
	  {
		int temp = Color.rgb(Color.red(c),Color.green(c),Color.blue(c));
		//int tempR = Color.red(Colo)
	    s[0] += Color.red(c);
	    s[1] += Color.green(c);
	    s[2] += Color.blue(c);
		//s+=temp;
	    p[0][0] += Color.red(c)*Color.red(c); p[0][1] += Color.red(c)*Color.green(c); p[0][2] += Color.red(c)*Color.blue(c);
	    p[1][0] += Color.green(c)*Color.red(c); p[1][1] += Color.green(c)*Color.green(c); p[1][2] += Color.green(c)*Color.blue(c);
	    p[2][0] += Color.blue(c)*Color.red(c); p[2][1] += Color.blue(c)*Color.green(c); p[2][2] += Color.blue(c)*Color.blue(c);

	    count++;
	    
	  }
	  public void finalize(Gaussian g,  int totalCount, boolean computeEigens) 
	  {
	    // Running into a singular covariance matrix is problematic. So we'll add a small epsilon
	    // value to the diagonal elements to ensure a positive definite covariance matrix.
	    final float Epsilon = (float)0.0001;

	    if (count==0)
	      {
	        g.pi = 0;
	      }
	    else
	      {
	        // Compute mean of gaussian
	       g.mu = Color.rgb(s[0]/count,s[1]/count,s[2]/count);
	    	//g.mu = s/count;
	        // Compute covariance matrix
	        g.covariance[0][0] = p[0][0]/count-Color.red(g.mu)*Color.red(g.mu) + Epsilon; 
	        g.covariance[0][1] = p[0][1]/count-Color.red(g.mu)*Color.green(g.mu); 
	        g.covariance[0][2] = p[0][2]/count-Color.red(g.mu)*Color.blue(g.mu);
	        g.covariance[1][0] = p[1][0]/count-Color.green(g.mu)*Color.red(g.mu); 
	        g.covariance[1][1] = p[1][1]/count-Color.green(g.mu)*Color.green(g.mu) + Epsilon; 
	        g.covariance[1][2] = p[1][2]/count-Color.green(g.mu)*Color.blue(g.mu);
	        g.covariance[2][0] = p[2][0]/count-Color.blue(g.mu)*Color.red(g.mu); 
	        g.covariance[2][1] = p[2][1]/count-Color.blue(g.mu)*Color.green(g.mu); 
	        g.covariance[2][2] = p[2][2]/count-Color.blue(g.mu)*Color.blue(g.mu) + Epsilon;
	        
	        // Compute determinant of covariance matrix
	        g.determinant = g.covariance[0][0]*(g.covariance[1][1]*g.covariance[2][2]-g.covariance[1][2]*g.covariance[2][1]) 
	  	- g.covariance[0][1]*(g.covariance[1][0]*g.covariance[2][2]-g.covariance[1][2]*g.covariance[2][0]) 
	  	+ g.covariance[0][2]*(g.covariance[1][0]*g.covariance[2][1]-g.covariance[1][1]*g.covariance[2][0]);

	        // Compute inverse (cofactor matrix divided by determinant)
	        g.inverse[0][0] =  (g.covariance[1][1]*g.covariance[2][2] - g.covariance[1][2]*g.covariance[2][1]) / g.determinant;
	        g.inverse[1][0] = -(g.covariance[1][0]*g.covariance[2][2] - g.covariance[1][2]*g.covariance[2][0]) / g.determinant;
	        g.inverse[2][0] =  (g.covariance[1][0]*g.covariance[2][1] - g.covariance[1][1]*g.covariance[2][0]) / g.determinant;
	        g.inverse[0][1] = -(g.covariance[0][1]*g.covariance[2][2] - g.covariance[0][2]*g.covariance[2][1]) / g.determinant;
	        g.inverse[1][1] =  (g.covariance[0][0]*g.covariance[2][2] - g.covariance[0][2]*g.covariance[2][0]) / g.determinant;
	        g.inverse[2][1] = -(g.covariance[0][0]*g.covariance[2][1] - g.covariance[0][1]*g.covariance[2][0]) / g.determinant;
	        g.inverse[0][2] =  (g.covariance[0][1]*g.covariance[1][2] - g.covariance[0][2]*g.covariance[1][1]) / g.determinant;
	        g.inverse[1][2] = -(g.covariance[0][0]*g.covariance[1][2] - g.covariance[0][2]*g.covariance[1][0]) / g.determinant;
	        g.inverse[2][2] =  (g.covariance[0][0]*g.covariance[1][1] - g.covariance[0][1]*g.covariance[1][0]) / g.determinant;     



	        // The weight of the gaussian is the fraction of the number of pixels in this Gaussian to the number of 
	        // pixels in all the gaussians of this GMM.
	        g.pi = (float)count/totalCount;

	        if (computeEigens)
	  	{
	  	  // Build OpenCV wrappers around our data.
	        	/*
	  	  Mat mat = new Mat(3, 3, CvType.CV_32FC1);
	  	  for (int i = 0;i < 3;i++)
	  		  for (int j = 0; j<3; j++) mat.put(i, j, g.covariance[i][j]);
	  	  Mat eval = new Mat(3, 1, CvType.CV_32FC1);
	  	  for (int i = 0;i < 3;i++)
	  		   eval.put(i, 0, g.eigenvalues[i]);	  	  
	  	  Mat evec = new Mat(3, 3, CvType.CV_32FC1);
	  	  for (int i = 0;i < 3;i++)
	  		  for (int j = 0; j<3; j++) evec.put(i, j, g.eigenvectors[i][j]);
	  		  */	  			
	  	  // Compute eigenvalues and vectors using SVD
	  	 // cvSVD( &mat, &eval, &evec );
	  	//Calib3d.decomposeProjectionMatrix(mat,eval,evec,mat);
	  	}
	      }
	  }
}
