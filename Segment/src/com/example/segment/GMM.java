package com.example.segment;

import java.util.ArrayList;

import org.opencv.core.Mat;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import com.example.utils.Gaussian;
import com.example.utils.GaussianFitter;

public class GMM {
	Gaussian[] m_gaussians;
	
	private int m_K;		// number of gaussians
	public static final int MASK_BG = 0;
	public static final int MASK_FG = 255;
	
	public GMM(int K){
	m_K = K;
	m_gaussians = new Gaussian[m_K];
	//for(Gaussian m : m_gaussians){m = new Gaussian();}
	for(int i = 0;i<m_K;i++){m_gaussians[i] = new Gaussian();}
	}
	
	public boolean usePixel(int val, int label)
	{
	  if( val == label ) // specified
	    return true;

	  return false;
	}
	public int K() { return m_K; }
	public double p(int c)
	{
	  float result = 0;

	  if (m_gaussians.length>0)
	    {
	      for (int i=0; i < m_K; i++)
		result += m_gaussians[i].pi * p(i, c);
	    }

	  return result;
	}
	
	public double p( int i, int c)//c is a color
	{
	  float r, g, b, d, result;

	  result = 0;
	  if( m_gaussians[i].pi > 0 )
	    {
	      if (m_gaussians[i].determinant > 0)
		{
		  r = Color.red(c) - Color.red(m_gaussians[i].mu);
		  g = Color.green(c) - Color.green(m_gaussians[i].mu);
		  b = Color.blue(c) - Color.blue(m_gaussians[i].mu);
				
		  d = r * (r*m_gaussians[i].inverse[0][0] + g*m_gaussians[i].inverse[1][0] + b*m_gaussians[i].inverse[2][0]) +
		    g * (r*m_gaussians[i].inverse[0][1] + g*m_gaussians[i].inverse[1][1] + b*m_gaussians[i].inverse[2][1]) +
		    b * (r*m_gaussians[i].inverse[0][2] + g*m_gaussians[i].inverse[1][2] + b*m_gaussians[i].inverse[2][2]);

		  result = (float)(1.0/(Math.sqrt(m_gaussians[i].determinant)) * Math.exp(-0.5*d));
		} 
	    }

	  return result;
	}
	public static void BuildGMMs(GMM backgroundGMM, GMM foregroundGMM,  int[][] components, Bitmap image, int[][] hardSegmentation)
	{
	  // Step 3: Build GMMs using Orchard-Bouman clustering algorithm
	  int x, y;
	  int i, j, nBack, nFore, maxK;
	  int c;//Color

	  // Set up Gaussian Fitters
	  GaussianFitter[] backFitters = new GaussianFitter[backgroundGMM.K()];
	  for(i=0; i<backgroundGMM.K();i++) backFitters[i] = new GaussianFitter();
	  GaussianFitter[] foreFitters = new GaussianFitter[foregroundGMM.K()];
	  for(i=0; i<foregroundGMM.K();i++) foreFitters[i] = new GaussianFitter();
	  int foreCount = 0, backCount = 0;

	  // Initialize the first foreground and background clusters
	  //Log.d("imageH",image.getHeight() +"");
	  //Log.d("imageW",image.getWidth() +"");
	  for(y = 0; y < image.getHeight(); y++)
	    {
	      for(x = 0; x < image.getWidth(); x++)
		{
		  components[x][y] = 0;

		  if (hardSegmentation[x][y] == MASK_FG)
		    {
			  c = image.getPixel(x, y);
		      //c = new Color(Color.red(p),Color.green(p),Color.blue(p));
		      foreFitters[0].add( c );
		      foreCount++;
		    }
		  else if(hardSegmentation[x][y] == MASK_BG)
		    {
			  c = image.getPixel(x, y);
		      backFitters[0].add( c );
		      backCount++;
		    }
		}
	    }

	  backFitters[0].finalize(backgroundGMM.m_gaussians[0], backCount, true);
	  foreFitters[0].finalize(foregroundGMM.m_gaussians[0], foreCount, true);

	  nBack = 0;
	  nFore = 0;
	  maxK = backgroundGMM.K() > foregroundGMM.K() ? backgroundGMM.K() : foregroundGMM.K();

	  // Compute clusters
	  for(i = 1; i < maxK; i++)
	    {
	      // Reset the fitters for the splitting clusters
	      backFitters[nBack] = new GaussianFitter();
	      foreFitters[nFore] = new GaussianFitter();

	      // For brevity, get references to the splitting Gaussians
	      Gaussian bg = backgroundGMM.m_gaussians[nBack];
	      Gaussian fg = foregroundGMM.m_gaussians[nFore];

	      // Compute splitting points
	      float splitBack = bg.eigenvectors[0][0] * Color.red(bg.mu) + bg.eigenvectors[1][0] * Color.green(bg.mu) + bg.eigenvectors[2][0] * Color.blue(bg.mu);
	      float splitFore = fg.eigenvectors[0][0] * Color.red(fg.mu) + fg.eigenvectors[1][0] * Color.green(fg.mu) + fg.eigenvectors[2][0] * Color.blue(fg.mu);

	      // Split clusters nBack and nFore, place split portion into cluster i
	      for ( y = 0; y < image.getHeight(); y++)
		{
		  for( x = 0; x < image.getWidth(); x++)
		    {
			  c = image.getPixel(x, y);

		      if (i < foregroundGMM.K() && hardSegmentation[x][y] == MASK_FG && components[x][y] == nFore)
			{
			  if (fg.eigenvectors[0][0] * Color.red(c) + fg.eigenvectors[1][0] * Color.green(c) + fg.eigenvectors[2][0] * Color.blue(c) > splitFore)
			    {
			      components[x][y] = i;
			      foreFitters[i].add(c);
			    }
			  else
			    {
			      foreFitters[nFore].add(c);
			    }
			}
		      else if (i < backgroundGMM.K() && hardSegmentation[x][y] == MASK_BG && components[x][y] == nBack)
			{
			  if (bg.eigenvectors[0][0] * Color.red(c) + bg.eigenvectors[1][0] * Color.green(c) + bg.eigenvectors[2][0] * Color.blue(c) > splitBack)
			    {
			      components[x][y] = i;
			      backFitters[i].add(c);
			    }
			  else
			    {
			      backFitters[nBack].add(c);
			    }
			}
		    }
		}

	      // Compute new split Gaussians
	      backFitters[nBack].finalize(backgroundGMM.m_gaussians[nBack], backCount, true);
	      foreFitters[nFore].finalize(foregroundGMM.m_gaussians[nFore], foreCount, true);

	      if (i < backgroundGMM.K())
		backFitters[i].finalize(backgroundGMM.m_gaussians[i], backCount, true);
	      if (i < foregroundGMM.K())
		foreFitters[i].finalize(foregroundGMM.m_gaussians[i], foreCount, true);

	      // Find clusters with highest eigenvalue
	      nBack = 0;
	      nFore = 0;

	      for ( j = 0; j <= i; j++ )
		{
		  if (j < backgroundGMM.K() && backgroundGMM.m_gaussians[j].eigenvalues[0] > backgroundGMM.m_gaussians[nBack].eigenvalues[0])
		    nBack = j;

		  if (j < foregroundGMM.K() && foregroundGMM.m_gaussians[j].eigenvalues[0] > foregroundGMM.m_gaussians[nFore].eigenvalues[0])
		    nFore = j;
		}
	    }

	  backFitters = null;
	  foreFitters = null;
	}
	public 	static void LearnGMMs(GMM backgroundGMM, GMM foregroundGMM, int[][]  components, Bitmap image, int[][] hardSegmentation)
	{
	  int x, y;
	  int i;
	  int c;//color

	  // Step 4: Assign each pixel to the component which maximizes its probability
	  for(y = 0; y < image.getHeight(); y++)
	    {
	      for(x = 0; x < image.getWidth(); x++)
		{
		  c = image.getPixel(x, y);

		  if (hardSegmentation[x][y] == MASK_FG)
		    {
		      int k = 0;
		      double max = 0;

		      for ( i = 0; i < foregroundGMM.K(); i++)
			{
			  double p = foregroundGMM.p(i, c);
			  if (p > max)
			    {
			      k = i;
			      max = p;
			    }
			}

		      components[x] [y] = k;
		    }
		  else if(hardSegmentation[x][y] == MASK_BG)
		    {
		      int k = 0;
		      double max = 0;

		      for ( i = 0; i < backgroundGMM.K(); i++)
			{
			  double p = backgroundGMM.p(i, c);
			  if (p > max)
			    {
			      k = i;
			      max = p;
			    }
			}

		      components[x][ y] = k;
		    }
		}
	    }

	  // Step 5: Relearn GMMs from new component assignments

	  // Set up Gaussian Fitters
	  GaussianFitter[] backFitters = new GaussianFitter[backgroundGMM.K()];
	  for(i=0; i<backgroundGMM.K();i++) backFitters[i] = new GaussianFitter();
	  GaussianFitter[] foreFitters = new GaussianFitter[foregroundGMM.K()];
	  for(i=0; i<foregroundGMM.K();i++) foreFitters[i] = new GaussianFitter();

	   int foreCount = 0, backCount = 0;

	  for(y = 0; y < image.getHeight(); y++)
	    {
	      for(x = 0; x < image.getWidth(); x++)
		{
		  c = image.getPixel(x, y);

		  if(hardSegmentation[x][y] == MASK_FG)
		    {
		      foreFitters[components[x][y]].add(c);
		      foreCount++;
		    }
		  else if(hardSegmentation[x][y] == MASK_BG)
		    {
		      backFitters[components[x][y]].add(c);
		      backCount++;
		    }
		}
	    }

	  if( backCount != 0 )
	    {
	      for ( i = 0; i < backgroundGMM.K(); i++)
		backFitters[i].finalize(backgroundGMM.m_gaussians[i], backCount, false);
	    }
	  if( foreCount != 0 )
	    {
	      for ( i = 0; i < foregroundGMM.K(); i++)
		foreFitters[i].finalize(foregroundGMM.m_gaussians[i], foreCount, false);
	    }

	   backFitters = null;
	   foreFitters = null;
	}


}