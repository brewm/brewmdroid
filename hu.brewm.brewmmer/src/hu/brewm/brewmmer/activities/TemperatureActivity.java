package hu.brewm.brewmmer.activities;

import hu.brewm.brewmmer.R;
import hu.brewm.brewmmer.Utils;

import java.text.DateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class TemperatureActivity extends ActionBarActivity {
	private static final String resourceUrl = "http://192.168.1.110:3551/temperature";
	
	private TextView currentTemperature;
	private TextView currentTime;

	private Handler handler;
	private Runnable refresh;
	private Integer refresh_interval;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_temperature);

		handler = new Handler();

		currentTemperature = (TextView) findViewById(R.id.temperature_value);
		currentTime = (TextView) findViewById(R.id.timestamp);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		prefs.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				refresh_interval = Integer.parseInt(sharedPreferences.getString("refresh_interval", "10"));

				Toast.makeText(getBaseContext(), "Refresh interval updated!", Toast.LENGTH_LONG).show();
			}
		});

		refresh_interval = Integer.parseInt(prefs.getString("refresh_interval", "10"));

		refresh = new Runnable() {
			public void run() {
				// call AsynTask to perform network operation on separate thread
				new HttpAsyncTask().execute(resourceUrl);

				handler.postDelayed(refresh, refresh_interval * 1000);
			}
		};
		handler.post(refresh);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.temperature, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent i = new Intent(this, SettingsActivity.class);
			startActivity(i);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onStop() {
		handler.removeCallbacks(refresh);
		super.onStop();
	}
	
	@Override
	protected void onRestart() {
		handler.post(refresh);
		super.onRestart();
	}

	@Override
	protected void onPause() {
		handler.removeCallbacks(refresh);
		super.onPause();
	}

	@Override
	protected void onResume() {
		handler.post(refresh);
		super.onResume();
	}

	private class HttpAsyncTask extends AsyncTask<String, Void, String> {
		private static final String TIMESTAMP = "timestamp";
		private static final String TEMPERATURE = "temperature";

		@Override
		protected String doInBackground(String... urls) {
			return Utils.getGET(urls[0]);
		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(String result) {
			Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_LONG).show();
			try {
				JSONObject measurement = new JSONObject(result);

				currentTemperature.setText(measurement.get(TEMPERATURE).toString());
				Date now = new Date(Long.parseLong(measurement.get(TIMESTAMP).toString()));
				currentTime.setText(DateFormat.getTimeInstance(DateFormat.MEDIUM).format(now));

			} catch (JSONException e) {
				Log.d("InputStream", e.getLocalizedMessage());
			}
		}
	}
}
