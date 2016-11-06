package com.rhuanecc.oscilino;

import java.util.ArrayList;

/**
 * Created by rhuan on 2016-09-27.
 */

public class GraphBuffer {
    private static ArrayList<Integer> graphBuffer = new ArrayList<>();

    public static synchronized ArrayList<Integer> getInstance() {
        return (ArrayList<Integer>) graphBuffer.clone();          //retorna copia atual do array
    }

    public static synchronized void add(Integer p) {
        int size = GraphActivity.graphPointsNumber * GraphActivity.takeSampleEvery;    //tamanho do buffer de acordo com escala de tempo
        while (graphBuffer.size() >= size)   //remove pontos mais antigos
            graphBuffer.remove(0);
        graphBuffer.add(p);
    }
}
