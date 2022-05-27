package com.example.seproject;
import org.json.JSONException;
import org.json.JSONObject;

public class Parser {

    Bus[] busList = new Bus[3];
    int current,busy;

    public class Bus {
        private String l1;
        private String l2;
        private String state;

        public String getL1() {
            return l1;
        }
        public String getL2() {
            return l2;
        }
        public String getState(){
            return state;
        }
        public void setL1(String l1) {
            this.l1 = l1;
        }
        public void setL2(String l2) {
            this.l2 = l2;
        }
        public void setState(String state) {
            this.state = state;
        }
        public Bus(String l1,String l2, String state){
            this.l1 = l1;
            this.l2 = l2;
            this.state = state;
        }
    }

    protected void jsonParsing(String message) {

       // String example = "{\"A\":{\"l1\":\"3\", \"l2\":\"1\", \"state\":\"true\"}, \"B\":{\"l1\":\"1\", \"l2\":\"1\", \"state\":\"false\"}, \"current\":1, \"C\":{\"l1\":\"3\", \"l2\":\"3\", \"state\":\"false\"}, \"busy\":\"2\"}";

        try {
            JSONObject parse_item = new JSONObject(message);

            JSONObject obj = (JSONObject) parse_item.get("A");
            String Al1 = obj.getString("l1");
            String Al2 = obj.getString("l2");
            String Astate = obj.getString("state");
            busList[0]= new Bus(Al1,Al2,Astate);

            JSONObject obj2 = (JSONObject) parse_item.get("B");
            String Bl1 = obj2.getString("l1");
            String Bl2 = obj2.getString("l2");
            String Bstate = obj2.getString("state");
            busList[1] = new Bus(Bl1,Bl2,Bstate);

            JSONObject obj3 = (JSONObject) parse_item.get("C");
            String Cl1 = obj3.getString("l1");
            String Cl2 = obj3.getString("l2");
            String Cstate = obj3.getString("state");
            busList[2] = new Bus(Cl1,Cl2,Cstate);

            current = parse_item.getInt("current");
            busy = parse_item.getInt("busy");

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //parsing 출력시 예제
    /*("BUS A = l1:"+busList[0].getL1()+",l2:"+busList[0].getL2()+", state:"+busList[0].getState()
       +"\n"+"BUS B = l1:"+busList[1].getL1()+",l2:"+busList[1].getL2()+", state:"+busList[1].getState()
               +"\n"+"BUS C = l1:"+busList[2].getL1()+",l2:"+busList[2].getL2()+", state:"+busList[2].getState()
       +"\n"+"Current:"+current+"\n"+"Busy:"+busy);
    * */
}
