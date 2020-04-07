package com.laughingstock.ritwick.shutup.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.Toast;


import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.laughingstock.ritwick.shutup.Adapters.*;
import com.laughingstock.ritwick.shutup.Fragments.CSFragment;
import com.laughingstock.ritwick.shutup.MessageEvent;
import com.laughingstock.ritwick.shutup.R;

import org.greenrobot.eventbus.EventBus;


public class CallSchedulerService extends Service implements TextToSpeech.OnInitListener
{
    Context context;
    Intent intent,callIntent;
    TextToSpeech tts;
    String name;
    boolean ring=true,vibrate=true,ttsinitiated=false,repeatcall=false;
    CSFragment csFragment;
    ArrayList<Bundle> schedinfo;
    Bundle b;
    int position,currmode,currvol,repeatinterval=24;
    AudioManager audioManager;
    MediaPlayer mediaPlayer;
    Vibrator vibrator;

    @Override
    public IBinder onBind(Intent intent)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        context=getApplicationContext();
        this.intent=intent;

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire(60000);
        audioManager=(AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mediaPlayer=MediaPlayer.create(context, R.raw.schedaudio);
        vibrator = (Vibrator) this.context.getSystemService(Context.VIBRATOR_SERVICE);

        tts = new TextToSpeech(context,CallSchedulerService.this);
        currvol=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),0);
        currmode=audioManager.getMode();

        position=intent.getIntExtra("position",0);

        csFragment=new CSFragment();
        schedinfo=csFragment.readFromInternalStorage(context);
        if(position>=schedinfo.size())
            stopSelf();
        b=schedinfo.get(position);
        name=b.getString("name");
        ring=b.getBoolean("ring");
        vibrate=b.getBoolean("vibrate");
        repeatcall=b.getBoolean("repeatcall");
        repeatinterval=b.getInt("repeatinterval");

        callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        callIntent.setData(Uri.parse("tel:"+b.getString("dialnumber")));

        if(vibrate)
        {
            long[] pattern = {0, 300, 500,300,500,300,500};
            vibrator.vibrate(pattern,((ring)?0:-1));
            // TODO: 3/27/17 vibration stuff sync
        }

        if(ring)
        {
            mediaPlayer.start();
            tts.setOnUtteranceProgressListener(utteranceProgressListener);
            mediaPlayer.setOnCompletionListener((mp)->
            {
                    if(ttsinitiated)
                    {
                        if(Build.VERSION.SDK_INT<21)
                            tts.speak("Hi! Shut up here. Calling "+name+"now.", TextToSpeech.QUEUE_FLUSH, null);
                        else
                            tts.speak("Hi! Shut up here. Calling "+name+"now.",TextToSpeech.QUEUE_FLUSH,null,"ring");
                    }

            });
        }
        else
            startCall();

        wl.release();

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy()
    {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        vibrator.cancel();
        audioManager.setMode(currmode);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,currvol,0);
        super.onDestroy();
    }

    @Override
    public void onInit(int status)
    {

        if (status == TextToSpeech.SUCCESS)
        {

            int result = tts.setLanguage(Locale.getDefault());

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
            {
                Log.e("TTS", "This Language is not supported");
                startCall();
            }
            else
            {
                ttsinitiated=true;
            }

        }
        else
        {
            startCall();
        }

    }

    private UtteranceProgressListener utteranceProgressListener = new UtteranceProgressListener()
    {
        @Override
        public void onStart(String utteranceId)
        {
            audioManager.setMode(AudioManager.MODE_IN_CALL);
            audioManager.setSpeakerphoneOn(true);
            vibrator.cancel();
        }

        @Override
        public void onDone(String utteranceId)
        {
            audioManager.setMode(currmode);
            audioManager.setSpeakerphoneOn(false);
            startCall();
        }

        @Override
        public void onError(String utteranceId)
        {
            startCall();
        }
    };

    public void startCall()
    {
        context.startActivity(callIntent);

        if(!repeatcall)
            schedinfo.remove(position);
        else
        {
            Bundle temp= schedinfo.get(position);
            long timeinmills=temp.getLong("timeinmills")+3600000*repeatinterval;
            temp.putLong("timeinmills",timeinmills);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timeinmills);
            String datentime=sdf.format(calendar.getTime());
            String time=datentime.split(" ")[0];
            String date=datentime.split(" ")[1];
            temp.putString("date",date);
            temp.putString("time",time);
            schedinfo.set(position,temp);
        }
        EventBus.getDefault().post(new MessageEvent("schedcallperformed"));
        csFragment.saveToInternalStorage(context, schedinfo);
        ScheduleContactsAdapter schedulecontactsAdapter = new ScheduleContactsAdapter(context, schedinfo);
        schedulecontactsAdapter.notifyDataSetChanged();
        stopSelf();
    }
}
