package com.rhuanecc.oscilino;

import java.util.ArrayList;

/**
 * Created by rhuan on 2016-09-20.
 */

public class Buffer {
    private Byte ch;
    private ArrayList<Float> pontos;
    private Long cks;

    public Buffer() {
        ch = null;
        pontos = new ArrayList<>();
        cks = null;
    }

    public void setCks(long cks) {
        this.cks = cks;
    }

    public void setCh(byte ch) {
        this.ch = ch;
    }

    public Byte getCh() {
        return ch;
    }

    public ArrayList<Float> getPontos() {
        return pontos;
    }

    public boolean isValid(){
        if(ch == null || cks == null) //se nao recebeu desde o começo
            return false;

        long soma = 0;
        for(Float p : pontos)
            soma+=p;

        if(soma == cks.longValue()) //se checksum bater
            return true;
        else
            return false;
    }

    public void add(Float value){
        pontos.add(value);
    }

    public void setScale(float step){
        for(Float p : pontos)
            p = p*step;
    }

    @Override
    public String toString() {
        String s = "[";

        if(ch!= null)
            s = s.concat(ch.toString()+";");
        else
            s = s.concat("ch=null;");

        for(Float p : pontos){
            s = s.concat(p.toString()+",");
        }

        if(cks != null)
            s = s.concat(cks.toString()+";");
        else
            s = s.concat("cks=null;");

        s = s.concat("]");

        return s;
    }
}