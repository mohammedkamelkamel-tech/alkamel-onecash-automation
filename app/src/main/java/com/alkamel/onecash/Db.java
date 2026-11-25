package com.alkamel.onecash;

import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import java.util.*;

public class Db extends SQLiteOpenHelper {
    public Db(Context c) { super(c, "onecash.db", null, 1); }
    @Override public void onCreate(SQLiteDatabase d) {
        d.execSQL("CREATE TABLE customers(id INTEGER PRIMARY KEY AUTOINCREMENT, payer_name TEXT NOT NULL, normalized TEXT UNIQUE NOT NULL, phone TEXT NOT NULL)");
        d.execSQL("CREATE TABLE cards(id INTEGER PRIMARY KEY AUTOINCREMENT, denomination INTEGER NOT NULL, code TEXT UNIQUE NOT NULL, state TEXT NOT NULL DEFAULT 'AVAILABLE')");
        d.execSQL("CREATE TABLE transfers(id INTEGER PRIMARY KEY AUTOINCREMENT, fingerprint TEXT UNIQUE NOT NULL, sender TEXT, payer_name TEXT, amount INTEGER, phone TEXT, card_id INTEGER, status TEXT, message TEXT, created_at INTEGER)");
        d.execSQL("CREATE TABLE settings(key TEXT PRIMARY KEY, value TEXT)");
        d.execSQL("INSERT INTO settings(key,value) VALUES('wallet_sender',''),('auto_send','0')");
    }
    @Override public void onUpgrade(SQLiteDatabase d, int oldV, int newV) {}
    public String setting(String key) { Cursor c=getReadableDatabase().rawQuery("SELECT value FROM settings WHERE key=?",new String[]{key}); try{return c.moveToFirst()?c.getString(0):"";} finally{c.close();} }
    public void setSetting(String key,String value){getWritableDatabase().execSQL("INSERT OR REPLACE INTO settings(key,value) VALUES(?,?)",new Object[]{key,value});}
    public boolean addCustomer(String name,String phone) {
        String normalized=TransferParser.normalizeName(name);
        if(normalized.isEmpty()||phone.trim().isEmpty())return false;
        ContentValues v=new ContentValues();v.put("payer_name",name.trim());v.put("normalized",normalized);v.put("phone",phone.trim());
        try{return getWritableDatabase().insertOrThrow("customers",null,v)>0;}catch(Exception e){return false;}
    }
    public boolean updateCustomer(String oldName,String name,String phone) {
        String oldNorm=TransferParser.normalizeName(oldName), norm=TransferParser.normalizeName(name);
        if(norm.isEmpty()||phone.trim().isEmpty())return false;
        ContentValues v=new ContentValues();v.put("payer_name",name.trim());v.put("normalized",norm);v.put("phone",phone.trim());
        try{return getWritableDatabase().update("customers",v,"normalized=?",new String[]{oldNorm})>0;}catch(Exception e){return false;}
    }
    public boolean deleteCustomer(String name){return getWritableDatabase().delete("customers","normalized=?",new String[]{TransferParser.normalizeName(name)})>0;}
    public List<String[]> customers() {
        List<String[]> l=new ArrayList<>();Cursor c=getReadableDatabase().rawQuery("SELECT payer_name,phone FROM customers ORDER BY payer_name",null);
        try{while(c.moveToNext())l.add(new String[]{c.getString(0),c.getString(1)});}finally{c.close();}return l;
    }
    public String customerPhone(String name) { Cursor c=getReadableDatabase().rawQuery("SELECT phone FROM customers WHERE normalized=?",new String[]{TransferParser.normalizeName(name)});try{return c.moveToFirst()?c.getString(0):null;}finally{c.close();}}
    public int importCards(int denomination,List<String> codes) {
        int count=0;SQLiteDatabase d=getWritableDatabase();d.beginTransaction();
        try{for(String code:codes){String clean=code.trim();if(clean.isEmpty())continue;ContentValues v=new ContentValues();v.put("denomination",denomination);v.put("code",clean);v.put("state","AVAILABLE");if(d.insertWithOnConflict("cards",null,v,SQLiteDatabase.CONFLICT_IGNORE)>0)count++;}d.setTransactionSuccessful();}finally{d.endTransaction();}return count;
    }
    public int available(int denomination){Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM cards WHERE denomination=? AND state='AVAILABLE'",new String[]{String.valueOf(denomination)});try{c.moveToFirst();return c.getInt(0);}finally{c.close();}}
    public long reserveCard(int denomination){SQLiteDatabase d=getWritableDatabase();d.beginTransaction();try{Cursor c=d.rawQuery("SELECT id FROM cards WHERE denomination=? AND state='AVAILABLE' ORDER BY id LIMIT 1",new String[]{String.valueOf(denomination)});long id=-1;try{if(c.moveToFirst())id=c.getLong(0);}finally{c.close();}if(id>0){int changed=d.update("cards",new ContentValues(){ {put("state","RESERVED");} },"id=? AND state='AVAILABLE'",new String[]{String.valueOf(id)});if(changed!=1)id=-1;}d.setTransactionSuccessful();return id;}finally{d.endTransaction();}}
    public String cardCode(long id){Cursor c=getReadableDatabase().rawQuery("SELECT code FROM cards WHERE id=?",new String[]{String.valueOf(id)});try{return c.moveToFirst()?c.getString(0):null;}finally{c.close();}}
    public void cardState(long id,String state){getWritableDatabase().execSQL("UPDATE cards SET state=? WHERE id=?",new Object[]{state,id});}
    public boolean addTransfer(String fp,String sender,String payer,int amount,String phone,long cardId,String status,String message){ContentValues v=new ContentValues();v.put("fingerprint",fp);v.put("sender",sender);v.put("payer_name",payer);v.put("amount",amount);v.put("phone",phone);if(cardId>0)v.put("card_id",cardId);v.put("status",status);v.put("message",message);v.put("created_at",System.currentTimeMillis());return getWritableDatabase().insertOrThrow("transfers",null,v)>0;}
    public boolean transferExists(String fp){Cursor c=getReadableDatabase().rawQuery("SELECT 1 FROM transfers WHERE fingerprint=?",new String[]{fp});try{return c.moveToFirst();}finally{c.close();}}
    public boolean hasRecentSimilarTransfer(String sender,String payer,int amount,long windowMs){long cutoff=System.currentTimeMillis()-windowMs;Cursor c=getReadableDatabase().rawQuery("SELECT 1 FROM transfers WHERE sender=? AND payer_name=? AND amount=? AND created_at>=? LIMIT 1",new String[]{sender,payer,String.valueOf(amount),String.valueOf(cutoff)});try{return c.moveToFirst();}finally{c.close();}}
    public void updateTransfer(String fp,String status,String msg){getWritableDatabase().execSQL("UPDATE transfers SET status=?,message=? WHERE fingerprint=?",new Object[]{status,msg,fp});}
    public void attachCardToTransfer(String fp,long card){getWritableDatabase().execSQL("UPDATE transfers SET card_id=?,status='SENDING',message='بانتظار نتيجة SMS' WHERE fingerprint=?",new Object[]{card,fp});}
    public void resolveTransferPhone(String fp,String phone){getWritableDatabase().execSQL("UPDATE transfers SET phone=?,status='WAITING',message='تمت مطابقة الاسم — جاهزة للمعالجة' WHERE fingerprint=? AND status='REVIEW'",new Object[]{phone,fp});}
    public void rematchPending(){Cursor c=getReadableDatabase().rawQuery("SELECT fingerprint,payer_name FROM transfers WHERE status='REVIEW' AND phone IS NULL",null);try{while(c.moveToNext()){String fp=c.getString(0),name=c.getString(1),phone=customerPhone(name);if(phone!=null)resolveTransferPhone(fp,phone);}}finally{c.close();}}
    public List<String[]> pendingTransfers(){List<String[]> l=new ArrayList<>();Cursor c=getReadableDatabase().rawQuery("SELECT fingerprint,payer_name,amount,phone,status FROM transfers WHERE status IN ('WAITING','REVIEW','FAILED') ORDER BY id DESC",null);try{while(c.moveToNext())l.add(new String[]{c.getString(0),c.getString(1),String.valueOf(c.getInt(2)),c.getString(3),c.getString(4)});}finally{c.close();}return l;}
    public List<String> recentTransfers(){List<String> l=new ArrayList<>();Cursor c=getReadableDatabase().rawQuery("SELECT payer_name,amount,status,message FROM transfers ORDER BY id DESC LIMIT 80",null);try{while(c.moveToNext())l.add(c.getString(0)+" • "+c.getInt(1)+" • "+c.getString(2)+" • "+c.getString(3));}finally{c.close();}return l;}
    public int sentCardsByDenomination(int denomination){Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM cards WHERE denomination=? AND state='SENT'",new String[]{String.valueOf(denomination)});try{c.moveToFirst();return c.getInt(0);}finally{c.close();}}
    public int transferCountByStatus(String status){Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM transfers WHERE status=?",new String[]{status});try{c.moveToFirst();return c.getInt(0);}finally{c.close();}}
    public int transfersToday(){long now=System.currentTimeMillis();java.util.Calendar cal=java.util.Calendar.getInstance();cal.setTimeInMillis(now);cal.set(java.util.Calendar.HOUR_OF_DAY,0);cal.set(java.util.Calendar.MINUTE,0);cal.set(java.util.Calendar.SECOND,0);cal.set(java.util.Calendar.MILLISECOND,0);Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM transfers WHERE created_at>=?",new String[]{String.valueOf(cal.getTimeInMillis())});try{c.moveToFirst();return c.getInt(0);}finally{c.close();}}
    public int countTransfers(){Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM transfers",null);try{c.moveToFirst();return c.getInt(0);}finally{c.close();}}
    public int sentCount(){Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM transfers WHERE status='SENT'",null);try{c.moveToFirst();return c.getInt(0);}finally{c.close();}}
    public int reviewCount(){Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM transfers WHERE status IN ('REVIEW','WAITING','FAILED')",null);try{c.moveToFirst();return c.getInt(0);}finally{c.close();}}
}
