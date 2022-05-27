package com.example.driverui;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button button;
    int num = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.drivebutton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(num % 2 == 1){
                    button.setBackgroundColor(Color.RED);
                    button.setText("중지");
                } else {
                    button.setBackgroundColor(Color.parseColor("#7E7E7E"));
                    button.setText("운행");
                }
                num++;
            }
        });
    }
}