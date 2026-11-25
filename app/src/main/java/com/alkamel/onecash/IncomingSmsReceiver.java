package com.alkamel.onecash;

import android.content.*;
import android.app.PendingIntent;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import java.security.MessageDigest;

public class IncomingSmsReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context,Intent intent) {
        if(!Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction()))return;
        SmsMessage[] msgs=Telephony.Sms.Intents.getMessagesFromIntent(intent);
        if(msgs==null||msgs.length==0)return;
        StringBuilder body=new StringBuilder();String sender=msgs[0].getOriginatingAddress();long timestamp=msgs[0].getTimestampMillis();
        for(SmsMessage m:msgs)body.append(m.getMessageBody());
        Db db=new Db(context);String expected=db.setting("wallet_sender");
        // Sender whitelist is mandatory; empty whitelist means do not process any SMS.
        if(expected.trim().isEmpty()||sender==null||!sender.equals(expected.trim()))return;
        TransferParser.Parsed p=TransferParser.parse(body.toString());if(p==null)return;
        String fp=hash(sender+"|"+body+"|"+timestamp);
        if(db.transferExists(fp))return;
        String phone=db.customerPhone(p.payerName);
        if(phone==null){safeTransfer(db,fp,sender,p.payerName,p.amount,null,-1,"REVIEW","اسم المحوّل غير مسجل");return;}
        if(!db.setting("auto_send").equals("1")){safeTransfer(db,fp,sender,p.payerName,p.amount,phone,-1,"WAITING","الإرسال الآلي متوقف — راجع الحوالة");return;}
        long card=db.reserveCard(p.amount);
        if(card<0){safeTransfer(db,fp,sender,p.payerName,p.amount,phone,-1,"REVIEW","لا يوجد مخزون لهذه الفئة");return;}
        String code=db.cardCode(card);
        if(code==null){db.cardState(card,"AVAILABLE");safeTransfer(db,fp,sender,p.payerName,p.amount,phone,-1,"FAILED","تعذر قراءة الكرت المحجوز");return;}
        String out="حوالتك وصلت. كرت "+p.amount+" ريال: "+code+" - الكامل";
        if(!safeTransfer(db,fp,sender,p.payerName,p.amount,phone,card,"SENDING","بانتظار نتيجة SMS")){
            db.cardState(card,"AVAILABLE");return;
        }
        Intent sentIntent=new Intent(context,SmsSentReceiver.class).setAction("com.alkamel.onecash.SMS_SENT").putExtra("fp",fp).putExtra("card",card);
        PendingIntent sent=PendingIntent.getBroadcast(context,fp.hashCode(),sentIntent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        try{android.telephony.SmsManager.getDefault().sendTextMessage(phone,null,out,sent,null);}
        catch(Exception e){db.cardState(card,"AVAILABLE");db.updateTransfer(fp,"FAILED","تعذر إرسال SMS: "+e.getMessage());}
    }
    private static boolean safeTransfer(Db db,String fp,String sender,String payer,int amount,String phone,long card,String status,String msg){try{return db.addTransfer(fp,sender,payer,amount,phone,card,status,msg);}catch(Exception e){return false;}}
    private static String hash(String s){try{byte[] b=MessageDigest.getInstance("SHA-256").digest(s.getBytes("UTF-8"));StringBuilder x=new StringBuilder();for(byte v:b)x.append(String.format("%02x",v));return x.toString();}catch(Exception e){return String.valueOf(s.hashCode());}}
}
