package jingwei.mac.firstattempt;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Menu;
import android.view.View;

//On creation activity of app
public class MainActivity extends Activity {
	public final static String TAG = "DIYDiagnostics";
	private final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 567; //arbitrarily defined
	private final static int CHOOSE_IMAGE_ACTIVITY_REQUEST_CODE = 321; //arbitrarily defined
	public final static String imageToBeAnalyzed = "Image";
	private final int MAX_IMAGE_SIZE = 1000000;

	private int inSampleSize = 1; //used continuously for resizing the image
	private String absolutePath = "";
	private Uri selectedImage;

	//initialize app to be oriented in portrait
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		inSampleSize = 1;
		
		//image resizing code has been tested on 2.3.6, 4.3.0 and 4.4.2
		if(resultCode != RESULT_CANCELED){ //has result
			if ((requestCode == CHOOSE_IMAGE_ACTIVITY_REQUEST_CODE  || requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) 
					&& resultCode == RESULT_OK ){ //if either button is pressed

				//if choosing an image from gallery, we need to extract the image uri, this is
				//not necessary for taking a photo because selectedImage is already set in ActivateCamera
				if (requestCode == CHOOSE_IMAGE_ACTIVITY_REQUEST_CODE){
					selectedImage = data.getData();
				}

				//setup for resizing the image
				String[] filePathColumn = { MediaStore.Images.Media.DATA };
				Cursor cursor = getContentResolver().query(selectedImage,
						filePathColumn, null, null, null);
				cursor.moveToFirst();
				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				String picturePath = cursor.getString(columnIndex);
				cursor.close();
				Bitmap bits = null;
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;				
				options.inSampleSize = inSampleSize;

				//resize image until it is smaller than the maximum carry size of android
				do{
					inSampleSize *=2;
					options.inSampleSize = inSampleSize;
					try {
						BitmapFactory.decodeStream(new FileInputStream(picturePath),null,options);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				while(options.outHeight*options.outWidth > MAX_IMAGE_SIZE);  //bitMap needs to be smaller than 999 KB
				
				options.inJustDecodeBounds = false;
				
				try {
					bits = BitmapFactory.decodeStream(new FileInputStream(picturePath),null,options);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				//this while loop was added because some android os are not compatible with the do/while loop above
				//if a more encompassing method is found to resize, both these loops should be replaced
				while (getSizeInBytes(bits)>999000){
					options.inSampleSize *= 2;
					try {
						bits = BitmapFactory.decodeStream(new FileInputStream(picturePath),null,options);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				//pack image and absolute path into intent, start DisplayImageActivity
				Intent intent = new Intent(this, DisplayImageActivity.class);
				intent.putExtra(imageToBeAnalyzed, bits);
				
		        String[] proj = { MediaStore.Images.Media.DATA };
		        @SuppressWarnings("deprecation")
				Cursor cursor1 = managedQuery(selectedImage, proj, null, null, null);
		        int column_index = cursor1.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		        cursor1.moveToFirst();
		        absolutePath = cursor1.getString(column_index);
				
				intent.putExtra("imagePath", absolutePath);
				startActivity(intent);
			}
		}
	}

	//called when "Choose an Image" button is pressed
	public void chooseImage(View view){
		Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(i, CHOOSE_IMAGE_ACTIVITY_REQUEST_CODE);
	}
	
	//called when "Take Photo" button is pressed
	public void activateCamera(View view){		
		Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		
		ContentValues values = new ContentValues();
		values.put(MediaStore.Images.Media.TITLE, "picture picked");
		selectedImage = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
		i.putExtra(MediaStore.EXTRA_OUTPUT, selectedImage);
		
		// start the image capture Intent
		startActivityForResult(i, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
	}
	
	public void getGPS(View view){
		Intent i = new Intent(this, GPSActivity.class);
		startActivity(i);
	} 
	 
	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    if (selectedImage != null) {
	        outState.putString("cameraImageUri", selectedImage.toString());
	    }
	}

	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
	    super.onRestoreInstanceState(savedInstanceState);
	    if (savedInstanceState.containsKey("cameraImageUri")) {
	        selectedImage = Uri.parse(savedInstanceState.getString("cameraImageUri"));
	    }
	}
	
	//returns the bytes occupied of a given Bitmap object
	public static long getSizeInBytes(Bitmap bitmap) {
	        return bitmap.getRowBytes() * bitmap.getHeight();
	} 
}
