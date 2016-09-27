package com.rhuanecc.oscilino;

import java.util.ArrayList;

/**
 * Created by rhuan on 2016-09-27.
 */

public class GraphBuffer {
    private static ArrayList<Float> graphBuffer = new ArrayList<>();

    public static synchronized ArrayList<Float> getInstance() {
        return (ArrayList<Float>) graphBuffer.clone();          //retorna copia atual do array
    }

    public static synchronized void add(Float p) {
        if (graphBuffer.size() >= GraphActivity.POINTS_COUNT)   //limita em 1000 pontos retirando mais antigo
            graphBuffer.remove(0);
        graphBuffer.add(p);
    }
}
