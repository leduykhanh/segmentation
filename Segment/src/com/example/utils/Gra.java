package com.example.utils;

import java.util.ArrayList;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

public class Gra{
	public Path path;
	public int color;
	public ArrayList<Float> x,y;
	public Gra(Path path,int color){
		this.path = path;
		this.color = color;
		this.x = new ArrayList<Float>(100);
		this.y = new ArrayList<Float>(100);

	}
	public void add(float x,float y){
		this.x.add(new Float(x));
		this.y.add(new Float(y));
	}
}
