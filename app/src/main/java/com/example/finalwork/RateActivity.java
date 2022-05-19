package com.example.finalwork;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class RateActivity extends AppCompatActivity implements Runnable{
    private final String TAG="tag";
    private double rateDollar=0.1577;
    private double rateEuro=0.1418;
    private double rateWon=191.2046;
    TextView res;
    EditText edit;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate);

        SharedPreferences  sharedPreferences = getSharedPreferences("myRate", Activity.MODE_PRIVATE);
        rateDollar=Double.parseDouble(sharedPreferences.getString("rateDollar","0.1577"));
        rateEuro=Double.parseDouble(sharedPreferences.getString("rateEuro","0.1418"));
        rateWon=Double.parseDouble(sharedPreferences.getString("rateWon","191.2046"));

        Log.i("data",""+rateDollar);
        Log.i("data",""+rateEuro);
        Log.i("data",""+rateWon);

        res=findViewById(R.id.hello);
        edit=findViewById(R.id.inputNum);
        //在主线程中开启子线程
        Thread t = new Thread(this);
        t.start();
        //处理线程消息
        handler = new Handler(Looper.myLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {//msg为线程中的msg
                if(msg.what==5){
                    String str = (String) msg.obj;
                    Log.i("revThreadMsg",str);
                }
                super.handleMessage(msg);
            }
        };
    }
    //返回页面用的函数
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode==1&&resultCode==2){
            Bundle bundle = data.getExtras();
            rateDollar = bundle.getDouble("rateDollar",0.1d);
            rateEuro = bundle.getDouble("rateEuro",0.1d);
            rateWon = bundle.getDouble("rateWon",0.1d);

            //将新的汇率保存在sharePreference中
            SharedPreferences  sharedPreferences = getSharedPreferences("myRate", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor =  sharedPreferences.edit();
            editor.putString("rateDollar",""+rateDollar);
            editor.putString("rateEuro",""+rateEuro);
            editor.putString("rateWon",""+rateWon);
            editor.commit();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    //下拉选项菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu,menu);
        return true;
    }
    //处理菜单按钮事件
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            openConfig();
        }
        if (item.getItemId() == R.id.openLIST) {
            Intent list = new Intent(this, RateListActivity.class);//这个intent对象在ma2中被getIntent获取
            startActivity(list);
        }
        return super.onOptionsItemSelected(item);
    }

    //实现Runnable接口中的run方法
    @Override
    public void run() {
        Log.i("RUN","run");
        for(int i=1;i<6;i++){
            Log.i("sleep","sleep"+i);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Bundle bundle=new Bundle();
        try {
            Document doc= null;
            doc = Jsoup.connect("http://en.wikipedia.org").get();
            Log.i(TAG,"run: "+doc.title());
            Elements tables = doc.getElementsByTag("table");
            Element table6=tables.get(5);
            Elements tds=table6.getElementsByTag("td");
            for(int i=0;i<tds.size();i+=8){
                Element td1=tds.get(i);
                Element td2=tds.get(i+5);
                String str1=td1.text();
                String val=td2.text();
                if("美元".equals(str1)){
                    bundle.putFloat("dollar-rate",100f/Float.parseFloat(val));
                }else if("欧元".equals(str1)){
                    bundle.putFloat("dollar-rate",100f/Float.parseFloat(val));
                }else if("韩元".equals(str1)){
                    bundle.putFloat("dollar-rate", 100f/Float.parseFloat(val));
                }
                Log.i(TAG,"run:"+str1+"==>"+val);
                float v=100f/Float.parseFloat(val);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Message msg = handler.obtainMessage(5);
        msg.obj=bundle;
        handler.sendMessage(msg);


    }
    //处理汇率转换事件
    public void change(View v){
        Log.i("main","onClick Btn");
        String strInputRMB=edit.getText().toString();
        if (strInputRMB.length() == 0){
            res.setText("请输入金额！");
            return;
        }
        double inputRMB=Float.parseFloat(strInputRMB);
        switch (v.getId()){
            case R.id.btnDollar:
                res.setText(""+inputRMB*rateDollar+"＄");
                break;
            case R.id.btnEuro:
                res.setText(""+inputRMB*rateEuro+"€");
                break;
            case R.id.btnWon:
                res.setText(""+inputRMB*rateWon+"＄");
                break;
            default:
                break;
        }
    }
    //提取的页面转移方法
    private void openConfig() {
        Intent main2 = new Intent(this, OwnSetActivity.class);//这个intent对象在ma2中被getIntent获取
        main2.putExtra("rateDollar",rateDollar);
        main2.putExtra("rateEuro",rateEuro);
        main2.putExtra("rateWon",rateWon);
        Log.i("change","change2Main2");
        //startActivity(main2);
        startActivityForResult(main2,1);
    }

}