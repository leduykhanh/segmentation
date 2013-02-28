package com.example.utils;

public class JNILoad {
	
    public native String  test();
    public native String  fuck();
    public native String  unimplementedFunct();
	  static {
	        //if (!OpenCVLoader.initDebug()) {
	           // Log.d("OpenCV","not loaded");
		  
	          // System.loadLibrary("test");
	          // System.loadLibrary("testcpp");
	        //}
	    }
	 public JNILoad(){
		
	 }

}
