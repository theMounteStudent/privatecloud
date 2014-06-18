package ch.ffhs.privatecloudffhs.sync;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import ch.ffhs.privatecloudffhs.R;
import ch.ffhs.privatecloudffhs.Settings;
import ch.ffhs.privatecloudffhs.database.PrivateCloudDatabase;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;


public class SyncService extends Service {
	private static final String TAG = "SyncService";

	private Timer myTimer = null;
	private final IBinder syncServiceBinder = new SyncServiceBinder ();

	private SyncManager syncManagerObj;

	private static final String KEY_SYNCINTERVAL = "syncinterval";
	private static final String NAME_MYPREF = "cloudsettings";
	private static final String KEY_ONWIFI = "onwifi";
	private static final String KEY_ONCHARGE = "oncharge";
	private SharedPreferences settings;
	
	
	/** inner class implements the broadcast timer*/
	private class TimeServiceTimerTask extends TimerTask {	
		private static final String TAG = "TimeServiceTimerTask";
		private Context context;

		public TimeServiceTimerTask(Context context) {
			this.context = context;
		}
		
		public void run() {	
			Boolean syncPerm = true;
			
			if(settings.getBoolean(KEY_ONWIFI, false)) {
				ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

				if (!mWifi.isConnected()) {
					syncPerm = false;
					Log.d(TAG, "NO WIFI");

				}  
			}
			
			if(settings.getBoolean(KEY_ONCHARGE, false) && syncPerm) {
				final Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
				
			    if(batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1) != BatteryManager.BATTERY_STATUS_CHARGING) {
			    	syncPerm = false;
					Log.d(TAG, "NO CHARGE");

			    }
			}
			
			if(syncPerm) sync();
			
			// update timer interval
			initTimer();
		}
	}

	private void sync()
	{		
		syncManagerObj.sync();
	}
	
	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");
		
		super.onCreate();
		
		settings = getSharedPreferences(NAME_MYPREF, MODE_PRIVATE);

		syncManagerObj = new SyncManager(this);

		initTimer();
	}

	synchronized void initTimer()
	{
		if(myTimer != null)
		{
			myTimer.cancel();
			myTimer = null;
		}

		myTimer = new Timer();			

		int syncInterval = settings.getInt(KEY_SYNCINTERVAL, 1) * 1000 *60;
		myTimer.schedule( new TimeServiceTimerTask(this), syncInterval, syncInterval);		
	}
		
	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "onBind");
		return syncServiceBinder;		
	}
	
	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);
		Log.d(TAG, "onRebind");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		
	    Intent bIntent = new Intent(this, SyncService.class);       
	    PendingIntent pbIntent = PendingIntent.getActivity(this, 0 , bIntent, Intent.FLAG_ACTIVITY_CLEAR_TOP);
	  
	    NotificationCompat.Builder bBuilder =
	            new NotificationCompat.Builder(this)
	                .setContentTitle("privatecloud")
	                .setContentText("sync")
	                .setAutoCancel(true)
	                .setOngoing(true)
	                .setContentIntent(pbIntent);
	    startForeground(1, bBuilder.build());
	    
	    return START_STICKY;
	}
	
	
	@Override
	public boolean onUnbind(Intent intent) {
		Log.d(TAG, "onUnbind2");
		return super.onUnbind(intent);
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();
		stopForeground(true);
	}
	
	public class SyncServiceBinder extends Binder {		
		public void syncNow()
		{
			if(!syncManagerObj.isRunning())
			{
				sync();
				Toast.makeText(getApplicationContext(), R.string.service_sync_start, Toast.LENGTH_LONG).show();
			}
			else
			{
				Toast.makeText(getApplicationContext(), R.string.service_sync_running, Toast.LENGTH_LONG).show();
			}
		}	
	}
}
