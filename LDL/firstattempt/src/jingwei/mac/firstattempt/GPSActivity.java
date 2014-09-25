package jingwei.mac.firstattempt;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;


import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
public class GPSActivity extends Activity {

	TextView gpsText;
	String latLongString;
	double lat, lon;
	double s1, s2, s3, s4, s5, s6, s7, s8;
	@Override 
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		String imagePath = intent.getStringExtra("imagePath");
		s1 = intent.getDoubleExtra("s1", 0);
		s2 = intent.getDoubleExtra("s2", 0);
		s3 = intent.getDoubleExtra("s3", 0);
		s4 = intent.getDoubleExtra("s4", 0);
		s5 = intent.getDoubleExtra("s5", 0);
		s6 = intent.getDoubleExtra("s6", 0);
		s7 = intent.getDoubleExtra("s7", 0);
		s8 = intent.getDoubleExtra("s8", 0);

		System.out.println("what");
		System.out.println(imagePath);
		setContentView(R.layout.activity_gps);
		gpsText = (TextView)findViewById(R.id.gpstext);
		System.out.println("he");


		LocationManager locationManager;
		String svcName = Context.LOCATION_SERVICE;
		locationManager = (LocationManager)getSystemService(svcName);

		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setSpeedRequired(false);
		criteria.setCostAllowed(true);

		String provider = locationManager.getBestProvider(criteria, true);

		Location l = locationManager.getLastKnownLocation(provider);

		lat = 0.0;
		lon = 0.0;
		if (l != null) {
			lat = l.getLatitude();
			lon = l.getLongitude();
		}

		locationManager.requestLocationUpdates(provider, 2000, 10,
				locationListener);
		System.out.println("gps: "+ lat);
		//		sendGPSData();
	}



	// When user clicks button, calls AsyncTask.
	// Before attempting to fetch the URL, makes sure that there is a network connection.
	public void getConnection(View view) {
		// Gets the URL from the UI's text field.
		//        String stringUrl = urlText.getText().toString();
		ConnectivityManager connMgr = (ConnectivityManager) 
				getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			new uploadData().execute("");
		} else {
			gpsText.setText("No network connection available.");
		}
	}

	// Uses AsyncTask to create a task away from the main UI thread. This task takes a 
	// URL string and uses it to create an HttpUrlConnection. Once the connection
	// has been established, the AsyncTask downloads the contents of the webpage as
	// an InputStream. Finally, the InputStream is converted into a string, which is
	// displayed in the UI by the AsyncTask's onPostExecute method.
	private class uploadData extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			sendGPSData();
			// params comes from the execute() call: params[0] is the url.
			return "done";

		}
		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(String result) {
			gpsText.setText(result);
		}
	}
	private void sendGPSData(){
		Intent intent = getIntent();
		String imagePath = intent.getStringExtra("imagePath");
		System.out.println("what");
		System.out.println(imagePath);
		String url = "http://www.djangotestalpha.com/myproject/transfer/";

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("latitude", (Double.toString(lat))));
		nameValuePairs.add(new BasicNameValuePair("longitude", (Double.toString(lon))));
		nameValuePairs.add(new BasicNameValuePair("s1", (Double.toString(s1))));
		nameValuePairs.add(new BasicNameValuePair("s2", (Double.toString(s2))));
		nameValuePairs.add(new BasicNameValuePair("s3", (Double.toString(s3))));
		nameValuePairs.add(new BasicNameValuePair("s4", (Double.toString(s4))));
		nameValuePairs.add(new BasicNameValuePair("s5", (Double.toString(s5))));
		nameValuePairs.add(new BasicNameValuePair("s6", (Double.toString(s6))));
		nameValuePairs.add(new BasicNameValuePair("s7", (Double.toString(s7))));
		nameValuePairs.add(new BasicNameValuePair("s8", (Double.toString(s8))));
		nameValuePairs.add(new BasicNameValuePair("image", imagePath));

		HttpClient httpClient = new DefaultHttpClient();
		HttpContext localContext = new BasicHttpContext();
		HttpPost httpPost = new HttpPost(url);

		try { 
			MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			for(int index=0; index < nameValuePairs.size(); index++) {
				if(nameValuePairs.get(index).getName().equalsIgnoreCase("image")) {
					// If the key equals to "image", we use FileBody to transfer the data
					entity.addPart(nameValuePairs.get(index).getName(), new FileBody(new File (nameValuePairs.get(index).getValue())));
				} else {
					// Normal string data
					entity.addPart(nameValuePairs.get(index).getName(), new StringBody(nameValuePairs.get(index).getValue()));
				}
			}

			httpPost.setEntity(entity);

			HttpResponse response = httpClient.execute(httpPost, localContext);
			String responseHTML = EntityUtils.toString(response.getEntity());
			System.out.println(responseHTML);


			//old browser code
			Intent i = new Intent();
			//
			// MUST instantiate android browser, otherwise it won't work
			i.setComponent(new ComponentName("com.android.browser", "com.android.browser.BrowserActivity"));
			i.setAction(Intent.ACTION_VIEW);

			// May work without url encoding, but I think is advisable
			// URLEncoder.encode replace space with "+", must replace again with %20
			String dataUri = "data:text/html," + URLEncoder.encode(responseHTML).replaceAll("\\+","%20");
			i.setData(Uri.parse(dataUri));

			startActivity(i);


			//new browser code
//			Intent browserIntent = new Intent(Intent.ACTION_VIEW);
//			String dataUri = "data:text/html," + URLEncoder.encode(responseHTML).replaceAll("\\+","%20");
//			Uri uri = Uri.parse(dataUri);
//			browserIntent.setDataAndType(uri,  "text/html");
//			browserIntent.addCategory(Intent.CATEGORY_BROWSABLE);
//			startActivity(browserIntent);
			
		} catch (IOException e) {
			e.printStackTrace();
		}


		//	    // Create a new HttpClient and Post Header
		//	    HttpClient httpclient = new DefaultHttpClient();
		//	    HttpPost httppost = new HttpPost("http://www.djangotestalpha.com/myproject/transfer/");
		//
		//	    try {
		//	        // Add your data
		//	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		//	        nameValuePairs.add(new BasicNameValuePair("latitude", "12345"));
		//	        nameValuePairs.add(new BasicNameValuePair("longitude", "54321"));
		//	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		//
		//	        // Execute HTTP Post Request
		//	        HttpResponse response = httpclient.execute(httppost);
		//	        String responseHTML = EntityUtils.toString(response.getEntity());
		//	        System.out.println(responseHTML);
		//
		//	        
		//	        
		//	        Intent i = new Intent();
		//
		//	     // MUST instantiate android browser, otherwise it won't work
		//	     i.setComponent(new ComponentName("com.android.browser", "com.android.browser.BrowserActivity"));
		//	     i.setAction(Intent.ACTION_VIEW);
		//
		//	     // May work without url encoding, but I think is advisable
		//	     // URLEncoder.encode replace space with "+", must replace again with %20
		//	     String dataUri = "data:text/html," + URLEncoder.encode(responseHTML).replaceAll("\\+","%20");
		//	     i.setData(Uri.parse(dataUri));
		//
		//	     startActivity(i);
		//	        System.out.println(responseHTML);
		//	    } catch (ClientProtocolException e) {
		//	        // TODO Auto-generated catch block
		//	    } catch (IOException e) {
		//	        // TODO Auto-generated catch block
		//	    }
	}

	private final LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			if (location != null) {
				lat = location.getLatitude();
				lon = location.getLongitude();
			}
		}

		public void onProviderDisabled(String provider) {}
		public void onProviderEnabled(String provider) {}
		public void onStatusChanged(String provider, int status, 
				Bundle extras) {}
	};

}

