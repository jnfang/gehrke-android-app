package processing_algorithm;
import processing.core.*;


public class PrimitiveProcessing extends PApplet {
	// PImage
	PImage img;  

	public void setup() {
	  size(640, 360);
	  // Load image into file
	  img = loadImage("../img/test-image.jpg");  
	}

	public void draw() {
	  // Displays the image at its actual size at point (0,0)
	  image(img, 0, 0);
	}
	
	public static void main(String args[]) {
	    PApplet.main(new String[] { "--present", "PrimitiveProcessing" });
	}
}
