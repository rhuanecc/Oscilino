package com.rhuanecc.oscilino;

import java.io.Serializable;

/**
 * Created by rhuan on 2016-09-06.
 */
public class Ponto implements Serializable {
    public int canal;
    public int tempo;
    public float valor;

    public Ponto(int canal, int tempo, float valor) {
        this.canal = canal;
        this.tempo = tempo;
        this.valor = valor;
    }

    @Override
    public String toString() {
        return "{"+ canal +","+ tempo +","+ valor +'}';
    }
}
