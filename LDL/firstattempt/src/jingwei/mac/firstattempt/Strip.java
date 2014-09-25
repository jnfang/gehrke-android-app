package jingwei.mac.firstattempt;


public class Strip {
	public int index;
	public Region artificialMarker;
	boolean positiveControl, test, negativeControl;
	public boolean valid, result;
	public Point stripAnchor, posAnchor;
	public double blank, pos, neg, tes;
	double deciResult;
	
	public Strip(int stripIndex, Region AMRegion){
		index = stripIndex;
		artificialMarker = AMRegion;
		stripAnchor = artificialMarker.findCenter();
		positiveControl = false;
		test = false;
		negativeControl = false;
		valid = false;
		result = false;
	}
	
	public void printStrip(){
		System.out.println("\nResults for Strip "+ index + ":");
		if (positiveControl == true){
			System.out.println("Positive Control: VALID" + pos);
		}
		else{
			System.out.println("Positive Control: INVALID" + pos);
		}
		
		if (negativeControl == false){
			System.out.println("Negative Control: VALID" + neg);
		}
		else{
			System.out.println("Negative Control: INVALID" + neg);
		}
		
		if (test == true){
			System.out.println("TEST RESULT: POSITIVE" + tes);
		}
		else{
			System.out.println("TEST RESULT: NEGATIVE" + tes);
		}
	}
	
	public String posToString(){
		if (positiveControl == true){
			return ("Positive Control for Strip " + index + ": VALID");
		}
		else{
			return ("Positive Control for Strip " + index + ": INVALID");
		}
	}
	
	public String negToString(){
		if (negativeControl == false){
			return ("Negative Control for Strip " + index + ": VALID");
		}
		else{
			return ("Negative Control for Strip " + index + ": INVALID");
		}
	}
	
	public String testToString(){
		if (test == true){
			return ("TEST RESULT for Strip " + index + ": POSITIVE");
		}
		else{
			return("TEST RESULT for Strip " + index + ": NEGATIVE");
		}
	}
	//public void get
	
	
}
