package com.laughingstock.ritwick.shutup.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import com.laughingstock.ritwick.shutup.Services.OffhookListenerService;
import com.laughingstock.ritwick.shutup.Services.RingingListenerService;

public class PhoneStateReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, final Intent intent)
    {
        if(!intent.getAction().equals("android.intent.action.PHONE_STATE"))   return;

        Intent ringinglistenerservice=new Intent(context,RingingListenerService.class);
        Intent offhooklistenerservice=new Intent(context,OffhookListenerService.class);

        offhooklistenerservice.putExtra("callnumber",intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER));

        if(intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_RINGING))
        {
            context.startService(ringinglistenerservice);
        }
        else if(intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_OFFHOOK))
        {
            context.stopService(ringinglistenerservice);
            context.startService(offhooklistenerservice);
        }
        else if(intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_IDLE))
        {

            context.stopService(offhooklistenerservice);
            context.stopService(ringinglistenerservice);
            //Toast.makeText(context,"Call ended",Toast.LENGTH_LONG).show();
        }

    }

}