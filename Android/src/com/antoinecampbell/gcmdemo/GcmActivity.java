package com.antoinecampbell.gcmdemo;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

public class GcmActivity extends FragmentActivity implements OnClickListener
{
    public static final String EXTRA_MESSAGE = "message";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    GoogleCloudMessaging gcm;
    String regid;
    Context context;
    AtomicInteger msgId = new AtomicInteger();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_gcm);

	// GCM startup
	gcm = GoogleCloudMessaging.getInstance(this);
	context = getApplicationContext();
	regid = getRegistrationId(context);
	((TextView) findViewById(R.id.gcm_userid_textview)).setText(regid);
	
	EditText editText = ((EditText) findViewById(R.id.message_edittext));
	editText.setOnEditorActionListener(new OnEditorActionListener()
	{
	    @Override
	    public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
	    {
		switch (actionId)
		{
		    case EditorInfo.IME_ACTION_SEND:
		    {
			String message = v.getText().toString();
			if (message != "")
			{
			    sendMessage(message);
			    return true;
			}
		    }
		}
		return false;
	    }
	});
	
	// Handle possible notification intent if app was not running
	handleNotification(getIntent().getExtras());
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
	super.onNewIntent(intent);
	// Handle possible notification intent if app is already running
	handleNotification(intent.getExtras());
    }
    
    /**
     * If this activity was started or brought to the front using an intent from a notification type 
     * GCM message inform other devices the message was handled
     * @param extras Extras bundle from incoming intent
     */
    private void handleNotification(Bundle extras)
    {
	if(extras != null && extras.containsKey("action") 
		&& extras.containsKey("notification_key")
		&& "com.antoinecampbell.gcmdemo.NOTIFICATION".equalsIgnoreCase(extras.getString("action"))) {
	    // Send a notification clear message upstream to clear on other devices
	    sendClearMessage(extras.getString("notification_key"));
	}
    }

    @Override
    public void onClick(View v)
    {

	switch (v.getId())
	{
	    case R.id.register_button:
		// Check device for Play Services APK. If check succeeds, proceed with
		// GCM registration.
		if (checkPlayServices())
		{
		    // Retrieve registration id from local storage
		    regid = getRegistrationId(context);
		    
		    if (TextUtils.isEmpty(regid))
		    {
			registerInBackground();
		    }
		    else
		    {
			((TextView) findViewById(R.id.gcm_userid_textview)).setText(regid);
		    }
		}
		else
		{
		    Log.i(Globals.TAG, "No valid Google Play Services APK found.");
		}
		break;
	    case R.id.unregister_button:
		unregister();
		break;
	    case R.id.send_message_button:
		String message = ((EditText) findViewById(R.id.message_edittext)).getText().toString();
		if (message != "")
		{
		    sendMessage(message);
		}
		break;
	}
    }
    
    /**
     * Upstream a GCM message letting other devices know to clear the notification as 
     * it has been handled on this device
     * @param notification_key The GCM registered notification key for the user's devices
     */
    private void sendClearMessage(String notification_key)
    {
	if (regid == null || regid.equals(""))
	{
	    Toast.makeText(this, "You must register first", Toast.LENGTH_LONG).show();
	    return;
	}
	new AsyncTask<String, Void, String>()
	{
	    @Override
	    protected String doInBackground(String... params)
	    {
		String msg = "";
		try
		{
		    Bundle data = new Bundle();
		    data.putString("action", "com.antoinecampbell.gcmdemo.CLEAR_NOTIFICATION");
		    String id = Integer.toString(msgId.incrementAndGet());
		    gcm.send(params[0], id, Globals.GCM_TIME_TO_LIVE, data);
		    msg = "Sent notification clear message";
		}
		catch (IOException ex)
		{
		    msg = "Error :" + ex.getMessage();
		}
		return msg;
	    }

	    @Override
	    protected void onPostExecute(String msg)
	    {
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	    }
	}.execute(notification_key);

    }

    /**
     * Upstream a GCM message up to the 3rd party server
     * @param message
     */
    private void sendMessage(String message)
    {
	if(regid == null || regid.equals(""))
	{
	    Toast.makeText(this, "You must register first", Toast.LENGTH_LONG).show();
	    return;
	}
	String messageType = ((Spinner)findViewById(R.id.spinner_message_type)).getSelectedItem().toString();
	new AsyncTask<String, Void, String>()
	{
	    @Override
	    protected String doInBackground(String... params)
	    {
		String msg = "";
		try
		{
		    Bundle data = new Bundle();
		    data.putString("message", params[0]);
		    if(params[1].equals("Echo"))
		    {
			data.putString("action", "com.antoinecampbell.gcmdemo.ECHO");
		    }
		    else if(params[1].equals("Broadcast"))
		    {
			data.putString("action", "com.antoinecampbell.gcmdemo.BROADCAST");
		    }
		    else if(params[1].equals("Notification"))
		    {
			data.putString("action", "com.antoinecampbell.gcmdemo.NOTIFICATION");
		    }
		    String id = Integer.toString(msgId.incrementAndGet());
		    gcm.send(Globals.GCM_SENDER_ID + "@gcm.googleapis.com", id, Globals.GCM_TIME_TO_LIVE, data);
		    msg = "Sent message";
		}
		catch (IOException ex)
		{
		    msg = "Error :" + ex.getMessage();
		}
		return msg;
	    }

	    @Override
	    protected void onPostExecute(String msg)
	    {
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	    }
	}.execute(message, messageType);
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If it
     * doesn't, display a dialog that allows users to download the APK from the
     * Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices()
    {
	int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
	if (resultCode != ConnectionResult.SUCCESS)
	{
	    if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
	    {
		GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST)
			.show();
	    }
	    else
	    {
		Log.i(Globals.TAG, "This device is not supported.");
		finish();
	    }
	    return false;
	}
	return true;
    }

    /**
     * Gets the current registration ID for application on GCM service, if there
     * is one.
     * <p>
     * If result is empty, the app needs to register.
     * 
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context)
    {
	final SharedPreferences prefs = getGcmPreferences(context);
	String registrationId = prefs.getString(Globals.PREFS_PROPERTY_REG_ID, "");
	if (registrationId == null || registrationId.equals(""))
	{
	    Log.i(Globals.TAG, "Registration not found.");
	    return "";
	}
	// Check if app was updated; if so, it must clear the registration ID
	// since the existing regID is not guaranteed to work with the new
	// app version.
	int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
	int currentVersion = getAppVersion(context);
	if (registeredVersion != currentVersion)
	{
	    Log.i(Globals.TAG, "App version changed.");
	    return "";
	}
	return registrationId;
    }

    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     * 
     * @param context
     *            application's context.
     * @param regId
     *            registration ID
     */
    private void storeRegistrationId(Context context, String regId)
    {
	final SharedPreferences prefs = getGcmPreferences(context);
	int appVersion = getAppVersion(context);
	Log.i(Globals.TAG, "Saving regId on app version " + appVersion);
	SharedPreferences.Editor editor = prefs.edit();
	editor.putString(Globals.PREFS_PROPERTY_REG_ID, regId);
	editor.putInt(PROPERTY_APP_VERSION, appVersion);
	editor.commit();
    }
    
    /**
     * Removes the registration ID from the application's 
     * {@code SharedPreferences}.
     * @param context 
     * 		the application context
     */
    private void removeRegistrationId(Context context)
    {
	final SharedPreferences prefs = getGcmPreferences(context);
	int appVersion = getAppVersion(context);
	Log.i(Globals.TAG, "Removig regId on app version " + appVersion);
	SharedPreferences.Editor editor = prefs.edit();
	editor.remove(Globals.PREFS_PROPERTY_REG_ID);
	editor.commit();
	regid = null;
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground()
    {
	new AsyncTask<Void, Void, String>()
	{
	    @Override
	    protected String doInBackground(Void... params)
	    {
		String msg = "";
		try
		{
		    if (gcm == null)
		    {
			gcm = GoogleCloudMessaging.getInstance(context);
		    }
		    regid = gcm.register(Globals.GCM_SENDER_ID);
		    msg = "Device registered, registration ID=" + regid;

		    // You should send the registration ID to your server over
		    // HTTP, so it can use GCM/HTTP or CCS to send messages to your app.
		    sendRegistrationIdToBackend();

		    // For this demo: we use upstream GCM messages to send the
		    // registration ID to the 3rd party server

		    // Persist the regID - no need to register again.
		    storeRegistrationId(context, regid);
		}
		catch (IOException ex)
		{
		    msg = "Error :" + ex.getMessage();
		    // If there is an error, don't just keep trying to register.
		    // Require the user to click a button again, or perform
		    // exponential back-off.
		}
		return msg;
	    }

	    @Override
	    protected void onPostExecute(String msg)
	    {
		((TextView) findViewById(R.id.gcm_userid_textview)).setText(regid);
	    }
	}.execute(null, null, null);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context)
    {
	try
	{
	    PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
	    return packageInfo.versionCode;
	}
	catch (NameNotFoundException e)
	{
	    // should never happen
	    throw new RuntimeException("Could not get package name: " + e);
	}
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGcmPreferences(Context context)
    {
	// This sample app persists the registration ID in shared preferences,
	// but how you store the regID in your app is up to you.
	return getSharedPreferences(Globals.PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Sends the registration ID to the 3rd party server via an upstream 
     * GCM message. Ideally this would be done via HTTP to guarantee success or failure 
     * immediately, but it would require an HTTP endpoint.
     */
    private void sendRegistrationIdToBackend()
    {
	Log.d(Globals.TAG, "REGISTER USERID: " + regid);
	String name = ((EditText)findViewById(R.id.name_edittext)).getText().toString();
	new AsyncTask<String, Void, String>()
	{
	    @Override
	    protected String doInBackground(String... params)
	    {
		String msg = "";
		try
		{
		    Bundle data = new Bundle();
		    data.putString("name", params[0]);
		    data.putString("action", "com.antoinecampbell.gcmdemo.REGISTER");
		    String id = Integer.toString(msgId.incrementAndGet());
		    gcm.send(Globals.GCM_SENDER_ID + "@gcm.googleapis.com", id, Globals.GCM_TIME_TO_LIVE, data);
		    msg = "Sent registration";
		}
		catch (IOException ex)
		{
		    msg = "Error :" + ex.getMessage();
		}
		return msg;
	    }

	    @Override
	    protected void onPostExecute(String msg)
	    {
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	    }
	}.execute(name);
    }
    
    /**
     * Send an upstream GCM message to the 3rd party server to remove this 
     * device's registration ID, and contact the GCM server to do the same.
     */
    private void unregister()
    {
	Log.d(Globals.TAG, "UNREGISTER USERID: " + regid);
	new AsyncTask<Void, Void, String>()
	{
	    @Override
	    protected String doInBackground(Void... params)
	    {
		String msg = "";
		try
		{
		    Bundle data = new Bundle();
		    data.putString("action", "com.antoinecampbell.gcmdemo.UNREGISTER");
		    String id = Integer.toString(msgId.incrementAndGet());
		    gcm.send(Globals.GCM_SENDER_ID + "@gcm.googleapis.com", id, Globals.GCM_TIME_TO_LIVE, data);
		    msg = "Sent unregistration";
		    gcm.unregister();
		}
		catch (IOException ex)
		{
		    msg = "Error :" + ex.getMessage();
		}
		return msg;
	    }

	    @Override
	    protected void onPostExecute(String msg)
	    {
		removeRegistrationId(getApplicationContext());
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
		((TextView)findViewById(R.id.gcm_userid_textview)).setText(regid);
	    }
	}.execute();
    }

}
