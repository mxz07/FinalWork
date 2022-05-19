package com.example.finalwork;

import androidx.annotation.NonNull;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class RateListActivity extends ListActivity implements Runnable, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private SimpleAdapter listItemAdapter;
    private ArrayList<HashMap<String,String>> listItems;
    private String TAG = "myList2";
    Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        SharedPreferences sharedPreferences = getSharedPreferences("myRate", Activity.MODE_PRIVATE);
        //构建hashmap
        ArrayList<HashMap<String,String>>listItems = new ArrayList<HashMap<String, String>>();
        for(int i=0;i<27;i++){
            HashMap<String,String> map = new HashMap<String,String>();
            map.put("huobi",sharedPreferences.getString("List"+i,"0---->0").split("---->")[0]);
            map.put("huilv",sharedPreferences.getString("List"+i,"0---->0").split("---->")[1]);
            listItems.add(map);
        }
        MyAdapter myAdapter = new MyAdapter(this,
                R.layout.list_item,
                listItems);
        this.setListAdapter(myAdapter);

        //获取到控件内容
        getListView().setOnItemClickListener(this);
        getListView().setOnItemLongClickListener(this);
    }

    @Override
    public void run() {
        Log.i("RUN","run");
        //获取msg对像用于返回主线程
        Message msg = handler.obtainMessage();
        msg.what=5;
        msg.obj = "Hello from run()";
        handler.sendMessage(msg);

        SharedPreferences sharedPreferences = getSharedPreferences("myRate", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor =  sharedPreferences.edit();

        String url = "https://www.boc.cn/sourcedb/whpj/index.html";
        List<String> retList = new ArrayList<String>();
        String Time=sharedPreferences.getString("Time","2022-05-18");
        String curDateStr = (new SimpleDateFormat("yyyy-MM-dd")).format(new Date());
        if(curDateStr.equals(Time)){
            //如果相等，则不从网络中获取数据
            Log.i("run","日期相等，从数据库中获取数据");
            DBManager dbManager = new DBManager(RateListActivity.this);
            for(RateItem rateItem : dbManager.listAll()){
                retList.add(rateItem.getCurName() + "=>" + rateItem.getCurRate());
            }
        }else {
            Log.i("run", "日期相等，从网络中获取在线数据");
            //获取网络数据

            try {
                List<RateItem> rateList = new ArrayList<RateItem>();
                Document doc = Jsoup.connect(url).get();
                Elements tables = doc.getElementsByTag("table");
                Element table1 = tables.get(1);
                Elements tds = table1.getElementsByTag("td");
                for (int i = 0; i < 27; i++) {
                    Element td1 = tds.get(i * 8);
                    Element td2 = tds.get(i * 8 + 5);
                    retList.add(td1.text() + "---->" + td2.text());
                    Log.i("data", retList.get(i));
                    editor.putString("List" + i, retList.get(i));

                    RateItem rateItem = new RateItem(td1.text(), td2.text());
                    rateList.add(rateItem);
                }
                DBManager dbManager = new DBManager(RateListActivity.this);
                dbManager.deleteAll();
                Log.i("db","删除所有记录");
                dbManager.addAll(rateList);
                Log.i("db","添加新记录集");
                editor.commit();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //更新记录日期
            SharedPreferences sp = getSharedPreferences("myrate", Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = sp.edit();
            edit.putString(Time, curDateStr);
            edit.commit();
            Log.i("run","更新日期结束：" + curDateStr);
        }

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Log.i(TAG,"onItemClick:parent=" + adapterView);
        Log.i(TAG,"onItemClick:view=" + view);
        Log.i(TAG,"onItemClick:position=" + i);
        Log.i(TAG,"onItemClick:id=" + l);
        //点击行获得相应的数据项，从相应的数据项中取数据（map）
        HashMap<String,String> map = (HashMap<String,String>)getListView().getItemAtPosition(i);
        String titleStr=map.get("huobi");
        String detailStr=map.get("huilv");
        Log.i(TAG, titleStr+detailStr);
        //通过view
        TextView title = (TextView) view.findViewById(R.id.textView);
        TextView detail = (TextView) view.findViewById(R.id.textView2);
        String title2 = String.valueOf(title.getText());
        String detail2 = String.valueOf(detail.getText());
        Log.i(TAG, title2+detail2);

        Intent rateCalc = new Intent(this,RatecalActivity.class);
        rateCalc.putExtra("huobi",titleStr);
        rateCalc.putExtra("huilv",Float.parseFloat(detailStr));
        startActivity(rateCalc);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view,final int i, long l) {
        Log.i(TAG, "onItemLongClick: 长按列表项position=" + i);
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("提示:").setMessage("请确认是否删除当前数据").setPositiveButton("是",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i(TAG, "onClick: 对话框事件处理");
                listItems.remove(i);
                listItemAdapter.notifyDataSetChanged();
            }
        }).setNegativeButton("否",null);
        builder.create().show();
        return true;
    }
}