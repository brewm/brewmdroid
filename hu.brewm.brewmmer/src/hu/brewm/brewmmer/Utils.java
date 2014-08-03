package hu.brewm.brewmmer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

public class Utils {
	
	public static String getGET(String url) {
		InputStream responseStream = null;
		String response = "";
		try {
			responseStream = new DefaultHttpClient().execute(new HttpGet(url)).getEntity().getContent();

			if (responseStream != null)
				response = convertInputStreamToString(responseStream);

		} catch (Exception e) {
			Log.d("InputStream", e.getLocalizedMessage());
		}
		return response;
	}
	
	private static String convertInputStreamToString(InputStream inputStream) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		String line = "";
		String result = "";
		while ((line = bufferedReader.readLine()) != null){
			result += line;
		}

		inputStream.close();
		return result;
	}
}
