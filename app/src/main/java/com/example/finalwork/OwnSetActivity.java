package com.example.finalwork;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class OwnSetActivity extends AppCompatActivity {

    EditText edit1;
    EditText edit2;
    EditText edit3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_own_set);

        edit1=findViewById(R.id.editTextNumber);
        edit2=findViewById(R.id.editTextNumber2);
        edit3=findViewById(R.id.editTextNumber3);
        Intent main2 = getIntent();
        double rateDollar = main2.getDoubleExtra("rateDollar",0.0d);
        double rateEuro = main2.getDoubleExtra("rateEuro",0.0d);
        double rateWon = main2.getDoubleExtra("rateWon",0.0d);

        edit1.setText(""+rateDollar);
        edit2.setText(""+rateEuro);
        edit3.setText(""+rateWon);
    }

    public void saveRate(View v){
        //获取新的值
        String strRateDollar=edit1.getText().toString();
        String strRateEuro=edit2.getText().toString();
        String strRateWon=edit3.getText().toString();
        if(strRateDollar.length()==0||strRateEuro.length()==0||strRateWon.length()==0){
            return;
        }
        //保存到Bundle
        Intent main =getIntent();
        Bundle bd1 = new Bundle();
        bd1.putDouble("rateDollar",Double.parseDouble(strRateDollar));
        bd1.putDouble("rateEuro",Double.parseDouble(strRateEuro));
        bd1.putDouble("rateWon",Double.parseDouble(strRateWon));
        main.putExtras(bd1);
        setResult(2,main);
        //返回到调用页面
        finish();
        //startActivity(main);

    }
}