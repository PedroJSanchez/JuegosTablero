/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smma.juegosTablero.util;

import java.util.ArrayList;
import java.util.List;
import juegosTablero.dominio.elementos.ProponerJuego;

/**
 *
 * @author pedroj
 */
public class RegistroJuego {
    private final ProponerJuego juegoPropuesto;
    private final List listaJugadores;

    public RegistroJuego(ProponerJuego juegoPropuesto) {
        this.juegoPropuesto = juegoPropuesto;
        this.listaJugadores = new ArrayList();
    }

    public ProponerJuego getJuegoPropuesto() {
        return juegoPropuesto;
    }

    public List getListaJugadores() {
        return listaJugadores;
    }

    /**
     * Jugadores registrados para el juego
     * @return número de agentes jugador
     */
    public int numJugadores() {
        return listaJugadores.size();
    }
    
    @Override
    public String toString() {
        return "RegistroJuego{" + "juegoPropueto=" + juegoPropuesto + ", listaJugadores=" + listaJugadores + '}';
    }
}
