package jingwei.mac.firstattempt;


public class RGB {
	
	protected Matrix redMat;
	protected Matrix blueMat;
	protected Matrix greenMat;
	protected Matrix blank;
	protected int imageWidth, imageHeight;

	public RGB(int width, int height){
		imageWidth = width;
		imageHeight = height;
		redMat = new Matrix(imageHeight, imageWidth);
		greenMat = new Matrix(imageHeight, imageWidth);
		blueMat = new Matrix(imageHeight, imageWidth);
		blank = new Matrix(imageHeight, imageWidth);
	}

	public void destroy(){
		redMat = null;
		greenMat = null;
		blueMat = null;
		blank = null;
	}

	public int getImageWidth(){
		return imageWidth;
	}

	public int getImageHeight(){
		return imageHeight;
	}

	protected void setBlank(int row, int col, int val){
		blank.put(row, col, val);
	}

	protected void setRed(int row, int col, int val){
		redMat.put(row, col, val);
	}

	protected void setGreen(int row, int col, int val){
		greenMat.put(row, col, val);
	}

	protected void setBlue(int row, int col, int val){
		blueMat.put(row, col, val);
	}

	protected void setRGB(int row, int col, int red, int green, int blue){
		redMat.put(row, col, red);
		greenMat.put(row, col, green);
		blueMat.put(row, col, blue);
	}

	protected int getBlank(int row, int col){
		return blank.get(row, col);
	}

	protected int getRed(int row, int col){
		return redMat.get(row, col);
	}

	protected int getGreen(int row, int col){
		return greenMat.get(row, col);
	}

	protected int getBlue(int row, int col){
		return blueMat.get(row, col);
	}
	
	protected Matrix getRedMat(){
		return redMat;
	}
	
	protected Matrix getGreenMat(){
		return greenMat;
	}
	
	protected Matrix getBlueMat(){
		return blueMat;
	}

	protected void rotate90CW(){
		Matrix newBlank = new Matrix(imageWidth, imageHeight);
		Matrix red = new Matrix(imageWidth, imageHeight);
		Matrix green = new Matrix(imageWidth, imageHeight);
		Matrix blue = new Matrix(imageWidth, imageHeight);

		for (int row = 0; row < imageWidth; row ++){
			for (int col = 0; col < imageHeight; col ++){
				newBlank.put(row, col, blank.get(imageHeight-1-col, row));
				red.put(row, col, redMat.get(imageHeight-1-col, row));
				green.put(row, col, greenMat.get(imageHeight-1-col, row));
				blue.put(row, col, blueMat.get(imageHeight-1-col, row));
			}
		}

		redMat = red;
		greenMat = green;
		blueMat = blue;
		blank = newBlank;

		int temp = imageWidth;
		imageWidth = imageHeight;
		imageHeight = temp;
	}

	protected void flipVertical(){
		Matrix newBlank = new Matrix(imageHeight, imageWidth);
		Matrix red = new Matrix(imageHeight, imageWidth);
		Matrix green = new Matrix(imageHeight, imageWidth);
		Matrix blue = new Matrix(imageHeight, imageWidth);

		for (int row = 0; row < imageHeight; row ++){
			for (int col = 0; col < imageWidth; col ++){
				newBlank.put(row, col, blank.get(imageHeight-1-row, col));
				red.put(row, col, redMat.get(imageHeight-1-row, col));
				green.put(row, col, greenMat.get(imageHeight-1-row, col));
				blue.put(row, col, blueMat.get(imageHeight-1-row, col));
			}
		}

		redMat = red;
		greenMat = green;
		blueMat = blue;
		blank = newBlank;
	}

	protected void flipHorizontal(){
		Matrix newBlank = new Matrix(imageHeight, imageWidth);
		Matrix red = new Matrix(imageHeight, imageWidth);
		Matrix green = new Matrix(imageHeight, imageWidth);
		Matrix blue = new Matrix(imageHeight, imageWidth);

		for (int row = 0; row < imageHeight; row ++){
			for (int col = 0; col < imageWidth; col ++){
				newBlank.put(row, col, blank.get(row, imageWidth-col-1));
				red.put(row, col, redMat.get(row, imageWidth-col-1));
				green.put(row, col, greenMat.get(row, imageWidth-col-1));
				blue.put(row, col, blueMat.get(row, imageWidth-col-1));
			}
		}

		redMat = red;
		greenMat = green;
		blueMat = blue;
		blank = newBlank;
	}

	protected RGB cloneRGB(){
		RGB newRGB = new RGB(imageWidth, imageHeight);
		newRGB.redMat = redMat.cloneMatrix();
		newRGB.greenMat = greenMat.cloneMatrix();
		newRGB.blueMat = blueMat.cloneMatrix();
		newRGB.blank = blank.cloneMatrix();
		return newRGB;
	}

	protected void resetBlank(){
		blank = new Matrix(imageHeight, imageWidth);
	}

	protected void compressImage(int shortSide){
		double scale = (double)shortSide/(double)Math.min(imageWidth, imageHeight);
		imageWidth = (int)(scale*(double)imageWidth);
		imageHeight = (int)(scale*(double)imageHeight);
		redMat = redMat.compressMatrix(imageHeight, imageWidth);
		greenMat = greenMat.compressMatrix(imageHeight, imageWidth);
		blueMat = blueMat.compressMatrix(imageHeight, imageWidth);
		blank = blank.compressMatrix(imageHeight, imageWidth);
	}

	protected static RGB resizeRGB(RGB rgb, int newWidth, int newHeight){
		RGB newRGB = rgb.cloneRGB();
		newRGB.redMat = rgb.redMat.compressMatrix(newHeight, newWidth);
		newRGB.greenMat = rgb.greenMat.compressMatrix(newHeight, newWidth);
		newRGB.blueMat = rgb.blueMat.compressMatrix(newHeight, newWidth);
		newRGB.blank = rgb.blank.compressMatrix(newHeight, newWidth);

		newRGB.imageWidth = newWidth;
		newRGB.imageHeight = newHeight;
		return newRGB;
	}

	protected void subRGBImage(int ceiling, int floor, int left, int right){
		redMat = redMat.subMatrix(ceiling, floor, left, right);
		greenMat = greenMat.subMatrix(ceiling, floor, left, right);
		blueMat = blueMat.subMatrix(ceiling, floor, left, right);
		blank = blank.subMatrix(ceiling, floor, left, right);

		imageWidth = right - left + 1;
		imageHeight = floor - ceiling + 1;
	}

}

