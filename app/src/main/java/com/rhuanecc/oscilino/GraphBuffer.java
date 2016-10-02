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
        int size = GraphActivity.graphPointsNumber *GraphActivity.takeSampleEvery;    //tamanho do buffer de acordo com escala de tempo
        while (graphBuffer.size() >= size)   //remove pontos mais antigos
            graphBuffer.remove(0);
        graphBuffer.add(p);
    }
}
