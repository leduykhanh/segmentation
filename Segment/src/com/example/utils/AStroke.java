package com.example.utils;

import java.util.ArrayList;

import org.opencv.core.Point;



public class AStroke {
public ArrayList<Point> StrokePoints;
public int strokeSize;
public AStroke(){
	strokeSize = 5;
	StrokePoints = new ArrayList<Point>(1000);
}
public AStroke(ArrayList<Point> p)
{
	strokeSize = 5;
	StrokePoints = new ArrayList<Point>(1000);
	for(int i = 0;i<p.size();i++){
		
		StrokePoints.add( p.get(i));
	}
}
}
