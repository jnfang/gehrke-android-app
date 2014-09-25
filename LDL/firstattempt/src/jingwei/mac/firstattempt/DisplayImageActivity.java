package jingwei.mac.firstattempt;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class DisplayImageActivity extends Activity {

	private Bitmap oldbits, bits;
	private ImageView imageView;
	private RGBImage rgb, oldWindow, newWindow;
	private boolean buttonSwitch = false;
	private String absolutePath = "";

	private SeekBar redControl = null;
	private int RBBoost = 30;

	private SeekBar sideSquareRatioBar = null;
	private int sideSquareRatio = 20;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//reset orientation to portrait in case changes occurred between activities
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		Intent intent = getIntent();
		bits = (Bitmap)intent.getParcelableExtra(MainActivity.imageToBeAnalyzed);	
		absolutePath = intent.getStringExtra("imagePath");
		setContentView(R.layout.activity_display_image);

		//if coming back from the next activity
		if(savedInstanceState != null){
			absolutePath = savedInstanceState.getString("path");
			buttonSwitch = savedInstanceState.getBoolean("switch button");
		}
		else{
			intent = getIntent();
			bits = (Bitmap)intent.getParcelableExtra(MainActivity.imageToBeAnalyzed);	
			absolutePath = intent.getStringExtra("imagePath");
			// Get the data carried by the intent
		}

		//Switch button text after being clicked once
		Button analyzeButton = (Button)findViewById(R.id.analyze);
		if (buttonSwitch){
			analyzeButton.setText("markers are correct");
		}

		imageView = (ImageView) findViewById(R.id.image_display);
		imageView.setImageBitmap(bits);
	}


	//method should be refactored
	public void analyzeImage(View view){

		//configure seek bar to determine the threshold value to be used for
		//determining the results of test markers.  Right now this is a RBBoost value, need to change
		Button analyzeButton = (Button)findViewById(R.id.analyze);
		redControl = (SeekBar)findViewById(R.id.seek);
		redControl.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
				RBBoost = progress;
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
				Toast.makeText(DisplayImageActivity.this,"seek bar progress:" + RBBoost, 
						Toast.LENGTH_SHORT).show();
			}
		});

		
		//This second seekbar is commented out for now. It was originally intended to make the location of
		//the fifth black marker more flexible. But if the casing design becomes final, this step is unnecessary.
//		sideSquareRatioBar = (SeekBar)findViewById(R.id.square_ratio);
//		sideSquareRatioBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
//			public void onProgressChanged(SeekBar sideSquareRatioBar, int progress, boolean fromUser){
//				sideSquareRatio = progress;
//			}
//
//			public void onStartTrackingTouch(SeekBar sideSquareRatioBar) {
//			}
//
//			public void onStopTrackingTouch(SeekBar sideSquareRatioBar) {
//				Toast.makeText(DisplayImageActivity.this,"side square ratio bar progress:" + sideSquareRatio, 
//						Toast.LENGTH_SHORT).show();
//			}
//		});

		//simply display image if analyze button is not pressed
		if (!buttonSwitch){
			buttonSwitch = true;
			
			//Bitmap => RGBImage
			bits = bits.copy(bits.getConfig(), true);
			rgb = new RGBImage(bits.getWidth(), bits.getHeight());

			bitmapToRGB(bits, rgb);

			boolean markersFound = rgb.getMarkers();

			if (!markersFound){
				Toast.makeText(getApplicationContext(),
						getString(R.string.markers_not_found_message), Toast.LENGTH_SHORT).show();
				finish();
			}

			rgb.repaintMarkers(0, 255, 0);  //repaint black markers found to green
			
			//RGBImage => Bitmap
			RGBToBitmap(rgb, bits);
			imageView.setImageBitmap(bits);
			analyzeButton.setText("markers are correct");  //prompt the user to confirm the markers found are correct
		}
		
		//when button is pressed, analyze image
		else{ 
			
			//stretch out and reorient the image
			rgb.appointCorners();
			rgb.repaintP2();	
			rgb.setOrientation();	
			oldWindow = rgb.stretchEdgesAndGetWindow();	

			oldWindow = RGBImage.resizeRGBImage(oldWindow, 500, 400);
			newWindow = oldWindow.cloneRGBImage();
			int P2top = newWindow.getP2TopEdge();
			int P2bottom = newWindow.getP2BottomEdge();

			newWindow.redistributeVertically(P2top, P2bottom);
			oldWindow.redistributeVertically(P2top, P2bottom);
			P2top = newWindow.getP2TopEdge();
			P2bottom = newWindow.getP2BottomEdge();

			oldbits = Bitmap.createScaledBitmap (bits, newWindow.getImageWidth(), newWindow.getImageHeight(), false);
			RGBToBitmap(newWindow, oldbits);

			double padMultiplier = 1.0;

			newWindow.removeGrey(padMultiplier);
			oldWindow.removeGrey(padMultiplier);

			newWindow.removeVerticalNoise((double)(3.0/16.0), (double)(5.0/16.0));

			newWindow.boostRB(RBBoost);

			Point start = new Point(newWindow.getP2RightEdge(P2top, P2bottom), P2bottom);
			newWindow.setRGB(start.y, start.x, 0, 0, 255);


			newWindow.findArtificialMarkers(start, oldWindow);

			bits = Bitmap.createScaledBitmap (bits, newWindow.getImageWidth(), newWindow.getImageHeight(), false);

			RGBToBitmap(newWindow, bits);

			setContentView(R.layout.activity_show_results);
			ImageView resultView = (ImageView) findViewById(R.id.result_display);
			resultView.setImageBitmap(oldbits);
			
			
			
//			//stretch out and reorient the image
//			rgb.appointCorners();
//			rgb.repaintP2();	
//			rgb.setOrientation();	
//			rgb.stretchEdges();	
//
//			rgb.getWindow().resizeRGBImage(500, 400);
//			rgb.getWindow().oldWindow.resizeRGBImage(500, 400);
//
//			int P2top = rgb.getWindow().getP2TopEdge();
//			int P2bottom = rgb.getWindow().getP2BottomEdge();
//
//			rgb.getWindow().redistributeVertically(P2top, P2bottom);
//			rgb.getWindow().oldWindow.redistributeVertically(P2top, P2bottom);
//			P2top = rgb.getWindow().getP2TopEdge();
//			P2bottom = rgb.getWindow().getP2BottomEdge();
//
//			oldbits = Bitmap.createScaledBitmap (bits, rgb.getWindow().getImageWidth(), rgb.getWindow().getImageHeight(), false);
//			RGBToBitmap(rgb.getWindow(), oldbits);
//
//			double padMultiplier = 1.0;
//
//			rgb.getWindow().removeGrey(padMultiplier);
//			rgb.getWindow().oldWindow.removeGrey(padMultiplier);
//
//			rgb.getWindow().removeVerticalNoise((double)(3.0/16.0), (double)(5.0/16.0));
//
//			rgb.getWindow().boostRB(RBBoost);
//
//			Point start = new Point(rgb.getWindow().getP2RightEdge(P2top, P2bottom), P2bottom);
//			rgb.getWindow().setRGB(start.y, start.x, 0, 0, 255);
//
//
//			rgb.getWindow().findArtificialMarkers(start);
//
//			bits = Bitmap.createScaledBitmap (bits, rgb.getWindow().getImageWidth(), rgb.getWindow().getImageHeight(), false);
//
//			RGBToBitmap(rgb.getWindow(), bits);
//
//			setContentView(R.layout.activity_show_results);
//			ImageView resultView = (ImageView) findViewById(R.id.result_display);
//			resultView.setImageBitmap(oldbits);
		}
	}

	public void toStringView(View view){
		setContentView(R.layout.display_strings);
	}
	
	//results for positive control lane
	public void setPosString(View view){
		TextView text =  (TextView) findViewById(R.id.text_results);
		String s = "";
		if (newWindow.strips != null){
			for (Strip strip : newWindow.strips){
				s = s.concat(strip.posToString());
				s = s.concat("\n");
			}
		}
		text.setText(s);
		text.invalidate();
	}

	//results for negative control lane
	public void setNegString(View view){
		TextView text =  (TextView) findViewById(R.id.text_results);
		String s = "";
		if (newWindow.strips != null){
			for (Strip strip : newWindow.strips){
				s = s.concat(strip.negToString());
				s = s.concat("\n");
			}
		}
		System.out.println(s);
		text.setText(s);
		text.invalidate();
	}

	//results for testing lane
	public void setTestString(View view){
		TextView text =  (TextView) findViewById(R.id.text_results);
		String s = "";
		if (newWindow.strips != null){

			for (Strip strip : newWindow.strips){
				s = s.concat(strip.testToString());
				s = s.concat("\n");
			}
		}
		text.setText(s);
		text.invalidate();
	}

	//Bitmap=>RGBImage
	private static void bitmapToRGB(Bitmap bits, RGBImage rgb){

		for (int i = 0; i<bits.getHeight(); i++){
			for (int j = 0; j<bits.getWidth(); j++){
				int color = bits.getPixel(j, i);
				rgb.setRGB(i, j, Color.red(color), Color.green(color), Color.blue(color));
			}
		}
	}

	//RGBImage=>Bitmap
	private static void RGBToBitmap(RGBImage rgb, Bitmap bits){
		for (int i = 0; i< rgb.getImageHeight(); i++){
			for (int j = 0; j<rgb.getImageWidth(); j++){
				int color = Color.rgb(rgb.getRed(i, j), rgb.getGreen(i, j), rgb.getBlue(i, j));
				bits.setPixel(j, i, color);
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle toSave) {
		super.onSaveInstanceState(toSave);
		toSave.putBoolean("switch button", buttonSwitch);
		toSave.putString("path", absolutePath);
	}


	//package up image and analysis results and start GPS activity
	public void getGPS(View view){

		Intent i = new Intent(this, GPSActivity.class);
		i.putExtra("imagePath", absolutePath);

		i.putExtra("s1", newWindow.strips.get(0).deciResult);
		i.putExtra("s2", newWindow.strips.get(1).deciResult);
		i.putExtra("s3", newWindow.strips.get(2).deciResult);
		i.putExtra("s4", newWindow.strips.get(3).deciResult);
		i.putExtra("s5", newWindow.strips.get(4).deciResult);
		i.putExtra("s6", newWindow.strips.get(5).deciResult);
		i.putExtra("s7", newWindow.strips.get(6).deciResult);
		i.putExtra("s8", newWindow.strips.get(7).deciResult);

		startActivity(i);
		this.finish();
	}
}
