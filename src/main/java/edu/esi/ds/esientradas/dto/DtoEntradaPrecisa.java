package edu.esi.ds.esientradas.dto;

public class DtoEntradaPrecisa extends DtoEntrada {
    private int fila, columna, planta;

    public int getFila() { 
        return fila; 
    }

    public void setFila(int fila) { 
        this.fila = fila; 
    }

    public int getColumna() { 
        return columna; 
    }

    public void setColumna(int columna) { 
        this.columna = columna; 
    }

    public int getPlanta() { 
        return planta; 
    }

    public void setPlanta(int planta) { 
        this.planta = planta; 
    }
}