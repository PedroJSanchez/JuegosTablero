/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smma.juegosTablero.util;

import java.io.Serializable;
import java.util.Objects;
import juegosTablero.dominio.elementos.ClasificacionJuego;
import juegosTablero.dominio.elementos.Grupo;

/**
 *
 * @author pedroj
 */
public class RegistroClasificacionJuego implements Serializable {
    private final String idJuego;
    private final Grupo grupoJuegos;
    private final ClasificacionJuego clasificacion;

    public RegistroClasificacionJuego(String idJuego, Grupo grupoJuegos, ClasificacionJuego clasificacion) {
        this.idJuego = idJuego;
        this.grupoJuegos = grupoJuegos;
        this.clasificacion = clasificacion;
    }

    public String getIdJuego() {
        return idJuego;
    }

    public Grupo getGrupoJuegos() {
        return grupoJuegos;
    }

    public ClasificacionJuego getClasificacion() {
        return clasificacion;
    }

    @Override
    public String toString() {
        return "RegistroClasificacionJuego{" + "idJuego= " + idJuego + 
                "\n\tgrupoJuegos= " + grupoJuegos + "\n\tclasificacion=" + clasificacion + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.idJuego);
        hash = 67 * hash + Objects.hashCode(this.grupoJuegos);
        hash = 67 * hash + Objects.hashCode(this.clasificacion);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RegistroClasificacionJuego other = (RegistroClasificacionJuego) obj;
        if (!Objects.equals(this.idJuego, other.idJuego)) {
            return false;
        }
        if (!Objects.equals(this.grupoJuegos.getNombre(), other.grupoJuegos.getNombre())) {
            return false;
        }
        return true;
    }
}
