package com.example.seproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    EditText atextView;
    EditText btextView;
    EditText S_ID,S_ID2;
    String ID_total;
    Button button;
    String val;
    int num;

    private GoogleMap mMap;
    SupportMapFragment mapFragment;

    Thread tracking_thread;

    FirebaseDatabase database;
    DatabaseReference bus;

    String serverMessage;

    MarkerOptions[] marker = new MarkerOptions[]{new MarkerOptions(), new MarkerOptions(), new MarkerOptions()};

    double l1 = 37.4220005, l2 = 122.0839996;

    Bus[] ABC = new Bus[]{new Bus(), new Bus(), new Bus()};

    int current,busy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindingView();

        mapFragment.getMapAsync(this);

        database = FirebaseDatabase.getInstance();
        bus = database.getReference("bus");

        alertBusy(2);


        TrackHandler myHandler = new TrackHandler();
        tracking_thread = new Thread(new Runnable(){
            public void run(){

                try{
                    while(true) {
                        Thread.sleep(1000);
                        Message msg = myHandler.obtainMessage();
                        myHandler.sendMessage(msg);
                    }
                }
                catch(Exception ex){
                    Log.e("MainActivity", "Exception in processing message.", ex);
                }

            }
        });

        tracking_thread.start();



        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                num++;
                Intent intent = new Intent(getApplicationContext(),InputActivity.class);
                Bundle bundle = new Bundle();
                if(num<=5){
                     val = atextView.getText().toString();
                     ID_total = S_ID.getText().toString();

                }else{
                    val = btextView.getText().toString();
                    ID_total = "";
                    ID_total = S_ID2.getText().toString();
                }
                bundle.putString("total", ID_total);
                bundle.putString("countNum",val);
                intent.putExtras(bundle);

                startActivityForResult(intent,101);

            }
        });


        database.getReference("bus").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Log.d("MainActivity", "ValueEventListener : " + snapshot.getValue());
                    serverMessage = (String) snapshot.getValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void bindingView(){
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        button = findViewById(R.id.inputButton);
        atextView = findViewById(R.id.aId);
        btextView = findViewById(R.id.bId);
        S_ID = findViewById(R.id.edit1);
        S_ID2 = findViewById(R.id.edit2);
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);

        Bundle bundle = data.getExtras();
        int count = bundle.getInt("number");
        String value = Integer.toString(count);
        String ID = bundle.getString("student_id");
        if(num <= 5){
            S_ID.setText(ID);
            atextView.setText(value);
        }
       else {
            S_ID2.setText(ID);
            btextView.setText(value);
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;

        LatLng SEOUL = new LatLng(37.4220005, 122.0839996);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(SEOUL));                 // 초기 위치
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));                         // 줌의 정도
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);                           // 지도 유형 설정

    }

    public class TrackHandler extends Handler {
        public void handleMessage(Message msg){
            jsonParsing(serverMessage);

            l1 += 0.0010115;
            l2 += 0.0010115;

            Log.d("MainActivity", String.valueOf(l1) + String.valueOf(l2));


            marker[0].position(new LatLng(l1, l2));

            mMap.clear();

            mMap.addMarker(marker[0]);

        }
    }

    public void trackLocation(){


//
//        marker[i].position(new LatLng(l1, l2));
//
//        marker[i].title("seoul");                         // 마커 제목
//        marker[i].snippet("한국의 수도");         // 마커 설명
//        mMap.addMarker(marker[i]);
    }

    public void alertBusy(int num){
        updateDb(bus.child("busy"), num);
    }

    public void updateDb(DatabaseReference dr, Object value){
        dr.setValue(value);
    }

    protected void jsonParsing(String message) {

        // String example = "{\"A\":{\"l1\":\"3\", \"l2\":\"1\", \"state\":\"true\"}, \"B\":{\"l1\":\"1\", \"l2\":\"1\", \"state\":\"false\"}, \"current\":1, \"C\":{\"l1\":\"3\", \"l2\":\"3\", \"state\":\"false\"}, \"busy\":\"2\"}";

        try {
            JSONObject parse_item = new JSONObject(message);

            JSONObject obj = (JSONObject) parse_item.get("A");
            ABC[0].l1 = obj.getDouble("l1");
            ABC[0].l2 = obj.getDouble("l2");
            ABC[0].state = obj.getBoolean("state");

            JSONObject obj2 = (JSONObject) parse_item.get("B");
            ABC[1].l1 = obj2.getDouble("l1");
            ABC[1].l2 = obj2.getDouble("l2");
            ABC[1].state = obj2.getBoolean("state");

            JSONObject obj3 = (JSONObject) parse_item.get("C");
            ABC[2].l1 = obj3.getDouble("l1");
            ABC[2].l2 = obj3.getDouble("l2");
            ABC[2].state = obj3.getBoolean("state");

            current = parse_item.getInt("current");
            busy = parse_item.getInt("busy");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}