package jingwei.mac.firstattempt;

import java.util.ArrayList;
import java.util.HashMap;

public class Region {
	public ArrayList<Point> points;   //contains all points in the region
	public ArrayList<Point> checkList;  //contains points that need to be checked against target color
	//public Point centroid, topLeft, topRight, lowerLeft, lowerRight;
	private Point centroid, center;

	//	public Point topLeft, bottomLeft, topRight, bottomRight;

	private int top, bottom, left, right;
	//instantiate both variables
	public Region() {
		points = new ArrayList<Point>();
		checkList = new ArrayList<Point>();
		centroid = null;
		center = null;

		top = 1000;
		bottom = 0;
		left = 1000;
		right = 0;

		//			top = 0;
		//			bottom = 1000;
		//			left = 1000;
		//			right = 0;
		//			topLeft = new Point(1000, 1000);
		//			topRight = new Point(0, 1000);
		//			bottomLeft = new Point(1000, 0);
		//			bottomRight = new Point(0, 0);
	}

	//method that tests if a point is contained in the region
	public boolean contains(int x, int y) {
		for (Point p : points) {
			if (p.x == x && p.y == y) {
				return true;
			}
		}
		return false;
	}

	//n^2, slow
	public void countNeighbors(){
		for (Point p : points){
			if (contains(p.x+1, p.y)){
				p.neighCount ++;
			}
			if (contains(p.x-1, p.y)){
				p.neighCount ++;
			}
			if (contains(p.x, p.y+1)){
				p.neighCount ++;
			}
			if (contains(p.x, p.y-1)){
				p.neighCount ++;
			}

		}
	}
	public Point findCentroid(){
		int totalx = 0, totaly = 0;
		for (Point point : points){
			totalx += point.x;
			totaly += point.y;
		}
		return centroid = new Point(totalx/points.size(), totaly/points.size());
	}

	public Point findCenter(){
		//int up, down, left, right;
		//			top = 1000;
		//			bottom = 0;
		//			left = 1000;
		//			right = 0;
		//			for (Point point : points){
		//				if (point.x < left){
		//					left = point.x;
		//				}
		//				if (point.x > right){
		//					right = point.x;
		//				}
		//				if (point.y < top){
		//					top = point.y;
		//				}
		//				if (point.y > bottom){
		//					bottom = point.y;
		//				}
		//			}

		//	System.out.println(up + " " + down + " " + left + " " + right);

		return center = new Point ((left + right)/2, (top+bottom)/2);
	}

	public void add(Point newPoint){
		points.add(newPoint);

		if (newPoint.x < left){
			left = newPoint.x;
		}
		if (newPoint.x > right){
			right = newPoint.x;
		}
		if (newPoint.y < top){
			top = newPoint.y;
		}
		if (newPoint.y > bottom){
			bottom = newPoint.y;
		}
		//			System.out.println("topLeft : x: " + topLeft.x + "  y: " + topLeft.y);
		//			System.out.println("topRight : x: " + topRight.x + "  y: " + topRight.y);
		//			
		//			if (newPoint.x < topLeft.x && newPoint.y < topLeft.y){
		//				topLeft = Point.clonePoint(newPoint);
		////				System.out.println("topLeft : x: " + topLeft.x + "  y: " + topLeft.y);
		////				System.out.println("topRight : x: " + topRight.x + "  y: " + topRight.y);
		//
		//			}
		//			
		//			if (newPoint.x > topRight.x && newPoint.y < topRight.y){
		//				topRight = Point.clonePoint(newPoint);
		////				System.out.println("topLeft : x: " + topLeft.x + "  y: " + topLeft.y);
		////				System.out.println("topRight : x: " + topRight.x + "  y: " + topRight.y);
		//
		//			}
		//			
		//			if (newPoint.x < bottomLeft.x && newPoint.y > bottomLeft.y){
		//				bottomLeft = Point.clonePoint(newPoint);
		//			}
		//			
		//			if (newPoint.x > bottomRight.x && newPoint.y > bottomRight.y){
		//				bottomRight = Point.clonePoint(newPoint);
		//			}
		//			System.out.println("after topLeft : x: " + topLeft.x + "  y: " + topLeft.y);
		//			System.out.println("after topRight : x: " + topRight.x + "  y: " + topRight.y);
		//			
	}

	public double getProportion(){
		countNeighbors();
		findCenter();
		double max = 0;
		double min = 2000;
		for (Point point : points){
			double dist = Point.getDistance(point, center);
			if ( point.neighCount < 4 && dist > max){
				max = dist;
			}
			if ( point.neighCount < 4 && dist < min){
				min = dist;
			}
		}

		return max/min;
		//			System.out.println("pro top: " + top);
		//			System.out.println("pro bottom: " + bottom);

		//			double up = Point.getDistance(topLeft, topRight);
		//			double down = Point.getDistance(bottomRight, bottomLeft);
		//			double left = Point.getDistance(topLeft, bottomLeft);
		//			double right = Point.getDistance(topRight, bottomRight);
		//			System.out.println("topLeft : x: " + topLeft.x + "  y: " + topLeft.y);
		//			System.out.println("topRight : x: " + topRight.x + "  y: " + topRight.y);
		//			
		//			System.out.println("up: " + up);
		//			System.out.println("down: " + down);
		//			System.out.println("left: " + left);
		//			System.out.println("right: " + right);

		//return Math.max(Math.max(up, down)/Math.min(left, right), Math.max(left, right)/Math.min(up, down));
		//			System.out.println("pro height: " + height);
		//			System.out.println("pro width: " + width);
		//			System.out.println("proportion: " + Math.max(height, width)/Math.min(height, width));
		//			System.out.println(" ");

		//return Math.max(height, width)/Math.min(height, width);
	}

	public int getHeight(){
		return bottom - top;
	}

	public int getWidth(){
		return right - left;
	}

	public Point getCentroid(){
		return centroid;
	}

	public int getBottom(){
		return bottom;
	}

	public int getRight(){
		return right;
	}

	public void merge(Region region){
		for (Point point: region.points){
			add(point);
		}

		getCentroid();
	}

	public int getHorizontalSpan(){
		ArrayList<Point> span = new ArrayList<Point>();
//		span.add(new Point(0,0));
		
		for (Point point: points){
			boolean add = true;
			for (Point spanPoint : span){
//				System.out.println("spanpointx: " + spanPoint.x);
				if (point.x == spanPoint.x){
					add = false;
										break;
				}
			}
			if (add){
				span.add(point);
			}
//			if (span.size() == 0){
//				span.add(point);
//			}
		}
		System.out.println("span is: " + span.size());
		return span.size();
	}
}
