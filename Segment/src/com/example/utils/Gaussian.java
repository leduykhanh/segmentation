package com.example.utils;

import android.graphics.Color;

public class Gaussian {
		public int mu;
		public float[][] covariance;
		public float determinant;
		public float[][] inverse;
		public float pi;
		public float[] eigenvalues;
		public float[][] eigenvectors;
	public Gaussian(){
	    mu = 0;					// mean of the gaussian
	    covariance = new float[3][3];		// covariance matrix of the gaussian
	    determinant = 0;			// determinant of the covariance matrix
	    inverse = new float[3][3];		// inverse of the covariance matrix
	    pi = 0;					// weighting of this gaussian in the GMM.

	  // These are only needed during Orchard and Bouman clustering.
	    eigenvalues = new float[3];		// eigenvalues of covariance matrix
	    eigenvectors = new float[3][3];	// eigenvectors of   "
	  
}
}