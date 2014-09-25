package jingwei.mac.firstattempt;

//import java.awt.Canvas;
//import java.awt.image.BufferedImage;
import java.util.ArrayList;



public class RGBImage extends RGB{

	//compressBar determines how much to compress the image
	private int compressBar = 500; //arbitrary, need to change if image is compressed further
	private int maxRegionArea = (int)((compressBar/5)*(compressBar/5)*1.5); //(compressBar/5)^2
	private int minMarkerSide = 10; //arbitrary, might need to change
	private int minRegionArea = minMarkerSide*minMarkerSide; //min marker side ^2
	private int maxMarkerSide = (int)(compressBar/5*1.75);  //compressBar/5*1.5
	private double proportionControl = 3;
	private boolean fastLooping = true;
	private final int P2Green = 15;

	private enum Direction{ UP, DOWN, LEFT, RIGHT };
	private ArrayList <Region> markerRegions;
	public Point P1, P2, P3, P4, P5;

	public ArrayList<Strip> strips;

	public RGBImage(int width, int height){
		super(width, height);

		markerRegions = null;
		P1 = new Point(0, 0);
		P2 = new Point(0, 0);
		P3 = new Point(0, 0);
		P4 = new Point(0, 0);
		P5 = new Point(0, 0);
	}

	public void destroy(){
		super.destroy();
		markerRegions = null;
	}

	public void repaintMarkers(int red, int green, int blue){
		for (Region region : markerRegions){
			for (Point point : region.points){
				setRGB(point.y, point.x, red, green, blue);
				if (point.neighCount < 4){
					setRGB(point.y, point.x, 0, 255, 0);
				}
			}
		}
	}

	public RGBImage cloneRGBImage(){
		RGBImage newRGB = new RGBImage(getImageWidth(), getImageHeight());
		newRGB.redMat = redMat.cloneMatrix();
		newRGB.greenMat = greenMat.cloneMatrix();
		newRGB.blueMat = blueMat.cloneMatrix();
		newRGB.blank = blank.cloneMatrix();
		//cloning of markerRegions is only for testing purposes
		if (markerRegions != null){
			newRGB.markerRegions = (ArrayList<Region>) markerRegions.clone();
		}
		return newRGB;

	}

	private void findMarkerRegions(int red, int green, int blue, int markerColorDiff){
		resetBlank();
		markerRegions = new ArrayList <Region> ();

		for (int i = 0; i<getImageHeight(); i++){
			for (int j = 0; j<getImageWidth(); j++){					
				//If a pixel matches, start a floodRegion search started from this pixel.
				if (getBlank(i,j)==0 && compareColor(i,j, red, green, blue, markerColorDiff)){

					Region newRegion = floodRegion(j, i, red, green, blue, markerColorDiff);	

					//region size and shape restrictions
					if (newRegion.points.size() > minRegionArea && newRegion.points.size() < maxRegionArea 
							//&& newRegion.getProportion() < proportionControl 
							&& newRegion.getHeight() > minMarkerSide && newRegion.getHeight() < maxMarkerSide
							&& newRegion.getWidth() > minMarkerSide && newRegion.getWidth() < maxMarkerSide
							){

						//if fastlooping, add region directly
						if (fastLooping == true){
							markerRegions.add(newRegion);
						}
						//if in slow looping mode, check for proportion criteria
						else if (newRegion.getProportion() < proportionControl){
							markerRegions.add(newRegion);
						}
					}

					//debuggin lines
					else if (newRegion.points.size()> minRegionArea){
						//						System.out.println(" bad height: " + newRegion.getHeight());
						//						System.out.println(" bad region size: " + newRegion.points.size());
						//						System.out.println(" bad height: " + newRegion.getHeight());
						//						System.out.println(" bad width: " + newRegion.getWidth());
					}
				}
			}
		}
	}

	public boolean getMarkers(){

		int maxMarkerColorDiff = 75, minMarkerColorDiff = 60;
		int markerColorDiff = maxMarkerColorDiff;

		findMarkerRegions(0, 0, 0, markerColorDiff);

		//fast loop with no proportion restriction
		fastLooping = true;
		//decrement color difference 
		while (markerRegions.size() != 5 && markerColorDiff > minMarkerColorDiff){

			RGBImage temp = cloneRGBImage();
			temp.repaintMarkers(0, 0, 255);

			markerColorDiff -= 1;
			findMarkerRegions(0, 0, 0, markerColorDiff); //black
		}

		//uncomment to enable slow looping
		//		fastLooping = false;

		if (!fastLooping){
			//slow loop with proportion restriction
			maxMarkerColorDiff = 90;
			minMarkerColorDiff = 50;
			markerColorDiff = maxMarkerColorDiff;
			while (markerRegions.size() != 5 && proportionControl < 6){

				while (markerRegions.size() != 5 && markerColorDiff > minMarkerColorDiff){
					RGBImage temp = cloneRGBImage();
					temp.repaintMarkers(0, 0, 255);

					markerColorDiff -= 1;
					findMarkerRegions(0, 0, 0, markerColorDiff);
				}
				proportionControl ++;
			}
			return true;
		}
		else{
			//debugging lines
			//			System.out.println("final proportion control: " + (proportionControl -1));
			//			System.out.println("final color diff: " + maxMarkerColorDiff);
			//			System.out.println("final number of regions: " + markerRegions.size());
			if (markerRegions.size() == 5){
				return true;
			}
			else{
				return false;
			}
		}
	}

	//A method that determines if a pixel approximately matches with the target color
	private boolean compareColor(int i, int j, int stdRed, int stdGreen, int stdBlue, int markerColorDiff){
		return (Math.abs(getBlue(i, j) - stdBlue)<=markerColorDiff 
				&& Math.abs(getGreen(i, j) - stdGreen)<=markerColorDiff
				&& Math.abs(getRed(i, j) - stdRed)<=markerColorDiff);
	}

	private boolean compareRBColor(int i, int j, int stdRed, int stdBlue, int markerColorDiff){
		return (
				Math.abs(getRed(i, j) - stdRed)<=markerColorDiff
				);
	}

	//look for all points in the region after one point with 
	//coordinates (x,y) has been determined to match with the target color
	private Region floodRegion(int x, int y, int stdRed, int stdGreen, int stdBlue, int markerColorDiff){
		Region newRegion = new Region();
		newRegion.checkList.add(new Point(x,y));  //add first point to list

		//As long as there are elements in checkList to be checked, the algorithm runs.
		while (!newRegion.checkList.isEmpty()){
			Point checkPoint = newRegion.checkList.get(0);  //retrieve a point from checkList
			int i = checkPoint.y;
			int j = checkPoint.x;
			newRegion.checkList.remove(checkPoint);  //remove this point from checkList

			//no action taken if blank shows the point has already been visited.
			if (getBlank(i,j)==0){		//if blank shows this point has not been visited yet then it is marked off
				setBlank(i,j,1);

				//if this point matches with target color, it is added to region's list of points
				if (compareColor(i,j, stdRed, stdGreen, stdBlue, markerColorDiff)){ 
					newRegion.add(checkPoint);

					//if the point is not at the edge of the frame, add its neighbors to checkList
					//so they can be checked later
					if (i-1>0 && i+1<getImageHeight() && j-1>0 && j+1<getImageWidth()){  
						newRegion.checkList.add(new Point(j,i+1));
						newRegion.checkList.add(new Point(j,i-1));
						newRegion.checkList.add(new Point(j+1,i));
						newRegion.checkList.add(new Point(j-1,i));
					}
				}		
			}
		}
		return newRegion;
	}

	//look for all points in the region after one point with 
	//coordinates (x,y) has been determined to match with the target color
	private Region floodRBRegion(int x, int y, int stdRed, int stdBlue, int markerColorDiff){
		//System.out.println("new region " + x +" and " + y);
		Region newRegion = new Region();
		newRegion.checkList.add(new Point(x,y));  //add first point to list

		//As long as there are elements in checkList to be checked, the algorithm runs.
		while (!newRegion.checkList.isEmpty()){
			Point checkPoint = newRegion.checkList.get(0);  //retrieve a point from checkList
			int i = checkPoint.y;
			int j = checkPoint.x;
			newRegion.checkList.remove(checkPoint);  //remove this point from checkList

			//no action taken if blank shows the point has already been visited.
			if (getBlank(i,j)==0){		//if blank shows this point has not been visited yet then it is marked off
				setBlank(i,j,1);

				//if this point matches with target color, it is added to region's list of points
				if (compareRBColor(i,j, stdRed, stdBlue, markerColorDiff)){ 
					newRegion.add(checkPoint);
					//System.out.println("jump");

					//if the point is not at the edge of the frame, add its neighbors to checkList
					//so they can be checked later
					if (i-1>0 && i+1<getImageHeight() && j-1>0 && j+1<getImageWidth()){  
						newRegion.checkList.add(new Point(j,i+1));
						newRegion.checkList.add(new Point(j,i-1));
						newRegion.checkList.add(new Point(j+1,i));
						newRegion.checkList.add(new Point(j-1,i));
					}
				}		
			}
		}

		return newRegion;
	}

	private Region floodRBRegionWithBounds(int x, int y, int stdRed, int stdBlue, int upper, int lower, int left, int right, int markerColorDiff){
		//System.out.println("new region " + x +" and " + y);
		Region newRegion = new Region();
		newRegion.checkList.add(new Point(x,y));  //add first point to list

		//As long as there are elements in checkList to be checked, the algorithm runs.
		while (!newRegion.checkList.isEmpty()){
			Point checkPoint = newRegion.checkList.get(0);  //retrieve a point from checkList
			int i = checkPoint.y;
			int j = checkPoint.x;
			newRegion.checkList.remove(checkPoint);  //remove this point from checkList

			//no action taken if blank shows the point has already been visited.
			if (getBlank(i,j)==0){		//if blank shows this point has not been visited yet then it is marked off
				setBlank(i,j,1);

				//if this point matches with target color, it is added to region's list of points
				if (compareRBColor(i,j, stdRed, stdBlue, markerColorDiff)){ 
					newRegion.add(checkPoint);
					//System.out.println("jump");

					//if the point is not at the edge of the frame, add its neighbors to checkList
					//so they can be checked later
					if (i-1>lower && i+1<upper && j-1>left && j+1<right){  
						newRegion.checkList.add(new Point(j,i+1));
						newRegion.checkList.add(new Point(j,i-1));
						newRegion.checkList.add(new Point(j+1,i));
						newRegion.checkList.add(new Point(j-1,i));
					}
				}		
			}
		}

		return newRegion;
	}

	public void appointCorners(){
		//		System.out.println(markerRegions);
		ArrayList <Point> centroids = new ArrayList<Point>(5);
		Point tempP1 = new Point(-1000,-1000);
		Point tempP2A = new Point(-1000, -1000);
		Point tempP2B = new Point(-1000, -1000);
		Point tempP3 = new Point(-1000, -1000);

		//get centroids
		int minDistance = 10000, secondMinDistance = 10000;

		//can find centroid OR center 
		for (Region region: markerRegions){
			centroids.add(region.findCentroid());
		}

		//find least and second least distances
		for (Point A: centroids){
			for (Point B: centroids){
				int newDistance = Point.getDistance(A, B);
				if (newDistance != 0 && newDistance < minDistance){

					//assign last min distance variables to second min
					secondMinDistance = minDistance;
					tempP2B = tempP1;
					tempP3 = tempP2A;

					//assign new min distance variables
					tempP1 = A;
					tempP2A = B;
					minDistance = newDistance;

				}
				else if (newDistance != 0 && newDistance < secondMinDistance && newDistance != minDistance){
					secondMinDistance = newDistance;
					tempP2B = A;
					tempP3 = B;
				}
			}
		}

		if (tempP1.overLaps(tempP2B)){
			P1 = tempP2A;
			P2 = tempP1;
			P3 = tempP3;
		}
		else if (tempP1.overLaps(tempP3)){
			P1 = tempP2A;
			P2 = tempP1;
			P3 = tempP2B;
		}		
		else if (tempP2A.overLaps(tempP2B)){
			P1 = tempP1;
			P2 = tempP2A;
			P3 = tempP3;
		}
		else{
			P1 = tempP1;
			P2 = tempP2A;
			P3 = tempP2B;
		}

		for (Point point : centroids){
			if (!point.overLaps(P1) && !point.overLaps(P2) && !point.overLaps(P3)){
				if (P4.overLaps(new Point(0, 0))){
					P4 = point;
				}
				else {
					P5 = point;
				}
			}
		}

		if ( (Math.abs(P1.x - P3.x) > Math.abs(P1.y - P3.y) && (P5.x - P4.x)*(P1.x - P3.x) < 0) 
				||  (Math.abs(P1.x - P3.x) <= Math.abs(P1.y - P3.y) && (P5.y - P4.y)*(P1.y - P3.y) < 0)){
			Point temp = P4;
			P4 = P5;
			P5 = temp;
		}
	}

	//repaint side black square
	public void repaintP2(){
		for (Region region: markerRegions){
			if (region.getCentroid().overLaps(P2)){
				for (Point point: region.points){
					setRGB(point.y, point.x, 0, P2Green, 0);
				}
			}
		}
	}

	public void setOrientation(){

		if (Math.abs(P1.x-P2.x) > Math.abs(P1.y - P2.y)){
			rotate90CW();
			P1.rotate90CW(getImageWidth());
			P2.rotate90CW(getImageWidth());
			P3.rotate90CW(getImageWidth());
			P4.rotate90CW(getImageWidth());
			P5.rotate90CW(getImageWidth());
		}

		if (P1.y < P2.y){
			flipVertical();
			P1.flipVertical(getImageHeight());
			P2.flipVertical(getImageHeight());
			P3.flipVertical(getImageHeight());
			P4.flipVertical(getImageHeight());
			P5.flipVertical(getImageHeight());
		}

		if (P1.x > P5.x){
			flipHorizontal();
			P1.flipHorizontal(getImageWidth());
			P2.flipHorizontal(getImageWidth());
			P3.flipHorizontal(getImageWidth());
			P4.flipHorizontal(getImageWidth());
			P5.flipHorizontal(getImageWidth());

		}
	}

	public RGBImage stretchEdgesAndGetWindow(){
		Line P13 = new Line(P1, P3);
		Line P34 = new Line(P3, P4);
		Line P45 = new Line(P4, P5);
		Line P51 = new Line(P1, P5);

		Point floor = new Point(0, 0);

		//stretch down
		//P1 is the floor, 
		if (P1.y > P5.y){
			floor.x = P1.x;
			floor.y = P1.y;
			stretch(Direction.RIGHT, Direction.DOWN, floor, P34, P51, P45);
		}
		else{
			floor.x = P5.x;
			floor.y = P5.y;
			stretch(Direction.LEFT, Direction.DOWN, floor, P34, P51, P13);
		}

		P13 = new Line(P1, P3);
		P34 = new Line(P3, P4);
		P45 = new Line(P4, P5);
		P51 = new Line(P1, P5);
		//setLines( P13,  P34,  P45,  P51);

		//stretch up
		if (P3.y > P4.y){
			floor.x = P4.x;
			floor.y = P4.y;
			stretch(Direction.LEFT, Direction.UP, floor, P51, P34, P13);

		}
		else{
			floor.x = P3.x;
			floor.y = P3.y;
			stretch(Direction.RIGHT, Direction.UP, floor, P51, P34, P45);
		}

		//setLines( P13,  P34,  P45,  P51);
		P13 = new Line(P1, P3);
		P34 = new Line(P3, P4);
		P45 = new Line(P4, P5);
		P51 = new Line(P1, P5);

		return unShearAndGetWindow(P13, P45);		
	}

	//stretch image if necessary
	private void stretch(Direction sweepDirection, Direction stretchDirection, Point floor, Line ceiling, Line edge, Line sideCeiling){
		int minRow, maxRow, minCol, maxCol;
		Line newEdge;

		RGBImage temp = cloneRGBImage();
		switch(stretchDirection){
		case UP:
			if (sweepDirection == Direction.RIGHT){
				minCol = floor.x;
				maxCol = getImageWidth();
				P4.y = floor.y;
				newEdge = new Line(P4, P5);
			}
			else{
				minCol = 0;
				maxCol = floor.x;
				P3.y = floor.y;
				newEdge = new Line(P1, P3);
			}

			minRow = floor.y;

			for (int col = minCol; col < maxCol; col ++){

				double ciel;
				if ((col < P5.x && sweepDirection == Direction.RIGHT) || col > P1.x && sweepDirection == Direction.LEFT){

					ciel = ceiling.getY(col); //ceiling height
					double drop = ciel - edge.getY(col); //original height
					double scale = drop/(ciel - (double)minRow); //scale over stretched height

					for (int row = minRow; row < ciel; row ++){
						int oldRow = (int)(ciel - ((ciel - (double)row)*scale));
						if (oldRow < getImageHeight() && row < getImageHeight()){
							setRGB(row, col, temp.getRed(oldRow, col), temp.getGreen(oldRow, col), temp.getBlue(oldRow, col));
						}
					}
				}

				else{
					ciel = sideCeiling.getY(col); //ceiling height
					double drop = ciel - edge.getY(col); //original height
					double newEdgeCiel = newEdge.getY(col);
					double scale = drop/(newEdgeCiel - (double)minRow); //scale over stretched height

					for (int row = minRow; row < ciel; row ++){

						int oldRow = (int)(ciel - (newEdgeCiel - (double)row)*scale);

						if (oldRow < getImageHeight() && row < getImageHeight()){
							try{
								setRGB(row, col, temp.getRed(oldRow, col), temp.getGreen(oldRow, col), temp.getBlue(oldRow, col));
							}
							catch(ArrayIndexOutOfBoundsException e){
								System.err.println("stretching error");
								return;
							}
						}
					}
				}
			}

			break;

		case DOWN:
			if (sweepDirection == Direction.RIGHT){
				minCol = floor.x;
				maxCol = getImageWidth();
				P5.y = floor.y;
				newEdge = new Line(P4, P5);
			}
			else{
				minCol = 0;
				maxCol = floor.x;
				P1.y = floor.y;
				newEdge = new Line(P1, P3);
			}

			maxRow = floor.y;

			for (int col = minCol; col < maxCol; col ++){

				double ciel;
				if ((col < P4.x && sweepDirection == Direction.RIGHT) || col > P3.x && sweepDirection == Direction.LEFT){
					ciel = ceiling.getY(col);//ceiling height

					double drop = edge.getY(col) - ciel; //original height

					double scale = drop/((double)maxRow - ciel); //scale over stretched height

					for (int row = (int)Math.round(ciel); row < maxRow; row ++){

						int oldRow = (int)Math.round(((double)row - ciel)*scale + ciel);
						if (oldRow >= 0 && row >= 0){
							setRGB(row, col, temp.getRed(oldRow, col), temp.getGreen(oldRow, col), temp.getBlue(oldRow, col));
						}
					}
				}
				else {
					ciel = sideCeiling.getY(col);
					double drop = edge.getY(col) - ciel; //original height

					double newEdgeCiel = newEdge.getY(col);
					double scale = drop/((double)maxRow - newEdgeCiel); //scale over stretched height

					for (int row = (int)Math.round(newEdgeCiel); row < maxRow; row ++){

						int oldRow = (int)Math.round(((double)row - newEdgeCiel)*scale + ciel);

						if (oldRow >= 0 && row >= 0){
							setRGB(row, col, temp.getRed(oldRow, col), temp.getGreen(oldRow, col), temp.getBlue(oldRow, col));
						}
					}
				}
			}
			break;
		default: break;
		}
	}

	//window is made here
	private RGBImage unShearAndGetWindow(Line leftEdge, Line rightEdge){
		int windowWidth = Math.max(P4.x - P3.x, P1.x - P5.x);
		int windowHeight = P1.y - P3.y;
		int topPad = P3.y;

		RGBImage window = new RGBImage(windowWidth, windowHeight);

		for (int row = 0; row < windowHeight; row ++){

			double leftDist = leftEdge.getX(row + topPad);
			double rightDist = rightEdge.getX(row + topPad);
			double scale = (rightDist - leftDist)/windowWidth;

			for (int col = 0; col < windowWidth; col++){
				int oldCol = (int)( (double)col*scale + leftDist);
				window.setRGB(row, col, 
						getRed(row + topPad, oldCol), 
						getGreen(row + topPad, oldCol), 
						getBlue(row + topPad, oldCol));
			}
		}
		return window;
	}

	public int getP2TopEdge(){
		int edgeLeftBound = getImageWidth()/50;
		int edgeRightBound = edgeLeftBound*2;

		//find top edge of second marker
		int sumEdge = 0;
		int edgeCount = 0;
		for (int col = edgeLeftBound; col < edgeRightBound; col ++){
			for (int row = 0; row < getImageHeight(); row ++){
				if (getRed(row, col) == 0 && getBlue(row, col) == 0 && getGreen(row, col) == P2Green){
					sumEdge += row;
					edgeCount ++;
					row = getImageHeight();	
				}
			}
		}

		int averageEdge = sumEdge/edgeCount;


		//draw average Edge
		for (int col = 0; col < 100; col ++){
			setRGB(averageEdge + 3, col, 0, 0, 200);
		}

		return averageEdge;
	}

	public int getP2BottomEdge(){
		int edgeLeftBound = getImageWidth()/50;
		int edgeRightBound = edgeLeftBound*2;

		//find bottom edge of second marker
		int sumEdge = 0;
		int edgeCount = 0;
		for (int col = edgeLeftBound; col < edgeRightBound; col ++){
			for (int row = getImageHeight()-1; row > 0; row --){
				if (getRed(row, col) == 0 && getBlue(row, col) == 0 && getGreen(row, col) == P2Green){
					sumEdge += row;
					edgeCount ++;
					row = 0;	
				}
			}
		}

		int averageEdge = sumEdge/edgeCount;

		//draw average Edge
		for (int col = 0; col < 50; col ++){
			setRGB(averageEdge + 3, col, 0, 0, 200);
		}

		return averageEdge;
	}

	public int getP2RightEdge(int top, int bottom){
		int edgeTopBound = top + 10;
		int edgeBottomBound = bottom - 10;

		//find bottom edge of second marker
		int sumEdge = 0;
		int edgeCount = 0;
		for (int row = edgeTopBound; row < edgeBottomBound; row ++){
			for (int col = getImageWidth()/5; col > 0; col --){
				if (getRed(row, col) == 0 && getBlue(row, col) == 0 && getGreen(row, col) == P2Green){
					sumEdge += col;
					edgeCount ++;
					col = 0;	
				}
			}
		}

		//should throw exception here
		if (edgeCount == 0){
			edgeCount = 1;
		}


		int averageEdge = sumEdge/edgeCount;



		//draw average Edge
		for (int row = 100; row < 300; row ++){
			setRGB(row, averageEdge + 3, 0, 0, 200);
		}

		return averageEdge;
	}

	public void redistributeVertically(int halfPoint, int quarterPoint){

		RGBImage newRGB = new RGBImage(getImageWidth(), getImageHeight());
		System.out.println("halfPoint: " + halfPoint);
		System.out.println("quarter point: " + quarterPoint);
		for (int col = 0; col < getImageWidth(); col ++){
			for (int row = 0; row < getImageHeight(); row ++){
				int oldRow;

				oldRow = (getImageHeight()*halfPoint*row - (getImageHeight()/2)*halfPoint*row)
						/(getImageHeight()*(getImageHeight()/2) - (getImageHeight()/2)*halfPoint + halfPoint*row - (imageHeight/2)*row);
				int red = getRed(oldRow, col);
				int green = getGreen(oldRow, col);
				int blue = getBlue(oldRow, col);

				newRGB.setRGB(row, col, red, green, blue);
			}
		}

		redMat = newRGB.getRedMat();
		greenMat = newRGB.getGreenMat();
		blueMat = newRGB.getBlueMat();

	}

	public void compressImage(int shortSide){

		double scale = (double)shortSide/(double)Math.min(getImageWidth(), getImageHeight());
		imageWidth = (int)(scale*(double)getImageWidth());
		imageHeight = (int)(scale*(double)getImageHeight());
		redMat = redMat.compressMatrix(getImageHeight(), getImageWidth());
		greenMat = greenMat.compressMatrix(getImageHeight(), getImageWidth());
		blueMat = blueMat.compressMatrix(getImageHeight(), getImageWidth());
		blank = blank.compressMatrix(getImageHeight(), getImageWidth());
	}

	public void updateRatios(int compressB, int minMarkerS){
		compressBar = compressB;
		maxRegionArea = (int)((compressBar/5)*(compressBar/5)*1.5); //(compressBar/5)^2
		minMarkerSide = minMarkerS; //arbitrary, might need to change
		minRegionArea = minMarkerSide*minMarkerSide; //min marker side ^2
		maxMarkerSide = (int)(compressBar/5*1.75); 
	}

	public static RGBImage resizeRGBImage(RGBImage rgb, int newWidth, int newHeight){
		RGBImage newRGB = rgb.cloneRGBImage();
		newRGB.redMat = rgb.redMat.compressMatrix(newHeight, newWidth);
		newRGB.greenMat = rgb.greenMat.compressMatrix(newHeight, newWidth);
		newRGB.blueMat = rgb.blueMat.compressMatrix(newHeight, newWidth);
		newRGB.blank = rgb.blank.compressMatrix(newHeight, newWidth);

		newRGB.markerRegions = null;

		newRGB.imageWidth = newWidth;
		newRGB.imageHeight = newHeight;
		return newRGB;
	}

	public void subRGBImage(int ceiling, int floor, int left, int right){
		redMat = redMat.subMatrix(ceiling, floor, left, right);
		greenMat = greenMat.subMatrix(ceiling, floor, left, right);
		blueMat = blueMat.subMatrix(ceiling, floor, left, right);
		blank = blank.subMatrix(ceiling, floor, left, right);

		markerRegions = null;

		imageWidth = right - left + 1;
		imageHeight = floor - ceiling + 1;
	}

	//really, it's remove green
	public void removeGrey(double padMultiplier){

		for (int row = 0; row < getImageHeight(); row ++){
			for (int col = 0; col < getImageWidth(); col ++){
				int red = getRed(row,  col);
				int green = getGreen(row,  col);
				int blue = getBlue(row,  col);
				int pad = green;

				int newRed = (int)((red - pad)*padMultiplier);
				int newBlue = (int)((blue - pad)*padMultiplier);
				if (newRed < 0){
					newRed = 0;
				}

				if (newBlue <0){
					newBlue = 0;
				}

				if (newRed > 255){
					newRed = 255;
				}

				if (newBlue > 255){
					newBlue = 255;
				}

				//should consider setting new green to 0 since marker is a mixture of red and blue
				//setRGB(row, col, newRed, green - pad, newBlue);
				if (!(red == 0 && blue == 0 && green == P2Green)){
					setRGB(row, col, newRed, 0, newBlue);
				}

			}
		}
	}

	public void gray(){
		for (int row = 0; row < getImageHeight(); row ++){
			for (int col = 0; col < getImageWidth(); col ++){

				int red = getRed(row, col);
				int green = getGreen(row, col);
				int blue = getBlue(row, col);
				int gray = (int)(0.2126*red + 0.7152*green + 0.0722*blue);
				System.out.println(gray);

				if (!(red == 0 && blue == 0 && green == P2Green)){
					setRGB(row, col, gray, gray, gray);
				}
			}
		}
	}

	public void removeNoise(){
		int redSum = 0;
		int greenSum = 0;
		int blueSum = 0;
		for (int row = 50; row < 200; row ++){
			for (int col = 5; col < 20; col ++){
				redSum += (255 -getRed(row, col));
				greenSum += (255 - getGreen(row, col));
				blueSum += (255 -getBlue(row, col));
			}
		}
		int redNoise = redSum/2250;
		int blueNoise = blueSum/2250;
		int greenNoise = greenSum/2250;

		redSum = 0;
		greenSum = 0;
		blueSum = 0;
		for (int row = 320; row < 345; row ++){
			for (int col = 150; col < 300; col ++){
				redSum += (255 -getRed(row, col));
				greenSum += (255 - getGreen(row, col));
				blueSum += (255 -getBlue(row, col));
			}
		}

		redNoise = Math.min(redNoise, redSum/2250);
		greenNoise = Math.min(greenNoise, greenSum/2250);
		blueNoise = Math.min(blueNoise,  blueSum/2250);

		//right noise
		redSum = 0;
		greenSum = 0;
		blueSum = 0;
		for (int row = 50; row < 200; row ++){
			for (int col = 430; col < 445; col ++){
				redSum += (255 -getRed(row, col));
				greenSum += (255 - getGreen(row, col));
				blueSum += (255 -getBlue(row, col));
			}
		}

		int RightredNoise = redSum/2250;
		int RightblueNoise = blueSum/2250;
		int RightgreenNoise = greenSum/2250;

		redNoise = Math.max(redNoise, RightredNoise);
		greenNoise = Math.max(greenNoise, RightgreenNoise);
		blueNoise = Math.max(blueNoise,  RightblueNoise);
		//		int redChop = greenNoise - redNoise;
		//		int blueChop = greenNoise - blueNoise;

		for (int row = 0; row < getImageHeight(); row ++){
			for (int col = 0; col < getImageWidth(); col ++){
				int red = getRed(row,  col);
				int green = getGreen(row,  col);
				int blue = getBlue(row,  col);




				int newRed = (int)((red + redNoise));
				int newBlue = (int)((blue + blueNoise));
				int newGreen = (int)((green + greenNoise));


				if (newRed > 255){
					newRed = 255;
				}
				if (newBlue > 255){
					newBlue = 255;
				}
				if (newGreen > 255){
					newGreen = 255;
				}


				int pad = Math.min(Math.min(newRed, newGreen), newBlue);

				newRed = (int)((newRed - pad));
				newBlue = (int)((newBlue - pad));
				newGreen = (int)((newGreen - pad));
				if (newRed < 0){
					newRed = 0;
				}

				if (newBlue <0){
					newBlue = 0;
				}

				if (newGreen <0){
					newGreen = 0;
				}



				if (!(red == 0 && blue == 0 && green == P2Green)){
					setRGB(row, col, newRed, newGreen, newBlue);
				}

			}

		}

		for (int row = 50; row < 200; row ++){
			for (int col = 5; col < 20; col ++){
				setRGB(row, col, 0, 255, 255);
			}
		}
	}

	//remove the background noise based on the noise fraction, fraction of the height starting from the top
	public void removeVerticalNoise(double topNoiseFraction, double bottomNoiseFraction){

		//get avg noise
		double topNoiseLength = topNoiseFraction*getImageHeight();
		double bottomNoiseLength = bottomNoiseFraction*getImageHeight();
		double noiseLength = (int)bottomNoiseLength - (int)topNoiseLength;

		for (int col = 0; col < getImageWidth(); col++){		
			double redSum = 0, blueSum = 0;

			for (int row = (int)topNoiseLength; row < (int)bottomNoiseLength; row ++){
				redSum += redMat.get(row, col);
				blueSum += blueMat.get(row, col);
			}

			double redNoise = redSum/noiseLength;
			double blueNoise = blueSum/noiseLength;

			//red and blue have noise reduction from background, green is set to zero
			for (int row = 0; row < getImageHeight(); row ++){
				double newRed = redMat.get(row, col) - redNoise;
				double newBlue = blueMat.get(row, col) - blueNoise;
				if (newRed < 0){
					newRed = 0;
				}

				if (newBlue < 0){
					newBlue = 0;
				}
				redMat.put(row, col, (int)newRed);
				blueMat.put(row, col, (int)newBlue);
			}

			setGreen((int)topNoiseLength, col, 255);
			setGreen((int)bottomNoiseLength, col, 255);
		}


	}

	public void boostRB(double RBmultiplier){
		for (int row = 0; row < getImageHeight(); row ++){
			for (int col = 0; col < getImageWidth(); col ++){

				int newRed = (int)(getRed(row, col)*RBmultiplier);
				if (newRed > 255){
					newRed = 255;
				}

				setRed(row, col, newRed);

				//blue, optional
				int newBlue = (int)(getBlue(row, col)*RBmultiplier);
				if (newBlue > 255){
					newBlue = 255;
				}
				setBlue(row, col, newBlue);

			}
		}
	}

	public void boostRGB(int RGBmultiplier){
		for (int row = 0; row < getImageHeight(); row ++){
			for (int col = 0; col < getImageWidth(); col ++){

				int newRed = (int)(getRed(row, col)*RGBmultiplier);
				if (newRed > 255){
					newRed = 255;
				}

				setRed(row, col, newRed);

				//blue, optional
				int newBlue = (int)(getBlue(row, col)*RGBmultiplier);
				if (newBlue > 255){
					newBlue = 255;
				}
				setBlue(row, col, newBlue);

				if (!(newRed == 0 && newBlue == 0 && getGreen(row, col) == P2Green)){
					int newGreen = (int)(getGreen(row, col)*RGBmultiplier);
					if (newGreen > 255){
						newGreen = 255;
					}
					setGreen(row, col, newGreen);
				}

			}
		}
	}

	//bottom to top searching for purple blocks
	public void findRBRegions(){
		int row = getImageHeight();

		int markerColorDiff = 90;
		markerRegions = new ArrayList <Region> ();			


		resetBlank();

		while(
				row > (5.0/8.0)*getImageHeight()-5){
			row --;
			for (int col = 0; col < getImageWidth(); col ++){

				//If a pixel matches, start a floodRegion search started from this pixel.
				if (getBlank(row,col) == 0 && compareRBColor(row, col, 255, 255, markerColorDiff)){
					Region newRegion = floodRBRegion(col, row, 255, 255, markerColorDiff);	
					if (newRegion.points.size() > 0){
						markerRegions.add(newRegion);
					}
				}
			}
		}

		repaintMarkers(0, 255, 0);	

	}

	public void findArtificialMarkers(Point start, RGBImage oldWindow){

		//starting position of search is arbitrarily 3/4 height for now, can be soft coded later by repainting marker 2 to a fresh color

		int markerColorDiff = 0;

		int stripWidth = (int)((3.0/32.0)*getImageHeight());
		int groveOffSet = (int)((1.0/8.0)*getImageHeight());

		//try to find base artificial marker first
		resetBlank();
		markerRegions = new ArrayList <Region> ();
		Region baseAMRegion = new Region();

		int row = start.y;
		int leftPad = start.x;

		for (int col = leftPad; col < stripWidth + leftPad && markerRegions.size() == 0; col ++){
			if (getBlank(row,col) == 0 && compareRBColor(row, col, 255, 255, markerColorDiff)){
				baseAMRegion = floodRBRegion(col, row, 255, 255, markerColorDiff);	
				if (baseAMRegion.points.size() > minRegionArea){
					markerRegions.add(baseAMRegion);
				}
			}
		}

		for (int col = leftPad; col < stripWidth + leftPad; col++){
			setGreen(start.y, col, 255);
		}


		if (markerRegions.size() == 0){
			System.out.println("no strips found!");
			return;
		}
		else{
			strips = new ArrayList <Strip> ();
			Strip firstStrip = new Strip(0, baseAMRegion);			
			strips.add(firstStrip);

			Strip currentStrip = firstStrip;
			Strip nextStrip;
			while ( (nextStrip = getNextStrip(currentStrip, stripWidth, groveOffSet, markerColorDiff)) != null ){
				strips.add(nextStrip);
				currentStrip = nextStrip;

			}

			for (Strip strip: strips){
				System.out.println("anchor: " + strip.stripAnchor.x + " " + strip.stripAnchor.y);
				System.out.println("index: " + strip.index);
			}

			analyzeStrips(strips, groveOffSet, markerColorDiff, oldWindow);
		}
		repaintMarkers(0, 255, 0);	
	}

	public void analyzeStrips(ArrayList<Strip> strips, int groveOffset, int markerColorDiff, RGBImage oldWindow){
		int horizontalExpansion = 15;
		int minMarkerSize = 150;
		for (Strip strip : strips){

			//positive control
			resetBlank(); //to avoid contamination among groves and among different strips

			Region posControl = new Region();
			int col = strip.stripAnchor.x;

			int lowerBound = strip.stripAnchor.y - (int)(groveOffset*(5.0/4.0)) - 15;
			int upperBound = strip.stripAnchor.y - (int)(groveOffset*(3.0/4.0));
			int leftBound = strip.stripAnchor.x - horizontalExpansion;
			int rightBound = strip.stripAnchor.x + horizontalExpansion;
			for (int colRange = col-horizontalExpansion; colRange < col + horizontalExpansion; colRange ++){
				for (int row = lowerBound; row < upperBound; row ++){
					if (getBlank(row,colRange) == 0 && compareRBColor(row, colRange, 255, 255, markerColorDiff)){
						posControl.merge(floodRBRegionWithBounds(colRange, row, 255, 255, upperBound, lowerBound, leftBound, rightBound, markerColorDiff));	
					}
				}
			}

			markerColorDiff = 100;

			posControl.getHorizontalSpan();

			//set positive control
			if (posControl.points.size() > minMarkerSize && posControl.getHorizontalSpan() > horizontalExpansion*1.5){
				strip.positiveControl = true;

				strip.posAnchor = posControl.findCentroid();
			}
			else{
				strip.positiveControl = false;
			}

			//shows search range
			for (int coll = col-horizontalExpansion; coll < col + horizontalExpansion; coll++){
				setGreen(upperBound, coll, 200);
				setGreen(lowerBound, coll, 200);
			}

			int posSum = 0;
			//repaint markers
			for (Point point : posControl.points){
				setRGB(point.y, point.x, 0, 255, 0);
				posSum +=oldWindow.getRed(point.y, point.x);
			}
			double posAvg = 0;
			if (posControl.points.size()!= 0){
				posAvg = (double)posSum/(double)posControl.points.size();
			}

			//test
			resetBlank(); //to avoid contamination among groves and among different strips

			Region testMarker = new Region();

			if (strip.posAnchor != null){
				//				col = strip.posAnchor.x;
				lowerBound = strip.posAnchor.y - (int)(groveOffset*(5.0/4.0));
				upperBound = strip.posAnchor.y - (int)(groveOffset*(3.0/4.0));
			}
			else{
				//col = strip.stripAnchor.x;
				lowerBound = strip.stripAnchor.y - (int)(groveOffset*(9.0/4.0));
				upperBound = strip.stripAnchor.y - (int)(groveOffset*(7.0/4.0));
			}



			for (int colRange = col-horizontalExpansion; colRange < col + horizontalExpansion; colRange ++){
				for (int row = lowerBound; row < upperBound; row ++){
					if (getBlank(row,colRange) == 0 && compareRBColor(row, colRange, 255, 255, markerColorDiff)){
						testMarker.merge(floodRBRegionWithBounds(colRange, row, 255, 255, upperBound, lowerBound, leftBound, rightBound, markerColorDiff));	
					}
				}
			}

			//set positive control
			if (testMarker.points.size() > minMarkerSize && testMarker.getHorizontalSpan() > horizontalExpansion*1.5){
				strip.test = true;
			}
			else{
				strip.test = false;
			}

			//shows search range
			if (strip.posAnchor != null){
				for (int coll = col-horizontalExpansion; coll < col + horizontalExpansion; coll++){
					setGreen(upperBound, coll, 200);
					setGreen(lowerBound, coll, 200);
				}
			}

			int testSum = 0;
			//repaint markers
			for (Point point : testMarker.points){
				setRGB(point.y, point.x, 0, 255, 0);
				testSum += oldWindow.getRed(point.y, point.x);
			}

			double testAvg = 0;
			if (testMarker.points.size() != 0){
				testAvg = (double)testSum/(double)testMarker.points.size();
			}
			;
			//negative control
			resetBlank(); //to avoid contamination among groves and among different strips

			Region negControl = new Region();

			if (strip.posAnchor != null){
				//				col = strip.posAnchor.x;
				lowerBound = strip.posAnchor.y - (int)(groveOffset*(9.0/4.0));
				upperBound = strip.posAnchor.y - (int)(groveOffset*(7.0/4.0));
			}
			else{
				//col = strip.stripAnchor.x;
				lowerBound = strip.stripAnchor.y - (int)(groveOffset*(13.0/4.0));
				upperBound = strip.stripAnchor.y - (int)(groveOffset*(11.0/4.0));
			}

			for (int colRange = col-horizontalExpansion; colRange < col + horizontalExpansion; colRange ++){
				for (int row = lowerBound; row < upperBound; row ++){
					if (getBlank(row,colRange) == 0 && compareRBColor(row, colRange, 255, 255, markerColorDiff)){
						negControl.merge(floodRBRegionWithBounds(colRange, row, 255, 255, upperBound, lowerBound, leftBound, rightBound, markerColorDiff));	
					}
				}
			}

			//set negative control
			if (negControl.points.size() > minMarkerSize && negControl.getHorizontalSpan() > horizontalExpansion*1.5){
				strip.negativeControl = true;
			}
			else{
				strip.negativeControl = false;
			}

			//shows search range
			if (strip.posAnchor != null){
				for (int coll = col-horizontalExpansion; coll < col + horizontalExpansion; coll++){
					setGreen(upperBound, coll, 200);
					setGreen(lowerBound, coll, 200);
				}
			}

			//repaint markers
			int negSum = 0;
			for (Point point : negControl.points){
				setRGB(point.y, point.x, 0, 255, 0);
				negSum += oldWindow.getRed(point.y, point.x);
			}
			double negAvg = 0;
			if (negControl.points.size() != 0){
				negAvg = (double)negSum/(double)negControl.points.size();
			}

			strip.neg = negAvg;
			strip.pos = posAvg;
			strip.tes = testAvg;
			strip.deciResult = (testAvg-negAvg)/(posAvg - negAvg);
		}


		for (Strip strip : strips){
			strip.printStrip();
		}
	}

	public Strip getNextStrip(Strip currentStrip, int stripWidth, int groveOffSet, int markerColorDiff){

		Point currentCenter = currentStrip.artificialMarker.findCentroid();

		int YOffSet;

		if ((currentStrip.index % 2) == 0){
			YOffSet = groveOffSet/2;
		}
		else{
			YOffSet = -groveOffSet/2;
		}

		int row = currentCenter.y + YOffSet;
		Region newAM = null;

		for (int rowRange = row - stripWidth/4; rowRange < row + stripWidth/4; rowRange ++){
			newAM = floodRBRegion(currentCenter.x + stripWidth, rowRange, 255, 255, markerColorDiff);	
			if (newAM.points.size() > minRegionArea){
				markerRegions.add(newAM);
				break; //need to think more on this to avoid breaking
			}
		}

		for (int rowRange = row - stripWidth/4; rowRange < row + stripWidth/4; rowRange ++){
			setGreen(rowRange, currentCenter.x + stripWidth, 255);
		}

		int newIndex = currentStrip.index + 1;
		Strip newStrip = new Strip(newIndex, newAM);
		if (markerRegions.size() > currentStrip.index + 1){
			System.out.println("found second am");
			return newStrip;
		}
		else{
			return null;
		}
	}
}
