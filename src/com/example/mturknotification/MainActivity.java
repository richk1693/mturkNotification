package com.example.mturknotification;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	private TextView txtMsg;
	TextView txtRatio;
	Button btnGo;
	Intent intentMyService;
	ComponentName service;
	ArrayList<String> tests = new ArrayList<String>();
	ArrayList<String> urls = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		txtMsg = (TextView) findViewById(R.id.txtMsg);
		txtRatio = (TextView) findViewById(R.id.txtRatio);
		btnGo = (Button) findViewById(R.id.btnReadXml);
 
		
		//intentMyService = new Intent(this,	MyService.class);
		
		intentMyService = new Intent("com.example.mturknotification.MyService");
		
		btnGo.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				
				try {
					intentMyService.putExtra("ratio", Double.parseDouble(txtRatio.getText().toString()));
					
				} catch (Exception e) {
					System.out.println("Bad ratio");
				}
				
				
				//new backgroundAsyncTask().execute();
				startService(intentMyService);
				finish();
				
	
			}
		});
	}

	// Method to check thread titles for values and make notification if the
	// ratio is high
	public void testTitles2(ArrayList<String> test) {

		double val = -1;
		double time = -1;
		double ratio = 0;
		double alertVal = .3;
		String req[] = { "", "", "" };
		Pattern valP = Pattern.compile("\\d*\\.\\d{2}");
		Pattern timeP = Pattern.compile("\\/(\\d+\\.?\\d?\\d?)[ ]*(?i)[s|m]");
		Pattern timeP2 = Pattern.compile("\\d:\\d{2}");
		// Pattern timeP = Pattern.compile("(\\d+)[ ]*[s|m]");
		Pattern reqP = Pattern.compile(">\\d+%?");

		try {
			alertVal = Double.parseDouble(txtRatio.getText().toString());
		} catch (Exception e) {
			System.out.println("Bad ratio");
		}

		// Loop through the array list containing titles.
		for (int i = 0; i < test.size(); i++) {

			// reset values to deafult
			val = -1;
			time = -1;
			ratio = 0;

			if (test.get(i) != null) {
				System.out.println("Attempting " + test.get(i));

				Matcher m = valP.matcher(test.get(i));

				// If we match the value pattern val = val else val =-1;
				if (m.find()) {
					System.out.println("Value Match: " + m.group(0));
					val = Double.parseDouble(m.group(0));
				} else {
					System.out.println("No val match");
					val = -1;
				}

				m = timeP.matcher(test.get(i));
				Matcher mt2 = timeP2.matcher(test.get(i));
				// If we match our time pattern:
				if (m.find()) {
					// Search through our time result for only numbers
					System.out.println("Initial Time Match" + m.group(0));
					Pattern p2 = Pattern.compile("\\d*\\.?\\d?\\d?");
					// Matcher m2 = p2.matcher(m.group(0));
					Matcher m2 = p2.matcher(m.group(0).substring(1));

					if (m2.find())
						System.out.println("Matched Time: " + m2.group(0));
					time = Double.parseDouble(m2.group(0));
					if (m.group(0).contains("s")) {
						time /= 60;
					}
				} else if (mt2.find()) {
					System.out.println(mt2.group(0));
					String[] a = (mt2.group(0).split(":", 2));
					System.out.println(a[1]);
					time = (Double.parseDouble(a[0]) + ((Double
							.parseDouble(a[1]) / 60)));

				} else {
					time = -1;
				}// end of time pattern

				if (time != -1 && val != -1) {
					ratio = val / time;
				}

				System.out.println("TitleZZZ: " + test.get(i));
				System.out.println("Time is " + time);
				System.out.println("val is " + val);
				System.out.println("Ratio is " + ratio);
				System.out.println("I AM IN THE NEW THING");

				if (ratio > alertVal) {

					/*
					 * NotificationManager NM; NM = (NotificationManager)
					 * getSystemService(Context.NOTIFICATION_SERVICE);
					 * Notification notify = new
					 * Notification(android.R.drawable.btn_star, "Valuable Hit",
					 * System.currentTimeMillis()); PendingIntent pending =
					 * PendingIntent.getActivity( getApplicationContext(), 0,
					 * new Intent(), 0);
					 * notify.setLatestEventInfo(getApplicationContext
					 * (),"High Ratio: " + ratio, test.get(i), pending);
					 * NM.notify(0, notify);
					 */

					createNotification("High Ratio: " + ratio, test.get(i)
							.toString(), urls.get(i).toString());

				}
				System.out.println("\n\n\n\n\n");
			}
		}// end of if not null

	}// end of loop

	// Method to turn a URL into a useable format
	public InputStream getUrlData(String url) throws URISyntaxException,
			ClientProtocolException, IOException {

		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet method = new HttpGet(new URI(url));
		HttpResponse res = client.execute(method);
		return res.getEntity().getContent();
	}

	private void createNotification(String text, String desc, String link) {

		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
				this).setAutoCancel(true).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(text).setContentText(desc);

		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		// pending intent is redirection using the deep-link
		Intent resultIntent = new Intent(Intent.ACTION_VIEW);
		resultIntent.setData(Uri.parse(link));

		PendingIntent pending = PendingIntent.getActivity(this, 0,
				resultIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
		notificationBuilder.setContentIntent(pending);

		// using the same tag and Id causes the new notification to replace an
		// existing one
		mNotificationManager.notify(String.valueOf(System.currentTimeMillis()),
				android.R.drawable.btn_star, notificationBuilder.build());
	}

	
	public class backgroundAsyncTask extends

			AsyncTask<Integer, Void, StringBuilder> {

		ProgressDialog dialog = new ProgressDialog(MainActivity.this);

		@Override
		protected void onPostExecute(StringBuilder result) {
			super.onPostExecute(result);
			dialog.dismiss();
			txtMsg.setText(result.toString());
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog.setMessage("Please wait...");
			dialog.setCancelable(false);
			dialog.show();

		}

		public String getXmlFromUrl(String url) {
			String xml = null;

			try {
				// defaultHttpClient
				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost(url);

				HttpResponse httpResponse = httpClient.execute(httpPost);
				HttpEntity httpEntity = httpResponse.getEntity();
				xml = EntityUtils.toString(httpEntity);

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// return XML
			return xml;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
		}

		@Override
		protected StringBuilder doInBackground(Integer... params) {
			// int xmlResFile = params[0];

			// XmlPullParser parser = getResources().getXml(xmlResFile);
			ArrayList<String> nodeList = new ArrayList<String>();
			StringBuilder stringBuilder = new StringBuilder();
			String nodeText = "";
			String nodeName = "";
			try {

				XmlPullParserFactory factory;
				factory = XmlPullParserFactory.newInstance();
				factory.setNamespaceAware(true);
				XmlPullParser parser = factory.newPullParser();
				parser.setInput(new InputStreamReader(
						getUrlData("http://reddit.com/r/hitsworthturkingfor/.rss")));

				int eventType = -1;
				while (eventType != XmlPullParser.END_DOCUMENT) {

					eventType = parser.next();

					if (eventType == XmlPullParser.START_DOCUMENT) {
						// stringBuilder.append("\nSTART_DOCUMENT");

					} else if (eventType == XmlPullParser.END_DOCUMENT) {
						// stringBuilder.append("\nEND_DOCUMENT");

					} else if (eventType == XmlPullParser.START_TAG) {
						nodeName = parser.getName();
						if (nodeName.equals("title")) {
							// stringBuilder.append("\nSTART_TAG: " + nodeName);
							// stringBuilder.append(getAttributes(parser));
						}

					} else if (eventType == XmlPullParser.END_TAG) {
						nodeName = parser.getName();
						// stringBuilder.append("\nEND_TAG:   " + nodeName);

					} else if (eventType == XmlPullParser.TEXT) {
						nodeText = parser.getText();
						if (nodeName.equals("title")) {
							stringBuilder.append(nodeText + "\n\n");
							nodeList.add(nodeText);

						}

						if (nodeName.equals("link")) {
							// stringBuilder.append(nodeText + "\n\n");
							stringBuilder.append(nodeText + "\n\n");
							urls.add(nodeText);

						}

					}
				}
			} catch (Exception e) {
				Log.e("<<PARSING ERROR>>", e.getMessage());

			}
			testTitles2(nodeList);
			return stringBuilder;
		}// doInBackground

		private String getAttributes(XmlPullParser parser) {
			StringBuilder stringBuilder = new StringBuilder();
			// trying to detect inner attributes nested inside a node tag
			String name = parser.getName();
			if (name != null) {
				int size = parser.getAttributeCount();

				for (int i = 0; i < size; i++) {
					String attrName = parser.getAttributeName(i);
					String attrValue = parser.getAttributeValue(i);
					stringBuilder.append("\n     Attrib <key,value>= "
							+ attrName + ", " + attrValue);
				}

			}
			return stringBuilder.toString();

		}// innerElements

	}// backroundAsyncTask
	
	
	
	
	
	
}