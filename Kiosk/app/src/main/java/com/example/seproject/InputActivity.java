package com.example.seproject;

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

public class InputActivity extends AppCompatActivity {
    EditText S_ID;
    Button button;
    String ID;
     int count ;
    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        context = getApplicationContext();
        S_ID = findViewById(R.id.idInput);

        Intent passedIntent = getIntent();
        Bundle bundle = passedIntent.getExtras();

        if(passedIntent != null){
            ID = bundle.getString("total");
            String val = bundle.getString("countNum");
            count = Integer.parseInt(val);
        }

        button = findViewById(R.id.loginButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                count++;
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                ID = ID + "\n" + S_ID.getText().toString();
                bundle.putString("student_id",ID);
                bundle.putInt("number",count);
                intent.putExtras(bundle);
                setResult(RESULT_OK,intent);

                finish();

            }
        });
    }

}