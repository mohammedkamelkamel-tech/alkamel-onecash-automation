package com.alkamel.onecash;

import android.app.*;
import android.os.*;
import android.content.*;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.io.*;

public class MainActivity extends Activity {
    Db db; LinearLayout root,body;
    @Override public void onCreate(Bundle b){super.onCreate(b);db=new Db(this);requestPerms();showHome();}
    void requestPerms(){if(Build.VERSION.SDK_INT>=23&&checkSelfPermission(android.Manifest.permission.RECEIVE_SMS)!=0){requestPermissions(new String[]{android.Manifest.permission.RECEIVE_SMS,android.Manifest.permission.READ_SMS,android.Manifest.permission.SEND_SMS},12);}}
    TextView text(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setPadding(12,10,12,10);return t;}
    Button button(String s,Runnable r){Button b=new Button(this);b.setText(s);b.setOnClickListener(v->r.run());return b;}
    void base(String title){root=new LinearLayout(this);root.setOrientation(1);root.setPadding(12,12,12,12);root.setBackgroundColor(0xfff4f6fa);TextView h=text(title,22);h.setTextColor(0xff153b63);root.addView(h);ScrollView sc=new ScrollView(this);body=new LinearLayout(this);body.setOrientation(1);sc.addView(body);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));LinearLayout nav=new LinearLayout(this);nav.addView(button("الرئيسية",()->showHome()));nav.addView(button("العملاء",()->showCustomers()));nav.addView(button("المخزون",()->showInventory()));nav.addView(button("الحوالات",()->showTransfers()));nav.addView(button("الإعدادات",()->showSettings()));root.addView(nav);setContentView(root);}
    void showHome(){base("الكامل — أتمتة ون كاش");body.addView(text("إجمالي الحوالات: "+db.countTransfers(),18));body.addView(text("تم إرسالها: "+db.sentCount(),18));body.addView(text("تحتاج مراجعة: "+db.reviewCount(),18));body.addView(text("مخزون الكروت",18));for(int n:new int[]{100,200,250,500})body.addView(text(n+" ريال: "+db.available(n)+" كرت",16));body.addView(text("الإرسال الآلي: "+(db.setting("auto_send").equals("1")?"مفعّل":"متوقف"),16));body.addView(button("إضافة عميل",()->showCustomers()));body.addView(button("استيراد كروت",()->showInventory()));body.addView(button("إعداد مراقبة الرسائل",()->showSettings()));}
    void showCustomers(){base("إدارة العملاء");EditText name=new EditText(this);name.setHint("اسم المحوّل كما يظهر في ون كاش");EditText phone=new EditText(this);phone.setHint("رقم الجوال المستلم");phone.setInputType(3);body.addView(name);body.addView(phone);body.addView(button("حفظ العميل",()->{if(db.addCustomer(name.getText().toString(),phone.getText().toString())){toast("تم الحفظ");showCustomers();}else toast("تعذر الحفظ: الاسم مكرر أو البيانات ناقصة");}));for(String[] c:db.customers())body.addView(text(c[0]+" — "+c[1],16));}
    void showInventory(){base("مخزون الكروت");for(int n:new int[]{100,200,250,500}){body.addView(text(n+" ريال — المتوفر "+db.available(n),18));body.addView(button("استيراد TXT لفئة "+n,()->pickFile(n)));}body.addView(text("كل سطر في TXT يمثل كرتًا واحدًا. يتم تجاهل السطور الفارغة ومنع الأكواد المكررة.",14));}
    void pickFile(int denomination){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("text/plain");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,denomination);}
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){super.onActivityResult(requestCode,resultCode,data);if(resultCode!=RESULT_OK||data==null)return;try{Uri u=data.getData();InputStream in=getContentResolver().openInputStream(u);BufferedReader r=new BufferedReader(new InputStreamReader(in));List<String> codes=new ArrayList<>();String line;while((line=r.readLine())!=null)codes.add(line.trim());r.close();int n=db.importCards(requestCode,codes);toast("تم استيراد "+n+" كرت");showInventory();}catch(Exception e){toast("فشل استيراد الملف");}}
    void showTransfers(){base("سجل الحوالات");for(String s:db.recentTransfers())body.addView(text(s,16));}
    void showSettings(){base("الإعدادات");EditText sender=new EditText(this);sender.setHint("عنوان/رقم مرسل ون كاش كما يظهر في SMS");sender.setText(db.setting("wallet_sender"));body.addView(sender);body.addView(button("حفظ مرسل ون كاش",()->{db.setSetting("wallet_sender",sender.getText().toString().trim());toast("تم حفظ المرسل");}));Switch sw=new Switch(this);sw.setText("تفعيل الإرسال التلقائي (اختبر أولاً)");sw.setChecked(db.setting("auto_send").equals("1"));body.addView(sw);body.addView(button("حفظ إعداد الإرسال",()->{db.setSetting("auto_send",sw.isChecked()?"1":"0");toast(sw.isChecked()?"تحذير: الإرسال الآلي مفعّل":"الإرسال الآلي متوقف");}));body.addView(text("لا تفعّل الإرسال الآلي إلا بعد اختبار المرسل والرسالة ومطابقة الاسم والمخزون. إذا كان الاسم غير معروف أو الفئة فارغة فلن يتم الإرسال.",14));}
    void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
}
