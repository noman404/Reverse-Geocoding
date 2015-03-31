package com.example.applocatoinrevgeocod;

import android.app.Activity;
import android.app.ProgressDialog;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends Activity implements ConnectionCallbacks,
		OnConnectionFailedListener, LocationListener {
	ProgressDialog pb;

	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

	public Location mLastLocation;

	private GoogleApiClient mGoogleApiClient;

	private boolean mRequestingLocationUpdates = false;

	private LocationRequest mLocationRequest;

	private static int UPDATE_INTERVAL = 10000; // 10 sec
	private static int FATEST_INTERVAL = 5000; // 5 sec
	private static int DISPLACEMENT = 1; // 1 meters

	private TextView location;
	private Button btnShowLocation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		location = (TextView) findViewById(R.id.lblLocation);
		btnShowLocation = (Button) findViewById(R.id.btnShowLocation);
		if (checkPlayServices()) {

			buildGoogleApiClient();

			createLocationRequest();
		}
		btnShowLocation.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				displayLocation();
				togglePeriodicLocationUpdates();

			}
		});

	}

	@Override
	protected void onStart() {
		super.onStart();
		if (mGoogleApiClient != null) {
			mGoogleApiClient.connect();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		checkPlayServices();

		if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
			startLocationUpdates();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		stopLocationUpdates();
	}

	private void displayLocation() {

		mLastLocation = LocationServices.FusedLocationApi
				.getLastLocation(mGoogleApiClient);

		if (mLastLocation != null) {
			double latitude = mLastLocation.getLatitude();
			double longitude = mLastLocation.getLongitude();
			location.setText("lat-lng: " + latitude + ", " + longitude);
			new GetAddress().execute(latitude, longitude);
		} else {

			location
					.setText("(Couldn't get the location. Make sure location is enabled on the device)");
		}
	}

	class GetAddress extends AsyncTask<Double, Void, Void> {

		String result = null;

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			pb.dismiss();
			try {
				location.append("\nAddress: "
						+ myAddresFormat(Xmlparse.str1, Xmlparse.str2));
			} catch (IndexOutOfBoundsException e) {

			} catch (Exception e) {

			}
		}

		public String myAddresFormat(String s1, String s2)
				throws IndexOutOfBoundsException, Exception {

			final String splitter = ", ";
			String address[] = s2.split(splitter);

			return address[0] + ", " + s1;
		}

		public String myAddresFormat(String s)
				throws IndexOutOfBoundsException, Exception {

			String spliter = ",";
			String address[] = s.split(spliter);

			return address[0] + spliter + address[1] + spliter + address[2];
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pb = new ProgressDialog(MainActivity.this);
			pb.setTitle("Recognizing your address...");
			pb.show();

		}

		@Override
		protected Void doInBackground(Double... params) {

			Xmlparse parse = new Xmlparse();

			parse.execute("" + params[0], "" + params[1]);

			return null;
		}

	}

	private void togglePeriodicLocationUpdates() {
		if (!mRequestingLocationUpdates) {

			mRequestingLocationUpdates = true;

			startLocationUpdates();

		}
	}

	protected synchronized void buildGoogleApiClient() {
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API).build();
	}

	protected void createLocationRequest() {
		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(UPDATE_INTERVAL);
		mLocationRequest.setFastestInterval(FATEST_INTERVAL);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
	}

	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Toast.makeText(getApplicationContext(),
						"This device is not supported.", Toast.LENGTH_LONG)
						.show();
				finish();
			}
			return false;
		}
		return true;
	}

	protected void startLocationUpdates() {

		LocationServices.FusedLocationApi.requestLocationUpdates(
				mGoogleApiClient, mLocationRequest, this);

	}

	protected void stopLocationUpdates() {
		LocationServices.FusedLocationApi.removeLocationUpdates(
				mGoogleApiClient, this);
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Toast.makeText(getApplicationContext(), "Connection Failed",
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onConnected(Bundle arg0) {

		// Once connected with google api, get the location
		displayLocation();

		if (mRequestingLocationUpdates) {
			startLocationUpdates();
		}
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		mGoogleApiClient.connect();
	}

	@Override
	public void onLocationChanged(Location location) {
		mLastLocation = location;

		displayLocation();
	}

}