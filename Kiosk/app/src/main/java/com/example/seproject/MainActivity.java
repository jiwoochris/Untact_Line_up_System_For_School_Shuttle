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

    Button button;
    EditText inputText;

    private GoogleMap mMap;
    SupportMapFragment mapFragment;

    Thread tracking_thread;

    FirebaseDatabase database;
    DatabaseReference bus;

    String serverMessage;

    MarkerOptions[] marker = new MarkerOptions[]{new MarkerOptions(), new MarkerOptions(), new MarkerOptions()};

    double l1 = 37.4220005, l2 = 122.0839996;

    Bus[] ABC = new Bus[]{new Bus(), new Bus(), new Bus()};

    int[] numOfReservation = new int[]{0, 0, 0};
    int[] MAXNUM = {17, 17, 21};

    int current, busy, currentReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindingView();

        mapFragment.getMapAsync(this);

        database = FirebaseDatabase.getInstance();
        bus = database.getReference("bus");

        alertBusy(0);

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


        database.getInstance().getReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Log.d("MainActivity", "ValueEventListener : " + snapshot.getValue());
                    serverMessage = snapshot.getValue().toString();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkEnough())
                    numOfReservation[current]++;
                else{
                    if(busy == 3){
                        // 예약 전부 마감 알림
                    }
                    else{
                        busy++;
                        alertBusy(busy);
                        current = (current + 1) % 3;
                        numOfReservation[current]++;
                    }
                }
            }
        });

    }

    private void bindingView(){
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        button = findViewById(R.id.inputButton);
        inputText = findViewById(R.id.inputText);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;

        LatLng CENTER = new LatLng(37.453444, 127.131636);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(CENTER));                 // 초기 위치
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14.8f));                 // 줌의 정도
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);                           // 지도 유형 설정

    }

    public class TrackHandler extends Handler {
        public void handleMessage(Message msg){

            jsonParsing();

            trackLocation();

        }
    }

    public void trackLocation(){
        for(int i=0; i<3; i++)
            marker[i].position(new LatLng(ABC[i].l1, ABC[i].l2));

        mMap.clear();

        for(int i=0; i<3; i++)
            if(ABC[i].state == true)
                mMap.addMarker(marker[i]);
    }

    public void alertBusy(int num){
        updateDb(bus.child("busy"), num);
    }

    public void updateDb(DatabaseReference dr, Object value){
        dr.setValue(value);
    }

    private boolean checkEnough(){
        if(numOfReservation[current] < MAXNUM[current])
            return true;
        else
            return false;
    }

    private void resetList(){
        // currentReset 정원 0으로 초기화해주는 함수
    }

    protected void jsonParsing() {

        // String example = "{\"A\":{\"l1\":\"3\", \"l2\":\"1\", \"state\":\"true\"}, \"B\":{\"l1\":\"1\", \"l2\":\"1\", \"state\":\"false\"}, \"current\":1, \"C\":{\"l1\":\"3\", \"l2\":\"3\", \"state\":\"false\"}, \"busy\":\"2\"}";

        try {
            JSONObject parse_item = new JSONObject(serverMessage);

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

            if(currentReset != parse_item.getInt("current")){
                resetList();
                currentReset = parse_item.getInt("current");
            }

            busy = parse_item.getInt("busy");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}