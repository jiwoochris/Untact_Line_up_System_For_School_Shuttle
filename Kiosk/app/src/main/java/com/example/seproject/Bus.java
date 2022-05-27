package com.example.seproject;

public class Bus {
    protected double l1;
    protected double l2;
    protected boolean state;

    public Bus(){
        this.l1 = 37.451943;
        this.l2 = 127.130543;
        this.state = false;
    }
    public Bus(double l1,double l2, boolean state){
        this.l1 = l1;
        this.l2 = l2;
        this.state = state;
    }

}
