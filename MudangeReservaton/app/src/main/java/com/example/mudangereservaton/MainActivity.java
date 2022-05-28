package com.example.mudangereservaton;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    EditText atextView;
    EditText btextView;
    EditText ctextView;

    EditText inputText;


    Button button;
    String count;
    int num = 0, countTotal=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        button = findViewById(R.id.inputButton);
        inputText = findViewById(R.id.inputText);

        //카운트
        atextView = findViewById(R.id.aId);
        btextView = findViewById(R.id.bId);
        ctextView = findViewById(R.id.cId);



        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                countTotal++;
                num++;
                count = String.valueOf(num);
                if(countTotal <= 5) {


                    atextView.setText(count);
                }
                else if(10 >= countTotal && countTotal > 5){

                    btextView.setText(count);
                }
                else {

                    ctextView.setText(count);
                }

                if(num == 5)
                    num = 0;
                if(num ==10)
                    num = 0;

            }
        });

    }

}