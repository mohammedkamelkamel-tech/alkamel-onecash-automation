package com.alkamel.onecash;

import android.content.*;

public class SmsSentReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c,Intent i){
        String fp=i.getStringExtra("fp");long card=i.getLongExtra("card",-1);Db db=new Db(c);
        if(fp==null)return;
        if(getResultCode()==android.app.Activity.RESULT_OK){
            if(card>0)db.cardState(card,"SENT");
            db.updateTransfer(fp,"SENT","تم إرسال الكرت SMS");
        } else {
            if(card>0)db.cardState(card,"AVAILABLE");
            db.updateTransfer(fp,"FAILED","فشل إرسال SMS — أعيد الكرت للمخزون");
        }
    }
}
