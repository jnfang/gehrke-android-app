package jingwei.mac.firstattempt;

//Helper class that gets points along the line in a 2D space
public class Line {
	//private Point A, B;
	private double slope;
	private double yintcpt;
	private boolean vertical = false;
	private double verticalOffset;
	
	public Line(Point A, Point B){
		if (A.x - B.x != 0){
			slope = (double)(A.y-B.y)/(double)(A.x - B.x);
			yintcpt = (double)A.y - slope*(double)A.x;
		}
		else{
			vertical = true;
			verticalOffset = A.x;
		}
	}
	
	public double getY(int x){
		//System.out.println("slope " + slope*(double)x);
		//System.out.println("yintcept " + yintcpt);
		return (slope*(double)x + yintcpt);
	}
	
	public double getX(int y){
		if (vertical == true){
			return verticalOffset;
		}
		else{
		return ((y - yintcpt)/slope);
		}
	}
	
	public Point getPointFromX(int x){
		return new Point(x, (int)getY(x));
	}
	
	public Point getPointFromY(int y){
		return new Point((int)getX(y), y);
	}
}
