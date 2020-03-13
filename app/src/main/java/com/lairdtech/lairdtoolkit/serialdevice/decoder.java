package com.lairdtech.lairdtoolkit.serialdevice;

public class decoder {
    String s;
    float Max = 10000f;

    public decoder(String s, int max) {
        this.s = s;
        Max = max;
    }

    public decoder(String s) {
        this.s = s;
    }

    public int decoderData (){
        int n = Integer.parseInt(s);
        int data = n % 10000;
//        data = data/1000;
        return data ;
    }

    public int decoderChannel (){
        int n = Integer.parseInt(s);
        int data = n / 10000;
        return data ;
    }
    public int mappingColor(){
        int  color = (int)(256 - (255 * (decoderData()/Max)));
        return color;
    }

    public int MVIC(){
        int mvic = (int)( (decoderData()/Max) * 100);
        return mvic;
    }
}