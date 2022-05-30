package com.example.seproject;

import androidx.annotation.DrawableRes;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import android.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
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
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    Button button;
    EditText inputText;
    TextView textView0;
    TextView textView1;
    TextView textView2;
    TextView[] edit = new TextView[3];

    private GoogleMap mMap;
    private UiSettings mUiSettings;
    SupportMapFragment mapFragment;
    PolylineOptions track1, track2;

    Thread tracking_thread;

    FirebaseDatabase database;
    DatabaseReference bus;

    String serverMessage = "";

    MarkerOptions[] marker;
    MarkerOptions markerStop1, markerStop2;

    double l1 = 37.4220005, l2 = 122.0839996;

    Bus[] ABC = new Bus[]{new Bus(), new Bus(), new Bus()};

    int[] numOfReservation = new int[]{0, 0, 0};
    int[] MAXNUM = {17, 22, 22};

    String[] sId = {"", "", ""};

    int current, busy, currentReset;

    public List<Student> userList ;
    DataAdapter mDbHelper;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindingView();

        mapFragment.getMapAsync(this);
        MapsInitializer.initialize(this);

        database = FirebaseDatabase.getInstance();
        bus = database.getReference("bus");

        alertBusy(0);
        current = 0;

        initLoadDB();

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
                if(!checkValidSid())
                    return;

                if(getName() == "") {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("오류 메시지")
                            .setMessage("잘못된 학번입니다.")
                            .create()
                            .show();

                    inputText.setText("");
                }

                else{
                    if(checkEnough()) {

                        numOfReservation[current]++;
                        setCount(current);

                        viewList();
                    }
                    else{
                        if(busy == 3){
                            // 예약 전부 마감 알림
                        }
                        else{
                            busy++;
                            alertBusy(busy);
                            current = (current + 1) % 3;
                            numOfReservation[current]++;
                            setCount(current);

                            viewList();
                        }
                    }
                }
            }
        });

    }



    private void bindingView(){
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        button = findViewById(R.id.inputButton);
        inputText = findViewById(R.id.inputText);
        textView0 = findViewById(R.id.aId);
        textView1 = findViewById(R.id.bId);
        textView2 = findViewById(R.id.cId);

        edit[0] = findViewById(R.id.edit1);
        edit[1] = findViewById(R.id.edit2);
        edit[2] = findViewById(R.id.edit3);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;

        mUiSettings = mMap.getUiSettings();
        mUiSettings.setTiltGesturesEnabled(false);
        mUiSettings.setRotateGesturesEnabled(false);
        mUiSettings.setScrollGesturesEnabled(false);
        mUiSettings.setZoomGesturesEnabled(false);
        mUiSettings.setCompassEnabled(false);


        BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.icon);
        Bitmap b=bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, 90, 70, false);

        BitmapDrawable bitmapdraw1=(BitmapDrawable)getResources().getDrawable(R.drawable.stop);
        Bitmap b1=bitmapdraw1.getBitmap();
        Bitmap smallMarker1 = Bitmap.createScaledBitmap(b1, 60, 70, false);

        marker = new MarkerOptions[]{
                new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(smallMarker)),
                new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(smallMarker)),
                new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(smallMarker))};

        markerStop1 = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(smallMarker1));
        markerStop2 = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(smallMarker1));

        LatLng CENTER = new LatLng(37.452033, 127.131109);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(CENTER));                 // 초기 위치
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14.8f));
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);                           // 지도 유형 설정

        track1 = new PolylineOptions()
                .add(
                        new LatLng(37.450552, 127.127534),
                        new LatLng(37.449944, 127.129903),
                        new LatLng(37.450950, 127.130342),
                        new LatLng(37.451208, 127.130873),
                        new LatLng(37.452233, 127.131483),
                        new LatLng(37.453076, 127.133614),
                        new LatLng(37.453529, 127.134208),
                        new LatLng(37.453893, 127.134787),
                        new LatLng(37.454318, 127.134879)
                );

        track2 = new PolylineOptions()
                .add(
                        new LatLng(37.451886, 127.131202),
                        new LatLng(37.452604, 127.130563),
                        new LatLng(37.452472, 127.130028),
                        new LatLng(37.452064, 127.129487),
                        new LatLng(37.451708, 127.127530),
                        new LatLng(37.450552, 127.127534)
                        );
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

        mMap.addMarker(markerStop1.position(new LatLng(37.450912, 127.126884)));
        mMap.addMarker(markerStop2.position(new LatLng(37.454461, 127.134890)));

        mMap.addPolyline(track1);
        mMap.addPolyline(track2);

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
        numOfReservation[currentReset] = 0;
        sId[currentReset] = "";
        edit[currentReset].setText(sId[currentReset]);
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
                currentReset = parse_item.getInt("current");
                resetList();
            }

            busy = parse_item.getInt("busy");

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    public void setCount(int current){

        if(current==0){
            textView0.setText(Integer.toString(numOfReservation[current]));
        }else if(current==1){
            textView1.setText(Integer.toString(numOfReservation[current]));
        }else{
            textView2.setText(Integer.toString(numOfReservation[current]));
        }
    }

    private void viewList(){
        String inputSId = String.valueOf(inputText.getText());
        sId[current] += inputSId.substring(0,2) + "****" + inputSId.substring(6) + " " +
                getName().substring(0,1) + "*" + getName().substring(2) + "\n";
        edit[current].setText(sId[current]);
        inputText.setText("");
        Log.d("aaaaaaaa", sId[current]);
    }

    private boolean checkValidSid() {
        if(inputText.getText().toString().equals(""))
            return false;

        return true;
    }

    private void initLoadDB() {

        mDbHelper = new DataAdapter(getApplicationContext());
        mDbHelper.createDatabase();
        mDbHelper.open();

        // db에 있는 값들을 model을 적용해서 넣는다.
        userList = mDbHelper.getTableData();

        // db 닫기
        mDbHelper.close();
    }

    private String getName() {
        db = mDbHelper.mDbHelper.getReadableDatabase();
        Cursor c = db.query("moodang", null, null, null, null, null, null);
        /* query (String table, String[] columns, String selection, String[]
         * selectionArgs, String groupBy, String having, String orderBy)
         */

        while (c.moveToNext()) {
            int _id = c.getInt(c.getColumnIndexOrThrow("student_id"));
            String name = c.getString(c.getColumnIndexOrThrow("name"));

            try {
                if (_id == Integer.parseInt(inputText.getText().toString()))
                    return name;
            }
            catch(Exception e){
                return "";
            }

        }

        return "";
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceId) {
        Drawable background = ContextCompat.getDrawable(context, R.drawable.ic_baseline_directions_bus_24);
        background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        vectorDrawable.setBounds(40, 20, vectorDrawable.getIntrinsicWidth() + 40, vectorDrawable.getIntrinsicHeight() + 20);
        Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(), background.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}