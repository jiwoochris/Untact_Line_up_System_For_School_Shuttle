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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


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

        LatLng SEOUL = new LatLng(37.56, 126.97);

        MarkerOptions markerOptions = new MarkerOptions();         // 마커 생성
        markerOptions.position(SEOUL);
        markerOptions.title("서울");                         // 마커 제목
        markerOptions.snippet("한국의 수도");         // 마커 설명
        mMap.addMarker(markerOptions);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(SEOUL));                 // 초기 위치
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));                         // 줌의 정도
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);                           // 지도 유형 설정

    }
}