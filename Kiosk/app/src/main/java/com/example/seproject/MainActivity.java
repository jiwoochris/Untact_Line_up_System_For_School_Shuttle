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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
    private Context context;

    private GoogleMap mMap;
    Thread tracking_thread;

    FirebaseDatabase database;
    DatabaseReference bus;

    int A = 0;
    int B = 0;
    int C = 0;

    MarkerOptions[] marker = new MarkerOptions[]{new MarkerOptions(), new MarkerOptions(), new MarkerOptions()};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        database = FirebaseDatabase.getInstance();
        bus = database.getReference("bus");


        TrackHandler myHandler = new TrackHandler();
        tracking_thread = new Thread(new Runnable(){
            public void run(){

                try{
                    while(true) {
                        Thread.sleep(3000);
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

        context = getApplicationContext();
        button = findViewById(R.id.inputButton);
        atextView = findViewById(R.id.aId);
        btextView = findViewById(R.id.bId);

        S_ID = findViewById(R.id.edit1);
        S_ID2 = findViewById(R.id.edit2);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                num++;
                Intent intent = new Intent(context,InputActivity.class);
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
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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

        LatLng SEOUL = new LatLng(37.4220005, -122.0839996);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(SEOUL));                 // 초기 위치
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));                         // 줌의 정도
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);                           // 지도 유형 설정

    }

    public class TrackHandler extends Handler {
        public void handleMessage(Message msg){
            trackLocation();
        }
    }

    public void trackLocation(){
        String[] abc = new String[]{"A", "B", "C"};

        database.getInstance().getReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Log.d("MainActivity", "ValueEventListener : " + snapshot.getValue());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError){

            }
        });


//
//        marker[i].position(new LatLng(l1, l2));
//
//        marker[i].title("seoul");                         // 마커 제목
//        marker[i].snippet("한국의 수도");         // 마커 설명
//        mMap.addMarker(marker[i]);
    }

    public void alertBusy(int num){
        updateDb(database.getReference("busy"), num);
    }

    public void updateDb(DatabaseReference dr, Object value){
        dr.setValue(value);
    }

}