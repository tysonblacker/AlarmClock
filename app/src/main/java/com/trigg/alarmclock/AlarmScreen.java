package com.trigg.alarmclock;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

public class AlarmScreen extends Activity {

    public final String TAG = this.getClass().getSimpleName();

    private WakeLock mWakeLock;
    private MediaPlayer mPlayer;

    private static final int WAKELOCK_TIMEOUT = 60 * 1000;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setup layout
        this.setContentView(R.layout.activity_alarm_screen);

        String name = getIntent().getStringExtra(AlarmManagerHelper.NAME);
        int timeHour = getIntent().getIntExtra(AlarmManagerHelper.TIME_HOUR, 0);
        int timeMinute = getIntent().getIntExtra(AlarmManagerHelper.TIME_MINUTE, 0);
        String tone = getIntent().getStringExtra(AlarmManagerHelper.TONE);

        TextView tvName = (TextView) findViewById(R.id.alarm_screen_name);
        tvName.setText(name);

        TextView tvTime = (TextView) findViewById(R.id.alarm_screen_time);
        tvTime.setText(String.format("%02d : %02d", timeHour, timeMinute));

        Button dismissButton = (Button) findViewById(R.id.alarm_screen_button);
        dismissButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                mPlayer.stop();

                finish();
            }
        });

        Button snoozeButton = (Button) findViewById(R.id.alarm_snooze_button);
        snoozeButton.setOnClickListener(new OnClickListener()
             {
                 public void onClick(View v) {

                      /*Toast.makeText(getBaseContext(),
                      "Please wait, connecting to server.",
                               Toast.LENGTH_SHORT).show();
                      */
                     mPlayer.pause();

                     final Handler handler = new Handler();
                     handler.postDelayed(new Runnable() {
                         @Override
                         public void run() {
                             mPlayer.start();
                             Log.d("player delay", "player delay");
                         }
                     }, 10000);

                      // Create Inner Thread Class
                      Thread background = new Thread(new Runnable() {
                            private final HttpClient Client = new DefaultHttpClient();
                            private String URL = "http://snooze-api.ngrok.com/snooze";

                            // After call for background.start this run method call
                            public void run() {
                                try {
                                    String SetServerString = "";
                                    HttpGet httpget = new HttpGet(URL);
                                    ResponseHandler<String> responseHandler = new BasicResponseHandler();
                                    SetServerString = Client.execute(httpget, responseHandler);
                                    threadMsg(SetServerString);
                                } catch (Throwable t) {
                                    // just end the background thread
                                    Log.i("Animation", "Thread  exception " + t);
                                }
                            }

                            private void threadMsg(String msg) {

                              if (!msg.equals(null) && !msg.equals("")) {
                                  Message msgObj = handler.obtainMessage();
                                  Bundle b = new Bundle();
                                  b.putString("message", msg);
                                  msgObj.setData(b);
                                  handler.sendMessage(msgObj);
                              }
                          }

                          // Define the Handler that receives messages from the thread and update the progress
                          private final Handler handler = new Handler() {

                               public void handleMessage(Message msg) {

                                    Integer balance, cost_last_snooze;
                                    balance = 0;
                                    cost_last_snooze =0 ;
                                    String aResponse = msg.getData().getString("message");
                                    Log.d("ALARM_SCREEN", aResponse );
                                    try {
                                          JSONObject obj = new JSONObject(aResponse);
                                          balance = obj.getInt("balance");
                                          cost_last_snooze = obj.getInt("cost_last_snooze");
                                          Log.d ("cost_last_snooze", cost_last_snooze.toString());
                                          Log.d ("balance", balance.toString());


                               } catch (JSONException jex) {

                                    Log.d("ALARM_SCREEN",jex.toString() );
                               }

                               if ((null != aResponse)) {

                                   // ALERT MESSAGE
                                   Toast.makeText(
                                       getBaseContext(),
                                       "Current Balance: " + balance + "\nSnooze cost: " + cost_last_snooze,
                                           Toast.LENGTH_LONG).show();
                               } else {

                               // ALERT MESSAGE
                               Toast.makeText(
                                   getBaseContext(),
                                   "Not Got Response From Server.",
                                   Toast.LENGTH_SHORT).show();
                               }



                          }
                      };

                 });
                 // Start Thread
                 background.start();  //After call start method thread called run Method
             }
             }

        );



    //Play alarm tone
    mPlayer=new

    MediaPlayer();

    try

    {
        if (tone != null && !tone.equals("")) {
            Uri toneUri = Uri.parse(tone);
            if (toneUri != null) {
                mPlayer.setDataSource(this, toneUri);
                mPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mPlayer.setLooping(true);
                mPlayer.prepare();
                mPlayer.start();
            }
        }
    }

    catch(
    Exception e
    )

    {
        e.printStackTrace();
    }

    //Ensure wakelock release
    Runnable releaseWakelock = new Runnable() {

        @Override
        public void run() {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

            if (mWakeLock != null && mWakeLock.isHeld()) {
                mWakeLock.release();
            }
        }
    };

    new

    Handler()

    .

    postDelayed(releaseWakelock, WAKELOCK_TIMEOUT);

}


    @SuppressWarnings("deprecation")
    @Override
    protected void onResume() {
        super.onResume();

        // Set the window to keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        // Acquire wakelock
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        if (mWakeLock == null) {
            mWakeLock = pm.newWakeLock((PowerManager.FULL_WAKE_LOCK | PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), TAG);
        }

        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
            Log.i(TAG, "Wakelock aquired!!");
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        mPlayer.stop();
    }
}
