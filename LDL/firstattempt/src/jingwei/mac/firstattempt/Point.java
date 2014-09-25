package jingwei.mac.firstattempt;

	public class Point{
		public int x, y;
		public int neighCount = 0;
		public Point(int myx, int myy){
			x = myx;
			y = myy;
		}
		
		public boolean overLaps(Point test){
			return test.x == x && test.y == y;
		}
		
		public void rotate90CW(int oldHeight){
			int temp = y;
			y = x;
			x = oldHeight - 1 - temp;
		}
		
		public void flipVertical(int height){
			y = height - 1 - y;
		}
		
		public void flipHorizontal(int width){
			x = width - 1 - x;
		}
		
		public static int getDistance(Point A, Point B){
			return (int)Math.sqrt(Math.pow((A.x-B.x), 2) + Math.pow((A.y-B.y), 2));
		}
		
		public static Point clonePoint(Point original){
			return new Point(original.x, original.y);
		}
	}